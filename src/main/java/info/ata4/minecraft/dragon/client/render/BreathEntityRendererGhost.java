package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathGhost;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileGhost;
import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class BreathEntityRendererGhost extends Render<EntityBreathGhost>
{
  public BreathEntityRendererGhost(RenderManager renderManager)
  {
    super(renderManager);
  }

  @Override
  public void doRender(EntityBreathGhost entity, double x, double y, double z, float yaw, float partialTicks)
  {
    // render the lightning from the origin (mouth of the dragon) to the current location
    // Based on vanilla lightning render:
    // multiple strands per strike.  One reaches all the way from top to bottom.  The others start part way
    //   down from the top, and finish above the ground
    // lightning is made of four 'shells' of increasing size, to make the core of the lighting bright (most opaque) and
    //  the outer part pale (translucent)
    // The unscaled lightning is 1.0 high (y = 0.0 at the dragon mouth, y = 1.0 at the target point)
    //     and follows a 'random walk' in x and z, deviating by up to:
    //     1 in 3 for the main strand; or 1 in 1 for the other strands
    //  for vanilla the walk starts from 0,0,0 (ground level hits a particular point) and deviates to a different
    //    x and z at the top.
    //  For the breath weapon, we force to 0,y,0 at both mouth and target.

    // during rendering the y axis of the lightning (from y = 0 at mouth to y = 1 at target) is mapped onto the vector between the
    //   the dragon's mouth and the target point.
    // This is accomplished by:
    // 1) scale to the correct length
    // 2) rotate to the correct angle (rotate along the shortest path, i.e. around the vector formed from the
    //      cross product of the lightning [0, 1, 0] and the target->mouth vector
    // 3) translate the origin to the target point
    // OpenGL transforms are used to achieve this

    double beamLength = getLightningLength(entity);// target minus mouth

    try {
      GL11.glPushMatrix();
      GlStateManager.translate((float) x, (float) y, (float) z);

      applyTransformation(entity);

      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

      // change the "multitexturing" lighting value (default value is the brightness of the last block rendered)
      // - this will make the lightning "glow" brighter than the surroundings if it is dark.
      final int SKY_LIGHT_VALUE = (int)(15 * entity.getLightIntensity(partialTicks));
      final int BLOCK_LIGHT_VALUE = 0;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, SKY_LIGHT_VALUE * 16.0F, BLOCK_LIGHT_VALUE * 16.0F);

      final float MIN_LENGTH_PER_SEGMENT = 2;  // approx minimum length of each segment (distance between "kinks")
      final int MAX_SEGMENTS_PERMITTED = 10;
      final int SEGMENT_COUNT = (beamLength / MAX_SEGMENTS_PERMITTED > MIN_LENGTH_PER_SEGMENT) ?
                                MAX_SEGMENTS_PERMITTED : (int)(beamLength / MIN_LENGTH_PER_SEGMENT) + 1;
      final int MAX_SEGMENT_IDX = SEGMENT_COUNT - 1;
      float[] mainStrandSegmentXlower = new float[SEGMENT_COUNT + 1];
      float[] mainStrandSegmentZlower = new float[SEGMENT_COUNT + 1];
      float[] mainStrandSegmentXupper = new float[SEGMENT_COUNT + 1];
      float[] mainStrandSegmentZupper = new float[SEGMENT_COUNT + 1];

      Random random = new Random(entity.getRandomSeed());
      final long STRAND_SHAPE_SEED = random.nextLong();

      final float LIGHTNING_HEIGHT = 1.0F;
      final float SEGMENT_HEIGHT = LIGHTNING_HEIGHT / SEGMENT_COUNT;
      final float MAX_DEVIATION_PER_SEGMENT_MAIN_STRAND = SEGMENT_HEIGHT / 3.0F;
      final float MAX_DEVIATION_PER_SEGMENT_OTHER_STRANDS = SEGMENT_HEIGHT;

      BreathNode.Power power = entity.getPower();
      final float BEAM_WIDTH_BLOCKS;
      switch (power) {
        case SMALL: {
          BEAM_WIDTH_BLOCKS = 0.06F;
          break;
        }
        case MEDIUM: {
          BEAM_WIDTH_BLOCKS = 0.12F;
          break;
        }
        case LARGE: {
          BEAM_WIDTH_BLOCKS = 0.2F;
          break;
        }
        default: {
          BEAM_WIDTH_BLOCKS = 0.2F;
          System.err.println("Unexpected power: "+ power);
        }
      }

      final float BEAM_WIDTH_SCALED = (float) (BEAM_WIDTH_BLOCKS / beamLength);

      float xSum = 0.0F;
      float zSum = 0.0F;
      for (int i = 0; i < SEGMENT_COUNT; ++i) {
        mainStrandSegmentXlower[i] = xSum;
        mainStrandSegmentZlower[i] = zSum;
        xSum += MathX.getRandomInRange(random, -MAX_DEVIATION_PER_SEGMENT_MAIN_STRAND, MAX_DEVIATION_PER_SEGMENT_MAIN_STRAND);
        zSum += MathX.getRandomInRange(random, -MAX_DEVIATION_PER_SEGMENT_MAIN_STRAND, MAX_DEVIATION_PER_SEGMENT_MAIN_STRAND);
        mainStrandSegmentXupper[i] = xSum;
        mainStrandSegmentZupper[i] = zSum;
      }

      // force end point to zero by subtracting the straight line between first segment and last segment
      for (int i = 0; i < SEGMENT_COUNT; ++i) {
        mainStrandSegmentXlower[i] -= xSum * i / (float) SEGMENT_COUNT;
        mainStrandSegmentZlower[i] -= zSum * i / (float) SEGMENT_COUNT;
        mainStrandSegmentXupper[i] -= xSum * (i + 1) / (float) SEGMENT_COUNT;
        mainStrandSegmentZupper[i] -= zSum * (i + 1) / (float) SEGMENT_COUNT;
      }

      final int MIN_NUMBER_OF_STRANDS = Math.min(3, SEGMENT_COUNT - 3);
      final int MAX_NUMBER_OF_STRANDS = Math.min(9, SEGMENT_COUNT - 1);
      int numberOfStrands = MathX.getRandomInRange(random, MIN_NUMBER_OF_STRANDS, MAX_NUMBER_OF_STRANDS);

//
//      worldrenderer.startDrawing(GL11.GL_QUADS);  // http://www.glprogramming.com/red/chapter02.html
//      float baseBrightness = 0.5F;
//      worldrenderer.setColorRGBA_F(0.9F * baseBrightness, 0.9F * baseBrightness, 1.0F * baseBrightness, 0.3F);
//
//      worldrenderer.addVertex(-0.2, 0.0, 0.0);
//      worldrenderer.addVertex( 0.2, 0.0, 0.0);
//      worldrenderer.addVertex( 0.2, 1.0, 0.0);
//      worldrenderer.addVertex(-0.2, 1.0, 0.0);
//      tessellator.draw();

      // lightning is made of four 'shells' of increasing size, to make the core of the lighting bright (most opaque) and
      //  the outer part pale (translucent)

      final float NUMBER_OF_SHELLS = 4;
      final float CORE_HALF_WIDTH = BEAM_WIDTH_SCALED / NUMBER_OF_SHELLS;
      final float HALF_WIDTH_PER_SHELL = CORE_HALF_WIDTH;
      final float ORIGIN_WIDTH_INCREASE_FACTOR = 2.0F;  // how much wider is the origin than the target (2 = twice as wide)
      final float SECONDARY_STRAND_RELATIVE_MIN_WIDTH = 0.3F;  // width of secondary strand relative to primary
      final float SECONDARY_STRAND_RELATIVE_MAX_WIDTH = 0.8F;  // width of secondary strand relative to primary

      for (int shell = 0; shell < NUMBER_OF_SHELLS; ++shell) {
        Random random1 = new Random(STRAND_SHAPE_SEED);

        // multiple strands per strike.  One (primary strand) reaches all the way from mouth to target.  The others
        //   (secondary strands) start part way along from the mouth, and finish before the target.

        for (int strandNumber = 0; strandNumber < numberOfStrands; ++strandNumber) {
          int uppermostYSegment = MAX_SEGMENT_IDX;
          int lowermostYsegment = 0;

          // for non-main strands, choose a random starting segment and ending segment
          if (strandNumber > 0) {
            final int SEGMENTS_FROM_MOUTH = 1;  // how close to the mouth can we branch out?
            final int SEGMENTS_FROM_TARGET = 1; // how close to the target could a branch finish?
            int HIGHEST_SEGMENT_POSSIBLE = Math.max(MAX_SEGMENT_IDX - SEGMENTS_FROM_TARGET, SEGMENTS_FROM_MOUTH);
            uppermostYSegment = MathX.getRandomInRange(random1, SEGMENTS_FROM_MOUTH,
                    HIGHEST_SEGMENT_POSSIBLE);
            lowermostYsegment = MathX.getRandomInRange(random1, SEGMENTS_FROM_MOUTH, uppermostYSegment);
          }

          float strandRelativeWidth = MathX.lerp(SECONDARY_STRAND_RELATIVE_MIN_WIDTH,
                  SECONDARY_STRAND_RELATIVE_MAX_WIDTH,
                  random1.nextFloat());

          float segmentX = mainStrandSegmentXlower[lowermostYsegment];
          float segmentZ = mainStrandSegmentZlower[lowermostYsegment];

          for (int ySegment = lowermostYsegment; ySegment <= uppermostYSegment; ++ySegment) {
            float segmentXlower = segmentX;
            float segmentZlower = segmentZ;

            if (strandNumber == 0) {
              segmentX = mainStrandSegmentXupper[ySegment];
              segmentZ = mainStrandSegmentZupper[ySegment];
            } else {
              segmentX += MathX.getRandomInRange(random1, -MAX_DEVIATION_PER_SEGMENT_OTHER_STRANDS, MAX_DEVIATION_PER_SEGMENT_OTHER_STRANDS);
              segmentZ += MathX.getRandomInRange(random1, -MAX_DEVIATION_PER_SEGMENT_OTHER_STRANDS, MAX_DEVIATION_PER_SEGMENT_OTHER_STRANDS);
            }

            worldrenderer.startDrawing(GL11.GL_TRIANGLE_STRIP);  // http://www.glprogramming.com/red/chapter02.html
            final float BASE_BRIGHTNESS = 2.0F;
            final float BRIGHTNESS_EACH_SHELL = Math.min(BASE_BRIGHTNESS / NUMBER_OF_SHELLS, 1);
            final float ALPHA_VALUE = 0.5F;
            worldrenderer.setColorRGBA_F(0.8F * BRIGHTNESS_EACH_SHELL, 0.8F * BRIGHTNESS_EACH_SHELL,
                    1.0F * BRIGHTNESS_EACH_SHELL, ALPHA_VALUE);
            float upperHalfWidth = CORE_HALF_WIDTH + shell * HALF_WIDTH_PER_SHELL;
            float lowerHalfWidth = CORE_HALF_WIDTH + shell * HALF_WIDTH_PER_SHELL;

            if (strandNumber == 0) {
              upperHalfWidth *= MathX.lerp(ORIGIN_WIDTH_INCREASE_FACTOR, 1.0F, (ySegment + 1) / (float)SEGMENT_COUNT);
              lowerHalfWidth *= MathX.lerp(ORIGIN_WIDTH_INCREASE_FACTOR, 1.0F, ySegment / (float)SEGMENT_COUNT);
            } else {
              upperHalfWidth *= strandRelativeWidth;
              lowerHalfWidth *= strandRelativeWidth;
            }

            // draws a vertical square tube, sides only, centred around [x,,z], over the given 16-block-high segment
            for (int vertex = 0; vertex < 5; ++vertex) {
              float xUpper = -upperHalfWidth;
              float zUpper = -upperHalfWidth;

              if (vertex == 1 || vertex == 2) {
                xUpper += upperHalfWidth * 2.0F;
              }

              if (vertex == 2 || vertex == 3) {
                zUpper += upperHalfWidth * 2.0F;
              }

              float xLower = -lowerHalfWidth;
              float zLower = -lowerHalfWidth;

              if (vertex == 1 || vertex == 2) {
                xLower += lowerHalfWidth * 2.0F;
              }

              if (vertex == 2 || vertex == 3) {
                zLower += lowerHalfWidth * 2.0F;
              }

              worldrenderer.addVertex(xLower + segmentXlower, SEGMENT_HEIGHT * ySegment, zLower + segmentZlower);
              worldrenderer.addVertex(xUpper + segmentX, SEGMENT_HEIGHT * (ySegment + 1), zUpper + segmentZ);
//              System.out.format("[%f, %f, %f] ", xBottom + segmentX, SEGMENT_HEIGHT * ySegment, zBottom + segmentZ);
//              System.out.format("[%f, %f, %f]\n", xTop + segmentXtop, SEGMENT_HEIGHT * (ySegment + 1), zTop + segmentZtop);
            }

            tessellator.draw();
//            System.out.format("Drawn\n");
//          System.out.format("[%f, %d, %f] %f to [%f, %d, %f] %f\n", deltaX, 16 * ySegment, deltaZ, bottomWidth,
//                  deltaXInitial, 16 * (ySegment + 1), deltaZInitial, topWidth);
//            System.out.format("%f, %f, %f, %f, %f, %f, %f, %f\n",
//                    segmentX, LIGHTNING_HEIGHT - SEGMENT_HEIGHT * ySegment, segmentZ, bottomWidth,
//                    segmentXtop, LIGHTNING_HEIGHT - SEGMENT_HEIGHT * (ySegment + 1), segmentZtop, topWidth);

          }
        }
      }

      GlStateManager.disableBlend();
      GlStateManager.enableLighting();
      GlStateManager.enableTexture2D();
    } finally {
      GL11.glPopMatrix();
    }
  }

  /** Apply OpenGL transforms to map the lighting onto the world coordinates
    During rendering the y axis of the lightning (from y = 0 at mouth to y = 1 at target) is mapped onto the vector between the
    the dragon's mouth and the target point.
    This is accomplished by:
    1) scale to the correct length
    2) rotate to the correct angle (rotate along the shortest path, i.e. around the vector formed from the
    cross product of the lightning [0, 1, 0] and the mouth->target vector
    3) translate the origin to the mouth point
   // openGL applies transformations in the reverse order...
   * @param entity
   */

  private void applyTransformation(EntityBreathGhost entity)
  {
    Vec3d startPoint = entity.getStartPoint();
    Vec3d endPoint = entity.getEndPoint();

    Vec3d startToEnd = endPoint.subtract(startPoint);
    double lightningLength = startToEnd.lengthVector();

    //    GL11.glTranslated(startPoint.xCoord, startPoint.yCoord, startPoint.zCoord);  // not required - already done by vanilla


    Vec3d lightningAxis = new Vec3d(0, 1, 0);
    Vec3d rotationAxis = lightningAxis.crossProduct(startToEnd);
    rotationAxis.normalize();

    final double ZERO_VEC_THRESHOLD = 0.5;
    double thetaDegrees;
    if (rotationAxis.dotProduct(rotationAxis) >= ZERO_VEC_THRESHOLD) {
      double cosTheta = lightningAxis.dotProduct(startToEnd) / lightningAxis.lengthVector() / startToEnd.lengthVector();
      thetaDegrees = Math.toDegrees(Math.acos(cosTheta));
    } else { // collinear, so check if we need to flip direction by 180 degrees.  use an arbitrary rotation axis
      thetaDegrees = lightningAxis.dotProduct(startToEnd) < 0 ? 180 : 0;
      rotationAxis = new Vec3d(1, 0, 0);  // is definitely not collinear with lightning axis
    }
    GL11.glRotated(thetaDegrees, rotationAxis.xCoord, rotationAxis.yCoord, rotationAxis.zCoord);

    GL11.glScaled(lightningLength, lightningLength, lightningLength);
  }

  private double getLightningLength(EntityBreathGhost entity)
  {
    Vec3d startPoint = entity.getStartPoint();
    Vec3d endPoint = entity.getEndPoint();
    double lightningLength = startPoint.distanceTo(endPoint);
    return lightningLength;
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
  @Override
  protected ResourceLocation getEntityTexture(EntityBreathGhost entity)
  {
    return null;
  }
}