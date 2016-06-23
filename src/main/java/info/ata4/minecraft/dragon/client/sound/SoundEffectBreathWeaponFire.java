package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;
import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by TheGreyGhost on 8/10/14.
 *
 * Used to create sound effects for the breath weapon tool - start up, sustained loop, and wind-down
 * The sound made by the dragon's head
 *   1) initial startup
 *   2) looping while breathing
 *   3) stopping when done
 *  Sometimes the sound doesn't layer properly on the first try.  I don't know why.  I have implemented a preload
 *    which seems to help.
 *
 * The SoundEffectBreathWeapon corresponds to the breath weapon of a single dragon.  Typical usage is:
 * 1) create an instance, and provide a callback function (WeaponSoundUpdateLink)
 * 2) startPlaying(), startPlayingIfNotAlreadyPlaying(), stopPlaying() to start or stop the sounds completely
 * 3) once per tick, call performTick().
 *   3a) performTick() will call the WeaponSoundUpdateLink.refreshWeaponSoundInfo(), which should return the
 *       current data relevant to the sound (eg whether the dragon is breathing, and the location of the beam)
 *
 * Is intended to be subclassed for future different breath weapons.
 *
 */
public class SoundEffectBreathWeaponFire extends SoundEffectBreathWeapon
{
  public SoundEffectBreathWeaponFire(SoundController i_soundController, WeaponSoundUpdateLink i_weaponSoundUpdateLink)
  {
    super(i_soundController, i_weaponSoundUpdateLink);
  }

  /**
   * Returns the sound for the given breed, lifestage, and sound part 
   * @param soundPart which part of the breathing sound?
   * @param lifeStage how old is the dragon?
   * @return the resourcelocation corresponding to the desired sound
   */
  @Override
  protected ResourceLocation weaponHeadSound(SoundPart soundPart, DragonLifeStage lifeStage)
  {
    final SoundEffectNames hatchling[] = {SoundEffectNames.HATCHLING_BREATHE_FIRE_START,
                                          SoundEffectNames.HATCHLING_BREATHE_FIRE_LOOP,
                                          SoundEffectNames.HATCHLING_BREATHE_FIRE_STOP};

    final SoundEffectNames juvenile[] = {SoundEffectNames.JUVENILE_BREATHE_FIRE_START,
                                          SoundEffectNames.JUVENILE_BREATHE_FIRE_LOOP,
                                          SoundEffectNames.JUVENILE_BREATHE_FIRE_STOP};

    final SoundEffectNames adult[] = {SoundEffectNames.ADULT_BREATHE_FIRE_START,
                                      SoundEffectNames.ADULT_BREATHE_FIRE_LOOP,
                                      SoundEffectNames.ADULT_BREATHE_FIRE_STOP};

    SoundEffectNames [] soundEffectNames;
    switch (lifeStage) {
      case HATCHLING: {
        soundEffectNames = hatchling;
        break;
      }
      case JUVENILE: {
        soundEffectNames = juvenile;
        break;
      }
      case ADULT: {
        soundEffectNames = adult;
        break;
      }
      default: {
        System.err.println("Unknown lifestage:" + lifeStage + " in weaponHeadSound()");
        soundEffectNames = hatchling; // dummy
      }
    }
    return new ResourceLocation(soundEffectNames[soundPart.ordinal()].getJsonName());
  }


}
