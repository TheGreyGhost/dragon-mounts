package info.ata4.minecraft.dragon.client.render.breeds;

import net.minecraft.util.math.Vec3d;

/**
 * Created by TGG on 11/02/2017.
 *
 * Interface to refer to both Entity and Particle (in previous versions of minecraft, Particles (EntityFX) were a
 *   subclass of Entity.  Not any more).
 */
public interface IEntityParticle {
  double getMotionX();
  double getMotionY();
  double getMotionZ();
  double getSpeedSQ();
  boolean isInWater();
  boolean isCollided();
  boolean isInLava();
  void setMotion(Vec3d newMotion);
}
