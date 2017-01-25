package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import info.ata4.minecraft.dragon.server.entity.helper.breath.DragonBreathMode;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

/** EntityFX used to refer to all BreathFX types
 * Created by TGG on 6/03/2016.
 */
public class BreathFX extends Particle {
  public BreathFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn,
                  double zSpeedIn) {
    super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
  }

  public void updateBreathMode(DragonBreathMode dragonBreathMode)
  {
    breathNode.changeBreathMode(dragonBreathMode);
  }

  protected BreathNode breathNode;
}
