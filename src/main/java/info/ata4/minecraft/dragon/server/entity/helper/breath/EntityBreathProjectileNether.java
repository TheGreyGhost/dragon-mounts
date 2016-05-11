package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Represents the Nether Breath weapon fireball (similar to ghast fireball)
 * Created by TGG on 14/03/2016.
 */
public class EntityBreathProjectileNether extends EntityBreathProjectile {

  public EntityBreathProjectileNether(World worldIn, EntityLivingBase shooter,
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
    final int BURN_DURATION_SECONDS = 1; // used to trigger rendering of fire
//    this.setFire(BURN_DURATION_SECONDS); //todo reinstate
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
   * Called when this EntityFireball hits a block or entity.
   */
  protected void onImpact(MovingObjectPosition movingObject)
  {
//    this.setDead();

//    return;
    if (!this.worldObj.isRemote) {   //todo reinstate
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

      boolean flag = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
      this.worldObj.newExplosion(null, this.posX, this.posY, this.posZ, explosionSize, flag, flag);
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
    public void spawnProjectile(World world, EntityTameableDragon dragon, Vec3 origin, Vec3 target, BreathNode.Power power)
    {
      if (coolDownTimerTicks > 0) return;

//      System.out.format("origin [%.1f, %.1f, %.1f] -> target [%.1f, %.1f, %.1f]\n",
//                        origin.xCoord, origin.yCoord, origin.zCoord,
//                        target.xCoord, target.yCoord, target.zCoord);  //todo remove
      final int COOLDOWN_TIME_TICKS = 40;
      EntityBreathProjectileNether entity = new EntityBreathProjectileNether(world, dragon, origin, target, power);
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
