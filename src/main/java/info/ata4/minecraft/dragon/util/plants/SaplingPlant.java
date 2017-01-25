package info.ata4.minecraft.dragon.util.plants;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Random;

/**
* Created by TGG on 3/01/2016.
*/ // copied from WorldGenSapling
public class SaplingPlant extends Plant {

  public SaplingPlant(BlockPlanks.EnumType enumSaplingType) {
    BlockSapling blockSapling = (BlockSapling) Blocks.SAPLING;
    saplingBlockState = blockSapling.getDefaultState().withProperty(BlockSapling.TYPE, enumSaplingType);
  }


  @Override
  public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
    boolean success = false;
    if (world.isAirBlock(blockPos)) {
      BlockSapling blockSapling = (BlockSapling)saplingBlockState.getBlock();
      if (blockSapling.canBlockStay(world, blockPos, saplingBlockState)) {
          world.setBlockState(blockPos, saplingBlockState, SET_BLOCKSTATE_FLAG);
          success = true;
      }
    }
    return success;
  }

  @Override
  public void grow(World world, BlockPos blockPos, float growthAmount)
  {
    BlockSapling blockSapling = (BlockSapling)saplingBlockState.getBlock();
    Random random = new Random();
    IBlockState primedSapling = saplingBlockState.withProperty(BlockSapling.STAGE, 1);
    blockSapling.grow(world, blockPos, primedSapling, random);
  }

  public static class SaplingPlantFactory extends PlantFactory
  {
    public Plant getPlantFromBlockState(IBlockState iBlockState)
    {
      if (iBlockState == null || iBlockState.getBlock() != Blocks.SAPLING) return null;

      return new SaplingPlant(iBlockState);
    }

    public Collection<Block> getBlocksUsedByThisPlant() {
      return ImmutableList.of((Block) Blocks.SAPLING);
    }
  }

  private SaplingPlant(IBlockState i_iBlockState)
  {
    saplingBlockState = i_iBlockState;
  }

  private IBlockState saplingBlockState;

//  @Override
//  public boolean tryClonePlant(World world, BlockPos sourcePlantPos, BlockPos destinationPlantPos, Random random) {
//    IBlockState iBlockState = world.getBlockState(sourcePlantPos);
//    if (iBlockState.getBlock() == Blocks.cactus) {
//      return trySpawnNewPlant(world, destinationPlantPos, random);
//    }
//    return false;
//  }
}
