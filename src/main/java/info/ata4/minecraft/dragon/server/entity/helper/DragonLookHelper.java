package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.ai.DragonBodyHelper;
import info.ata4.minecraft.dragon.server.util.DataLogger;
import info.ata4.minecraft.dragon.util.reflection.PrivateFields;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * For debugging.  Just copied the base class (too many private fields!)
 * Created by TGG on 22/07/2015.
 */
public class DragonLookHelper extends EntityLookHelper
{

  public DragonLookHelper(EntityTameableDragon i_dragon)
  {
    super(i_dragon);
    entity = i_dragon;
  }

    private EntityLiving entity;
    /** The amount of change that is made each update for an entity facing a direction. */
    private float deltaLookYaw;
    /** The amount of change that is made each update for an entity facing a direction. */
    private float deltaLookPitch;
    /** Whether or not the entity is trying to look at something. */
    private boolean isLooking;
    private double posX;
    private double posY;
    private double posZ;

    /**
     * Sets position to look at using entity
     */
    @Override
    public void setLookPositionWithEntity(Entity entityToLookAt, float p_75651_2_, float p_75651_3_)
    {
      this.posX = entityToLookAt.posX;

      if (entityToLookAt instanceof EntityLivingBase)
      {
        this.posY = entityToLookAt.posY + (double)entityToLookAt.getEyeHeight();
      }
      else
      {
        this.posY = (entityToLookAt.getEntityBoundingBox().minY + entityToLookAt.getEntityBoundingBox().maxY) / 2.0D;
      }

      this.posZ = entityToLookAt.posZ;
      this.deltaLookYaw = p_75651_2_;
      this.deltaLookPitch = p_75651_3_;
      this.isLooking = true;
    }

    /**
     * Sets position to look at
     */
    @Override
    public void setLookPosition(double targetXPos, double targetYpos, double targetZpos, float p_75650_7_, float p_75650_8_)
    {
      this.posX = targetXPos;
      this.posY = targetYpos;
      this.posZ = targetZpos;
      this.deltaLookYaw = p_75650_7_;
      this.deltaLookPitch = p_75650_8_;
      this.isLooking = true;
    }

    /**
     * Updates look
     */
    @Override
    public void onUpdateLook()
    {
      String lookRotationYawHead = "";
      String nonLookRotationYawHead = "";
      String clampRotationYawHead = "";

      this.entity.rotationPitch = 0.0F;

      if (this.isLooking) {
        this.isLooking = false;
        double deltaX = this.posX - this.entity.posX;
        double deltaY = this.posY - (this.entity.posY + (double)this.entity.getEyeHeight());
        double deltaZ = this.posZ - this.entity.posZ;
        double projectionOntoXZ = (double)MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float)(Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float)(-(Math.atan2(deltaY, projectionOntoXZ) * 180.0D / Math.PI));
        this.entity.rotationPitch = this.updateRotation(this.entity.rotationPitch, pitch, this.deltaLookPitch);
        this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, yaw, this.deltaLookYaw);
        lookRotationYawHead = Float.toString(this.entity.rotationYawHead);
      }
      else
      {
        this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, this.entity.renderYawOffset, 10.0F);
        nonLookRotationYawHead = Float.toString(this.entity.rotationYawHead);
      }

      float f2 = MathHelper.wrapAngleTo180_float(this.entity.rotationYawHead - this.entity.renderYawOffset);

      if (!this.entity.getNavigator().noPath())
      {
        if (f2 < -75.0F)
        {
          this.entity.rotationYawHead = this.entity.renderYawOffset - 75.0F;
          clampRotationYawHead = "Y";
        }

        if (f2 > 75.0F)
        {
          this.entity.rotationYawHead = this.entity.renderYawOffset + 75.0F;
          clampRotationYawHead = "Y";
        }
      }
      String logName = ((EntityTameableDragon)entity).isClient() ? "Client-onUpdateLook" : "Server-onUpdateLook";
      String output = lookRotationYawHead+ "," +
              nonLookRotationYawHead + "," +
              Float.toString(f2) + "," +
              clampRotationYawHead + "," +
              Float.toString(((EntityTameableDragon) entity).rotationYawHead);
      DataLogger.logData(logName, output);
    }

  private float updateRotation(float p_75652_1_, float p_75652_2_, float p_75652_3_)
  {
    float f3 = MathHelper.wrapAngleTo180_float(p_75652_2_ - p_75652_1_);

    if (f3 > p_75652_3_)
    {
      f3 = p_75652_3_;
    }

    if (f3 < -p_75652_3_)
    {
      f3 = -p_75652_3_;
    }

    return p_75652_1_ + f3;
  }

  @Override
    public boolean func_180424_b()
    {
      return this.isLooking;
    }

  @Override
    public double func_180423_e()
    {
      return this.posX;
    }

  @Override
    public double func_180422_f()
    {
      return this.posY;
    }

  @Override
    public double func_180421_g()
    {
      return this.posZ;
    }

}
