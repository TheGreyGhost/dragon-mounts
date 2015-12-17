package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import java.util.Random;

/**
 * Created by TGG on 30/07/2015.
 * BreathNodeWater
 */
public class BreathNodeWater extends BreathNode {
  public BreathNodeWater(Power i_power)
  {
    super(i_power, WATER_INITIAL_SPEED, WATER_NODE_DIAMETER_IN_BLOCKS, WATER_DEFAULT_AGE_IN_TICKS);
  }

  public static class BreathNodeWaterFactory implements BreathNodeFactory
  {
    @Override
    public BreathNode createBreathNode(Power i_power)
    {
      return new BreathNodeWater(i_power);
    }
  }

  private static final float WATER_INITIAL_SPEED = 2.0F;                // blocks per tick at full speed
  private static final float WATER_NODE_DIAMETER_IN_BLOCKS = 1.0F;
  private static final int WATER_DEFAULT_AGE_IN_TICKS = 30;
  private static final double SPEED_VARIATION_ABS = 0.05;          // plus or minus this amount (3 std deviations)

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
    return getConstantSizeBeamDiameter();
  }

  @Override
  public Vec3 getRandomisedStartingMotion(Vec3 initialDirection, Random rand)
  {
    return getRandomisedStartingMotion(initialDirection, rand, SPEED_VARIATION_ABS);
  }


}
