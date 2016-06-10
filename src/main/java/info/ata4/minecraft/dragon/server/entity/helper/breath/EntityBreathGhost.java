package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Represents the Ghost Breath weapon lightning strike, as a 'weather effect' entity
 * Used in conjunction with EntityBreathProjectileGhost, i.e.
 *   the projectile is spawned, and when it onUpdates() it adds the weather effect entity to handle the rendering
 * I did it this way to simplify the transmission from server to client and control the collisions better
 * Using the weather effect solves the problem of frustum check, i.e. that the entity doesn't render if the
 *   bounding box is outside your view.  Entity.ignoreFrustumCheck flag couldn't overcome this problem because of
 *   the way that entities are cached per chunk.
 * Created by TGG on 14/03/2016.
 */
public class EntityBreathGhost extends EntityWeatherEffect
{

  public EntityBreathGhost(World worldIn, Vec3 i_startPoint, Vec3 i_endPoint, BreathNode.Power i_power)
  {
    super(worldIn);
    startPoint = i_startPoint;
    endPoint = i_endPoint;
    randomSeed = System.currentTimeMillis();
//    randomSeed = 15125;  //todo remove
    power = i_power;
    this.setLocationAndAngles(startPoint.xCoord, startPoint.yCoord, startPoint.zCoord, 0.0F, 0.0F);
  }

  // I think this is never used for this entity.
  public EntityBreathGhost(World worldIn)
  {
    super(worldIn);
  }

  public Vec3 getEndPoint()
  {
    return endPoint;
  }
  public Vec3 getStartPoint()
  {
    return startPoint;
  }
  public long getRandomSeed()
  {
    return randomSeed;
  }
  public BreathNode.Power getPower()
  {
    return power;
  }

  @Override
  protected void entityInit() {}

  @Override
  protected void readEntityFromNBT(NBTTagCompound tagCompund) {}

  @Override
  protected void writeEntityToNBT(NBTTagCompound tagCompound) {}


  private Vec3 startPoint;
  private Vec3 endPoint;
  private long randomSeed;


  private BreathNode.Power power;

  public enum RenderStage {PRESTRIKE(1), STRIKE(2), POSTSTRIKE(5), DONE(0);
    RenderStage(int i_durationTicks)
    {
      durationTicks = i_durationTicks;
    }
    public int getDuration() {return durationTicks;}
    public RenderStage next() {return (this == DONE) ? DONE : RenderStage.values()[this.ordinal() + 1];}
    private int durationTicks;
  }

  @Override
  public void onUpdate()
  {
    super.onUpdate();
    ++timeInThisRenderStage;
    if (timeInThisRenderStage >= renderStage.getDuration()) {
      renderStage = renderStage.next();
      timeInThisRenderStage = 0;
      if (renderStage == RenderStage.STRIKE) {
        if (this.worldObj instanceof WorldClient) {
          WorldClient worldClient = (WorldClient)this.worldObj;
          worldClient.playSound(startPoint.xCoord, startPoint.xCoord, startPoint.xCoord, "ambient.weather.thunder",
                  10000.0F, 0.8F + this.rand.nextFloat() * 0.2F, false);
          float explosionVolume = 1.0F;
          switch (power) {
            case SMALL: {explosionVolume = 1.0F; break;}
            case MEDIUM: {explosionVolume = 2.0F; break;}
            case LARGE: {explosionVolume = 200.0F; break;}
          }

          worldClient.playSound(endPoint.xCoord, endPoint.yCoord, endPoint.zCoord, "random.explode",
                  explosionVolume, 0.5F + this.rand.nextFloat() * 0.2F, false);
        }
      }
      if (renderStage == RenderStage.DONE) {
          this.setDead();
      }
    }
  }

  /** return the relative intensity of the lightning (maximum during strike, and fades rapidly after that)
   * @param partialTick
   * @return multiplier from 0 - 1
   */
  public float getLightIntensity(float partialTick)
  {
    switch (renderStage) {
      case PRESTRIKE:
      case DONE: {
        return 0.0F;
      }
      case STRIKE: {
        return 1.0F;
      }
      case POSTSTRIKE: {
        final int AFTERGLOW_DURATION = 4;
        if (timeInThisRenderStage + partialTick > AFTERGLOW_DURATION) {
          return 0.0F;
        }
        return MathX.lerp(1.0F, 0.0F, (timeInThisRenderStage + partialTick) / AFTERGLOW_DURATION);
      }
    }
    return 1.0F; // dummy
  }

  private RenderStage renderStage = RenderStage.PRESTRIKE;
  private int timeInThisRenderStage = 0;
}
