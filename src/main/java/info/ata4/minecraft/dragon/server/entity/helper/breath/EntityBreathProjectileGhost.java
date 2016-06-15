package info.ata4.minecraft.dragon.server.entity.helper.breath;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.network.BreathWeaponTarget;
import info.ata4.minecraft.dragon.server.network.DragonOrbTargets;
import info.ata4.minecraft.dragon.server.network.DragonTargetMessage;
import info.ata4.minecraft.dragon.server.util.RayTraceServer;
import info.ata4.minecraft.dragon.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Represents the Ghost Breath weapon lightning strike
 * Used in conjunction with EntityBreathhost, i.e.
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
    randomSeed = System.currentTimeMillis();
    Preconditions.checkNotNull(objectStruck);
    breathWeaponTarget = objectStruck;
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
    randomSeed = System.currentTimeMillis();
    breathWeaponTarget = BreathWeaponTarget.targetDirection(destination.subtract(origin));
  }

  // used by spawn code on the client side.  Relevant member variables are populated by a subsequent call to
  //   readSpawnData()
  public EntityBreathProjectileGhost(World worldIn)
  {
    super(worldIn);
  }

  public enum LifeStage
  {PRESTRIKE(1), STRIKE(2), POSTSTRIKE(6), DONE(0);
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
            Vec3 targetPoint =  breathWeaponTarget.getTypeOfTarget() == BreathWeaponTarget.TypeOfTarget.DIRECTION
                              ? destination
                              : breathWeaponTarget.getTargetedPoint(worldObj, origin);
            EntityBreathGhost entityBreathGhost = new EntityBreathGhost(worldObj, origin, targetPoint, power);
            this.worldObj.addWeatherEffect(entityBreathGhost);
          }
          break;
        }
        case POSTSTRIKE: {
          //todo cause damage here
          break;
        }
        case DONE: {
          this.setDead();
        }
      }
    }
