package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileGhost;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class NullEntityRenderer extends Render<EntityBreathProjectileGhost>
{
  public NullEntityRenderer(RenderManager renderManager)
  {
    super(renderManager);
  }

  /**
   * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
   */
  @Override
  protected ResourceLocation getEntityTexture(EntityBreathProjectileGhost entity)
  {
    return null;
  }

  /** render nothing! **
   */
  @Override
  public void doRender(EntityBreathProjectileGhost entity, double x, double y, double z, float yaw, float partialTicks)
  {
    return;
  }
}