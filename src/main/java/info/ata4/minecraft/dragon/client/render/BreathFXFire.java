package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNodeFire;
import info.ata4.minecraft.dragon.util.EntityMoveAndResizeHelper;
import info.ata4.minecraft.dragon.util.math.RotatingQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by TGG on 21/06/2015.
 * EntityFX that makes up the flame breath weapon; client side.
 *
 * Usage:
 * (1) create a new BreathFXFire using createBreathFXIce
 * (2) spawn it as per normal
 *
 */
public class BreathFXFire extends EntityFX {
  private final ResourceLocation fireballRL = new ResourceLocation("dragonmounts:entities/breathweapon/breath_fire");

  private final float SMOKE_CHANCE = 0.1f;
  private final float LARGE_SMOKE_CHANCE = 0.3f;

  private static final float MAX_ALPHA = 0.99F;

  private BreathNode breathNode;

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
   * @return the new BreathFXFire
   */
  public static BreathFXFire createBreathFXFire(World world, double x, double y, double z,
                                                  double directionX, double directionY, double directionZ,
                                                  BreathNode.Power power,
                                                  float partialTicksHeadStart)
  {
    Vec3 direction = new Vec3(directionX, directionY, directionZ).normalize();

    Random rand = new Random();
    BreathNode breathNode = new BreathNodeFire(power);
    breathNode.randomiseProperties(rand);
    Vec3 actualMotion = breathNode.getRandomisedStartingMotion(direction, rand);

    x += actualMotion.xCoord * partialTicksHeadStart;
    y += actualMotion.yCoord * partialTicksHeadStart;
    z += actualMotion.zCoord * partialTicksHeadStart;
    BreathFXFire newBreathFXFire = new BreathFXFire(world, x, y, z, actualMotion, breathNode);
    return newBreathFXFire;
  }

  private BreathFXFire(World world, double x, double y, double z, Vec3 motion,
                       BreathNode i_breathNode) {
    super(world, x, y, z, motion.xCoord, motion.yCoord, motion.zCoord);

    breathNode = i_breathNode;
    particleGravity = Blocks.fire.blockParticleGravity;  /// arbitrary block!  maybe not even required.
    particleMaxAge = (int)breathNode.getMaxLifeTime(); // not used, but good for debugging
    this.particleAlpha = MAX_ALPHA;  // a value less than 1 turns on alpha blending

    //undo random velocity variation of vanilla EntityFX constructor
    motionX = motion.xCoord;
    motionY = motion.yCoord;
    motionZ = motion.zCoord;

    // set the texture to the flame texture, which we have previously added using TextureStitchEvent
    TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fireballRL.toString());
    func_180435_a(sprite);
    entityMoveAndResizeHelper = new EntityMoveAndResizeHelper(this);
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
  // BreathFXFire uses alphablending but we want depthmask writing on, otherwise translucent objects (such as water)
  //   render over the top of our breath.
  @Override
  public float func_174838_j()
  {
    return 1.0F;
  }

  @Override
  public int getBrightnessForRender(float partialTick)
  {
    return 0xf000f0;
  }

  /**
   * Render the EntityFX onto the screen.
   * The EntityFX is rendered as a two-dimensional object (Quad) in the world (three-dimensional coordinates).
   *   The corners of the quad are chosen so that the EntityFX is drawn directly facing the viewer (or in other words,
   *   so that the quad is always directly face-on to the screen.)
   * In order to manage this, it needs to know two direction vectors:
   * 1) the 3D vector direction corresponding to left-right on the viewer's screen (edgeLRdirection)
   * 2) the 3D vector direction corresponding to up-down on the viewer's screen (edgeURdirection)
   * These two vectors are calculated by the caller.
   * For example, the top right corner of the quad on the viewer's screen is equal to the centre point of the quad (x,y,z)
   *   plus the edgeLRdirection vector multiplied by half the quad's width, plus the edgeUDdirection vector multiplied
   *   by half the quad's height.
   * NB edgeLRdirectionY is not provided because it's always 0, i.e. the top of the viewer's screen is always directly
   *    up so moving left-right on the viewer's screen doesn't affect the y coordinate position in the world
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
    double minU = this.particleIcon.getMinU();
    double maxU = this.particleIcon.getMaxU();
    double minV = this.particleIcon.getMinV();
    double maxV = this.particleIcon.getMaxV();
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
    if (lifetimeFraction < YOUNG_AGE) {
      particleAlpha = MAX_ALPHA;
    } else if (lifetimeFraction < OLD_AGE) {
      particleAlpha = MAX_ALPHA;
    } else {
      particleAlpha = MAX_ALPHA * (1 - lifetimeFraction);
    }

    final float PARTICLE_SCALE_RELATIVE_TO_SIZE = 5.0F; // factor to convert from particleSize to particleScale
    float currentParticleSize = breathNode.getCurrentRenderDiameter();
    particleScale = PARTICLE_SCALE_RELATIVE_TO_SIZE * currentParticleSize;

    // spawn a smoke trail after some time
    if (SMOKE_CHANCE != 0 && rand.nextFloat() < lifetimeFraction && rand.nextFloat() <= SMOKE_CHANCE) {
      worldObj.spawnParticle(getSmokeParticleID(), posX, posY, posZ, motionX * 0.5, motionY * 0.5, motionZ * 0.5);
    }

    // smoke / steam when hitting water.  node is responsible for aging to death
    if (handleWaterMovement()) {
      worldObj.spawnParticle(getSmokeParticleID(), posX, posY, posZ, 0, 0, 0);
    }

    float newAABBDiameter = breathNode.getCurrentAABBcollisionSize();

    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;
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

  protected EnumParticleTypes getSmokeParticleID() {
    if (LARGE_SMOKE_CHANCE != 0 && rand.nextFloat() <= LARGE_SMOKE_CHANCE) {
      return EnumParticleTypes.SMOKE_LARGE;
    } else {
      return EnumParticleTypes.SMOKE_NORMAL;
    }
  }

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
}
