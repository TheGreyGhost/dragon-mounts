package info.ata4.minecraft.dragon.util.plants;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockReed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;

/**
* Created by TGG on 3/01/2016.
*/ // copied from WorldGenReed
public class ReedsPlant extends Plant {
  @Override
  public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
    boolean success = false;
    BlockPos groundPos = blockPos.down();

    if (world.getBlockState(groundPos.west()).getMaterial() == Material.WATER
            || world.getBlockState(groundPos.east()).getMaterial() == Material.WATER
            || world.getBlockState(groundPos.north()).getMaterial() == Material.WATER
            || world.getBlockState(groundPos.south()).getMaterial() == Material.WATER) {
      int reedHeight = 2 + random.nextInt(random.nextInt(3) + 1);

      for (int k = 0; k < reedHeight; ++k) {
        if (Blocks.REEDS.canBlockStay(world, blockPos)) {
          world.setBlockState(blockPos.up(k), Blocks.REEDS.getDefaultState(), SET_BLOCKSTATE_FLAG);
          success = true;
        }
      }
    }
    return success;
  }

  @Override
  public void grow(World world, BlockPos blockPos, float growthAmount)
  {
    checkArgument(growthAmount >= 0);
    IBlockState iBlockState = world.getBlockState(blockPos);
    if (iBlockState.getBlock() != Blocks.REEDS) return;

    final int MAX_REED_AGE = 15;
    int currentAge = ((Integer)iBlockState.getValue(BlockReed.AGE)).intValue();
    currentAge += MAX_REED_AGE * growthAmount;
    currentAge = Math.min(currentAge, MAX_REED_AGE);
    BlockPos topBlock = findTopReedBlock(world, blockPos);

    world.setBlockState(topBlock, iBlockState.withProperty(BlockReed.AGE, Integer.valueOf(currentAge)), SET_BLOCKSTATE_FLAG);

  }

  /**
   * finds the topmost block of the reeds.  Assumes that the starting position is a reeds block!!
   * @param world
   * @param startBlockPos
   * @return
   */
  private BlockPos findTopReedBlock(World world, BlockPos startBlockPos)
  {
    BlockPos nextPos = startBlockPos;
    do {
      nextPos = nextPos.up();
    } while (world.getBlockState(nextPos).getBlock() == Blocks.REEDS);
    return nextPos.down();
  }


  public static class ReedsPlantFactory extends PlantFactory
  {
    public Plant getPlantFromBlockState(IBlockState iBlockState)
    {
      if (iBlockState == null || iBlockState.getBlock() != Blocks.REEDS) return null;

      return new ReedsPlant();
    }

    public Collection<Block> getBlocksUsedByThisPlant() {
      return ImmutableList.of((Block) Blocks.REEDS);
    }
  }


}
