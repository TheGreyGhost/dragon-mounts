package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Represents the Ghost Breath weapon lightning strike
 * Created by TGG on 14/03/2016.
 */
public class EntityBreathProjectileGhost extends EntityBreathProjectile {

  public EntityBreathProjectileGhost(World worldIn, EntityLivingBase shooter,
                                     Vec3 origin, Vec3 destination, BreathNode.Power power)
  {
    super(worldIn, shooter, origin, destination, power);
    randomSeed = System.currentTimeMillis();
  }

  // used by spawn code on the client side.  Relevant member variables are populated by a subsequent call to
  //   readSpawnData()
  public EntityBreathProjectileGhost(World worldIn)
  {
    super(worldIn);
  }

  public enum RenderStage {PRESTRIKE(3), STRIKE(2), POSTSTRIKE(6), DONE(0);
    RenderStage(int i_durationTicks)
    {
      durationTicks = i_durationTicks;
    }
    public int getDuration() {return durationTicks;}
    public RenderStage next() {return (this == DONE) ? DONE : RenderStage.values()[this.ordinal() + 1];}
    private int durationTicks;
  }

  @Override
  public void onUpdate()
  {
    super.onUpdate();
    ++ageInTicks;
    ++timeInThisRenderStage;
    if (timeInThisRenderStage >= renderStage.getDuration()) {
      renderStage = renderStage.next();
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
    tagCompound.setInteger("RenderStage", renderStage.ordinal());
    tagCompound.setInteger("AgeInTicks", ageInTicks);
    tagCompound.setLong("RandomSeed", randomSeed);
    tagCompound.setLong("RandomSeed1", randomSeed1);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tagCompound)
  {
    super.readEntityFromNBT(tagCompound);
    randomSeed = tagCompound.getLong("RandomSeed");
    randomSeed1 = tagCompound.getLong("RandomSeed1");
    ageInTicks = tagCompound.getInteger("AgeInTicks");
    int renderIndex = tagCompound.getInteger("RenderStage");
    if (renderIndex >= 0 && renderIndex < RenderStage.values().length) {
      renderStage = RenderStage.values()[renderIndex];
    }
  }

  @Override
  public void writeSpawnData(ByteBuf buffer)
  {
    super.writeSpawnData(buffer);
    buffer.writeLong(randomSeed);
    buffer.writeLong(randomSeed1);
  }

  @Override
  public void readSpawnData(ByteBuf additionalData)
  {
    super.readSpawnData(additionalData);
    randomSeed = additionalData.readLong();
    randomSeed1 = additionalData.readLong();
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
  public Pair<RenderStage, Integer> getRenderStage()
  {
    return new Pair<RenderStage, Integer>(renderStage, timeInThisRenderStage);
  }

  private RenderStage renderStage = RenderStage.PRESTRIKE;
  private int timeInThisRenderStage = 0;
  private int ageInTicks = 0;
  private long randomSeed;
  private long randomSeed1;

  public static class BreathProjectileFactoryGhost implements BreathProjectileFactory {
    public void spawnProjectile(World world, EntityTameableDragon dragon, Vec3 origin, Vec3 target, BreathNode.Power power)
    {
      if (coolDownTimerTicks > 0) return;

      final int COOLDOWN_TIME_TICKS = 40;
      EntityBreathProjectileGhost entity = new EntityBreathProjectileGhost(world, dragon, origin, target, power);
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
