package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Created by TGG on 7/12/2015.
 */
public class BreathWeaponFXEmitterForest extends BreathWeaponFXEmitter
{
  public void spawnBreathParticles(World world, BreathNode.Power power, int tickCount)
  {
    final int CLOUDS_PER_TICK = 1;
    spawnMultipleWithSmoothedDirection(world, power, CLOUDS_PER_TICK, tickCount);
  }

  @Override
  protected BreathFX createSingleParticle(World world, Vec3d spawnOrigin, Vec3d spawnDirection, BreathNode.Power power,
                                           int tickCount, float partialTickHeadStart)
  {
    BreathFXForest breathFXForest = BreathFXForest.createBreathFXForest(world,
                                                                        spawnOrigin.xCoord,
                                                                        spawnOrigin.yCoord,
                                                                        spawnOrigin.zCoord,
                                                                        spawnDirection.xCoord,
                                                                        spawnDirection.yCoord,
                                                                        spawnDirection.zCoord,
                                                                        power,
                                                                        dragonBreathMode,
                                                                        tickCount,
                                                                        partialTickHeadStart, debugBreathFXSettings);
    return breathFXForest;
  }

}
