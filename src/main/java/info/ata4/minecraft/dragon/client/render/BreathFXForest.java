package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNodeForest;
import info.ata4.minecraft.dragon.server.entity.helper.breath.DragonBreathMode;
import info.ata4.minecraft.dragon.util.EntityMoveAndResizeHelper;
import info.ata4.minecraft.dragon.util.math.MathX;
import info.ata4.minecraft.dragon.util.math.RotatingQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by TGG on 21/06/2015.
 * EntityFX that makes up the forest breath weapon; client side.
 *
 * Usage:
 * (1) create a new BreathFXForest using createBreathFXForest
 * (2) spawn it as per normal
 *
 */
public class BreathFXForest extends BreathFX {
  private final ResourceLocation forestGasCloudRL = new ResourceLocation("dragonmounts:entities/breathweapon/breath_forest");
  private final ResourceLocation fireballRL = new ResourceLocation("dragonmounts:entities/breathweapon/breath_fire");

  private static final float MAX_ALPHA = 0.80F;

  /**
   * creates a single EntityFX from the given parameters.  Applies some random spread to direction.
   * @param world
   * @param x world [x,y,z] to spawn at (coordinates are the centre point of the fireball)
   * @param y
   * @param z
   * @param directionX initial world direction [x,y,z] - will be normalised.
   * @param directionY
   * @param directionZ
   * @param power the power of the ball
   * @param partialTicksHeadStart if spawning multiple EntityFX per tick, use this parameter to spread the starting
   *                              location in the direction
   * @return the new BreathFXForest
   */

  public static BreathFXForest createBreathFXForest(World world, double x, double y, double z,
                                                              double directionX, double directionY, double directionZ,
                                                              BreathNode.Power power, DragonBreathMode dragonBreathMode,
                                                              int tickCount, float partialTicksHeadStart)
  {
    Vec3d direction = new Vec3d(directionX, directionY, directionZ).normalize();

    Random rand = new Random();
    BreathNode breathNode = new BreathNodeForest(power, DragonBreathMode.FOREST_NOT_BURNING);
    breathNode.randomiseProperties(rand);
    Vec3d actualMotion = breathNode.getRandomisedStartingMotion(direction, rand);

    x += actualMotion.xCoord * partialTicksHeadStart;
    y += actualMotion.yCoord * partialTicksHeadStart;
    z += actualMotion.zCoord * partialTicksHeadStart;

    double spawnTickCount = tickCount - partialTicksHeadStart;
    double tickCountInFlight = partialTicksHeadStart / 20.0;

    BreathFXForest breathFXForest = new BreathFXForest(world, x, y, z, actualMotion, breathNode,
                                                       spawnTickCount, tickCountInFlight);
    return breathFXForest;
  }

  private BreathFXForest(World world, double x, double y, double z, Vec3d motion,
                         BreathNode i_breathNode, double i_spawnTimeTicks, double timeInFlightTicks) {
    super(world, x, y, z, motion.xCoord, motion.yCoord, motion.zCoord);

    breathNode = i_breathNode;
    particleGravity = Blocks.ICE.blockParticleGravity;  /// arbitrary block!  maybe not even required.
    particleMaxAge = (int)breathNode.getMaxLifeTime(); // not used, but good for debugging
    this.particleAlpha = MAX_ALPHA;

    //undo random velocity variation of vanilla EntityFX constructor
    motionX = motion.xCoord;
    motionY = motion.yCoord;
    motionZ = motion.zCoord;

    // set the texture to the flame texture, which we have previously added using TextureStitchEvent
    TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
            forestGasCloudRL.toString());
    setParticleTexture(sprite);
    entityMoveAndResizeHelper = new EntityMoveAndResizeHelper(this);

    textureUV = setRandomTexture(this.particleTexture);

    // set the texture to the flame texture, which we have previously added using TextureStitchEvent
    textureUVburning = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
            fireballRL.toString());

