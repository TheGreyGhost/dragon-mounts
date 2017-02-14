package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.client.render.breeds.IEntityParticle;
import info.ata4.minecraft.dragon.util.EntityMoveAndResizeHelper;
import info.ata4.minecraft.dragon.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Created by TGG on 31/07/2015.
 * Server side; tracks the position, motion, and collision detection of a breath node in a breath weapon stream,
 * Used with an associated BreathNode to track age, size and initial speed
 *
 * Usage:
 * 1) construct using createEntityBreathNodeServer
 * 2) call onUpdate() every tick to move and collide
 * 3) various getters for intensity, radius, and recent collisions.
  */
class EntityBreathNode extends Entity implements IEntityParticle
{
  public static EntityBreathNode createEntityBreathNodeServer(World world, double x, double y, double z,
                                                              double directionX, double directionY, double directionZ,
                                                              BreathNodeFactory breathNodeFactory, BreathNode.Power power,
                                                              DragonBreathMode dragonBreathMode)
  {
    Vec3d direction = new Vec3d(directionX, directionY, directionZ).normalize();

    Random rand = new Random();
    BreathNode breathNode = breathNodeFactory.createBreathNode(power, dragonBreathMode);
    Vec3d actualMotion = breathNode.getRandomisedStartingMotion(direction, rand);
    // don't randomise the other properties (size, age) on the server.

    EntityBreathNode newEntity = new EntityBreathNode(world, x, y, z, actualMotion, breathNode);
    return newEntity;
  }

  private EntityBreathNode(World world, double x, double y, double z, Vec3d motion, BreathNode i_breathNode)
  {
    super(world);
    breathNode = i_breathNode;

    final float ARBITRARY_START_SIZE = 0.2F;
    this.setSize(ARBITRARY_START_SIZE, ARBITRARY_START_SIZE);
    this.setPosition(x, y, z);
    lastTickPosX = x;
    lastTickPosY = y;
    lastTickPosZ = z;

    motionX = motion.xCoord;
    motionY = motion.yCoord;
    motionZ = motion.zCoord;
    entityMoveAndResizeHelper = new EntityMoveAndResizeHelper(this);
  }

  public void updateBreathMode(DragonBreathMode dragonBreathMode)
  {
    breathNode.changeBreathMode(dragonBreathMode);
  }

  public void setMotion(Vec3d newMotion)
  {
    motionX = newMotion.xCoord;
    motionY = newMotion.yCoord;
    motionZ = newMotion.zCoord;
  }

  @Override
  public void onUpdate() {

    handleWaterMovement();

    float newAABBDiameter = breathNode.getCurrentAABBcollisionSize();

    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;
    breathNode.modifyEntityVelocity(this);
    collisions = entityMoveAndResizeHelper.moveAndResizeEntity(motionX, motionY, motionZ, newAABBDiameter, newAABBDiameter);
    intensityAtCollision = getCurrentIntensity();

    if (isCollided && onGround) {
      motionY -= 0.01F;         // ensure that we hit the ground next time too
    }
    breathNode.updateAge(this);
    if (breathNode.isDead()) {
      setDead();
    }
  }

  public float getCurrentRadius() {
    return breathNode.getCurrentDiameterOfEffect() / 2.0F;
  }

  public float getCurrentIntensity() {return  breathNode.getCurrentIntensity();}

  /**
   * Get a collection of the collisions that occurred during the last tick update
   *@return returns a collection showing which parts of the entity collided with an object- eg
   *        (WEST, [3,2,6]-->[3.5, 2, 6] means the west face of the entity collided; the entity tried to move to
   *          x = 3, but got pushed back to x=3.5
   */
  public Collection<Pair<EnumFacing, AxisAlignedBB>> getRecentCollisions()
  {
    if (collisions == null) {
      collisions = new ArrayList<Pair<EnumFacing, AxisAlignedBB>>();
    }
    return collisions;
  }

  /** The intensity of the node at the time the last collision occurred
   * @return snapshot of getCurrentIntensity at the last collision.  Meaningless if getRecentCollisions() empty.
   */
  public float getIntensityAtCollision()
  {
    return intensityAtCollision;
  }

  private BreathNode breathNode;
  private EntityMoveAndResizeHelper entityMoveAndResizeHelper;

  @Override
  protected void entityInit()
  {
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound tagCompund)
  {
  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound tagCompound)
  {
  }

  private Collection<Pair<EnumFacing, AxisAlignedBB>> collisions;
  private float intensityAtCollision;

  public double getMotionX() {return motionX;}
  public double getMotionY() {return motionY;}
  public double getMotionZ() {return motionZ;}
  public double getSpeedSQ() {return motionX*motionX + motionY*motionY + motionZ*motionZ;}
//  public boolean isInWater() {return isInWater();}
  public boolean isCollided() {return isCollided;}
//  public boolean isInLava() {return super.isInLava();}
}
