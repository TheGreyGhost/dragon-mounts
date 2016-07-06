package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.util.math.MathX;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Created by TGG on 14/03/2016.
 * Copied from EntityFireball and modified a bit
 */
public abstract class EntityBreathProjectile extends Entity implements IEntityAdditionalSpawnData
{
  private int xTile = -1;
  private int yTile = -1;
  private int zTile = -1;
  private Block inTile;
  private boolean inGround;
  public EntityLivingBase shootingEntity;
  private int ticksAlive;
  private int ticksInAir;
  public double accelerationX;
  public double accelerationY;
  public double accelerationZ;
  protected Vec3 origin;
  protected Vec3 destination;

  public EntityBreathProjectile(World worldIn) {
    super(worldIn);
    this.setSize(1.0F, 1.0F);
    power = BreathNode.Power.SMALL;  // default
    ticksToLive = getLifeTimeTicks(power);
    parentDragon = null;
  }

  public EntityBreathProjectile(World worldIn, EntityTameableDragon shooter,
                                Vec3 i_origin, Vec3 i_destination, BreathNode.Power i_power) {
    super(worldIn);
    this.shootingEntity = shooter;
    parentDragon = shooter;
    origin = i_origin;
    destination = i_destination;
    power = i_power;
    this.setSizeFromPower(power);
    Vec3 offset = destination.subtract(origin);
    double yaw = MathX.calculateYaw(offset);
    double pitch = MathX.calculatePitch(offset);

    this.setLocationAndAngles(origin.xCoord, origin.yCoord, origin.zCoord,
            (float)yaw, (float)pitch);
//    this.setPosition(this.posX, this.posY, this.posZ);
    this.motionX = this.motionY = this.motionZ = 0.0D;

    final double ACCELERATION_BLOCKS_PER_TICK_SQ = 0.2;

    Vec3 normalisedOffset = offset.normalize();
    this.accelerationX = ACCELERATION_BLOCKS_PER_TICK_SQ * normalisedOffset.xCoord;
    this.accelerationY = ACCELERATION_BLOCKS_PER_TICK_SQ * normalisedOffset.yCoord;
    this.accelerationZ = ACCELERATION_BLOCKS_PER_TICK_SQ * normalisedOffset.zCoord;

    ticksToLive = getLifeTimeTicks(power);

//    projectileNumber =  ++projectilesFired;
//    ticksTillFreeze = 10 - projectilesFired;
  }

  protected void entityInit() {
  }

  /**
   * Return the lifetime of the projectile
   * @param power
   * @return lifetime in ticks
   */
  abstract protected int getLifeTimeTicks(BreathNode.Power power);

  /**
   * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
   * length * 64 * renderDistanceWeight Args: distance
   */
  @SideOnly(Side.CLIENT)
  public boolean isInRangeToRenderDist(double distance) {
    double d1 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
    d1 *= 64.0D;
    return distance < d1 * d1;
  }

//  public EntityBreathProjectile(World worldIn, double x, double y, double z, double accelX, double accelY,
//                                double accelZ) {
//    super(worldIn);
//    this.setSize(1.0F, 1.0F);
//    this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
//    this.setPosition(x, y, z);
//    double d6 = (double) MathHelper.sqrt_double(accelX * accelX + accelY * accelY + accelZ * accelZ);
//    this.accelerationX = accelX / d6 * 0.1D;
//    this.accelerationY = accelY / d6 * 0.1D;
//    this.accelerationZ = accelZ / d6 * 0.1D;
//  }


//  private static int projectilesFired = 0;  //todo remove
//  private int ticksTillFreeze; //todo remove
//  private int projectileNumber; //todo remove

  // used during initialisation to calculate the entity size based on the power.
  //  must not access member variables!
  protected abstract void setSizeFromPower(BreathNode.Power power);

  public Vec3 getCurrentPosition()
  {
    return new Vec3(posX, posY, posZ);
  }

