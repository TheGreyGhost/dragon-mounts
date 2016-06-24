package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.util.ResourceLocation;

/**
 * Created by TGG on 24/06/2016.
 * Plays nothing
 */
public class ComponentSoundSilent extends ComponentSound
{
  public ComponentSoundSilent()
  {
    super(SILENCE);
    final float OFF_VOLUME = 0.0F;
    volume = OFF_VOLUME;
  }

  @Override
  public void update()
  {
    final float OFF_VOLUME = 0.0F;
    this.volume = OFF_VOLUME;
    setDonePlaying();
  }

  static private ResourceLocation SILENCE = new ResourceLocation(SoundEffectNames.SILENCE.getJsonName());
}
