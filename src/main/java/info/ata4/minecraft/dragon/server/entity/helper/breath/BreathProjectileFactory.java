package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Created by TGG on 14/03/2016.
 */
public interface BreathProjectileFactory {
  public void spawnProjectile(World world, Vec3 origin, Vec3 target, BreathNode.Power i_power);
}