//    clockwiseRotation = rand.nextBoolean();
//    final float MIN_ROTATION_SPEED = 2.0F; // revolutions per second
//    final float MAX_ROTATION_SPEED = 6.0F; // revolutions per second
//    rotationSpeedQuadrantsPerTick = MIN_ROTATION_SPEED + rand.nextFloat() * (MAX_ROTATION_SPEED - MIN_ROTATION_SPEED);
//    rotationSpeedQuadrantsPerTick *= 4.0 / 20.0F; // convert to quadrants per tick

    spawnTimeTicks = i_spawnTimeTicks;
    ticksSinceSpawn = timeInFlightTicks;
    glowCycleTickLength = GLOW_CYCLE_TICKS_MIN + rand.nextInt(GLOW_CYCLE_TICKS_MAX + 1 - GLOW_CYCLE_TICKS_MIN);
  }

  private RotatingQuad setRandomTexture(TextureAtlasSprite textureAtlasSprite)
  {
    Random random = new Random();
    double minU = textureAtlasSprite.getMinU();
    double maxU = textureAtlasSprite.getMaxU();
    double midU = (minU + maxU) / 2.0;
    double minV = textureAtlasSprite.getMinV();
    double maxV = textureAtlasSprite.getMaxV();
    double midV = (minV + maxV) / 2.0;
    renderScaleFactor = 1.0F;

    if (random.nextBoolean()) {
      minU = midU;
    } else {
      maxU = midU;
    }
    if (random.nextBoolean()) {
      minV = midV;
    } else {
      maxV = midV;
    }

    RotatingQuad tex = new RotatingQuad(minU, minV, maxU, maxV);
    if (random.nextBoolean()) {
      tex.mirrorLR();
    }
    tex.rotate90(random.nextInt(4));
    return tex;
  }


  /**
   * Returns 1, which means "use a texture from the blocks + items texture sheet"
   * @return
   */
  @Override
  public int getFXLayer() {
    return 1;
  }

  // this function is used by EffectRenderer.addEffect() to determine whether depthmask writing should be on or not.
  // by default, vanilla turns off depthmask writing for entityFX with alphavalue less than 1.0
  // BreathFXWater uses alphablending but we want depthmask writing on, otherwise translucent objects (such as water)
  //   render over the top of our breath.
  @Override
  public float func_174838_j()
  {
    return 1.0F;
  }


  private static final int MIN_LIGHT = 0x08;
  private static final int MAX_LIGHT = 0x0f;
  private static final int GLOW_CYCLE_TICKS_MIN = 10;
  private static final int GLOW_CYCLE_TICKS_MAX = 20;

  @Override
  public int getBrightnessForRender(float partialTick)
  {
    if (glowCycleTickCount > glowCycleTickLength) {
      glowCycleTickCount = 0;
    }
    double cycleFraction = glowCycleTickCount / (double)glowCycleTickLength;
    double light;
    if (cycleFraction >= 0.5) {
      light = MathX.slerp(MIN_LIGHT, MAX_LIGHT, 2 * (1.0 - cycleFraction));
    } else {
      light = MathX.slerp(MIN_LIGHT, MAX_LIGHT, 2 * cycleFraction);
    }
    int lightInt = (int)light;
    int brightnessValue = (lightInt << 4) | (lightInt << 20);
//    System.out.format("cycleFraction %.2f; light %.1f; brightnessValue: %d", cycleFraction, light, brightnessValue);
    return brightnessValue;
  }

  /**
   * Render the EntityFX onto the screen.
   * The EntityFX is rendered as a two-dimensional object (Quad) in the world (three-dimensional coordinates).
   *   The corners of the quad are chosen so that the EntityFX is drawn directly facing the viewer (or in other words,
   *   so that the quad is always directly face-on to the screen.)
   * In order to manage this, it needs to know two direction vectors:
   * 1) the 3D vector direction corresponding to left-right on the viewer's screen (edgeLRdirection)
   * 2) the 3D vector direction corresponding to up-down on the viewer's screen (edgeUDdirection)
   * These two vectors are calculated by the caller.
   * For example, the top right corner of the quad on the viewer's screen is equal to the centre point of the quad (x,y,z)
   *   plus the edgeLRdirection vector multiplied by half the quad's width, plus the edgeUDdirection vector multiplied
   *   by half the quad's height.
   * NB edgeLRdirectionY is not provided because it's always 0, i.e. the top of the viewer's screen is always directly
   *    up so moving left-right on the viewer's screen doesn't affect the y coordinate position in the world
   *  Spray is rendered with lower opacity
   *  The other textures are rendered with random variation in size
   *  Teardrop is rotated to face direction of travel
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
    if (hasBeenIgnited) {
      renderBurningBall(worldRenderer, entity, partialTick,
                        edgeLRdirectionX, edgeUDdirectionY, edgeLRdirectionZ,
                        edgeUDdirectionX, edgeUDdirectionZ);
      return;
    }
    double scale = 0.1F * this.particleScale * renderScaleFactor;
    final double scaleLR = scale;
    final double scaleUD = scale;
    double x = this.prevPosX + (this.posX - this.prevPosX) * partialTick - interpPosX;
    double y = this.prevPosY + (this.posY - this.prevPosY) * partialTick - interpPosY + this.height / 2.0F;
    // centre of rendering is now y midpt not ymin
    double z = this.prevPosZ + (this.posZ - this.prevPosZ) * partialTick - interpPosZ;

    float alphaValue = this.particleAlpha;
    worldRenderer.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, alphaValue);
    worldRenderer.addVertexWithUV(x - edgeLRdirectionX * scaleLR - edgeUDdirectionX * scaleUD,
                                  y - edgeUDdirectionY * scaleUD,
                                  z - edgeLRdirectionZ * scaleLR - edgeUDdirectionZ * scaleUD,
                                  textureUV.getU(0), textureUV.getV(0));
    worldRenderer.addVertexWithUV(x - edgeLRdirectionX * scaleLR + edgeUDdirectionX * scaleUD,
            y + edgeUDdirectionY * scaleUD,
            z - edgeLRdirectionZ * scaleLR + edgeUDdirectionZ * scaleUD,
            textureUV.getU(1), textureUV.getV(1));
    worldRenderer.addVertexWithUV(x + edgeLRdirectionX * scaleLR + edgeUDdirectionX * scaleUD,
                                  y + edgeUDdirectionY * scaleUD,
                                  z + edgeLRdirectionZ * scaleLR + edgeUDdirectionZ * scaleUD,
                                  textureUV.getU(2),  textureUV.getV(2));
    worldRenderer.addVertexWithUV(x + edgeLRdirectionX * scaleLR - edgeUDdirectionX * scaleUD,
                                  y - edgeUDdirectionY * scaleUD,
                                  z + edgeLRdirectionZ * scaleLR - edgeUDdirectionZ * scaleUD,
                                  textureUV.getU(3), textureUV.getV(3));
  }

  private void renderBurningBall(WorldRenderer worldRenderer, Entity entity, float partialTick,
                                  float edgeLRdirectionX, float edgeUDdirectionY, float edgeLRdirectionZ,
                                  float edgeUDdirectionX, float edgeUDdirectionZ)
  {
    double minU = this.textureUVburning.getMinU();
    double maxU = this.textureUVburning.getMaxU();
    double minV = this.textureUVburning.getMinV();
    double maxV = this.textureUVburning.getMaxV();
    RotatingQuad tex = new RotatingQuad(minU, minV, maxU, maxV);
    Random random = new Random();
    if (random.nextBoolean()) {
      tex.mirrorLR();
    }
    tex.rotate90(random.nextInt(4));

    double scale = 0.1F * this.particleScale;
    final double scaleLR = scale;
    final double scaleUD = scale;
    double x = this.prevPosX + (this.posX - this.prevPosX) * partialTick - interpPosX;
    double y = this.prevPosY + (this.posY - this.prevPosY) * partialTick - interpPosY + this.height / 2.0F;
    // centre of rendering is now y midpt not ymin
    double z = this.prevPosZ + (this.posZ - this.prevPosZ) * partialTick - interpPosZ;

    worldRenderer.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
    worldRenderer.addVertexWithUV(x - edgeLRdirectionX * scaleLR - edgeUDdirectionX * scaleUD,
                                  y - edgeUDdirectionY * scaleUD,
                                  z - edgeLRdirectionZ * scaleLR - edgeUDdirectionZ * scaleUD,
                                  tex.getU(0),  tex.getV(0));
    worldRenderer.addVertexWithUV(x - edgeLRdirectionX * scaleLR + edgeUDdirectionX * scaleUD,
                                  y + edgeUDdirectionY * scaleUD,
                                  z - edgeLRdirectionZ * scaleLR + edgeUDdirectionZ * scaleUD,
                                  tex.getU(1),  tex.getV(1));
    worldRenderer.addVertexWithUV(x + edgeLRdirectionX * scaleLR + edgeUDdirectionX * scaleUD,
                                  y + edgeUDdirectionY * scaleUD,
                                  z + edgeLRdirectionZ * scaleLR + edgeUDdirectionZ * scaleUD,
                                  tex.getU(2),  tex.getV(2));
    worldRenderer.addVertexWithUV(x + edgeLRdirectionX * scaleLR - edgeUDdirectionX * scaleUD,
                                  y - edgeUDdirectionY * scaleUD,
                                  z + edgeLRdirectionZ * scaleLR - edgeUDdirectionZ * scaleUD,
                                  tex.getU(3),  tex.getV(3));
  }


  /** call once per tick to update the EntityFX size, position, collisions, etc
   */
  @Override
  public void onUpdate() {
    final float YOUNG_AGE = 0.25F;
    final float OLD_AGE = 0.75F;

    float lifetimeFraction = breathNode.getLifetimeFraction();
//    if (lifetimeFraction < YOUNG_AGE) {     // fading looks silly because depthmask writing sometimes causes the faded to hide the unfaded
//      particleAlpha = MAX_ALPHA;
//    } else if (lifetimeFraction < OLD_AGE) {
//      particleAlpha = MAX_ALPHA;
//    } else {
//      particleAlpha = MAX_ALPHA * (1 - lifetimeFraction);
//    }

    ++ticksSinceSpawn;
    ++glowCycleTickCount;

    final float PARTICLE_SCALE_RELATIVE_TO_SIZE = 5.0F; // factor to convert from particleSize to particleScale
    float currentParticleSize = breathNode.getCurrentRenderDiameter();
    particleScale = PARTICLE_SCALE_RELATIVE_TO_SIZE * currentParticleSize;

    float newAABBDiameter = breathNode.getCurrentAABBcollisionSize();

    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;
    breathNode.modifyEntityVelocity(this);
    entityMoveAndResizeHelper.moveAndResizeEntity(motionX, motionY, motionZ, newAABBDiameter, newAABBDiameter);

    if (isCollided && onGround) {
        motionY -= 0.01F;         // ensure that we hit the ground next time too
    }
    breathNode.updateAge(this);
    particleAge = (int)breathNode.getAgeTicks();  // not used, but good for debugging
    if (breathNode.isDead()) {
      setDead();
    }
  }

  @Override
  public void updateBreathMode(DragonBreathMode dragonBreathMode)
  {
    super.updateBreathMode(dragonBreathMode);
    if (dragonBreathMode.equals(DragonBreathMode.FOREST_BURNING)) {
      hasBeenIgnited = true;
    }
  }

//  protected EnumParticleTypes getSmokeParticleID() {
//    if (LARGE_SPLASH_CHANCE != 0 && rand.nextFloat() <= LARGE_SPLASH_CHANCE) {
//      return EnumParticleTypes.WATER_BUBBLE;
//    } else {
//      return EnumParticleTypes.WATER_SPLASH;
//    }
//  }

  /** Vanilla moveEntity does a pile of unneeded calculations, and also doesn't handle resize around the centre properly,
   * so replace with a custom one
   * @param dx the amount to move the entity in world coordinates [dx, dy, dz]
   * @param dy
   * @param dz
   */
  @Override
  public void moveEntity(double dx, double dy, double dz) {
    entityMoveAndResizeHelper.moveAndResizeEntity(dx, dy, dz, this.width, this.height);
  }

  private EntityMoveAndResizeHelper entityMoveAndResizeHelper;
  private RotatingQuad textureUV;
  private TextureAtlasSprite textureUVburning;
  private boolean hasBeenIgnited = false;

  private float renderScaleFactor;

//  private double ticksInFlight = 0;
  private double spawnTimeTicks = 0;
  private double ticksSinceSpawn = 0;
  private int glowCycleTickCount = 0;
  private int glowCycleTickLength = 0;

}