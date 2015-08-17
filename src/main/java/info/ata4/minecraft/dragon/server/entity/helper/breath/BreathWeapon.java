package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;

import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by TGG on 5/08/2015.
 *
 * Models a breathweapon for the dragon
 * Currently does fire only
 */
public class BreathWeapon
{
  /** if the hitDensity is high enough, manipulate the block (eg set fire to it)
   * @param world
   * @param blockPosition  the world [x,y,z] of the block
   * @param currentHitDensity
   * @return the updated block hit density
   */
  public BreathAffectedBlock affectBlock(World world, Vec3i blockPosition,
                                                     BreathAffectedBlock currentHitDensity)
  {
    checkNotNull(world);
    checkNotNull(blockPosition);
    checkNotNull(currentHitDensity);

    BlockPos blockPos = new BlockPos(blockPosition);
    IBlockState iBlockState = world.getBlockState(blockPos);
    Block block = iBlockState.getBlock();

    Random rand = new Random();

    // Flammable blocks: set fire to them once they have been exposed enough
    // Non-flammable blocks:
    // 1)


    for (EnumFacing facing : EnumFacing.values()) {
      BlockPos sideToIgnite = blockPos.offset(facing);
      if (block.isFlammable(world, sideToIgnite, facing)) {
        int flammability = block.getFlammability(world, sideToIgnite, facing);
        float thresholdHitDensity = convertFlammabilityToHitDensityThreshold(flammability);
        System.out.println("Threshold: " + thresholdHitDensity
                + ", current:" + facing + "=" + currentHitDensity.getHitDensity(facing));
        float densityOfThisFace = currentHitDensity.getHitDensity(facing);
        if (densityOfThisFace >= thresholdHitDensity && world.isAirBlock(sideToIgnite)) {
          final float MIN_PITCH = 0.8F;
          final float MAX_PITCH = 1.2F;
          final float VOLUME = 1.0F;
          world.playSoundEffect(sideToIgnite.getX() + 0.5, sideToIgnite.getY() + 0.5, sideToIgnite.getZ() + 0.5,
                  "fire.ignite", VOLUME, MIN_PITCH + rand.nextFloat() * (MAX_PITCH - MIN_PITCH));
          world.setBlockState(sideToIgnite, Blocks.fire.getDefaultState());
        }
      }
    }
    return currentHitDensity;
//    FurnaceRecipes.instance().getSmeltingResult(block.getItemDropped())
//    // non-silk harvest
//    Item item = this.getItemDropped(state, rand, fortune);
//    if (item != null)
//    {
//      ret.add(new ItemStack(item, 1, this.damageDropped(state)));
//    }
//
//    // silk harvest:
//    Item item = Item.getItemFromBlock(this);
//
//    if (item != null && item.getHasSubtypes())
//    {
//      i = this.getMetaFromState(state);
//    }
//
//    return new ItemStack(item, 1, i);
//


  }

  /** if the hitDensity is high enough, manipulate the block (eg set fire to it)
   * @param world
   * @param entityID  the world [x,y,z] of the block
   * @param currentHitDensity
   * @return the updated hit density; null if entity dead or doesn't exist
   */
  public BreathAffectedEntity affectEntity(World world, Integer entityID, BreathAffectedEntity currentHitDensity)
  {
    checkNotNull(world);
    checkNotNull(entityID);
    checkNotNull(currentHitDensity);

    Entity entity = world.getEntityByID(entityID);
    if (entity == null || !(entity instanceof EntityLivingBase) || entity.isDead) {
      return null;
    }

//    System.out.println("Burn " + entity.getName() + ":" + currentHitDensity);
    return currentHitDensity;
  }

  /**
   * returns the hitDensity threshold for the given block flammability (0 - 300 as per Block.getFlammability)
   * @param flammability
   * @return the hit density threshold above which the block catches fire
   */
  private float convertFlammabilityToHitDensityThreshold(int flammability)
  {
    checkArgument(flammability >= 0 && flammability <= 300);
    if (flammability == 0) return Float.MAX_VALUE;
    // typical values for items are 5 (coal, logs), 20 (gates etc), 60 - 100 for leaves & flowers & grass
    // want: leaves & flowers to burn instantly; gates to take ~1 second at full power, coal / logs to take ~3 seconds
    // hitDensity of 1 is approximately 1-2 ticks of full exposure from a single beam, so 3 seconds is ~30

    float threshold = 50.0F / flammability;
    return threshold;
  }

}