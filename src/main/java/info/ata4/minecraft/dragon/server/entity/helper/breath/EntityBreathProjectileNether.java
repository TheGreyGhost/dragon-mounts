package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.sound.SoundController;
import info.ata4.minecraft.dragon.client.sound.SoundEffectProjectile;
import info.ata4.minecraft.dragon.client.sound.SoundEffectProjectileNether;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Represents the Nether Breath weapon fireball (similar to ghast fireball)
 * Created by TGG on 14/03/2016.
 */
public class EntityBreathProjectileNether extends EntityBreathProjectile {

  public EntityBreathProjectileNether(World worldIn, EntityTameableDragon shooter,
                                      Vec3 origin, Vec3 destination, BreathNode.Power power)
  {
    super(worldIn, shooter, origin, destination, power);
  }

  // used by some spawn code under circumstances I don't fully understand yet
  public EntityBreathProjectileNether(World worldIn)
  {
    super(worldIn);
  }

  @Override
  public void onUpdate()
  {
    super.onUpdate();
    this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D,
            new int[0]);

    if (--ticksToLive <= 0) {
      final float SMOKE_Y_OFFSET = 0.0F;
      final float X_Z_SPREAD = 0.5F;
      final float MOTION_SPREAD = 0.1F;
      Random random = new Random();
      for (int i = 0; i < 8; ++i) {
        this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                this.posX + X_Z_SPREAD * 2 * (random.nextFloat() - 0.5),
                this.posY + SMOKE_Y_OFFSET,
                this.posZ + X_Z_SPREAD * 2 * (random.nextFloat() - 0.5),
                MOTION_SPREAD * 2 * (random.nextFloat() - 0.5),
                MOTION_SPREAD * 2 * (random.nextFloat() - 0.5),
                MOTION_SPREAD * 2 * (random.nextFloat() - 0.5),
                new int[0]);
      }
      this.setDead();
    }

    if (this.worldObj.isRemote) {
      EntityTameableDragon parentDragon = getParentDragon();
      if (soundEffectProjectile == null && parentDragon != null) {
        SoundController soundController = parentDragon.getBreathHelper().getSoundController(parentDragon.getEntityWorld());
        soundEffectProjectile = new SoundEffectProjectileNether(soundController, new SoundUpdateLink());
      }
      if (soundEffectProjectile != null) {
        soundEffectProjectile.performTick(Minecraft.getMinecraft().thePlayer);
      }
    }
  }

  @Override
  public void setDead()
  {
    super.setDead();
    if (getEntityWorld().isRemote && soundEffectProjectile != null) {
      soundEffectProjectile.performTick(Minecraft.getMinecraft().thePlayer);
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
        return 0.50F;
      }
      case MEDIUM: {
        return 0.80F;
      }
      case LARGE: {
        return  0.95F;
      }
      default: {
        System.err.println("Invalid Power in setSizeFromPower:" + power);
        return 0.95F;
      }
    }

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
    if (!this.worldObj.isRemote) {
      float explosionSize = 1.0F;
      float damageAmount = 1.0F;
      switch (power) {
        case SMALL: {
          explosionSize = 1.0F;
          damageAmount = 1.0F;
          break;
        }
        case MEDIUM: {
          explosionSize = 2.0F;
          damageAmount = 4.0F;
          break;
        }
        case LARGE: {
          explosionSize = 4.0F;
          damageAmount = 10.0F;
          break;
        }
        default: {
          System.err.println("Invalid Power in onImpact:" + power);
        }
      }

      if (movingObject.entityHit != null) {
        DamageSource fireballDamage = new EntityDamageSourceIndirect("fireball", this, shootingEntity).setFireDamage().setProjectile();
        movingObject.entityHit.attackEntityFrom(fireballDamage, damageAmount);
        this.func_174815_a(this.shootingEntity, movingObject.entityHit);
      }

      if (DragonMounts.instance.getConfig().isBreathAffectsBlocks()) {
        boolean flag = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
        this.worldObj.newExplosion(null, this.posX, this.posY, this.posZ, explosionSize, flag, flag);
      }

      this.setDead();
    }
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

  public static class BreathProjectileFactoryNether implements BreathProjectileFactory {
    public boolean spawnProjectile(World world, EntityTameableDragon dragon, Vec3 origin, Vec3 target, BreathNode.Power power)
    {
      if (coolDownTimerTicks > 0 || !mouthHasBeenClosed) return false;

      final int COOLDOWN_TIME_TICKS = 40;
      EntityBreathProjectileNether entity = new EntityBreathProjectileNether(world, dragon, origin, target, power);
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

  private SoundEffectProjectile soundEffectProjectile;

}
