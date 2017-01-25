package info.ata4.minecraft.dragon.util.plants;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Random;

/**
* Created by TGG on 3/01/2016.
*/ // copied from WorldGenDoublePlant
public class PlantDoublePlant extends Plant {
  public PlantDoublePlant(BlockDoublePlant.EnumPlantType enumPlantType) {
    plantType = enumPlantType;
  }

  @Override
  public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
    boolean success = false;
    if (world.isAirBlock(blockPos) && Blocks.DOUBLE_PLANT.canPlaceBlockAt(world, blockPos)) {
      Blocks.DOUBLE_PLANT.placeAt(world, blockPos, plantType, SET_BLOCKSTATE_FLAG);
      success = true;
    }
    return success;
  }

  @Override
  public void grow(World world, BlockPos blockPos, float growthAmount)
  {
    // do nothing....
  }

  public static class DoublePlantFactory extends PlantFactory
  {
    public Plant getPlantFromBlockState(IBlockState iBlockState)
    {
      BlockDoublePlant.EnumPlantType enumPlantType = (BlockDoublePlant.EnumPlantType)iBlockState.getValue(
              BlockDoublePlant.VARIANT);
      return new PlantDoublePlant(enumPlantType);
    }

    @Override
    public Collection<Block> getBlocksUsedByThisPlant()
    {
      return ImmutableList.of((Block) Blocks.DOUBLE_PLANT);
    }
  }

  private BlockDoublePlant.EnumPlantType plantType;
}
