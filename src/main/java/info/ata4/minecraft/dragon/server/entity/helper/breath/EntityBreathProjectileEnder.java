package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.render.CustomEntityFXTypes;
import info.ata4.minecraft.dragon.client.render.EntityFXEnderTrail;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
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
      final int NUMBER_OF_PARTICLES_PER_TICK = 20;
      for (int i = 0; i < NUMBER_OF_PARTICLES_PER_TICK; ++i) {
        double particleX = this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width;
        double particleY = this.posY + this.rand.nextDouble() * (double)this.height - 0.25D;
        double particleZ = this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width;
        double xMotion = (this.rand.nextDouble() - 0.5D) * 2.0D;
        double yMotion =  -this.rand.nextDouble();
        double zMotion = (this.rand.nextDouble() - 0.5D) * 2.0D;

        DragonMounts.proxy.spawnCustomEntityFX(CustomEntityFXTypes.ENDERTRAIL, this.worldObj,
                                               particleX, particleY, particleZ,
                                               xMotion, yMotion, zMotion);
      }
    }
    super.onUpdate();
    if (--ticksToLive <= 0) {
      final float CLOUD_Y_OFFSET = 0.0F;
      final float X_Y_Z_SPREAD = 0.3F;
      final float MOTION_SPREAD = 0.1F;
      final int PARTICLE_COUNT = 30;
      Random random = new Random();
      for (int i = 0; i < PARTICLE_COUNT; ++i) {
        this.worldObj.spawnParticle(EnumParticleTypes.SPELL_WITCH,
                this.posX + X_Y_Z_SPREAD * 2 * (random.nextFloat() - 0.5),
                this.posY + CLOUD_Y_OFFSET + X_Y_Z_SPREAD * 2 * (random.nextFloat() - 0.5),
                this.posZ + X_Y_Z_SPREAD * 2 * (random.nextFloat() - 0.5),
                MOTION_SPREAD * 2 * (random.nextFloat() - 0.5),
                MOTION_SPREAD * 2 * (random.nextFloat() - 0.5),
                MOTION_SPREAD * 2 * (random.nextFloat() - 0.5),
                new int[0]);
      }
      this.setDead();
    }

  }

  /**
   * Return the motion factor for this projectile. The factor is multiplied by the original motion.
   * effectively a 'drag' on the projectile motion
   */
  @Override
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

  @Override
  protected int getLifeTimeTicks(BreathNode.Power power)
  {
    return 60;
  }

  @Override
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

      if (movingObject.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
        this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "mob.endermen.portal", 1.0F, 1.0F);
        this.playSound("mob.endermen.portal", 1.0F, 1.0F);

        boolean mobGriefingOK = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
        if (mobGriefingOK && DragonMounts.instance.getConfig().isBreathAffectsBlocks()) {

          // delete all blocks within given radius of impact location
          for (int dx = -effectRadius; dx <= effectRadius; ++dx) {
            for (int dz = -effectRadius; dz <= effectRadius; ++dz) {
              for (int dy = -effectRadius; dy <= effectRadius; ++dy) {
                if (dx * dx + dy * dy + dz * dz <= effectRadius * effectRadius) {
                  BlockPos erasePosition = new BlockPos(movingObject.getBlockPos().add(dx, dy, dz));
                  worldObj.setBlockToAir(erasePosition);

                }
              }
            }
          }
        }
      }

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

    entityToTeleport.setPositionAndUpdate(newX, newY, newZ);
    List collisions = this.worldObj.getCollidingBoundingBoxes(entityToTeleport, entityToTeleport.getEntityBoundingBox());
    if (!collisions.isEmpty()) {
      entityToTeleport.setPosition(savedX, savedY, savedZ);
      return false;
    }

    this.worldObj.playSoundEffect(savedX, savedY, savedZ, "mob.endermen.portal", 1.0F, 1.0F);
    this.playSound("mob.endermen.portal", 1.0F, 1.0F);
    return true;
  }

  public static class BreathProjectileFactoryEnder implements BreathProjectileFactory {
    public boolean spawnProjectile(World world, EntityTameableDragon dragon, Vec3 origin, Vec3 target, BreathNode.Power power)
    {
      if (coolDownTimerTicks > 0 || !mouthHasBeenClosed) return false;

      final int COOLDOWN_TIME_TICKS = 60;
      EntityBreathProjectileEnder entity = new EntityBreathProjectileEnder(world, dragon, origin, target, power);
      world.spawnEntityInWorld(entity);
      coolDownTimerTicks = COOLDOWN_TIME_TICKS;
      return true;
    }

    public void updateTick(DragonBreathHelper.BreathState breathState)
    {
      if (coolDownTimerTicks > 0) {
        --coolDownTimerTicks;
      }
      if (breathState == DragonBreathHelper.BreathState.IDLE) {
        mouthHasBeenClosed = true;
      }
    }
    private int coolDownTimerTicks = 0;
    private boolean mouthHasBeenClosed = false;
  }

}
