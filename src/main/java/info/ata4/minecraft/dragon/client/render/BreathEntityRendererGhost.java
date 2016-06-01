package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileGhost;
import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class BreathEntityRendererGhost extends Render
{
  public BreathEntityRendererGhost(RenderManager renderManager)
  {
    super(renderManager);
  }

  public void doRender(EntityBreathProjectileGhost entity, double x, double y, double z, float yaw, float partialTicks)
  {
    // render the lightning from the origin (mouth of the dragon) to the current location
    // Based on vanilla lightning render:
    // a total of three strands per strike.  One reaches all the way from top to bottom.  The other two start part way
    //   down from the top, and finish above the ground
    // lightning is made of four 'shells' of increasing size, to make the core of the lighting bright (most opaque) and
    //  the outer part pale (translucent)
    // The unscaled lightning is 1.0 high and follows a 'random walk' in x and z, deviating by up to
    // 1 in 3 for the main strand; or 1 in 1 for the other strands
    //  for vanilla the walk starts from 0,0,0 and deviates upwards
    //  For the breath weapon, we force to 0,0,0 at both origin and target.

    double beamLength = 10.0;// target minus head


    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    GlStateManager.disableTexture2D();
    GlStateManager.disableLighting();
    GlStateManager.enableBlend();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
    final int SEGMENT_COUNT = 10;
    final int MAX_SEGMENT = SEGMENT_COUNT - 1;
    float[] segmentXmin = new float[SEGMENT_COUNT+1];
    float[] segmentZmin = new float[SEGMENT_COUNT+1];
    float[] segmentXmax = new float[SEGMENT_COUNT+1];
    float[] segmentZmax = new float[SEGMENT_COUNT+1];

    final long SEED = 2362;
    Random random = new Random();

    final float SEGMENT_HEIGHT = 1.0F / SEGMENT_COUNT;
    final float MAX_DEVIATION_PER_SEGMENT_MAIN_STRAND = SEGMENT_HEIGHT / 3.0F;
    final float MAX_DEVIATION_PER_SEGMENT_OTHER_STRANDS = SEGMENT_HEIGHT;

    final float BEAM_WIDTH_BLOCKS = 0.2F;
    final float BEAM_WIDTH_SCALED = (float)(BEAM_WIDTH_BLOCKS / beamLength);

    float xSum = 0.0F;
    float zSum = 0.0F;
    for (int i = 0; i < SEGMENT_COUNT; ++i) {
      segmentXmin[i] = xSum;
      segmentZmin[i] = zSum;
      xSum += MathX.getRandomInRange(random, -MAX_DEVIATION_PER_SEGMENT_MAIN_STRAND, MAX_DEVIATION_PER_SEGMENT_MAIN_STRAND);
      zSum += MathX.getRandomInRange(random, -MAX_DEVIATION_PER_SEGMENT_MAIN_STRAND, MAX_DEVIATION_PER_SEGMENT_MAIN_STRAND);
      segmentXmax[i] = xSum;
      segmentZmax[i] = zSum;
    }

    // force end point to zero by subtracting the straight line between first segment and last segment
    for (int i = 0; i < SEGMENT_COUNT; ++i) {
      segmentXmin[i] -= xSum * i / (float)SEGMENT_COUNT;
      segmentZmin[i] -= zSum * i / (float)SEGMENT_COUNT;
      segmentXmax[i] -= xSum * (i+1) / (float)SEGMENT_COUNT;
      segmentZmax[i] -= zSum * (i+1) / (float)SEGMENT_COUNT;
    }

    final int MAX_NUMBER_OF_STRANDS = 6;
    int numberOfStrands = random.nextInt(MAX_NUMBER_OF_STRANDS) + 1;

    // lightning is made of four 'shells' of increasing size, to make the core of the lighting bright (most opaque) and
    //  the outer part pale (translucent)

    for (int shell = 0; shell < 4; ++shell) {
      Random random1 = new Random(SEED);

      // multiple strands per strike.  One reaches all the way from top to bottom.  The others start part way
      //   down from the top, and finish above the ground.
      // In this case "top" is the dragon head and "bottom" is the target point

      for (int strandNumber = 0; strandNumber < numberOfStrands; ++strandNumber) {
        int uppermostYSegment = MAX_SEGMENT;
        int lowermostYsegment = 0;

        // for non-main strands, choose a random starting segment and ending segment
        if (strandNumber > 0) {
          final int SEGMENTS_FROM_HEAD = 1;  // how close to the mouth can we branch out?
          final int SEGMENTS_FROM_TARGET = 1; // how close to the target could a branch finish?
          uppermostYSegment = MathX.getRandomInRange(random1, SEGMENTS_FROM_TARGET,
                  MAX_SEGMENT - SEGMENTS_FROM_HEAD);
          lowermostYsegment = MathX.getRandomInRange(random1, SEGMENTS_FROM_TARGET, uppermostYSegment);
        }

        float segmentX = segmentXmax[uppermostYSegment];
        float segmentZ = segmentZmax[uppermostYSegment];

        for (int ySegment = uppermostYSegment; ySegment >= lowermostYsegment; --ySegment) {
          float segmentXtop = segmentX;
          float segmentZtop = segmentZ;

          if (strandNumber == 0) {
            segmentX = segmentXmin[ySegment];
            segmentZ = segmentZmin[ySegment];
          } else {
            segmentX += MathX.getRandomInRange(random, -MAX_DEVIATION_PER_SEGMENT_OTHER_STRANDS, MAX_DEVIATION_PER_SEGMENT_OTHER_STRANDS);
            segmentZ += MathX.getRandomInRange(random, -MAX_DEVIATION_PER_SEGMENT_OTHER_STRANDS, MAX_DEVIATION_PER_SEGMENT_OTHER_STRANDS);
          }

          worldrenderer.startDrawing(GL11.GL_TRIANGLE_STRIP);  // http://www.glprogramming.com/red/chapter02.html
          float baseBrightness = 0.5F;
          worldrenderer.setColorRGBA_F(0.9F * baseBrightness, 0.9F * baseBrightness, 1.0F * baseBrightness, 0.3F);
          float topWidth = BEAM_WIDTH_SCALED * (0.1F + shell * 0.2F);
          float bottomWidth = BEAM_WIDTH_SCALED * (0.1F + shell * 0.2F);

          if (strandNumber == 0) {  //todo consider scaling to be fattest in the middle and normal at the ends; also normalise for seg count!
            topWidth *= (ySegment + 1) * 0.1F + 1.0F;
            bottomWidth *= ySegment * 0.1F + 1.0F;
          }

          // draws a vertical square tube, sides only, centred around [x,,z], over the given 16-block-high segment
          for (int vertex = 0; vertex < 5; ++vertex) {
            float xTop = - topWidth;
            float zTop = - topWidth;

            if (vertex == 1 || vertex == 2) {
              xTop += topWidth * 2.0F;
            }

            if (vertex == 2 || vertex == 3) {
              zTop += topWidth * 2.0F;
            }

            float xBottom = bottomWidth;
            float zBottom = bottomWidth;

            if (vertex == 1 || vertex == 2) {
              xBottom += bottomWidth * 2.0F;
            }

            if (vertex == 2 || vertex == 3) {
              zBottom += bottomWidth * 2.0F;
            }

            worldrenderer.addVertex(xBottom + segmentX, SEGMENT_HEIGHT * ySegment, zBottom + segmentZ);
            worldrenderer.addVertex(xTop + segmentXtop, SEGMENT_HEIGHT * (ySegment + 1), zTop + segmentZtop);
          }

          tessellator.draw();
//          System.out.format("[%f, %d, %f] %f to [%f, %d, %f] %f\n", deltaX, 16 * ySegment, deltaZ, bottomWidth,
//                  deltaXInitial, 16 * (ySegment + 1), deltaZInitial, topWidth);
          System.out.format("%f, %f, %f, %f, %f, %f, %f, %f\n", segmentX, SEGMENT_HEIGHT * ySegment, segmentZ, bottomWidth,
                  segmentXtop, SEGMENT_HEIGHT * (ySegment + 1), segmentZtop, topWidth);

        }
      }
    }

    GlStateManager.disableBlend();
    GlStateManager.enableLighting();
    GlStateManager.enableTexture2D();
  }

  public void doRenderVanilla(EntityBreathProjectileGhost entity, double x, double y, double z, float yaw, float partialTicks)
  {
    // render the lightning from the origin (mouth of the dragon) to the current location
    // Based on vanilla lightning render:
    // a total of three strands per strike.  One reaches all the way from top to bottom.  The other two start part way
    //   down from the top, and finish above the ground
    // lightning is made of four 'shells' of increasing size, to make the core of the lighting bright (most opaque) and
    //  the outer part pale (translucent)
    // The lightning is 128 blocks high and follows a 'random walk' in x and z, deviating by up to +/- 5 blocks per
    //  16 y-blocks for the main strand; or three times that for the other two strands
    //  for vanilla the walk starts from 0,0,0 and deviates upwards
    //  For the breath weapon, we force to 0,0,0 at both origin and target.

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    GlStateManager.disableTexture2D();
    GlStateManager.disableLighting();
    GlStateManager.enableBlend();
    GlStateManager.blendFunc(770, 1);
    double[] xValues = new double[8];
    double[] zValues = new double[8];
    double xSum = 0.0D;
    double zSum = 0.0D;

    final long SEED = 2362;
    Random random = new Random(SEED);

    for (int i = 7; i >= 0; --i) {
      xValues[i] = xSum;
      zValues[i] = zSum;
      xSum += (double) (random.nextInt(11) - 5);
      zSum += (double) (random.nextInt(11) - 5);
    }

    // lightning is made of four 'shells' of increasing size, to make the core of the lighting bright (most opaque) and
    //  the outer part pale (translucent)
    for (int shell = 0; shell < 4; ++shell) {
      Random random1 = new Random(SEED);

      // a total of three strands per strike.  One reaches all the way from top to bottom.  The other two start part way
      //   down from the top, and finish above the ground
      for (int strandNumber = 0; strandNumber < 3; ++strandNumber) {
        int uppermostYSegment = 7;
        int lowermostYsegment = 0;

        if (strandNumber > 0) {
          uppermostYSegment = 7 - strandNumber;
        }

        if (strandNumber > 0) {
          lowermostYsegment = uppermostYSegment - 2;
        }

        double deltaX = xValues[uppermostYSegment] - xSum;
        double deltaZ = zValues[uppermostYSegment] - zSum;

        for (int ySegment = uppermostYSegment; ySegment >= lowermostYsegment; --ySegment) {
          double deltaXInitial = deltaX;
          double deltaZInitial = deltaZ;

          if (strandNumber == 0) {
            deltaX += (double) (random1.nextInt(11) - 5);
            deltaZ += (double) (random1.nextInt(11) - 5);
          } else {
            deltaX += (double) (random1.nextInt(31) - 15);
            deltaZ += (double) (random1.nextInt(31) - 15);
          }

          worldrenderer.startDrawing(GL11.GL_TRIANGLE_STRIP);  // http://www.glprogramming.com/red/chapter02.html
          float baseBrightness = 0.5F;
          worldrenderer.setColorRGBA_F(0.9F * baseBrightness, 0.9F * baseBrightness, 1.0F * baseBrightness, 0.3F);
          double topWidth = 0.1D + (double) shell * 0.2D;

          if (strandNumber == 0) {
            topWidth *= (double) ySegment * 0.1D + 1.0D;
          }

          double bottomWidth = 0.1D + (double) shell * 0.2D;

          if (strandNumber == 0) {
            bottomWidth *= (double) (ySegment - 1) * 0.1D + 1.0D;
          }

          // draws a vertical square tube, sides only, centred around [x,,z], over the given 16-block-high segment
          for (int vertex = 0; vertex < 5; ++vertex) {
            double xTop = x + 0.5D - topWidth;
            double zTop = z + 0.5D - topWidth;

            if (vertex == 1 || vertex == 2) {
              xTop += topWidth * 2.0D;
            }

            if (vertex == 2 || vertex == 3) {
              zTop += topWidth * 2.0D;
            }

            double xBottom = x + 0.5D - bottomWidth;
            double zBottom = z + 0.5D - bottomWidth;

            if (vertex == 1 || vertex == 2) {
              xBottom += bottomWidth * 2.0D;
            }

            if (vertex == 2 || vertex == 3) {
              zBottom += bottomWidth * 2.0D;
            }

            worldrenderer.addVertex(xBottom + deltaX, y + 16 * ySegment, zBottom + deltaZ);
            worldrenderer.addVertex(xTop + deltaXInitial, y + 16 * (ySegment + 1), zTop + deltaZInitial);
          }

          tessellator.draw();
//          System.out.format("[%f, %d, %f] %f to [%f, %d, %f] %f\n", deltaX, 16 * ySegment, deltaZ, bottomWidth,
//                  deltaXInitial, 16 * (ySegment + 1), deltaZInitial, topWidth);
          System.out.format("%f, %d, %f, %f, %f, %d, %f, %f\n", deltaX, 16 * ySegment, deltaZ, bottomWidth,
                  deltaXInitial, 16 * (ySegment + 1), deltaZInitial, topWidth);

        }
      }
    }

    GlStateManager.disableBlend();
    GlStateManager.enableLighting();
    GlStateManager.enableTexture2D();
  }


  /**
   * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
   */
  protected ResourceLocation getEntityTexture(EntityBreathProjectileGhost entity)
  {
    return null;
  }

  /**
   * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
   */
  protected ResourceLocation getEntityTexture(Entity entity)
  {
    return this.getEntityTexture((EntityBreathProjectileGhost) entity);
  }

  /**
   * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
   * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
   * (Render<T extends Entity>) and this method has signature public void func_76986_a(T entity, double d, double d1,
   * double d2, float f, float f1). But JAD is pre 1.5 so doe
   */
  public void doRender(Entity entity, double x, double y, double z, float p_76986_8_, float partialTicks)
  {
    this.doRender((EntityBreathProjectileGhost) entity, x, y, z, p_76986_8_, partialTicks);
  }
}