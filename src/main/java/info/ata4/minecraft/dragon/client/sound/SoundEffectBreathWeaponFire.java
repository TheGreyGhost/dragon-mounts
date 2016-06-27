package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;
import net.minecraft.util.ResourceLocation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by TheGreyGhost on 24/6/16.
 *  Sounds effects for the dragon's mouth for fire
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
  protected SoundEffectName weaponHeadSound(SoundPart soundPart, DragonLifeStage lifeStage)
  {
    final SoundEffectName hatchling[] = {SoundEffectName.HATCHLING_BREATHE_FIRE_START,
                                          SoundEffectName.HATCHLING_BREATHE_FIRE_LOOP,
                                          SoundEffectName.HATCHLING_BREATHE_FIRE_STOP};

    final SoundEffectName juvenile[] = {SoundEffectName.JUVENILE_BREATHE_FIRE_START,
                                          SoundEffectName.JUVENILE_BREATHE_FIRE_LOOP,
                                          SoundEffectName.JUVENILE_BREATHE_FIRE_STOP};

    final SoundEffectName adult[] = {SoundEffectName.ADULT_BREATHE_FIRE_START,
                                      SoundEffectName.ADULT_BREATHE_FIRE_LOOP,
                                      SoundEffectName.ADULT_BREATHE_FIRE_STOP};

    SoundEffectName[] soundEffectNames;
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
    return soundEffectNames[soundPart.ordinal()];
  }


}
