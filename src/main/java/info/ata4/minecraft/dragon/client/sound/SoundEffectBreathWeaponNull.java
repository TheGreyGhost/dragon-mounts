package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;

/**
 * Created by TheGreyGhost on 8/10/14.
 *
 * Null sound (silent)
 */
public class SoundEffectBreathWeaponNull extends SoundEffectBreathWeapon
{
  public SoundEffectBreathWeaponNull(SoundController i_soundController, WeaponSoundUpdateLink i_weaponSoundUpdateLink)
  {
    super(i_soundController, i_weaponSoundUpdateLink);
  }

  @Override
  public void performTick(EntityPlayerSP entityPlayerSP)
  {
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
    return null;
  }


}
