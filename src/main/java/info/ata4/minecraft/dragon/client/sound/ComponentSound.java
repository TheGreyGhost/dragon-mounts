package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by TGG on 24/06/2016.
 * ComponentSound is a single sound that can
 * 1) loop repeatedly
 * 2) move position
 * 3) change volume
 * 4) turn on/off
 *
 * Typical usage:
 * 1a) Optional: preload the sound in advance (reduces lag between triggering sound and it actually playing) using
 *      constructor (ResourceLocation, PRELOAD)
 * 1b) Construct the sound with an initial volume, repeat yes/no, plus a ComponentSoundSettings that will
 *       be used to read the updated settings while the sound is playing
 * 2)  Minecraft.getMinecraft().getSoundHandler().playSound(sound);
 * 3)  Update the ComponentSoundSettings object as the sound properties change.  Minecraft automatically ticks the
 *      sound which will update itself to the new properties.
 * 3b)  getPlayCountdown()/setPlayCountdown() can be used by the creator to determine how long the sound has been
 *     playing
 * 3c) isDonePlaying()/setDonePlaying() can be used to stop the sound without destructing it
 *
 *
 */
class ComponentSound extends PositionedSound implements ITickableSound
{
  public ComponentSound(ResourceLocation i_resourceLocation, float i_volume, RepeatType i_repeat,
                        ComponentSoundSettings i_soundSettings)
  {
    super(i_resourceLocation);
    repeat = (i_repeat == RepeatType.REPEAT);
    volume = i_volume;
    attenuationType = AttenuationType.NONE;
    soundSettings = i_soundSettings;
    playMode = Mode.PLAY;
  }

  /**
   * Preload for this sound (plays at very low volume).
   *
   * @param i_resourceLocation the sound to be played
   * @param mode               dummy argument.  Must always be PRELOAD
   */
  public ComponentSound(ResourceLocation i_resourceLocation, Mode mode)
  {
    super(i_resourceLocation);
    checkArgument(mode == Mode.PRELOAD);
    repeat = false;
    final float VERY_LOW_VOLUME = 0.001F;
    volume = VERY_LOW_VOLUME;
    attenuationType = AttenuationType.NONE;
    soundSettings = null;
    playMode = Mode.PRELOAD;
    preloadTimeCountDown = 5;  // play for a few ticks only
  }

  // settings for each component sound
  protected static class ComponentSoundSettings
  {
    public ComponentSoundSettings(float i_volume)
    {
      masterVolume = i_volume;
    }
    public float masterVolume;  // multiplier for the volume = 0 .. 1
    public Vec3 soundEpicentre;
    public float playerDistanceToEpicentre;
    public boolean playing;
  }

  public enum RepeatType {REPEAT, NO_REPEAT}
  public enum Mode {PRELOAD, PLAY}


  public int getPlayCountdown()
  {
    return playTimeCountDown;
  }

  public void setPlayCountdown(int countdown)
  {
    playTimeCountDown = countdown;
  }

  @Override
  public boolean isDonePlaying()
  {
    return donePlaying;
  }

  public void setDonePlaying()
  {
    donePlaying = true;
  }

  @Override
  public void update()
  {
    final float MINIMUM_VOLUME = 0.10F;
    final float MAXIMUM_VOLUME = 1.00F;
    final float INSIDE_VOLUME = 1.00F;
    final float OFF_VOLUME = 0.0F;

    if (playMode == Mode.PRELOAD) {
      if (--preloadTimeCountDown <= 0) {
        this.volume = OFF_VOLUME;
      }
      return;
    }

    --playTimeCountDown;
    if (!soundSettings.playing) {
      this.volume = OFF_VOLUME;
    } else {
      this.xPosF = (float) soundSettings.soundEpicentre.xCoord;
      this.yPosF = (float) soundSettings.soundEpicentre.yCoord;
      this.zPosF = (float) soundSettings.soundEpicentre.zCoord;
      if (soundSettings.playerDistanceToEpicentre < 0.01F) {
        this.volume = INSIDE_VOLUME;
      } else {
        final float MINIMUM_VOLUME_DISTANCE = 40.0F;
        float fractionToMinimum = soundSettings.playerDistanceToEpicentre / MINIMUM_VOLUME_DISTANCE;
        this.volume = soundSettings.masterVolume *
                MathX.clamp(MAXIMUM_VOLUME - fractionToMinimum * (MAXIMUM_VOLUME - MINIMUM_VOLUME),
                        MINIMUM_VOLUME, MAXIMUM_VOLUME);
      }
    }
  }

  private int playTimeCountDown = -1;
  private int preloadTimeCountDown = 0;
  private boolean donePlaying;
  private ComponentSoundSettings soundSettings;
  private Mode playMode;

}
