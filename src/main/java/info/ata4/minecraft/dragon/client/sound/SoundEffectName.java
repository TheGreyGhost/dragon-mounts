package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.DragonMounts;

/**
 * User: The Grey Ghost
 * Date: 17/04/2014
 * Contains (some of the) sound effect names used for the dragon
 */
public enum SoundEffectName
{
  SILENCE("mob.enderdragon.silence", 0),

  ADULT_BREATHE_FIRE_START("mob.enderdragon.breathweapon.adultbreathefirestart", 2.0),
  ADULT_BREATHE_FIRE_LOOP("mob.enderdragon.breathweapon.adultbreathefireloop", 5.336),
  ADULT_BREATHE_FIRE_STOP("mob.enderdragon.breathweapon.adultbreathefirestop", 1.0),
  JUVENILE_BREATHE_FIRE_START("mob.enderdragon.breathweapon.juvenilebreathefirestart", 2.0),
  JUVENILE_BREATHE_FIRE_LOOP("mob.enderdragon.breathweapon.juvenilebreathefireloop", 5.336),
  JUVENILE_BREATHE_FIRE_STOP("mob.enderdragon.breathweapon.juvenilebreathefirestop", 1.0),
  HATCHLING_BREATHE_FIRE_START("mob.enderdragon.breathweapon.hatchlingbreathefirestart", 2.0),
  HATCHLING_BREATHE_FIRE_LOOP("mob.enderdragon.breathweapon.hatchlingbreathefireloop", 5.336),
  HATCHLING_BREATHE_FIRE_STOP("mob.enderdragon.breathweapon.hatchlingbreathefirestop", 1.0),

  BREATHE_NETHER_SPAWN("mob.enderdragon.breathweapon.breathenetherspawn", 2.0),
  NETHER_PROJECTILE_LOOP("mob.enderdragon.breathweapon.projectilenetherloop", 3.4);

  public final String getJsonName() {return DragonMounts.AID + ":" + jsonName;}
  public final double getDurationInSeconds() {return durationInSeconds;}
  public final int getDurationInTicks() {return (int)(durationInSeconds * 20.0);}

  /**
   * Information about the sound effect
   * @param i_jsonName
   * @param i_durationInSeconds the duration of the sound effect (0 = unused) - in practice, this is the duration
   *                            before the cross-fade to the next sound starts.  For looping sounds no effect
   */
  private SoundEffectName(String i_jsonName, double i_durationInSeconds) {
    jsonName = i_jsonName;
    durationInSeconds = i_durationInSeconds;
  }
  private final String jsonName;
  private final double durationInSeconds;
}
