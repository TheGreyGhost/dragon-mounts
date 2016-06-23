package info.ata4.minecraft.dragon.server.entity.helper.breath;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.network.BreathWeaponTarget;
import info.ata4.minecraft.dragon.server.network.DragonOrbTargets;
import info.ata4.minecraft.dragon.server.network.DragonTargetMessage;
import info.ata4.minecraft.dragon.server.util.RayTraceServer;
import info.ata4.minecraft.dragon.util.Pair;
import info.ata4.minecraft.dragon.util.math.MathX;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.lwjgl.Sys;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Represents the Ghost Breath weapon lightning strike
 * Used in conjunction with EntityBreathGhost, i.e.
 *   the projectile is spawned, and when it onUpdates() it adds the weather effect entity to handle the rendering
 * I did it this way to simplify the transmission from server to client and control the collisions better
 * Using the weather effect solves the problem of frustum check, i.e. that the entity doesn't render if the
 *   bounding box is outside your view.  Entity.ignoreFrustumCheck flag couldn't overcome this problem because of
 *   the way that entities are cached per chunk.
 * Created by TGG on 14/03/2016.
 */
public class EntityBreathProjectileGhost extends EntityBreathProjectile {

  /**
   * Create the projectile entity, which has collided with an object
   * @param worldIn
   * @param shooter the dragon shooting the lightning
   * @param origin the position of the dragon's mouth
   * @param destination the end point of the lightning (the object struck)
   * @param power the power of the lightning
   * @param objectStruck the entity or block which was struck (not null!)
   */
  public EntityBreathProjectileGhost(World worldIn, EntityLivingBase shooter,
                                     Vec3 origin, Vec3 destination, BreathNode.Power power,
                                     BreathWeaponTarget objectStruck)
  {
    super(worldIn, shooter, origin, destination, power);
//    randomSeed = System.currentTimeMillis();
    Preconditions.checkNotNull(objectStruck);
    breathWeaponTarget = objectStruck;
    objectWasStruck = true;
  }

  /**
   * Create the projectile entity; no collision occurred
   * @param worldIn
   * @param shooter the dragon shooting the lightning
   * @param origin the position of the dragon's mouth
   * @param destination the endpoint position of the lightning strike (the end of the ray)
   * @param power the power of the lightning
   */
  public EntityBreathProjectileGhost(World worldIn, EntityLivingBase shooter,
                                     Vec3 origin, Vec3 destination, BreathNode.Power power)
  {
    super(worldIn, shooter, origin, destination, power);
//    randomSeed = System.currentTimeMillis();
    breathWeaponTarget = BreathWeaponTarget.targetDirection(destination.subtract(origin));
    objectWasStruck = false;
  }

  // used by spawn code on the client side.  Relevant member variables are populated by a subsequent call to
  //   readSpawnData()
  public EntityBreathProjectileGhost(World worldIn)
  {
    super(worldIn);
  }

  public enum LifeStage
  {PRESTRIKE(1), STRIKE(4), POSTSTRIKE(1), DONE(0);
    LifeStage(int i_durationTicks)
    {
      durationTicks = i_durationTicks;
    }
    public int getDuration() {return durationTicks;}
    public LifeStage next() {return (this == DONE) ? DONE : LifeStage.values()[this.ordinal() + 1];}
    private int durationTicks;
  }

  @Override
  public void onUpdate()
  {
    super.onUpdate();
    ++ageInTicks;
    ++timeInThisLifeStage;
    if (timeInThisLifeStage >= lifeStage.getDuration()) {
      lifeStage = lifeStage.next();
      timeInThisLifeStage = 0;
      switch (lifeStage) {
        case STRIKE: {
          if (this.worldObj.isRemote) {
            Vec3 targetPoint = (breathWeaponTarget.getTypeOfTarget() == BreathWeaponTarget.TypeOfTarget.DIRECTION)
                    ? destination
                    : breathWeaponTarget.getTargetedPoint(worldObj, origin);
            EntityBreathGhost entityBreathGhost = new EntityBreathGhost(worldObj, origin, targetPoint, power);
            this.worldObj.addWeatherEffect(entityBreathGhost);
            this.worldObj.setLastLightningBolt(2);
          }
          break;
        }
        case POSTSTRIKE: {
          // code copied from EntityLightningBolt.onUpdate()
          if (objectWasStruck) {
            Vec3 impactPoint = breathWeaponTarget.getTargetedPoint(worldObj, origin);
            float effectRadius = 1.0F;
            switch (power) {
              case SMALL: {effectRadius = 1.0F; break; }
              case MEDIUM: {effectRadius = 2.0F; break; }
              case LARGE: {effectRadius = 4.0F; break; }
              default: {System.err.println("Unexpected power:" + power); break; }
            }
            igniteBlock(this.worldObj, impactPoint, effectRadius);
            strikeEntities(this.worldObj, impactPoint, effectRadius);
          }
          break;
        }
        case DONE: {
          this.setDead();
        }
      }
    }
  }

