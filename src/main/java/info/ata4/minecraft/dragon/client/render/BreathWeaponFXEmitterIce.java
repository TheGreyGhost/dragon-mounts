package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Created by TGG on 7/12/2015.
 */
public class BreathWeaponFXEmitterIce extends BreathWeaponFXEmitter
{
  public void spawnBreathParticles(World world, BreathNode.Power power, int tickCount)
  {
    final int ICE_PARTICLES_PER_TICK = 4;
    spawnMultipleWithSmoothedDirection(world, power, ICE_PARTICLES_PER_TICK, tickCount);
  }

  @Override
  protected  EntityFX createSingleParticle(World world, Vec3 spawnOrigin, Vec3 spawnDirection, BreathNode.Power power,
                                           int tickCount, float partialTickHeadStart)
  {
    BreathFXIce breathFXIce = BreathFXIce.createBreathFXIce(world,
            spawnOrigin.xCoord, spawnOrigin.yCoord, spawnOrigin.zCoord,
            spawnDirection.xCoord, spawnDirection.yCoord, spawnDirection.zCoord,
            power,
            tickCount, partialTickHeadStart);
    return breathFXIce;
  }

}
