package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import speedytools.common.utilities.ErrorLog;
import speedytools.common.utilities.UsefulFunctions;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by TheGreyGhost on 8/10/14.
 *
 * Used to create sound effects for the breath weapon tool - start up, sustained loop, and wind-down
 * It overlays a number of component sounds to produce the breath weapon effect
 * The components are:
 * A) The sound made by the dragon's head
 *   1) initial startup
 *   2) looping while breathing
 *   3) stopping when done
 * B) The sound made by the breath weapon beam
 *   1) fades in
 *   2) loops while breathing
 *   3) fades out
 *   4) the volume and epicentre are set by the closest part of the beam to the player
 *
 *
 * The SoundEffectBreathWeapon corresponds to the breath weapon of a single dragon.  Typical usage is:
 * 1) create an instance, and provide a callback function (WeaponSoundUpdateLink)
 * 2) startPlaying(), startPlayingIfNotAlreadyPlaying(), stopPlaying() to start or stop the sounds completely
 * 3) once per tick, call performTick().
 *   3a) performTick() will call the WeaponSoundUpdateLink.refreshWeaponSoundInfo(), which should return the
 *       current data relevant to the sound (eg whether the dragon is breathing, and the location of the beam)
 *
 */
public class SoundEffectBreathWeapon
{
  public SoundEffectBreathWeapon(SoundController i_soundController, WeaponSoundUpdateLink i_weaponSoundUpdateLink)
  {
    soundController = i_soundController;
    headStartupResource = new ResourceLocation(SoundEffectNames.WEAPON_FIRE_START.getJsonName());
    headLoopResource = new ResourceLocation(SoundEffectNames.WEAPON_FIRE_LOOP.getJsonName());
    headStoppingResource = new ResourceLocation(SoundEffectNames.WEAPON_FIRE_STOP.getJsonName());
    weaponSoundUpdateLink = i_weaponSoundUpdateLink;
  }

  private final float HEAD_MIN_VOLUME = 0.02F;
  private final float HEAD_MAX_VOLUME = 0.2F;
  private final float PERFORM_MIN_VOLUME = 0.02F;
  private final float PERFORM_MAX_VOLUME = 0.2F;
  private final float FAIL_MIN_VOLUME = 0.02F;
  private final float FAIL_MAX_VOLUME = 0.2F;

  public void startPlaying(EntityPlayerSP entityPlayerSP)
  {
    stopAllSounds();
    currentWeaponState = WeaponSoundInfo.State.IDLE;
    performTick(entityPlayerSP);
  }

  public void startPlayingIfNotAlreadyPlaying(EntityPlayerSP entityPlayerSP)
  {
    if (performSound != null && !performSound.isDonePlaying()) return;
    startPlaying(entityPlayerSP);
  }

  public void stopPlaying()
  {
    stopAllSounds();
  }

  private void stopAllSounds()
  {
    if (headStartupSound != null) {
      soundController.stopSound(headStartupSound);
      headStartupSound = null;
    }
    if (headLoopSound1 != null) {
      soundController.stopSound(headLoopSound1);
      headLoopSound1 = null;
    }
    if (performSound != null) {
      soundController.stopSound(performSound);
      performSound = null;
    }
//    if (failSound != null) {
//      soundController.stopSound(failSound);
//      failSound = null;
//    }
  }

  private void setAllStopFlags()
  {
    if (headStartupSound != null) { headStartupSound.donePlaying = true;}
    if (headLoopSound1 != null) { headLoopSound1.donePlaying = true;}
    if (performSound != null) { performSound.donePlaying = true;}
//    if (failSound != null) { failSound.donePlaying = true;}
  }