  @Override
  public void onUpdate() {
    BlockPos entityTilePos = new BlockPos(this);
//    if (--ticksTillFreeze == 0) {
//      motionX = 0; motionY = 0; motionZ = 0;
//      accelerationX = 0; accelerationY = 0; accelerationZ = 0;
//      if (!this.worldObj.isRemote) {
//        System.out.format("%d stop at [%f, %f, %f]\n", projectileNumber, posX, posY, posZ);
//      }
//    }

    if (!this.worldObj.isRemote && !this.worldObj.isBlockLoaded(entityTilePos)) {
      this.setDead();
    } else {
      super.onUpdate();

      if (this.inGround) {
        if (this.worldObj.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile) {
          ++this.ticksAlive;

          if (this.ticksAlive == 600) {
            this.setDead();
          }

          return;
        }

        this.inGround = false;
        this.motionX *= (double) (this.rand.nextFloat() * 0.2F);
        this.motionY *= (double) (this.rand.nextFloat() * 0.2F);
        this.motionZ *= (double) (this.rand.nextFloat() * 0.2F);
        this.ticksAlive = 0;
        this.ticksInAir = 0;
      } else {
        ++this.ticksInAir;
      }

      Vec3 startPos = new Vec3(this.posX, this.posY, this.posZ);
      Vec3 endPos = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

      final boolean STOP_ON_LIQUID_FALSE = false;
      final boolean IGNORE_BLOCK_WITHOUT_BOUNDING_BOX_TRUE = true;
      final boolean RETURN_LAST_UNCOLLIDABLE_BLOCK_FALSE = false;
      MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(startPos, endPos,
                                                    STOP_ON_LIQUID_FALSE, IGNORE_BLOCK_WITHOUT_BOUNDING_BOX_TRUE,
                                                    RETURN_LAST_UNCOLLIDABLE_BLOCK_FALSE);

//      startPos = new Vec3(this.posX, this.posY, this.posZ);
//      endPos = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

      if (movingobjectposition != null) {
        endPos = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord,
                         movingobjectposition.hitVec.zCoord);
      }

      Entity firstEntityStruck = null;
      double smallestCollisionDistance = Double.MAX_VALUE;
      List collidingEntities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox()
                                                                               .addCoord(this.motionX, this.motionY,
                                                                                         this.motionZ)
                                                                               .expand(1.0D, 1.0D, 1.0D));
      for (Object entry : collidingEntities) {
        Entity collidingEntity = (Entity)entry;
        if (collidingEntity.canBeCollidedWith() &&
             (!collidingEntity.isEntityEqual(this.shootingEntity) || this.ticksInAir >= 25)) {
          final double f = 0.3F;
          AxisAlignedBB axisalignedbb = collidingEntity.getEntityBoundingBox().expand(f, f, f);
          MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(startPos, endPos);

          if (movingobjectposition1 != null) {
            double d1 = startPos.distanceTo(movingobjectposition1.hitVec);

            if (d1 < smallestCollisionDistance) {
              firstEntityStruck = collidingEntity;
              smallestCollisionDistance = d1;
            }
          }
        }
      }

      if (firstEntityStruck != null) {
        movingobjectposition = new MovingObjectPosition(firstEntityStruck);
      }

      if (movingobjectposition != null) {
        this.onImpact(movingobjectposition);
      }

      this.posX += this.motionX;
      this.posY += this.motionY;
      this.posZ += this.motionZ;
      float motionLength = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
//      final float THRESHOLD_SPEED = 0.01F; // minimum speed in blocks per tick - if less than this, die
//      if (motionLength < THRESHOLD_SPEED) {
//        float accelerationLength = MathHelper.sqrt_double(this.accelerationX * this.motionX + this.motionZ * this.motionZ);
//        this.setDead();
//      }
      this.rotationYaw = (float) (Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) + 90.0F;

      for (this.rotationPitch = (float) (Math.atan2((double) motionLength, this.motionY) * 180.0D / Math.PI) - 90.0F;
           this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
        ;
      }

      while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
        this.prevRotationPitch += 360.0F;
      }

      while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
        this.prevRotationYaw -= 360.0F;
      }

      while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
        this.prevRotationYaw += 360.0F;
      }

      this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
      this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
      float motionFactor = this.getMotionFactor();

      if (this.isInWater()) {
        inWaterUpdate();
        for (int j = 0; j < 4; ++j) {
          float f3 = 0.25F;
          this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double) f3,
                                      this.posY - this.motionY * (double) f3, this.posZ - this.motionZ * (double) f3,
                                      this.motionX, this.motionY, this.motionZ, new int[0]);
        }

        motionFactor *= 0.8F;
      }

      this.motionX += this.accelerationX;
      this.motionY += this.accelerationY;
      this.motionZ += this.accelerationZ;
      this.motionX *= (double) motionFactor;
      this.motionY *= (double) motionFactor;
      this.motionZ *= (double) motionFactor;

      this.setPosition(this.posX, this.posY, this.posZ);
