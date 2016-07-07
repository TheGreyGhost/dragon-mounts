package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;

/**
 * Created by TGG on 24/06/2016.
 */
public class SoundEffectProjectileEnder extends SoundEffectProjectile
{
  public SoundEffectProjectileEnder(SoundController i_soundController, ProjectileSoundUpdateLink i_projectileSoundUpdateLink)
  {
    super(i_soundController, i_projectileSoundUpdateLink);
  }

  /**
   * Returns the sound of the projectile for the given breed, lifestage, and sound part
   * @param soundPart which part of the breathing sound?
   * @param lifeStage how old is the dragon?
   * @return the resourcelocation corresponding to the desired sound.  null for none.
   */
  protected SoundEffectName projectileSound(SoundPart soundPart, DragonLifeStage lifeStage)
  {
    if (soundPart == SoundPart.SPAWN) return SoundEffectName.BREATHE_ENDER_SPAWN;
    if (soundPart == SoundPart.LOOP) return SoundEffectName.ENDER_PROJECTILE_LOOP;
    return SoundEffectName.SILENCE;
  }
}
