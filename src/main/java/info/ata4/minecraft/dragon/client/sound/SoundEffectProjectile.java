package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by TheGreyGhost on 8/10/14.
 *
 * Used to create sound effects for a projectile   - start up, sustained loop, and wind-down
 * The sound made by the projectile is
 *   1) initial startup
 *   2) looping while in flight
 *   3) stopping when done
 *  Sometimes the sound doesn't layer properly on the first try.  I don't know why.  I have implemented a preload
 *    which seems to help.
 *
 * The SoundEffectProjectile corresponds to a breath weapon projectile in flight.  Typical usage is:
 * 1) create an instance, and provide a callback function (ProjectileSoundUpdateLink)
 * 2) startPlaying(), stopPlaying() to start or stop the sounds completely
 * 3) once per tick, call performTick().
 *   3a) performTick() will call the ProjectileSoundUpdateLink.refreshSoundInfo(), which should return the
 *       current data relevant to the sound (eg what state the projectile is in, and its location)
 *
 * Is subclassed for different projectiles
 *
 * Has a lot in common with SoundEffectBreathWeapon but in the end I kept them separate to make them easier to understand
 */
public abstract class SoundEffectProjectile
{
  public SoundEffectProjectile(SoundController i_soundController, ProjectileSoundUpdateLink i_projectileSoundUpdateLink)
  {
    soundController = i_soundController;
    projectileSoundUpdateLink = i_projectileSoundUpdateLink;
  }

  private final float PROJECTILE_MIN_VOLUME = 0.02F;

  public void startPlaying(EntityPlayerSP entityPlayerSP)
  {
    stopAllSounds();
    currentProjectileState = ProjectileSoundInfo.State.IN_FLIGHT;
    performTick(entityPlayerSP);
  }

  public void stopPlaying()
  {
    stopAllSounds();
  }


  /**
   * Updates all the component sounds according to the state of the breath weapon.
   * @param entityPlayerSP
   */
  public void performTick(EntityPlayerSP entityPlayerSP) {
    ProjectileSoundInfo projectileSoundInfo = new ProjectileSoundInfo();
    boolean keepPlaying = projectileSoundUpdateLink.refreshSoundInfo(projectileSoundInfo);
    if (!keepPlaying) {
      setAllStopFlags();
      return;
    }
    checkNotNull(projectileSoundInfo.location);
    soundSettings.playing = true;
    soundSettings.masterVolume = projectileSoundInfo.relativeVolume;
    soundSettings.soundEpicentre = projectileSoundInfo.location;

    soundSettings.playerDistanceToEpicentre =
              (float) projectileSoundInfo.location.distanceTo(entityPlayerSP.getPositionVector());

    final int STARTUP_TICKS = 40;
    final int STOPPING_TICKS = 60;

    // if state has changed, stop and start component sounds appropriately

    if (projectileSoundInfo.breathingState != currentProjectileState) {
      switch (projectileSoundInfo.breathingState) {
        case NOT_CREATED: {
          stopAllProjectileSounds();
          break;
        }
        case IN_FLIGHT: {
          stopAllProjectileSounds();
          ComponentSound preloadLoop = new ComponentSound(projectileSound(SoundPart.LOOP, projectileSoundInfo.lifeStage),
                  ComponentSound.Mode.PRELOAD);
          soundController.playSound(preloadLoop);
          ComponentSound preLoadStop = new ComponentSound(projectileSound(SoundPart.STOP, projectileSoundInfo.lifeStage),
                  ComponentSound.Mode.PRELOAD);
          soundController.playSound(preLoadStop);
          startupSound = new ComponentSound(projectileSound(SoundPart.START, projectileSoundInfo.lifeStage),
                  PROJECTILE_MIN_VOLUME, ComponentSound.RepeatType.NO_REPEAT,
                  soundSettings);
          startupSound.setPlayCountdown(STARTUP_TICKS);
          soundController.playSound(startupSound);
          break;
        }
        case FINISHED: {
          stopAllProjectileSounds();
          stoppingSound = new ComponentSound(projectileSound(SoundPart.STOP, projectileSoundInfo.lifeStage),
                  PROJECTILE_MIN_VOLUME, ComponentSound.RepeatType.NO_REPEAT,
                  soundSettings);
          stoppingSound.setPlayCountdown(STOPPING_TICKS);
          soundController.playSound(stoppingSound);
          break;
        }
        default: {
          System.err.printf("Illegal projectileSoundInfo.breathingState:" + projectileSoundInfo.breathingState + " in " + this
                  .getClass());
        }
      }
      currentProjectileState = projectileSoundInfo.breathingState;
    }

    // update component sound settings based on weapon info and elapsed time

    switch (currentProjectileState) {
      case NOT_CREATED: {
        break;
      }
      case IN_FLIGHT: {
        if (startupSound != null && startupSound.getPlayCountdown() <= 0) {
          stopAllProjectileSounds();
          loopSound = new ComponentSound(projectileSound(SoundPart.LOOP, projectileSoundInfo.lifeStage),
                  PROJECTILE_MIN_VOLUME, ComponentSound.RepeatType.REPEAT, soundSettings);
          soundController.playSound(loopSound);
        }

        break;
      }
      case FINISHED: {
        if (stoppingSound != null) {
          if (stoppingSound.getPlayCountdown() <= 0) {   //|| !soundController.isSoundPlaying(stoppingSound)) {  causes strange bug "channel null in method 'stop'"
            soundController.stopSound(stoppingSound);
            stoppingSound = null;
          }
        }
        break;
      }
      default: {
        System.err.printf("Unknown currentProjectileState:" + currentProjectileState);
      }
    }
  }

