package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathGhost;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileGhost;
import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class NullEntityRenderer extends Render
{
  public NullEntityRenderer(RenderManager renderManager)
  {
    super(renderManager);
  }

  /**
   * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
   */
  protected ResourceLocation getEntityTexture(Entity entity)
  {
    return null;
  }

  /** render nothing! **
   */
  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks)
  {
    return;
  }
}