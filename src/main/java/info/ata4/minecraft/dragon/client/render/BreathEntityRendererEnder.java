package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileEnder;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileNether;
import info.ata4.minecraft.dragon.util.math.MathX;
import info.ata4.minecraft.dragon.util.math.RotatingQuad;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created by TGG on 26/03/2016.
 */
@SideOnly(Side.CLIENT)
public class BreathEntityRendererEnder extends Render<EntityBreathProjectileEnder> {
  private float scale;

  private ResourceLocation enderGlobeTextureRL;
  private ResourceLocation enderAuraTextureRL;
  public static final String TEX_BASE = "textures/entities/breathweapon/";

  public BreathEntityRendererEnder(RenderManager i_renderManager, float i_scale) {
    super(i_renderManager);
    this.scale = i_scale;
    enderGlobeTextureRL = new ResourceLocation(DragonMounts.AID, TEX_BASE + "breath_ender.png");
    enderAuraTextureRL = new ResourceLocation(DragonMounts.AID, TEX_BASE + "breath_ender_halo.png");
  }

  @Override
  public void doRender(EntityBreathProjectileEnder entity, double x, double y, double z, float yaw, float partialTicks) {

    // from RenderDragonFireball
//    GlStateManager.pushMatrix();
//    this.bindEntityTexture(entity);
//    GlStateManager.translate((float)x, (float)y, (float)z);
//    GlStateManager.enableRescaleNormal();
//    GlStateManager.scale(2.0F, 2.0F, 2.0F);
//    Tessellator tessellator = Tessellator.getInstance();
//    VertexBuffer vertexbuffer = tessellator.getBuffer();
//    float f = 1.0F;
//    float f1 = 0.5F;
//    float f2 = 0.25F;
//    GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
//    GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
//
//    if (this.renderOutlines)
//    {
//      GlStateManager.enableColorMaterial();
//      GlStateManager.enableOutlineMode(this.getTeamColor(entity));
//    }
//
//    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
//    vertexbuffer.pos(-0.5D, -0.25D, 0.0D).tex(0.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
//    vertexbuffer.pos(0.5D, -0.25D, 0.0D).tex(1.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
//    vertexbuffer.pos(0.5D, 0.75D, 0.0D).tex(1.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
//    vertexbuffer.pos(-0.5D, 0.75D, 0.0D).tex(0.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
//    tessellator.draw();
//
//    if (this.renderOutlines)
//    {
//      GlStateManager.disableOutlineMode();
//      GlStateManager.disableColorMaterial();
//    }
//
//    GlStateManager.disableRescaleNormal();
//    GlStateManager.popMatrix();
//    super.doRender(entity, x, y, z, entityYaw, partialTicks);


    try {
      GlStateManager.pushMatrix();
      this.bindEntityTexture(entity);
      GlStateManager.translate((float) x, (float) y, (float) z);
      GlStateManager.enableRescaleNormal();
      float f2 = this.scale;
      GlStateManager.scale(f2 / 1.0F, f2 / 1.0F, f2 / 1.0F);
      Tessellator tessellator = Tessellator.getInstance();
      VertexBuffer worldrenderer = tessellator.getBuffer();
      double uMin = 0.0F;
      double uMax = 1.0F;
      double vMin = 0.0F;
      double vMax = 1.0F;
      double xSize = entity.width;
      double ySize = entity.height;
      double xOffset = xSize / 2;
      double yOffset = ySize / 2;
      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      long timeMS = System.nanoTime() / 1000 / 1000;
      final long ROTATION_PERIOD_MS = 500;
      float rotationFraction = (timeMS % ROTATION_PERIOD_MS) / (float)ROTATION_PERIOD_MS;
      GlStateManager.rotate(rotationFraction * 360, 0, 0, 1.0F);

      worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
      worldrenderer.pos((0.0 - xOffset), (0.0 - yOffset), 0.0D).tex(uMin, vMax).normal(0.0F, 1.0F, 0.0F).endVertex();
      worldrenderer.pos((xSize - xOffset), (0.0 - yOffset), 0.0D).tex(uMax, vMax).normal(0.0F, 1.0F, 0.0F).endVertex();
      worldrenderer.pos((xSize - xOffset), (ySize - yOffset), 0.0D).tex(uMax, vMin).normal(0.0F, 1.0F, 0.0F).endVertex();
      worldrenderer.pos((0.0 - xOffset), (ySize - yOffset), 0.0D).tex(uMin, vMin).normal(0.0F, 1.0F, 0.0F).endVertex();
      tessellator.draw();
    } finally {
      GlStateManager.disableRescaleNormal();
      GlStateManager.popMatrix();
    }
    super.doRender(entity, x, y, z, yaw, partialTicks);

//    {
//      GlStateManager.disableLighting();
//      this.bindTexture(enderAuraTextureRL);
//
//      final float Z_NUDGE = -0.01F;
//
//      double uMin = 0.0F;
//      double uMax = 1.0F;
//      double vMin = 0.0F;
//      double vMax = 1.0F;
//      final double EXPAND_FACTOR = 1.2;  // make the aura project slightly beyond the globe itself
//      double xSize = EXPAND_FACTOR * entity.width;
//      double ySize = EXPAND_FACTOR * entity.height;
//      double xOffset = xSize / 2;
//      double yOffset = ySize / 2;
////      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
////      GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
//      GlStateManager.enableBlend();
//      GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//      GlStateManager.alphaFunc(GL11.GL_GREATER, 1/255.0F);
//
//      worldrenderer.startDrawingQuads();
//      worldrenderer.setNormal(0.0F, 1.0F, 0.0F);
//
//      RotatingQuad tex = new RotatingQuad(uMin, vMin, uMax, vMax);
////      Random random = new Random();
////      if (random.nextBoolean()) {
////        tex.mirrorLR();
////      }
////      tex.rotate90(random.nextInt(4));
//      final long GLOW_CYCLE_MILLISECONDS = 500;
//      long timeMS = System.nanoTime() / 1000 / 1000;
//      timeMS %= GLOW_CYCLE_MILLISECONDS;
//      final int min_brightness = 2;
//      final int max_brightness = 15;
//
//      float aveBrightness = (max_brightness + min_brightness)/2.0F;
//      float amplitudeBrightness = (max_brightness - min_brightness)/2.0F;
//      float skyBrightness =  aveBrightness + amplitudeBrightness * MathX.sin(timeMS * 2 * (float)Math.PI / (float)GLOW_CYCLE_MILLISECONDS);
//      worldrenderer.setBrightness(((int)skyBrightness) << 20);
//
//      worldrenderer.addVertexWithUV((0.0 - xOffset), (0.0 - yOffset), Z_NUDGE, tex.getU(3),  tex.getV(3));
//      worldrenderer.addVertexWithUV((xSize - xOffset), (0.0 - yOffset), Z_NUDGE, tex.getU(0),  tex.getV(0));
//      worldrenderer.addVertexWithUV((xSize - xOffset), (ySize - yOffset), Z_NUDGE, tex.getU(1),  tex.getV(1));
//      worldrenderer.addVertexWithUV((0.0 - xOffset), (ySize - yOffset), Z_NUDGE, tex.getU(2),  tex.getV(2));
//      tessellator.draw();
//
//      GlStateManager.enableLighting();
//    }

  }

  @Override
  protected ResourceLocation getEntityTexture(EntityBreathProjectileEnder entity) {
    return enderGlobeTextureRL;
  }

  static public class BEREnderFactory implements IRenderFactory<EntityBreathProjectileEnder>
  {
    public BEREnderFactory(float i_scale)
    {
      scale = i_scale;
    }

    public Render<EntityBreathProjectileEnder> createRenderFor(RenderManager manager)
    {
      return new BreathEntityRendererEnder(manager, scale);
    }
    final private float scale;
  }


}
