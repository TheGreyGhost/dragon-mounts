package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * Created by TGG on 30/07/2015.
 * BreathNodeIce
 */
public class BreathNodeIce extends BreathNode {
  public BreathNodeIce(Power i_power, DragonBreathMode i_dragonBreathMode)
  {
    super(i_power, i_dragonBreathMode, ICE_INITIAL_SPEED, ICE_NODE_DIAMETER_IN_BLOCKS, ICE_DEFAULT_AGE_IN_TICKS);
  }

  public static class BreathNodeIceFactory implements BreathNodeFactory
  {
    @Override
    public BreathNode createBreathNode(Power i_power, DragonBreathMode dragonBreathMode)
    {
      return new BreathNodeIce(i_power, DragonBreathMode.DEFAULT);
    }
  }

  private static final float ICE_INITIAL_SPEED = 0.8F;                // blocks per tick at full speed
  private static final float ICE_NODE_DIAMETER_IN_BLOCKS = 4.0F;
  private static final int ICE_DEFAULT_AGE_IN_TICKS = 50;
  private static final double SPEED_VARIATION_ABS = 0.25;          // plus or minus this amount (3 std deviations)

  /**
   * Update the age of the node based on what is happening (collisions) to the associated entity
   * Should be called once per tick
   * @param parentEntity the entity associated with this node
   * @param currentAge the current age of the entity (ticks)
   * @return the new age of the entity
   */
  @Override
  protected float calculateNewAge(Entity parentEntity, float currentAge)
  {
    if (parentEntity.isInLava()) {  // extinguish in lava
      ageTicks = getMaxLifeTime() + 1;
      return ageTicks;
    }

    if (ageTicks++ > getMaxLifeTime()) {
      return ageTicks;
    }

//    // collision ages breath node faster
//    if (parentEntity.isCollided) {
//      ageTicks += 5;
//    }

    // slow breath nodes age very fast (they look silly when sitting still)
    final double SPEED_THRESHOLD = getStartingSpeed() * 0.25;
    double speedSQ = parentEntity.motionX * parentEntity.motionX
            + parentEntity.motionY * parentEntity.motionY
            + parentEntity.motionZ * parentEntity.motionZ;
    if (speedSQ < SPEED_THRESHOLD * SPEED_THRESHOLD) {
      ageTicks += 20;
    }
    return ageTicks;
  }

  @Override
  public float getCurrentDiameterOfEffect()
  {
    return getConicalBeamDiameter();
  }

  @Override
  public Vec3d getRandomisedStartingMotion(Vec3d initialDirection, Random rand)
  {
    return getRandomisedStartingMotion(initialDirection, rand, SPEED_VARIATION_ABS);
  }


}