  /**
   * Updates all the component sounds according to the state of the beam weapon.
   * @param entityPlayerSP
   */
  public void performTick(EntityPlayerSP entityPlayerSP)
  {
    ++ticksElapsed;
    WeaponSoundInfo weaponSoundInfo = new WeaponSoundInfo();
    boolean keepPlaying = weaponSoundUpdateLink.refreshWeaponSoundInfo(weaponSoundInfo);
    if (!keepPlaying) {
      setAllStopFlags();
      return;
    }

    // if state has changed, stop and start component sounds appropriately

    if (weaponSoundInfo.breathingState != currentWeaponState) {
      switch (weaponSoundInfo.breathingState) {
        case IDLE: {
          breathingStopTick = ticksElapsed;
          if (headStartupSound != null) {
            soundController.stopSound(headStartupSound);
          }
          for (BreathWeaponSound breathWeaponSound : headLoopSounds) {
            soundController.stopSound(breathWeaponSound);
          }
          if (headStoppingSound != null) {
            soundController.stopSound(headStoppingSound);
          }
          headStoppingSound = new BreathWeaponSound(headStoppingResource, HEAD_MIN_VOLUME, RepeatType.NO_REPEAT,
                                                    headStoppingSettings);
          soundController.playSound(headStoppingSound);
          break;
        }
        case BREATHING: {
          breathingStartTick = ticksElapsed;
          if (headStartupSound != null) {
//             chargeSound.donePlaying = true;
            soundController.stopSound(headStartupSound);
          }
          headStartupSound = new BreathWeaponSound(headStartupResource, HEAD_MIN_VOLUME, RepeatType.NO_REPEAT,
                                                   headStartSettings);
          powerupStartTick = ticksElapsed;
          soundController.playSound(headStartupSound);
          break;
        }
        default: {
          System.err.printf("Illegal weaponSoundInfo.ringState:" + weaponSoundInfo.breathingState + " in " + this.getClass());
        }
      }
      currentWeaponState = weaponSoundInfo.breathingState;
    }

    // update component sound settings based on weapon info and elapsed time

    switch (currentWeaponState) {
      case BREATHING: {
        final int POWERUP_SOUND_DURATION_TICKS = 40;
        final int POWERUP_VOLUME_RAMP_TICKS = 10;
        final int POWERUP_VOLUME_CROSSFADE_TICKS = 10;
        if (ticksElapsed - powerupStartTick == POWERUP_SOUND_DURATION_TICKS) {
          if (headLoopSound1 != null) {
//            chargeLoopSound.donePlaying = true;
            soundController.stopSound(headLoopSound1);
          }
          headLoopSound1 = new BreathWeaponSound(headLoopResource, HEAD_MIN_VOLUME, RepeatType.REPEAT, headLoopSettings);
          soundController.playSound(headLoopSound1);
        }

        if (ticksElapsed - powerupStartTick <= POWERUP_SOUND_DURATION_TICKS) {
          float newVolume = HEAD_MIN_VOLUME + (HEAD_MAX_VOLUME - HEAD_MIN_VOLUME) * (ticksElapsed - powerupStartTick) / (float)POWERUP_VOLUME_RAMP_TICKS;
          headStartSettings.masterVolume = UsefulFunctions.clipToRange(newVolume, HEAD_MIN_VOLUME, HEAD_MAX_VOLUME);

          newVolume = PERFORM_MIN_VOLUME + (PERFORM_MAX_VOLUME - PERFORM_MIN_VOLUME) * (ticksElapsed - powerupStartTick) / (float)POWERUP_VOLUME_RAMP_TICKS;
          headStoppingSettings.masterVolume = UsefulFunctions.clipToRange(newVolume, PERFORM_MIN_VOLUME, PERFORM_MAX_VOLUME);
          headLoopSettings.masterVolume = 0.0F;
        } else if (ticksElapsed - powerupStartTick <= POWERUP_SOUND_DURATION_TICKS + POWERUP_VOLUME_CROSSFADE_TICKS) {
          int crossfadeTicks = ticksElapsed - powerupStartTick - POWERUP_SOUND_DURATION_TICKS;
          float newVolume = HEAD_MIN_VOLUME + (HEAD_MAX_VOLUME - HEAD_MIN_VOLUME) * crossfadeTicks / (float)POWERUP_VOLUME_CROSSFADE_TICKS;
          headLoopSettings.masterVolume = UsefulFunctions.clipToRange(newVolume, HEAD_MIN_VOLUME, HEAD_MAX_VOLUME);
          headStartSettings.masterVolume = HEAD_MAX_VOLUME - headLoopSettings.masterVolume;
          headStoppingSettings.masterVolume = PERFORM_MAX_VOLUME;
        } else {
          headLoopSettings.masterVolume = HEAD_MAX_VOLUME;
          headStoppingSettings.masterVolume = PERFORM_MAX_VOLUME;
          headStartSettings.masterVolume = 0;
        }

        final int PERFORM_VOLUME_FADEDOWN_TICKS = 5;
        if (currentWeaponState == WeaponSoundInfo.State.PERFORMING_ACTION) {
          headStoppingSettings.masterVolume = PERFORM_MAX_VOLUME;
          int crossfadeTime = ticksElapsed - performStartTick;
          if (crossfadeTime <= PERFORM_VOLUME_FADEDOWN_TICKS) {
            float newVolume = HEAD_MAX_VOLUME / (float)PERFORM_VOLUME_FADEDOWN_TICKS;
            headStartSettings.masterVolume = newVolume;
            headLoopSettings.masterVolume = newVolume;
          } else {
            headLoopSettings.masterVolume = 0;
            headStartSettings.masterVolume = 0;
          }
        }
        break;
      }
      case SPIN_DOWN:
      case SPIN_UP_ABORT: {
        headStartSettings.masterVolume = 0;
        headLoopSettings.masterVolume = 0;

        final int ABORT_VOLUME_FADEDOWN_TICKS = 20;
        headStoppingSettings.masterVolume -= PERFORM_MAX_VOLUME / (float)ABORT_VOLUME_FADEDOWN_TICKS;
        if (headStoppingSettings.masterVolume < 0) {
          headStoppingSettings.masterVolume = 0;
          if (performSound != null) {
//            performSound.donePlaying = true;
            soundController.stopSound(performSound);
          }
        }
        break;
      }
      case FAILURE: {
        headStartSettings.masterVolume = 0;
        headLoopSettings.masterVolume = 0;
        failSettings.masterVolume = FAIL_MAX_VOLUME;

        final int FAILURE_VOLUME_FADEDOWN_TICKS = 20;
        headStoppingSettings.masterVolume -= PERFORM_MAX_VOLUME / (float)FAILURE_VOLUME_FADEDOWN_TICKS;
        if (headStoppingSettings.masterVolume < 0) {
          headStoppingSettings.masterVolume = 0;
          if (performSound != null) {
//            performSound.donePlaying = true;
            soundController.stopSound(performSound);
          }
        }
        break;
      }
      case IDLE: {
//        performSound.donePlaying = true;
        if (performSound != null) {
          soundController.stopSound(performSound);
          performSound = null;
        }
        break;
      }
    }

  }

