package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Created by TGG on 21/06/2015.
 * Used to spawn breath particles on the client side (in future: will be different for different breath weapons)
 * Usage:
 * Each tick:
 * (1) setBeamEndpoints() to set the current beam origin and destination
 * (2) spawnBreathParticles() to spawn the particles()
 */
public abstract class BreathWeaponFXEmitter
{

  protected Vec3 origin;
  protected Vec3 direction;

  protected Vec3 previousOrigin;
  protected Vec3 previousDirection;
  protected int previousTickCount;

  /**
   * Set the current beam origin and target destination (used to calculate direction).
   * Will smooth out between ticks.
   * @param newOrigin the starting point of the beam (world coordinates)
   * @param newDestination the target of the beam (world coordinates)
   */
  public void setBeamEndpoints(Vec3 newOrigin, Vec3 newDestination)
  {
    origin = newOrigin;
    direction = newDestination.subtract(newOrigin).normalize();
  }

  /**
   * Spawn breath particles for this tick.  If the beam endpoints have moved, interpolate between them, unless
   *   the beam stopped for a while (tickCount skipped one or more tick)
   * @param world
   * @param power the strength of the beam
   * @param tickCount
   */
  abstract public void spawnBreathParticles(World world, BreathNode.Power power, int tickCount);


  /**
   * Spawn a number of EntityFX, interpolating between the direction at the previous tick and the direction of the current tick
   * Useful for breath weapons consisting of many particles, such as Fire.
   * @param world
   * @param power
   * @param particlesPerTick number of particles to spawn
   * @param tickCount running count of the number of ticks the game has performed
   */
  protected void spawnMultipleWithSmoothedDirection(World world, BreathNode.Power power, int particlesPerTick, int tickCount)
  {
    if (tickCount != previousTickCount + 1) {
      previousDirection = direction;
      previousOrigin = origin;
    } else {
      if (previousDirection == null) previousDirection = direction;
      if (previousOrigin == null) previousOrigin = origin;
    }
    for (int i = 0; i < particlesPerTick; ++i) {
      float partialTickHeadStart = i / (float)particlesPerTick;
      Vec3 interpDirection = interpolateVec(previousDirection, direction, partialTickHeadStart);
      Vec3 interpOrigin = interpolateVec(previousOrigin, origin, partialTickHeadStart);
      EntityFX entityFX = createSingleParticle(world, interpOrigin, interpDirection, power, partialTickHeadStart);
      Minecraft.getMinecraft().effectRenderer.addEffect(entityFX);
    }
    previousDirection = direction;
    previousOrigin = origin;
    previousTickCount = tickCount;
  }


  protected abstract EntityFX createSingleParticle(World world, Vec3 origin, Vec3 direction, BreathNode.Power power, float partialTickHeadStart);

  /**
   * interpolate from vector 1 to vector 2 using fraction
   * @param vector1
   * @param vector2
   * @param fraction 0 - 1; 0 = vector1, 1 = vector2
   * @return interpolated vector
   */
  protected Vec3 interpolateVec(Vec3 vector1, Vec3 vector2, float fraction)
  {
    return new Vec3(vector1.xCoord * (1-fraction) + vector2.xCoord * fraction,
                    vector1.yCoord * (1-fraction) + vector2.yCoord * fraction,
                    vector1.zCoord * (1-fraction) + vector2.zCoord * fraction
                    );
  }
}