  // ignite all flammable blocks within the given radius of the impact point
  // (or more strictly: for each air block within the effect radius (its centre is within effectRadius of the impactPoint)
  //   , check all adjacent blocks for a face which is flammable
  private void igniteBlock(World world, Vec3 impactPoint, float effectRadius)
  {
    BlockPos blockPosCentre = new BlockPos(impactPoint);
    if (world.isRemote
        || !world.getGameRules().getGameRuleBooleanValue("doFireTick")
        || !world.isAreaLoaded(blockPosCentre, 10)
        ) {
      return;
    }

    int numberOfIgnitions = 0;
    int xMin = (int)(blockPosCentre.getX() - effectRadius);
    int xMax = (int)(blockPosCentre.getX() + effectRadius);
    int yMin = (int)(blockPosCentre.getY() - effectRadius);
    int yMax = (int)(blockPosCentre.getY() + effectRadius);
    int zMin = (int)(blockPosCentre.getZ() - effectRadius);
    int zMax = (int)(blockPosCentre.getZ() + effectRadius);
    for (int y = yMin; y <= yMax; ++y) {
      for (int x = xMin; x <= xMax; ++x) {
        for (int z = zMin; z <= zMax; ++z) {
          BlockPos blockPos = new BlockPos(x, y, z);
          if (world.isAirBlock(blockPos)
              && blockPos.distanceSqToCenter(impactPoint.xCoord, impactPoint.yCoord, impactPoint.zCoord)
                  <= effectRadius * effectRadius) {
            for (EnumFacing facing : EnumFacing.values()) {
              BlockPos sideToIgnite = blockPos.offset(facing);
              IBlockState iBlockState = world.getBlockState(sideToIgnite);
              Block block = iBlockState.getBlock();
              if (!block.isAir(world, sideToIgnite) && block.isFlammable(world, sideToIgnite, facing)) {
                ++numberOfIgnitions;
                world.setBlockState(blockPos, Blocks.fire.getDefaultState());
              }
            }
          }
        }
      }
    }
    if (numberOfIgnitions > 0) {
      final float MIN_PITCH = 0.8F;
      final float MAX_PITCH = 1.2F;
      final float VOLUME = 1.0F * numberOfIgnitions;
      world.playSoundEffect(blockPosCentre.getX() + 0.5, blockPosCentre.getY() + 0.5, blockPosCentre.getZ() + 0.5,
              "fire.ignite", VOLUME, MIN_PITCH + rand.nextFloat() * (MAX_PITCH - MIN_PITCH));
    }
  }

