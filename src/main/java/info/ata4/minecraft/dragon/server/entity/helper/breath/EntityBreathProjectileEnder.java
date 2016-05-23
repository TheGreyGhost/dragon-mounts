package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Represents the Ender Breath weapon (similar to ghast fireball)
 * Created by TGG on 14/03/2016.
 */
public class EntityBreathProjectileEnder extends EntityBreathProjectile {

  public EntityBreathProjectileEnder(World worldIn, EntityLivingBase shooter,
                                     Vec3 origin, Vec3 destination, BreathNode.Power power)
  {
    super(worldIn, shooter, origin, destination, power);
  }

  // used by some spawn code under circumstances I don't fully understand yet
  public EntityBreathProjectileEnder(World worldIn)
  {
    super(worldIn);
  }

  @Override
  public void onUpdate()
  {
    if (this.worldObj.isRemote) {
      for (int i = 0; i < 2; ++i) {
        this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width,
                this.posY + this.rand.nextDouble() * (double)this.height - 0.25D,
                this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width,
                (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D, new int[0]);
      }
    }
    super.onUpdate();
  }

  /**
   * Return the motion factor for this projectile. The factor is multiplied by the original motion.
   * effectively a 'drag' on the projectile motion
   */
  protected float getMotionFactor() {
//    System.err.println("power:" + power);
    switch (power) {
      case SMALL: {
        return 0.60F;
      }
      case MEDIUM: {
        return 0.80F;
      }
      case LARGE: {
        return  0.90F;
      }
      default: {
        System.err.println("Invalid Power in setSizeFromPower:" + power);
        return 0.90F;
      }
    }

  }

  protected void setSizeFromPower(BreathNode.Power power)
  {
    switch (power) {
      case SMALL: {
        setSize(0.5F, 0.5F);
        break;
      }
      case MEDIUM: {
        setSize(0.9F, 0.9F);
        break;
      }
      case LARGE: {
        setSize(1.5F, 1.5F);
        break;
      }
      default: {
        System.err.println("Invalid Power in setSizeFromPower:" + power);
      }
    }
  }

  /**
   * Called when the EnderBall hits a block or entity.
   */
  protected void onImpact(MovingObjectPosition movingObject)
  {
    int teleportDistance = 0;
    int effectRadius = 0;


    if (!this.worldObj.isRemote) {
      switch (power) {
        case SMALL: {
          effectRadius = 1;
          teleportDistance = 5;
          break;
        }
        case MEDIUM: {
          effectRadius = 2;
          teleportDistance = 20;
          break;
        }
        case LARGE: {
          effectRadius = 4;
          teleportDistance = 40;
          break;
        }
        default: {
          System.err.println("Invalid Power in onImpact:" + power);
        }
      }

      if (movingObject.entityHit != null) {
        Entity entity = movingObject.entityHit;
        int verticalDistance = teleportDistance / 4;

        int numberOfAttempts = 0;
        final int MAX_ATTEMPTS = 10;
        boolean succeeded = false;
        while (numberOfAttempts < MAX_ATTEMPTS && !succeeded) {
          double deltaX = rand.nextFloat() * teleportDistance * 2 - teleportDistance;
          double deltaZ = rand.nextFloat() * teleportDistance * 2 - teleportDistance;
          double deltaY = rand.nextFloat() * verticalDistance * 2 - verticalDistance;
          succeeded = teleport(entity, entity.posX + deltaX, entity.posY + deltaY, entity.posZ + deltaZ);
          ++numberOfAttempts;
        }
      }
//      float explosionSize = 1.0F;
//      float damageAmount = 1.0F;
//
//      if (movingObject.entityHit != null) {
//        DamageSource fireballDamage = new EntityDamageSourceIndirect("fireball", this, shootingEntity).setFireDamage().setProjectile();
//        movingObject.entityHit.attackEntityFrom(fireballDamage, damageAmount);
//        this.func_174815_a(this.shootingEntity, movingObject.entityHit);
//      }
//
//      boolean flag = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
//      this.worldObj.newExplosion(null, this.posX, this.posY, this.posZ, explosionSize, flag, flag);
      this.setDead();
    }
  }

  /**
   * teleport an entity.  copied from EntityEnderman teleportTo
   * @param entityToTeleport
   * @param newX
   * @param newY
   * @param newZ
   * @return true for successful teleport
   */
  private boolean teleport(Entity entityToTeleport, double newX, double newY, double newZ) {
    double savedX = this.posX;
    double savedY = this.posY;
    double savedZ = this.posZ;

    entityToTeleport.setPositionAndUpdate(this.posX, this.posY, this.posZ);
    List collisions = this.worldObj.getCollidingBoundingBoxes(entityToTeleport, entityToTeleport.getEntityBoundingBox());
    if (!collisions.isEmpty()) {
      entityToTeleport.setPosition(savedX, savedY, savedZ);
      return false;
    }
    short particleCount = 128;

    for (int i = 0; i < particleCount; ++i) {
      double fractionalDistance = (double) i / ((double) particleCount - 1.0D);
      float xMotion = (this.rand.nextFloat() - 0.5F) * 0.2F;
      float yMotion = (this.rand.nextFloat() - 0.5F) * 0.2F;
      float zMotion = (this.rand.nextFloat() - 0.5F) * 0.2F;
      double particleX = savedX + (entityToTeleport.posX - savedX) * fractionalDistance + (this.rand.nextDouble() - 0.5D) * (double) entityToTeleport.width * 2.0D;
      double particleY = savedY + (entityToTeleport.posY - savedY) * fractionalDistance + this.rand.nextDouble() * (double) entityToTeleport.height;
      double particleZ = savedZ + (entityToTeleport.posZ - savedZ) * fractionalDistance + (this.rand.nextDouble() - 0.5D) * (double) entityToTeleport.width * 2.0D;
      this.worldObj
              .spawnParticle(EnumParticleTypes.PORTAL, particleX, particleY, particleZ, xMotion, yMotion, zMotion, new int[0]);
    }

    this.worldObj.playSoundEffect(savedX, savedY, savedZ, "mob.endermen.portal", 1.0F, 1.0F);
    this.playSound("mob.endermen.portal", 1.0F, 1.0F);
    return true;
  }

//  @Override
//  protected void inWaterUpdate()
//  {
////    Random rand = this.worldObj.rand;
////    this.worldObj.playSoundEffect(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D,
////                                  "random.fizz", 0.5F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F);
////
////    final float SMOKE_Y_OFFSET = 1.2F;
////    for (int i = 0; i < 8; ++i) {
////      this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
////              this.posX + Math.random(), this.posY + SMOKE_Y_OFFSET, this.posZ + Math.random(), 0.0D, 0.0D, 0.0D,
////              new int[0]);
////    }
////
////    setDead();
//  }


  public static class BreathProjectileFactoryEnder implements BreathProjectileFactory {
    public void spawnProjectile(World world, EntityTameableDragon dragon, Vec3 origin, Vec3 target, BreathNode.Power power)
    {
      if (coolDownTimerTicks > 0) return;

//      System.out.format("origin [%.1f, %.1f, %.1f] -> target [%.1f, %.1f, %.1f]\n",
//                        origin.xCoord, origin.yCoord, origin.zCoord,
//                        target.xCoord, target.yCoord, target.zCoord);  //todo remove
      final int COOLDOWN_TIME_TICKS = 60;
      EntityBreathProjectileEnder entity = new EntityBreathProjectileEnder(world, dragon, origin, target, power);
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