//      if (!this.worldObj.isRemote) {
//        System.out.format("%s Pos: [%f, %f, %f] motion:[%f, %f, %f]\n",
//                          this.worldObj.isRemote ? "client" : "server",
//                          this.posX, this.posY, this.posZ,
//                          this.motionX, this.motionY, this.motionZ);  //todo remove
//      }
    }
  }

  /**
   * Return the motion factor for this projectile. The factor is multiplied by the original motion.
   * effectively a 'drag' on the projectile motion
   */
  protected float getMotionFactor() {
    return 0.95F;
  }

  // Called every tick that the projectile is in water
  protected void inWaterUpdate()
  {
  }

  /**
   * Called when this EntityFireball hits a block or entity.
   */
  protected abstract void onImpact(MovingObjectPosition movingObject);

  /**
   * (abstract) Protected helper method to write subclass entity data to NBT.
   */
  @Override
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    tagCompound.setShort("xTile", (short) this.xTile);
    tagCompound.setShort("yTile", (short) this.yTile);
    tagCompound.setShort("zTile", (short) this.zTile);
    ResourceLocation resourcelocation = (ResourceLocation) Block.blockRegistry.getNameForObject(this.inTile);
    tagCompound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
    tagCompound.setByte("inGround", (byte) (this.inGround ? 1 : 0));
    tagCompound.setTag("direction", this.newDoubleNBTList(new double[]{this.motionX, this.motionY, this.motionZ}));
    tagCompound.setInteger("ProjectilePower", this.power.ordinal());
    tagCompound.setDouble("accelerationX", accelerationX);
    tagCompound.setDouble("accelerationY", accelerationY);
    tagCompound.setDouble("accelerationZ", accelerationZ);
    tagCompound.setInteger("ticksToLive", ticksToLive);
    tagCompound.setDouble("originX", origin.xCoord);
    tagCompound.setDouble("originY", origin.yCoord);
    tagCompound.setDouble("originZ", origin.zCoord);
    tagCompound.setDouble("destinationX", destination.xCoord);
    tagCompound.setDouble("destinationY", destination.yCoord);
    tagCompound.setDouble("destinationZ", destination.zCoord);
  }

  /**
   * (abstract) Protected helper method to read subclass entity data from NBT.
   */
  @Override
  public void readEntityFromNBT(NBTTagCompound tagCompound) {
    this.xTile = tagCompound.getShort("xTile");
    this.yTile = tagCompound.getShort("yTile");
    this.zTile = tagCompound.getShort("zTile");

    if (tagCompound.hasKey("inTile", 8)) {
      this.inTile = Block.getBlockFromName(tagCompound.getString("inTile"));
    } else {
      this.inTile = Block.getBlockById(tagCompound.getByte("inTile") & 255);
    }

    this.inGround = tagCompound.getByte("inGround") == 1;

    if (tagCompound.hasKey("direction", 9)) {
      NBTTagList nbttaglist = tagCompound.getTagList("direction", 6);
      this.motionX = nbttaglist.getDouble(0);
      this.motionY = nbttaglist.getDouble(1);
      this.motionZ = nbttaglist.getDouble(2);
    } else {
      this.setDead();
    }

    this.accelerationX = tagCompound.getDouble("accelerationX");
    this.accelerationY = tagCompound.getDouble("accelerationY");
    this.accelerationZ = tagCompound.getDouble("accelerationZ");

//    System.err.println("NBT read power:"+ power); // todo remove
    power = BreathNode.Power.SMALL;  // default
    if (tagCompound.hasKey("ProjectilePower", 99)) {
      int powerIndex = tagCompound.getInteger("ProjectilePower");
      if (powerIndex >= 0 && powerIndex < BreathNode.Power.values().length) {
        this.power = BreathNode.Power.values()[powerIndex];
//        System.err.println("NBT power:"+ power); // todo remove
      }
    }
    ticksToLive = tagCompound.getInteger("ticksToLive");

    double originX = tagCompound.getDouble("originX");
    double originY = tagCompound.getDouble("originY");
    double originZ = tagCompound.getDouble("originZ");
    double destinationX = tagCompound.getDouble("destinationX");
    double destinationY = tagCompound.getDouble("destinationY");
    double destinationZ = tagCompound.getDouble("destinationZ");
    origin = new Vec3(originX, originY, originZ);
    destination = new Vec3(destinationX, destinationY, destinationZ);
  }

  /**
   * Called by the server when constructing the spawn packet.
   * Data should be added to the provided stream.
   *
   * @param buffer The packet data stream
   */
  @Override
  public void writeSpawnData(ByteBuf buffer)
  {
    buffer.writeInt(power.ordinal());
    buffer.writeFloat((float)accelerationX);
    buffer.writeFloat((float)accelerationY);
    buffer.writeFloat((float)accelerationZ);
    buffer.writeDouble(origin.xCoord);
    buffer.writeDouble(origin.yCoord);
    buffer.writeDouble(origin.zCoord);
    buffer.writeDouble(destination.xCoord);
    buffer.writeDouble(destination.yCoord);
    buffer.writeDouble(destination.zCoord);
    buffer.writeInt(parentDragon.getEntityId());
  }

  /**
   * Called by the client when it receives a Entity spawn packet.
   * Data should be read out of the stream in the same way as it was written.
   *
   * @param additionalData The packet data stream
   */
  @Override
  public void readSpawnData(ByteBuf additionalData)
  {
    int powerOrdinal = additionalData.readInt();
    accelerationX = additionalData.readFloat();
    accelerationY = additionalData.readFloat();
    accelerationZ = additionalData.readFloat();

    if (powerOrdinal >= 0 && powerOrdinal < BreathNode.Power.values().length) {
      this.power = BreathNode.Power.values()[powerOrdinal];
//      System.err.println("NBT power readSpawnData:"+ power); // todo remove
    }
    this.setSizeFromPower(power);

    double originX = additionalData.readDouble();
    double originY = additionalData.readDouble();
    double originZ = additionalData.readDouble();
    double destinationX = additionalData.readDouble();
    double destinationY = additionalData.readDouble();
    double destinationZ = additionalData.readDouble();
    origin = new Vec3(originX, originY, originZ);
    destination = new Vec3(destinationX, destinationY, destinationZ);

    parentDragonID = additionalData.readInt();
  }

  /**
   * Returns true if other Entities should be prevented from moving through this Entity.
   */
  public boolean canBeCollidedWith() {
    return true;
  }

  public float getCollisionBorderSize() {
    return 1.0F;
  }

  /**
   * Called when the entity is attacked.
   */
  public boolean attackEntityFrom(DamageSource source, float amount) {
    return false;
//      if (this.isEntityInvulnerable(source))
//      {
//        return false;
//      }
//      else
//      {
//        this.setBeenAttacked();
//
//        if (source.getEntity() != null)
//        {
//          Vec3 vec3 = source.getEntity().getLookVec();
//
//          if (vec3 != null)
//          {
//            this.motionX = vec3.xCoord;
//            this.motionY = vec3.yCoord;
//            this.motionZ = vec3.zCoord;
//            this.accelerationX = this.motionX * 0.1D;
//            this.accelerationY = this.motionY * 0.1D;
//            this.accelerationZ = this.motionZ * 0.1D;
//          }
//
//          if (source.getEntity() instanceof EntityLivingBase)
//          {
//            this.shootingEntity = (EntityLivingBase)source.getEntity();
//          }
//
//          return true;
//        }
//        else
//        {
//          return false;
//        }
//      }
  }

  /**
   * Returns the dragon which spawned this projectile
   * @return the dragon, or null if invalid or doesn't exist.
   */
  public EntityTameableDragon getParentDragon()
  {
    if (parentDragon != null) return parentDragon;
    if (parentDragonID == null) throw new IllegalStateException("parentDragonID not initialised yet");
    Entity entityFromID = this.worldObj.getEntityByID(parentDragonID);
    if (entityFromID instanceof EntityTameableDragon) {
      parentDragon = (EntityTameableDragon)entityFromID;
    }
    return parentDragon;
  }

  /**
   * Gets how bright this entity is.
   */
  public float getBrightness(float p_70013_1_) {
    return 1.0F;
  }

  @SideOnly(Side.CLIENT)
  public int getBrightnessForRender(float p_70070_1_) {
    return 15728880;
  }

  protected BreathNode.Power power;
  protected int ticksToLive;
  private EntityTameableDragon parentDragon;
  private Integer parentDragonID = null;

}
