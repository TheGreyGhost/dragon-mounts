package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileNether;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by TGG on 26/03/2016.
 */
@SideOnly(Side.CLIENT)
public class BreathEntityRendererEnder extends Render {
  private float scale;

  private ResourceLocation enderBallTextureRL;
  public static final String TEX_BASE = "textures/entities/breathweapon/";

  public BreathEntityRendererEnder(RenderManager i_renderManager, float i_scale) {
    super(i_renderManager);
    this.scale = i_scale;
    enderBallTextureRL = new ResourceLocation(DragonMounts.AID, TEX_BASE + "breath_ender.png");
  }

  public void doRender(EntityBreathProjectileNether entity, double x, double y, double z, float yaw, float partialTicks) {
    GlStateManager.pushMatrix();
    this.bindEntityTexture(entity);
    GlStateManager.translate((float) x, (float) y, (float) z);
    GlStateManager.enableRescaleNormal();
    float f2 = this.scale;
    GlStateManager.scale(f2 / 1.0F, f2 / 1.0F, f2 / 1.0F);
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
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
    worldrenderer.startDrawingQuads();
    worldrenderer.setNormal(0.0F, 1.0F, 0.0F);
    worldrenderer.addVertexWithUV((0.0 - xOffset), (0.0 - yOffset), 0.0D, uMin, vMax);
    worldrenderer.addVertexWithUV((xSize - xOffset), (0.0 - yOffset), 0.0D, uMax, vMax);
    worldrenderer.addVertexWithUV( (xSize - xOffset),  (ySize - yOffset), 0.0D,  uMax, vMin);
    worldrenderer.addVertexWithUV( (0.0 - xOffset),  (ySize - yOffset), 0.0D,  uMin, vMin);
    tessellator.draw();

//    GlStateManager.disableLighting();
//    TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
//    TextureAtlasSprite fireLayer0 = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_0");
//    TextureAtlasSprite fireLayer1 = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_1");
//    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//
//    worldrenderer.startDrawingQuads();
//
//    this.bindTexture(TextureMap.locationBlocksTexture);
//    float fireLayer0MinU = fireLayer0.getMinU();
//    float fireLayer0MinV = fireLayer0.getMinV();
//    float fireLayer0MaxU = fireLayer0.getMaxU();
//    float fireLayer0MaxV = fireLayer0.getMaxV();
//
//    final float Z_NUDGE = -0.01F;
//    yOffset -= ySize / 2;  // draw flame from midpt of ball
//    worldrenderer.addVertexWithUV((0.0 - xOffset), (0.0 - yOffset), Z_NUDGE, fireLayer0MaxU, fireLayer0MaxV);
//    worldrenderer.addVertexWithUV((xSize - xOffset), (0.0 - yOffset), Z_NUDGE, fireLayer0MinU, fireLayer0MaxV);
//    worldrenderer.addVertexWithUV((xSize - xOffset), (ySize - yOffset), Z_NUDGE, fireLayer0MinU, fireLayer0MinV);
//    worldrenderer.addVertexWithUV((0.0 - xOffset),  (ySize - yOffset), Z_NUDGE, fireLayer0MaxU, fireLayer0MinV);
//
//    tessellator.draw();
//
//    GlStateManager.enableLighting();

    GlStateManager.disableRescaleNormal();
    GlStateManager.popMatrix();
    super.doRender(entity, x, y, z, yaw, partialTicks);
  }

  protected ResourceLocation getEntityTexture(Entity entity) {
    return enderBallTextureRL;
  }

  /**
   * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
   * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
   * (Render<T extends Entity>) and this method has signature public void func_76986_a(T entity, double d, double d1,
   * double d2, float f, float f1). But JAD is pre 1.5 so doe
   */
  @Override
  public void doRender(Entity entity, double x, double y, double z, float p_76986_8_, float partialTicks) {
    this.doRender((EntityBreathProjectileEnder)entity, x, y, z, p_76986_8_, partialTicks);
  }

}
