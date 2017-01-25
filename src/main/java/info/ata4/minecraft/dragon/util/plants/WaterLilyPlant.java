package info.ata4.minecraft.dragon.util.plants;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Random;

/**
* Created by TGG on 3/01/2016.
*/ // copied from WorldGenWaterLily
public class WaterLilyPlant extends Plant {
  @Override
  public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
    boolean success = false;
    if (world.isAirBlock(blockPos) && Blocks.WATERLILY.canPlaceBlockAt(world, blockPos)) {
      world.setBlockState(blockPos, Blocks.WATERLILY.getDefaultState(), SET_BLOCKSTATE_FLAG);
      success = true;
    }
    return success;
  }

  @Override
  public void grow(World world, BlockPos blockPos, float growthAmount)
  {
      // do nothing
  }

  public static class WaterLilyPlantFactory extends PlantFactory
  {
    public Plant getPlantFromBlockState(IBlockState iBlockState)
    {
      if (iBlockState == null || iBlockState.getBlock() != Blocks.WATERLILY) return null;

      return new WaterLilyPlant();
    }

    public Collection<Block> getBlocksUsedByThisPlant() {
      return ImmutableList.of((Block) Blocks.WATERLILY);
    }
  }
}
