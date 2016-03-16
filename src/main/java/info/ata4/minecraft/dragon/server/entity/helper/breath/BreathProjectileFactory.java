package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Created by TGG on 14/03/2016.
 * Used to generate a discrete projectile (eg the Nether dragon which fires fireballs)
 * Create a copy of the factory per dragon - the factory has a 'cooldown' to regulate how often projectiles are fired
 * 1) spawnProjectile(...) spawns a projectile entity, if the factory is ready.
 * 2) updateTick(...) every tick, to keep the factory synchronised (eg cooldown timer)
 */
public interface BreathProjectileFactory {
  public void spawnProjectile(World world, EntityTameableDragon dragon, Vec3 origin, Vec3 target, BreathNode.Power i_power);
  public void updateTick(DragonBreathHelper.BreathState breathState);
}
