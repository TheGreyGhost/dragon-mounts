package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import info.ata4.minecraft.dragon.server.entity.helper.breath.DragonBreathMode;
import info.ata4.minecraft.dragon.server.entity.helper.breath.NodeLineSegment;
import info.ata4.minecraft.dragon.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

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

  public void changeBreathMode(DragonBreathMode dragonBreathMode)
  {
    for (BreathFX breathFX : spawnedBreathFX) {
      breathFX.updateBreathMode(dragonBreathMode);
    }
  }


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
    // create a list of NodeLineSegments from the motion path of the BreathNodes
    Iterator<BreathFX> it = spawnedBreathFX.iterator();
    boolean foundLive = false;
    while (it.hasNext() && !foundLive) {
      BreathFX entity = it.next();
      if (entity.isDead) {
        it.remove();
      } else {
        foundLive = true;
      }
    }
    final int MAX_SPAWNED_SIZE = 1000;
    if (spawnedBreathFX.size() > MAX_SPAWNED_SIZE) {  // prevent leak in case EntityFX is never set to dead for some reason
      spawnedBreathFX.clear();
    }

    Random random = new Random();
    if (tickCount != previousTickCount + 1) {
      previousDirection = direction;
      previousOrigin = origin;
    } else {
      if (previousDirection == null) previousDirection = direction;
      if (previousOrigin == null) previousOrigin = origin;
    }
    for (int i = 0; i < particlesPerTick; ++i) {
      float partialTickHeadStart = (i + random.nextFloat()) / (float)particlesPerTick;   // random is for jitter to prevent aliasing
      Vec3 interpDirection = interpolateVec(previousDirection, direction, partialTickHeadStart);
      Vec3 interpOrigin = interpolateVec(previousOrigin, origin, partialTickHeadStart);
      BreathFX breathFX = createSingleParticle(world, interpOrigin, interpDirection, power, tickCount, partialTickHeadStart);
      Minecraft.getMinecraft().effectRenderer.addEffect(breathFX);
      spawnedBreathFX.add(breathFX);
    }
    previousDirection = direction;
    previousOrigin = origin;
    previousTickCount = tickCount;
  }


  protected abstract BreathFX createSingleParticle(World world, Vec3 origin, Vec3 direction, BreathNode.Power power,
                                                   int tickCount, float partialTickHeadStart);

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

  private ArrayList<BreathFX> spawnedBreathFX = new ArrayList<BreathFX>();

}
