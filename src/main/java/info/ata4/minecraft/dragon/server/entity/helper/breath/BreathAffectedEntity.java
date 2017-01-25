package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.util.math.Vec3d;

import static com.google.common.base.Preconditions.checkArgument;

/**
* Created by TGG on 7/08/2015.
 *  * Models an entity which is being affected by the breath weapon
 * Every tick that an entity is exposed to the breath weapon, its "hit density" increases.
 *  Keeps track of the number of ticks that the entity has been exposed to the breath.
 *  Typical usage:
 *  1) Create a new BreathAffectedEntity for the entity
 *  2) Each time it is hit, call addHitDensity() proportional to the exposure + strength of the weapon
 *  3) Every tick, call decayEntityEffectTick
 *
 *  Query the entity's damage by
 *  1a) getHitDensity
 *  1b) getHitDensityDirection
 *  2) isUnaffected
 *  3) each tick: if applyDamageThisTick() is true, apply the weapon damage now.  (This is used to space out the
 *     damage so that armour doesn't protect so much (eg 20 damage delivered once per second instead of 1 damage
 *     delivered twenty times per second - (a player with armour is invulnerable to that)  )
 *     When damage is applied, call resetHitDensity() to clear the density
 */
public class BreathAffectedEntity
{
  public BreathAffectedEntity()
  {
    hitDensity = 0.0F;
    timeSinceLastHit = 0;
    ticksUntilDamageApplied = TICKS_BETWEEN_DAMAGE_APPLICATION;
  }

  /**
   * increases the hit density of the entity
   * @param beamDirection the direction that the breathweapon is travelling (need not be normalised)
   * @param increase the amount to increase the hit density by
   */
  public void addHitDensity(Vec3d beamDirection, float increase)
  {
    Vec3d oldWeightedDirection = MathX.multiply(averageDirection.normalize(), hitDensity);
    Vec3d addedWeightedDirection = MathX.multiply(beamDirection.normalize(), increase);
    Vec3d newAverageDirection = oldWeightedDirection.add(addedWeightedDirection);
    averageDirection = newAverageDirection;

    hitDensity += increase;
    timeSinceLastHit = 0;
  }

  public void resetHitDensity()
  {
    hitDensity = 0;
  }

  /**
   * Gets the average direction of the applied hitDensity (--> for example: if the breath weapon is a stream of water,
   *   this returns the average direction the water is travelling in).
   * @return the direction with density (i.e. not normalised - magnitude equals the hitDensity)
   */
  public Vec3d getHitDensityDirection()
  {
    return averageDirection;
  }

  public float getHitDensity()
  {
    return hitDensity;
  }

  /**
   *  returns true if damage should be applied this tick
   *  @return true if damage should be applied.  Resets after the call (repeated calls return false)
   */
  public boolean applyDamageThisTick()
  {
    if (ticksUntilDamageApplied > 0) return false;
    ticksUntilDamageApplied = TICKS_BETWEEN_DAMAGE_APPLICATION;
    return true;
  }

  private final float ENTITY_RESET_EFFECT_THRESHOLD = 0.01F;
  private final int TICKS_BETWEEN_DAMAGE_APPLICATION = 20;  // apply damage every x ticks

  /** updates the breath weapon's effect for a given entity
   *   called every tick; used to decay the cumulative effect on the entity
   *   for example - an entity being gently bathed in flame might gain 0.2 every time from the beam, and lose 0.2 every
   *     tick in this method.
   */
  public void decayEntityEffectTick()
  {
    if (timeSinceLastHit == 0 && ticksUntilDamageApplied > 0) {
      --ticksUntilDamageApplied;
    }
    if (++timeSinceLastHit < ticksBeforeDecayStarts) return;
    hitDensity *= (1.0F - entityDecayPercentagePerTick / 100.0F);
    if (hitDensity < ENTITY_RESET_EFFECT_THRESHOLD){
      hitDensity = 0.0F;
    }
    averageDirection = MathX.multiply(averageDirection.normalize(), hitDensity);

    ++ticksUntilDamageApplied;
    ticksUntilDamageApplied = Math.min(ticksUntilDamageApplied, TICKS_BETWEEN_DAMAGE_APPLICATION);
  }

  /**
   * Decay the hit Density, without regard for how long ago the last hit occurred
   * @param decayPercentagePerTick the pecentage decay per tick [0.0 - 100.0]
   * @param delayUntilDecayTicks the delay in ticks until decay starts (delay since last call to addHitDensity)
   *                             must be >= 0
   */
  public void setDecayParameters(float decayPercentagePerTick, int delayUntilDecayTicks)
  {
    checkArgument(decayPercentagePerTick >= 0.0 && decayPercentagePerTick <= 100.0);
    checkArgument(delayUntilDecayTicks >= 0);
    entityDecayPercentagePerTick = decayPercentagePerTick;
    ticksBeforeDecayStarts = delayUntilDecayTicks;
  }

  /**
   * Check if this block is unaffected by the breath weapon
   * @return true if the block is currently unaffected
   */
  public boolean isUnaffected()
  {
    return hitDensity < ENTITY_RESET_EFFECT_THRESHOLD;
  }

  private float hitDensity;
  private int timeSinceLastHit;
  private int ticksUntilDamageApplied;
  private Vec3d averageDirection = new Vec3d(0, 0, 0);

  private float entityDecayPercentagePerTick = 5.0F;
  private int ticksBeforeDecayStarts = 40;

}