  private void stopAllSounds()
  {
    stopAllProjectileSounds();
  }

  private void stopAllProjectileSounds()
  {
    if (startupSound != null) {
      soundController.stopSound(startupSound);
      startupSound = null;
    }
    if (loopSound != null) {
      soundController.stopSound(loopSound);
      loopSound = null;
    }

    if (stoppingSound != null) {
      soundController.stopSound(stoppingSound);
      stoppingSound = null;
    }
  }

  private void setAllStopFlags()
  {
    if (startupSound != null) { startupSound.setDonePlaying();}
    if (loopSound != null) { loopSound.setDonePlaying();}
    if (stoppingSound != null) { stoppingSound.setDonePlaying();}
  }

  private ProjectileSoundInfo.State currentProjectileState = ProjectileSoundInfo.State.NOT_CREATED;

  private ComponentSound.ComponentSoundSettings soundSettings = new ComponentSound.ComponentSoundSettings(1.0F);

  private ComponentSound startupSound;
  private ComponentSound loopSound;
  private ComponentSound stoppingSound;

  private SoundController soundController;
  private ProjectileSoundUpdateLink projectileSoundUpdateLink;

  /**
   * Used as a callback to update the sound's position and
   */
  public interface ProjectileSoundUpdateLink
  {
    boolean refreshSoundInfo(ProjectileSoundInfo infoToUpdate);
  }

  public static class ProjectileSoundInfo
  {
    public enum State {NOT_CREATED, IN_FLIGHT, FINISHED}
    public State breathingState = State.NOT_CREATED;
    public Vec3 location;  // location of the projectile
    public float relativeVolume; // 0 to 1
    public DragonLifeStage lifeStage;
  }

  protected enum SoundPart {START, LOOP, STOP}

  /**
   * Returns the sound of the projectile for the given breed, lifestage, and sound part
   * @param soundPart which part of the breathing sound?
   * @param lifeStage how old is the dragon?
   * @return the resourcelocation corresponding to the desired sound. null for none.
   */
  abstract protected ResourceLocation projectileSound(SoundPart soundPart, DragonLifeStage lifeStage);

}