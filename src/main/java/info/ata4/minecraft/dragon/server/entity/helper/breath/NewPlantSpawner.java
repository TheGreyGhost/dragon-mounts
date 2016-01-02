package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by TGG on 3/01/2016.
 */
public interface NewPlantSpawner {
  /**
   * Attempt to spawn the plant at the given block location.
   * 1) checks if this is a suitable location for the plant
   * 2) if so, generate it
   * @param world
   * @param blockPos the position where the base of the plant will spawn.  eg for dirt - one above the dirt block
   * @param random
   * @return true if a plant was spawned, false otherwise
   */
  boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random);

  // copied from WorldGenCactus
  public class CactusSpawner implements NewPlantSpawner
  {
    @Override
    public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random)
    {
      boolean success = false;
      if (world.isAirBlock(blockPos)) {
        int cactusHeight = 1 + random.nextInt(random.nextInt(3) + 1);

        for (int k = 0; k < cactusHeight; ++k) {
          if (Blocks.cactus.canBlockStay(world, blockPos)) {
            world.setBlockState(blockPos.up(k), Blocks.cactus.getDefaultState(), SET_BLOCKSTATE_FLAG);
            success = true;
          }
        }
      }
      return success;
    }
  }

  // copied from WorldGenReed
  public class ReedSpawner implements NewPlantSpawner
  {
    @Override
    public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random)
    {
      boolean success = false;
      BlockPos groundPos = blockPos.down();

      if (world.getBlockState(groundPos.west()).getBlock().getMaterial() == Material.water
              || world.getBlockState(groundPos.east()).getBlock().getMaterial() == Material.water
              || world.getBlockState(groundPos.north()).getBlock().getMaterial() == Material.water
              || world.getBlockState(groundPos.south()).getBlock().getMaterial() == Material.water) {
        int reedHeight = 2 + random.nextInt(random.nextInt(3) + 1);

        for (int k = 0; k < reedHeight; ++k) {
          if (Blocks.reeds.canBlockStay(world, blockPos)) {
            world.setBlockState(blockPos.up(k), Blocks.reeds.getDefaultState(), SET_BLOCKSTATE_FLAG);
            success = true;
          }
        }
      }
      return success;
    }

    // copied from WorldGenVines
    public class VinesSpawner implements NewPlantSpawner {
      @Override
      public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
        boolean success = false;
        IBlockState vineToPlace = Blocks.vine.getDefaultState();
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL.facings()) {
          if (Blocks.vine.canPlaceBlockOnSide(world, blockPos, facing)) {
            success = true;
            switch (facing) {
              case NORTH: {
                vineToPlace = vineToPlace.withProperty(BlockVine.NORTH, true);
                break;
              }
              case SOUTH: {
                vineToPlace = vineToPlace.withProperty(BlockVine.SOUTH, true);
                break;
              }
              case EAST: {
                vineToPlace = vineToPlace.withProperty(BlockVine.EAST, true);
                break;
              }
              case WEST: {
                vineToPlace = vineToPlace.withProperty(BlockVine.WEST, true);
                break;
              }
              default: {
                assert false : "invalid facing:" + facing;
              }
            }
          }
        }
        if (success) {
          world.setBlockState(blockPos, vineToPlace, SET_BLOCKSTATE_FLAG);
        }
        return success;
      }
    }
  }

  final static int SET_BLOCKSTATE_FLAG = 3;  // update flag setting to use for setBlockState
}
