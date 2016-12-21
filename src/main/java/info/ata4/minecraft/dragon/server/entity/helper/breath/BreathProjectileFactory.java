package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Created by TGG on 14/03/2016.
 * Used to generate a discrete projectile (eg the Nether dragon which fires fireballs)
 * Create a copy of the factory per dragon - the factory has a 'cooldown' to regulate how often projectiles are fired
 * 1) spawnProjectile(...) spawns a projectile entity, if the factory is ready.  Returns true if spawned.
 * 2) updateTick(...) every tick, to keep the factory synchronised (eg cooldown timer)
 */
public interface BreathProjectileFactory {
  boolean spawnProjectile(World world, EntityTameableDragon dragon, Vec3d origin, Vec3d target, BreathNode.Power i_power);
  void updateTick(DragonBreathHelper.BreathState breathState);
}