  private int ticksElapsed;
  private int breathingStartTick;
  private int breathingStopTick;
  private int powerupStartTick;
  private int performStartTick;
  private int performStopTick;
  private int failureStartTick;
  private int spinupAbortTick;
  WeaponSoundInfo.State currentWeaponState = WeaponSoundInfo.State.IDLE;

  private ComponentSoundSettings headStartSettings = new ComponentSoundSettings(0.01F);
  private ComponentSoundSettings headLoopSettings = new ComponentSoundSettings(0.01F);
  private ComponentSoundSettings headStoppingSettings = new ComponentSoundSettings(0.01F);
  private ComponentSoundSettings failSettings = new ComponentSoundSettings(0.01F);

  private BreathWeaponSound headStartupSound;
  private ArrayList<BreathWeaponSound> headLoopSounds;
  private BreathWeaponSound headStoppingSound;
  private ArrayList<BreathWeaponSound> beamSounds;

  private SoundController soundController;
  private ResourceLocation headStartupResource;
  private ResourceLocation headLoopResource;
  private ResourceLocation headStoppingResource;
  private ResourceLocation beamLoopResource;

  private WeaponSoundUpdateLink weaponSoundUpdateLink;

  /**
   * Used as a callback to update the sound's position and
   */
  public interface WeaponSoundUpdateLink
  {
    public boolean refreshWeaponSoundInfo(WeaponSoundInfo infoToUpdate);
  }

  public static class WeaponSoundInfo
  {
    public enum State {IDLE, BREATHING}
    public State breathingState = State.IDLE;
    public Collection<Vec3> pointsWithinBeam;
  }

  // settings for each component sound
  private static class ComponentSoundSettings
  {
    public ComponentSoundSettings(float i_volume)
    {
      masterVolume = i_volume;
    }
    public float masterVolume;
    public Vec3 soundEpicentre;
    public float playerDistanceToEpicentre;
    public boolean playing;
  }

  public enum RepeatType {REPEAT, NO_REPEAT}

  private class BreathWeaponSound extends PositionedSound implements ITickableSound
  {
    public BreathWeaponSound(ResourceLocation i_resourceLocation, float i_volume, RepeatType i_repeat,
                             ComponentSoundSettings i_soundSettings)
    {
      super(i_resourceLocation);
      repeat = (i_repeat == RepeatType.REPEAT);
      volume = i_volume;
      attenuationType = AttenuationType.NONE;
      soundSettings = i_soundSettings;
    }

    private boolean donePlaying;
    ComponentSoundSettings soundSettings;

    @Override
    public boolean isDonePlaying() {
      return donePlaying;
    }

    @Override
    public void update() {
      final float MINIMUM_VOLUME = 0.01F;
      final float MAXIMUM_VOLUME = 0.05F;
      final float INSIDE_VOLUME = 0.10F;
      final float OFF_VOLUME = 0.0F;
      if (!soundSettings.playing) {
//        donePlaying = true;
        this.volume = OFF_VOLUME;
      } else {
//        System.out.println(boundaryHumInfo.playerDistanceToEpicentre);
        this.xPosF = (float)soundSettings.soundEpicentre.xCoord;
        this.yPosF = (float)soundSettings.soundEpicentre.yCoord;
        this.zPosF = (float)soundSettings.soundEpicentre.zCoord;
        if (soundSettings.playerDistanceToEpicentre < 0.01F) {
          this.volume = INSIDE_VOLUME;
        } else {
          final float MINIMUM_VOLUME_DISTANCE = 20.0F;
          float fractionToMinimum = soundSettings.playerDistanceToEpicentre / MINIMUM_VOLUME_DISTANCE;
          this.volume = MathX.clamp(MAXIMUM_VOLUME - fractionToMinimum * (MAXIMUM_VOLUME - MINIMUM_VOLUME),
                                    MINIMUM_VOLUME, MAXIMUM_VOLUME);
        }
      }
    }
  }
}