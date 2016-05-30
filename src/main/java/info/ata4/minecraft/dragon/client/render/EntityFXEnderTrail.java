package info.ata4.minecraft.dragon.client.render;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Based on the Portal FX, makes it appear more quickly (instantly)
 */

public class EntityFXEnderTrail extends EntityFX
{
  private float portalParticleScale;
  private double portalPosX;
  private double portalPosY;
  private double portalPosZ;

  public EntityFXEnderTrail(World world, double x, double y, double z,
                            double velocityX, double velocityY, double velocityZ)
  {
    super(world, x, y, z, velocityX, velocityY, velocityZ);

    //the vanilla EntityFX constructor added random variation to our starting velocity.  Undo it!
    this.motionX = velocityX;
    this.motionY = velocityY;
    this.motionZ = velocityZ;
    this.portalPosX = this.posX = x;
    this.portalPosY = this.posY = y;
    this.portalPosZ = this.posZ = z;
    float blueValue = this.rand.nextFloat() * 0.6F + 0.4F;
    this.portalParticleScale = this.particleScale = this.rand.nextFloat() * 0.2F + 0.5F;
    this.particleRed = this.particleGreen = this.particleBlue = 1.0F * blueValue;
    this.particleGreen *= 0.3F;
    this.particleRed *= 0.9F;
    this.particleMaxAge = (int)(Math.random() * 10.0D) + 40;
    this.noClip = true;
    this.setParticleTextureIndex((int)(Math.random() * 8.0D));
  }

  /**
   * Render the EntityFX onto the screen.  For more help with the tessellator see
   * http://greyminecraftcoder.blogspot.co.at/2014/12/the-tessellator-and-worldrenderer-18.html
   * <p/>
   * You don't actually need to override this method, this is just a deobfuscated example of the vanilla, to show you
   * how it works in case you want to do something a bit unusual.
   * <p/>
   * The EntityFX is rendered as a two-dimensional object (Quad) in the world (three-dimensional coordinates).
   * The corners of the quad are chosen so that the EntityFX is drawn directly facing the viewer (or in other words,
   * so that the quad is always directly face-on to the screen.)
   * In order to manage this, it needs to know two direction vectors:
   * 1) the 3D vector direction corresponding to left-right on the viewer's screen (edgeLRdirection)
   * 2) the 3D vector direction corresponding to up-down on the viewer's screen (edgeUDdirection)
   * These two vectors are calculated by the caller.
   * For example, the top right corner of the quad on the viewer's screen is equal to:
   * the centre point of the quad (x,y,z)
   * plus the edgeLRdirection vector multiplied by half the quad's width
   * plus the edgeUDdirection vector multiplied by half the quad's height.
   * NB edgeLRdirectionY is not provided because it's always 0, i.e. the top of the viewer's screen is always directly
   * up, so moving left-right on the viewer's screen doesn't affect the y coordinate position in the world
   *
   * @param worldRenderer
   * @param entity
   * @param partialTick
   * @param edgeLRdirectionX edgeLRdirection[XYZ] is the vector direction pointing left-right on the player's screen
   * @param edgeUDdirectionY edgeUDdirection[XYZ] is the vector direction pointing up-down on the player's screen
   * @param edgeLRdirectionZ edgeLRdirection[XYZ] is the vector direction pointing left-right on the player's screen
   * @param edgeUDdirectionX edgeUDdirection[XYZ] is the vector direction pointing up-down on the player's screen
   * @param edgeUDdirectionZ edgeUDdirection[XYZ] is the vector direction pointing up-down on the player's screen
   */
  @Override
  public void func_180434_a(WorldRenderer worldRenderer, Entity entity, float partialTick,
                            float edgeLRdirectionX, float edgeUDdirectionY, float edgeLRdirectionZ,
                            float edgeUDdirectionX, float edgeUDdirectionZ)
  {
    float fractionalAge = acceleratedAgeFraction(partialTick);

    fractionalAge = 1.0F - fractionalAge;
    fractionalAge *= fractionalAge;
    fractionalAge = 1.0F - fractionalAge;
    this.particleScale = this.portalParticleScale * fractionalAge;
    super.func_180434_a(worldRenderer, entity, partialTick, edgeLRdirectionX, edgeUDdirectionY, edgeLRdirectionZ,
            edgeUDdirectionX, edgeUDdirectionZ);
  }

  public int getBrightnessForRender(float partialTick)
  {
    int i = super.getBrightnessForRender(partialTick);
    float fractionalAge = acceleratedAgeFraction(partialTick);

    fractionalAge *= fractionalAge;
    fractionalAge *= fractionalAge;
    int j = i & 255;
    int k = i >> 16 & 255;
    k += (int)(fractionalAge * 15.0F * 16.0F);

    if (k > 240) {
      k = 240;
    }

    return j | k << 16;
  }

  /**
   * Gets how bright this entity is.
   */
  public float getBrightness(float partialTick)
  {
    float f1 = super.getBrightness(partialTick);
    float fractionalAge = acceleratedAgeFraction(partialTick);
    fractionalAge = fractionalAge * fractionalAge * fractionalAge * fractionalAge;
    return f1 * (1.0F - fractionalAge) + fractionalAge;
  }

  /**
   * Called to update the entity's position/logic.
   */
  public void onUpdate()
  {
    this.prevPosX = this.posX;
    this.prevPosY = this.posY;
    this.prevPosZ = this.posZ;
    float fractionalAge = acceleratedAgeFraction(0);
    float fractionalAge2 = fractionalAge;
    fractionalAge = -fractionalAge + fractionalAge * fractionalAge * 2.0F;
    fractionalAge = 1.0F - fractionalAge;
    this.posX = this.portalPosX + this.motionX * fractionalAge;
    this.posY = this.portalPosY + this.motionY * fractionalAge + (1.0F - fractionalAge2);
    this.posZ = this.portalPosZ + this.motionZ * fractionalAge;

    if (this.particleAge++ >= this.particleMaxAge) {
      this.setDead();
    }
  }

  /**
   * Rescale the age to make a much more rapid expansion than the vanilla portal
   * @param partialTick
   * @return
   */
  private float acceleratedAgeFraction(float partialTick)
  {
    float fractionalAge = ((float)this.particleAge + partialTick) / (float)this.particleMaxAge;
    fractionalAge = 0.5F + 0.5F * fractionalAge;
    return fractionalAge;
  }

}