  private void strikeEntities(World world, Vec3 impactPoint, float effectRadius)
  {
    EntityLightningBolt entityLightningBolt = new EntityLightningBolt(world,
            impactPoint.xCoord, impactPoint.yCoord, impactPoint.zCoord);
    AxisAlignedBB effectAABB = new AxisAlignedBB(impactPoint.xCoord - effectRadius,
            impactPoint.yCoord - effectRadius,
            impactPoint.zCoord - effectRadius,
            impactPoint.xCoord + effectRadius,
            impactPoint.yCoord + effectRadius,
            impactPoint.zCoord + effectRadius);

    List<Entity> list = (List<Entity>)world.getEntitiesWithinAABBExcludingEntity(this, effectAABB);

    for (Entity entity : list) {
      if (MathX.getClosestDistanceSQ(entity.getEntityBoundingBox(), impactPoint) <= effectRadius * effectRadius) {
        if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, entityLightningBolt))
          entity.onStruckByLightning(entityLightningBolt);
      }
    }
  }

  /**
   * not relevant
   */
  @Override
  protected float getMotionFactor() {
    return  0;
  }

  /**
   * Not relevant
   */
  @Override
  protected void setSizeFromPower(BreathNode.Power power)
  {
  }

  @Override
  protected int getLifeTimeTicks(BreathNode.Power power)
  {
    return 80;
  }

  /**
   * Not relevant
   */
  protected void onImpact(MovingObjectPosition movingObject)
  {
  }

  /*
    Not relevant
   */
  @Override
  protected void inWaterUpdate()
  {
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tagCompound)
  {
    super.writeEntityToNBT(tagCompound);
    tagCompound.setInteger("LifeStage", lifeStage.ordinal());
    tagCompound.setInteger("AgeInTicks", ageInTicks);
//    tagCompound.setLong("RandomSeed", randomSeed);
//    tagCompound.setLong("RandomSeed1", randomSeed1);
    tagCompound.setString("Target", breathWeaponTarget.toEncodedString());
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tagCompound)
  {
    super.readEntityFromNBT(tagCompound);
//    randomSeed = tagCompound.getLong("RandomSeed");
//    randomSeed1 = tagCompound.getLong("RandomSeed1");
    ageInTicks = tagCompound.getInteger("AgeInTicks");
    int renderIndex = tagCompound.getInteger("LifeStage");
    if (renderIndex >= 0 && renderIndex < LifeStage.values().length) {
      lifeStage = LifeStage.values()[renderIndex];
    }
    breathWeaponTarget = BreathWeaponTarget.fromEncodedString(tagCompound.getString("Target"));
  }

  @Override
  public void writeSpawnData(ByteBuf buffer)
  {
    super.writeSpawnData(buffer);
//    buffer.writeLong(randomSeed);
//    buffer.writeLong(randomSeed1);
    breathWeaponTarget.toBytes(buffer);
  }

  @Override
  public void readSpawnData(ByteBuf additionalData)
  {
    super.readSpawnData(additionalData);
//    randomSeed = additionalData.readLong();
//    randomSeed1 = additionalData.readLong();
    breathWeaponTarget = BreathWeaponTarget.fromBytes(additionalData);
  }

  /** returns the current render stage, and the time currently spent in this stage
    * @return
   */
  public Pair<LifeStage, Integer> getLifeStage()
  {
    return new Pair<LifeStage, Integer>(lifeStage, timeInThisLifeStage);
  }

  private LifeStage lifeStage = LifeStage.PRESTRIKE;
  private int timeInThisLifeStage = 0;
  private int ageInTicks = 0;
//  private long randomSeed;
//  private long randomSeed1;
  private BreathWeaponTarget breathWeaponTarget;
  boolean objectWasStruck = false;

  public static class BreathProjectileFactoryGhost implements BreathProjectileFactory {
    public void spawnProjectile(World world, EntityTameableDragon dragon, Vec3 origin, Vec3 target, BreathNode.Power power)
    {
      if (coolDownTimerTicks > 0) return;

      Pair<Float, Float> ranges = dragon.getBreed().getBreathWeaponRange(dragon.getLifeStageHelper().getLifeStage());
      float minDistance = ranges.getFirst();
      float maxDistance = ranges.getSecond();

      Set<Entity> entitiesToIgnore = Collections.emptySet();
      Vec3 direction = target.subtract(origin).normalize();
      MovingObjectPosition hitPoint = RayTraceServer.rayTraceServer(world, origin, direction, maxDistance, dragon, entitiesToIgnore);

      BreathWeaponTarget objectStruck = BreathWeaponTarget.fromMovingObjectPosition(hitPoint, null);

      final int COOLDOWN_TIME_TICKS = 40;
      EntityBreathProjectileGhost entity;
      if (objectStruck == null) {
        direction = MathX.multiply(direction, maxDistance);
        target = target.add(direction);
        entity = new EntityBreathProjectileGhost(world, dragon, origin, target, power);
      } else {
        entity = new EntityBreathProjectileGhost(world, dragon, origin, target, power, objectStruck);
      }

      world.spawnEntityInWorld(entity);
      coolDownTimerTicks = COOLDOWN_TIME_TICKS;
    }

    public void updateTick(DragonBreathHelper.BreathState breathState)
    {
      if (coolDownTimerTicks > 0) {
        --coolDownTimerTicks;
      }
    }
    private int coolDownTimerTicks = 0;
  }

}
