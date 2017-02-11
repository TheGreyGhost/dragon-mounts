package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.client.render.breeds.IEntityParticle;
import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * Created by TGG on 30/07/2015.
 * BreathNodeForest
 */
public class BreathNodeForest extends BreathNode {
  public BreathNodeForest(Power i_power, DragonBreathMode i_dragonBreathMode)
  {
    super(i_power, i_dragonBreathMode, FOREST_INITIAL_SPEED, INITIAL_NODE_DIAMETER, FOREST_LIFETIME_IN_TICKS);
    isBurning = (i_dragonBreathMode.equals(DragonBreathMode.FOREST_BURNING));
    burnStartAge = ageTicks;
  }

  public static class BreathNodeForestFactory implements BreathNodeFactory
  {
    @Override
    public BreathNode createBreathNode(Power i_power, DragonBreathMode dragonBreathMode)
    {
      return new BreathNodeForest(i_power, dragonBreathMode);
    }
  }

  private static final float FOREST_INITIAL_SPEED = 1.2F;                // blocks per tick at full speed
  private static final double SPEED_VARIATION_ABS = 0.05;          // plus or minus this amount (3 std deviations)

  // The gas node initally moves out very quickly then slows to a stop. It is small
  // After this initial rapid period, when it stops, it slowly expands outwards in a random direction getting bigger

  private static final int RAPID_MOVE_TICKS = 5;
  private static final int SLOW_DOWN_TICKS = 15;
  private static final int EXPANSION_TICKS = 40;
  private static final int FOREST_LIFETIME_IN_TICKS = RAPID_MOVE_TICKS + SLOW_DOWN_TICKS + EXPANSION_TICKS;

  private static final int BURN_LIFETIME_TICKS = 10;  // lifetime once ignited

  private static final float INITIAL_NODE_DIAMETER = 0.5F;  // in blocks
  private static final float FINAL_NODE_DIAMETER = 4.0F;
  private static final float EXPANSION_SPEED = 0.1F; // in blocks

  /**
   * Update the age of the node based on what is happening (collisions) to the associated entity
   * Should be called once per tick
   * @param parentEntity the entity associated with this node
   * @param currentAge the current age of the entity (ticks)
   * @return the new age of the entity
   */
  @Override
  protected float calculateNewAge(IEntityParticle parentEntity, float currentAge)
  {
    if (dragonBreathMode.equals(DragonBreathMode.FOREST_BURNING)) {
      if (!isBurning) {  // catch fire
        isBurning = true;
        burnStartAge = Math.max(ageTicks, RAPID_MOVE_TICKS + SLOW_DOWN_TICKS);
        // will burn for BURN_LIFETIME_TICKS, once the cloud expansion has begun
      }
    }

    if (isBurning) {
      if (ageTicks++ > burnStartAge + BURN_LIFETIME_TICKS) {  // after a short burn, die.
        return getMaxLifeTime() + 1;
      }
      return ageTicks;
    }

    if (ageTicks++ > getMaxLifeTime()) {
      return ageTicks;
    }

//    // slow breath nodes age very fast (they look silly when sitting still)
//    final double SPEED_THRESHOLD = getStartingSpeed() * 0.25;
//    double speedSQ = parentEntity.motionX * parentEntity.motionX
//            + parentEntity.motionY * parentEntity.motionY
//            + parentEntity.motionZ * parentEntity.motionZ;
//    if (speedSQ < SPEED_THRESHOLD * SPEED_THRESHOLD) {
//      ageTicks += 20;
//    }
    return ageTicks;
  }

  @Override
  public void modifyEntityVelocity(IEntityParticle entity)
  {
    Vec3d entityVelocity = new Vec3d(entity.getMotionX(), entity.getMotionY(), entity.getMotionZ());

    double speed = getStartingSpeed();

    if (startedExpansion) {
      return;
    }

    boolean shouldStartExpansion = false;
    if (entity.isCollided()) {
      shouldStartExpansion = true;
    } else {
      if (ageTicks < RAPID_MOVE_TICKS) {
        // don't change speed
      } else if (ageTicks < RAPID_MOVE_TICKS + SLOW_DOWN_TICKS) {
        double slowDownFraction = (ageTicks - RAPID_MOVE_TICKS) / SLOW_DOWN_TICKS;
  //      speed *= Math.cos(Math.PI / 2.0 * slowDownFraction);
        speed *= (1.0 - slowDownFraction);
        entityVelocity = MathX.multiply(entityVelocity.normalize(), speed);
      } else {
        shouldStartExpansion = true;
      }
    }

    if (shouldStartExpansion) {
      startedExpansion = true;
      Random random = new Random();
      entityVelocity = MathX.getRandomPointOnSphere(random, EXPANSION_SPEED);
    }

    entity.setMotion(entityVelocity);
  }

  @Override
  public float getCurrentDiameterOfEffect()
  {
    if (ageTicks < RAPID_MOVE_TICKS + SLOW_DOWN_TICKS) {
      return INITIAL_NODE_DIAMETER;
    } else {
      double expandFraction = (ageTicks - RAPID_MOVE_TICKS - SLOW_DOWN_TICKS) / EXPANSION_TICKS;
      return (float)MathX.lerp(INITIAL_NODE_DIAMETER, FINAL_NODE_DIAMETER, expandFraction);
    }
  }

  @Override
  public Vec3d getRandomisedStartingMotion(Vec3d initialDirection, Random rand)
  {
    return getRandomisedStartingMotion(initialDirection, rand, SPEED_VARIATION_ABS);
  }

  private boolean startedExpansion = false;
  private boolean isBurning = false;
  private float burnStartAge = 0;
}
