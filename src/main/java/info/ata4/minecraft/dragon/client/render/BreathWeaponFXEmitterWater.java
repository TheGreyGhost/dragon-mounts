package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Created by TGG on 7/12/2015.
 */
public class BreathWeaponFXEmitterWater extends BreathWeaponFXEmitter
{
  public void spawnBreathParticles(World world, BreathNode.Power power, int tickCount)
  {
//    spawnSpray = true;
//    final int SPRAY_PARTICLES_PER_TICK = 8;
//    spawnMultipleWithSmoothedDirection(world, power, SPRAY_PARTICLES_PER_TICK, tickCount);

//    spawnSpray = false;
    final int DROPLET_PARTICLES_PER_TICK = 10;
    spawnMultipleWithSmoothedDirection(world, power, DROPLET_PARTICLES_PER_TICK, tickCount);
  }

  @Override
  protected  EntityFX createSingleParticle(World world, Vec3 spawnOrigin, Vec3 spawnDirection, BreathNode.Power power,
                                           int tickCount, float partialTickHeadStart)
  {
    BreathFXWater breathFXWater = BreathFXWater.createBreathFXWater(world,
            spawnOrigin.xCoord, spawnOrigin.yCoord, spawnOrigin.zCoord,
            spawnDirection.xCoord, spawnDirection.yCoord, spawnDirection.zCoord,
            power,
            tickCount,
            partialTickHeadStart);
    return breathFXWater;
  }


}
