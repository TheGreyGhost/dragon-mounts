package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Created by TGG on 14/03/2016.
 */
public class EntityBreathProjectileNether extends EntityBreathProjectile {

  public EntityBreathProjectileNether(World worldIn, EntityLivingBase shooter,
                                      Vec3 origin, Vec3 destination, BreathNode.Power power)
  {
    super(worldIn, shooter, origin, destination, power);
  }

  protected void setSizeFromPower(BreathNode.Power power)
  {
    switch (power) {
      case SMALL: {
        setSize(0.5F, 0.5F);
        break;
      }
      case MEDIUM: {
        setSize(1.0F, 1.0F);
        break;
      }
      case LARGE: {
        setSize(2.0F, 2.0F);
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

      boolean flag = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
      this.worldObj.newExplosion(null, this.posX, this.posY, this.posZ, explosionSize, flag, flag);
      this.setDead();
    }
  }

  public static class BreathProjectileFactoryNether implements BreathProjectileFactory {
    public void spawnProjectile(World world, EntityTameableDragon dragon, Vec3 origin, Vec3 target, BreathNode.Power power)
    {
      if (coolDownTimerTicks > 0) return;

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