//    this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D,
//            new int[0]);
//
//    if (--ticksToLive <= 0) {
//      final float SMOKE_Y_OFFSET = 0.0F;
//      final float X_Z_SPREAD = 0.5F;
//      final float MOTION_SPREAD = 0.1F;
//      Random random = new Random();
//      for (int i = 0; i < 8; ++i) {
//        this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
//                this.posX + X_Z_SPREAD * 2 * (random.nextFloat() - 0.5),
//                this.posY + SMOKE_Y_OFFSET,
//                this.posZ + X_Z_SPREAD * 2 * (random.nextFloat() - 0.5),
//                MOTION_SPREAD * 2 * (random.nextFloat() - 0.5),
//                MOTION_SPREAD * 2 * (random.nextFloat() - 0.5),
//                MOTION_SPREAD * 2 * (random.nextFloat() - 0.5),
//                new int[0]);
//      }
//      this.setDead();
//    }
  }

  /**
   * Return the motion factor for this projectile. The factor is multiplied by the original motion.
   * effectively a 'drag' on the projectile motion
   */
  @Override
  protected float getMotionFactor() {
//    System.err.println("power:" + power);
//    switch (power) {
//      case SMALL: {
//        return 0.50F;
//      }
//      case MEDIUM: {
//        return 0.80F;
//      }
//      case LARGE: {
//        return  0.95F;
//      }
//      default: {
//        System.err.println("Invalid Power in setSizeFromPower:" + power);
//        return 0.95F;
//      }
//    }
    return  0;
  }

  @Override
  protected void setSizeFromPower(BreathNode.Power power)
  {
//    switch (power) {
//      case SMALL: {
//        setSize(0.5F, 0.5F);
//        break;
//      }
//      case MEDIUM: {
//        setSize(0.9F, 0.9F);
//        break;
//      }
//      case LARGE: {
//        setSize(1.5F, 1.5F);
//        break;
//      }
//      default: {
//        System.err.println("Invalid Power in setSizeFromPower:" + power);
//      }
//    }
  }

  @Override
  protected int getLifeTimeTicks(BreathNode.Power power)
  {
    return 80;
  }

  /**
   * Called when this EntityFireball hits a block or entity.
   */
  protected void onImpact(MovingObjectPosition movingObject)
  {
//    if (!this.worldObj.isRemote) {
//      float explosionSize = 1.0F;
//      float damageAmount = 1.0F;
//      switch (power) {
//        case SMALL: {
//          explosionSize = 1.0F;
//          damageAmount = 1.0F;
//          break;
//        }
//        case MEDIUM: {
//          explosionSize = 2.0F;
//          damageAmount = 4.0F;
//          break;
//        }
//        case LARGE: {
//          explosionSize = 4.0F;
//          damageAmount = 10.0F;
//          break;
//        }
//        default: {
//          System.err.println("Invalid Power in onImpact:" + power);
//        }
//      }
//
//      if (movingObject.entityHit != null) {
//        DamageSource fireballDamage = new EntityDamageSourceIndirect("fireball", this, shootingEntity).setFireDamage().setProjectile();
//        movingObject.entityHit.attackEntityFrom(fireballDamage, damageAmount);
//        this.func_174815_a(this.shootingEntity, movingObject.entityHit);
//      }
//
//      if (DragonMounts.instance.getConfig().isBreathAffectsBlocks()) {
//        boolean flag = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
//        this.worldObj.newExplosion(null, this.posX, this.posY, this.posZ, explosionSize, flag, flag);
//      }
//
//      this.setDead();
//    }
  }

  @Override
  protected void inWaterUpdate()
  {
    Random rand = this.worldObj.rand;
    this.worldObj.playSoundEffect(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D,
                                  "random.fizz", 0.5F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F);

    final float SMOKE_Y_OFFSET = 1.2F;
    for (int i = 0; i < 8; ++i) {
      this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
              this.posX + Math.random(), this.posY + SMOKE_Y_OFFSET, this.posZ + Math.random(), 0.0D, 0.0D, 0.0D,
              new int[0]);
    }

    setDead();
  }

  public Vec3 getTargetPoint()
  {
    return  new Vec3(0, 11, 0);
//    return destination;  todo uncomment
  }

  public Vec3 getMouthPoint()
  {
    return  new Vec3(0, 10, 0);
    // return origin; todo uncomment
  }


  @Override
  public void writeEntityToNBT(NBTTagCompound tagCompound)
  {
    super.writeEntityToNBT(tagCompound);
    tagCompound.setInteger("LifeStage", lifeStage.ordinal());
    tagCompound.setInteger("AgeInTicks", ageInTicks);
    tagCompound.setLong("RandomSeed", randomSeed);
    tagCompound.setLong("RandomSeed1", randomSeed1);
    tagCompound.setString("Target", breathWeaponTarget.toEncodedString());
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tagCompound)
  {
    super.readEntityFromNBT(tagCompound);
    randomSeed = tagCompound.getLong("RandomSeed");
    randomSeed1 = tagCompound.getLong("RandomSeed1");
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
    buffer.writeLong(randomSeed);
    buffer.writeLong(randomSeed1);
    breathWeaponTarget.toBytes(buffer);
  }

  @Override
  public void readSpawnData(ByteBuf additionalData)
  {
    super.readSpawnData(additionalData);
    randomSeed = additionalData.readLong();
    randomSeed1 = additionalData.readLong();
    breathWeaponTarget = BreathWeaponTarget.fromBytes(additionalData);
  }

  public long getRandomSeed()
  {
    return randomSeed;
  }
  public long getRandomSeed1()
  {
    return randomSeed1;
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
  private long randomSeed;
  private long randomSeed1;
  private BreathWeaponTarget breathWeaponTarget;

  public static class BreathProjectileFactoryGhost implements BreathProjectileFactory {
    public void spawnProjectile(World world, EntityTameableDragon dragon, Vec3 origin, Vec3 target, BreathNode.Power power)
    {
      if (coolDownTimerTicks > 0) return;

      float maxDistance = 0;
      switch (power) {
        case SMALL: {maxDistance = 10; break;}
        case MEDIUM: {maxDistance = 20; break;}
        case LARGE: {maxDistance = 40; break;}
      }

      Set<Entity> entitiesToIgnore = Collections.emptySet();
      MovingObjectPosition hitPoint = RayTraceServer.rayTraceServer(world, origin, target, maxDistance, dragon, entitiesToIgnore);

      BreathWeaponTarget objectStruck = BreathWeaponTarget.fromMovingObjectPosition(hitPoint, null);

      final int COOLDOWN_TIME_TICKS = 40;
      EntityBreathProjectileGhost entity;
      if (objectStruck == null) {
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
