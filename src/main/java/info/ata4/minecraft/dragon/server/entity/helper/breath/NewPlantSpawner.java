package info.ata4.minecraft.dragon.server.entity.helper.breath;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.*;

/**
 * Created by TGG on 3/01/2016.
 */
public interface NewPlantSpawner {
  /**
   * Attempt to spawn the plant at the given block location.
   * 1) checks if this is a suitable location for the plant
   * 2) if so, generate it
   *
   * @param world
   * @param blockPos the position where the base of the plant will spawn.  eg for dirt - one above the dirt block
   * @param random
   * @return true if a plant was spawned, false otherwise
   */
  boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random);

  /**
   * Which plants are handled by this spawner?
   *
   * @return a collection of all the plants this spawner can produce
   */
  Collection<Block> getSpawnablePlants();

  /**
   * Try to clone the plant at sourcePlantPos to the destinationPlantPos
   * Doesn't work if this spawner isn't the right type.  (Fails without error)
   *
   * @param world
   * @param sourcePlantPos
   * @param destinationPlantPos
   * @return success if spawned
   */
  boolean tryClonePlant(World world, BlockPos sourcePlantPos, BlockPos destinationPlantPos, Random random);

  // copied from WorldGenCactus
  public static class CactusSpawner implements NewPlantSpawner {
    @Override
    public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
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

    @Override
    public Collection<Block> getSpawnablePlants() {
      return ImmutableList.of((Block) Blocks.cactus);
    }

    @Override
    public boolean tryClonePlant(World world, BlockPos sourcePlantPos, BlockPos destinationPlantPos, Random random) {
      IBlockState iBlockState = world.getBlockState(sourcePlantPos);
      if (iBlockState.getBlock() == Blocks.cactus) {
        return trySpawnNewPlant(world, destinationPlantPos, random);
      }
      return false;
    }
  }

  // copied from WorldGenReed
  public static class ReedSpawner implements NewPlantSpawner {
    @Override
    public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
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

    @Override
    public Collection<Block> getSpawnablePlants() {
      return ImmutableList.of((Block) Blocks.reeds);
    }

    @Override
    public boolean tryClonePlant(World world, BlockPos sourcePlantPos, BlockPos destinationPlantPos, Random random) {
      IBlockState iBlockState = world.getBlockState(sourcePlantPos);
      if (iBlockState.getBlock() == Blocks.reeds) {
        return trySpawnNewPlant(world, destinationPlantPos, random);
      }
      return false;
    }
  }

  // copied from WorldGenVines
  public static class VinesSpawner implements NewPlantSpawner {
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

    @Override
    public Collection<Block> getSpawnablePlants() {
      return ImmutableList.of((Block) Blocks.vine);
    }

    @Override
    public boolean tryClonePlant(World world, BlockPos sourcePlantPos, BlockPos destinationPlantPos, Random random) {
      IBlockState iBlockState = world.getBlockState(sourcePlantPos);
      if (iBlockState.getBlock() == Blocks.vine) {
        return trySpawnNewPlant(world, destinationPlantPos, random);
      }
      return false;
    }
  }


  // copied from WorldGenWaterLily
  public static class WaterLilySpawner implements NewPlantSpawner {
    @Override
    public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
      boolean success = false;
      if (world.isAirBlock(blockPos) && Blocks.waterlily.canPlaceBlockAt(world, blockPos)) {
        world.setBlockState(blockPos, Blocks.waterlily.getDefaultState(), SET_BLOCKSTATE_FLAG);
        success = true;
      }
      return success;
    }

    @Override
    public Collection<Block> getSpawnablePlants() {
      return ImmutableList.of((Block) Blocks.waterlily);
    }

    @Override
    public boolean tryClonePlant(World world, BlockPos sourcePlantPos, BlockPos destinationPlantPos, Random random) {
      IBlockState iBlockState = world.getBlockState(sourcePlantPos);
      if (iBlockState.getBlock() == Blocks.waterlily) {
        return trySpawnNewPlant(world, destinationPlantPos, random);
      }
      return false;
    }
  }

  // copied from WorldGenFlowers
  public static class FlowersSpawner implements NewPlantSpawner {
    public FlowersSpawner(BlockFlower.EnumFlowerType enumFlowerType) {
      BlockFlower flowerBlock = enumFlowerType.getBlockType().getBlock();
      flowerToSpawn = flowerBlock.getDefaultState().withProperty(flowerBlock.getTypeProperty(), enumFlowerType);
    }

    @Override
    public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
      return trySpawnNewPlant(world, blockPos, flowerToSpawn);
    }

    private boolean trySpawnNewPlant(World world, BlockPos blockPos, IBlockState whichFlowerToPlace) {
      boolean success = false;
      if (world.isAirBlock(blockPos) && Blocks.yellow_flower.canBlockStay(world, blockPos, whichFlowerToPlace)) {
        world.setBlockState(blockPos, whichFlowerToPlace, SET_BLOCKSTATE_FLAG);
        success = true;
      }
      return success;
    }

    @Override
    public Collection<Block> getSpawnablePlants() {
      Set<Block> allFlowerBlocks = new HashSet<Block>();
      for (BlockFlower.EnumFlowerType flowerType : BlockFlower.EnumFlowerType.values()) {
        allFlowerBlocks.add(flowerType.getBlockType().getBlock());
      }
      return allFlowerBlocks;
    }

    @Override
    public boolean tryClonePlant(World world, BlockPos sourcePlantPos, BlockPos destinationPlantPos, Random random) {
      IBlockState sourceFlower = world.getBlockState(sourcePlantPos);
      if (sourceFlower.getBlock() instanceof BlockFlower) {
        return trySpawnNewPlant(world, destinationPlantPos, sourceFlower);
      }
      return false;
    }

    private FlowersSpawner() {
    }

    //    private BlockFlower flowerBlock;
    private IBlockState flowerToSpawn;
  }

  // copied from WorldGenDoublePlant
  public static class DoublePlantSpawner implements NewPlantSpawner {
    public DoublePlantSpawner(BlockDoublePlant.EnumPlantType enumPlantType) {
      plantType = enumPlantType;
    }

    @Override
    public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
      return trySpawnNewPlant(world, blockPos, plantType);
    }

    @Override
    public Collection<Block> getSpawnablePlants() {
      return ImmutableList.of((Block) Blocks.double_plant);
    }

    @Override
    public boolean tryClonePlant(World world, BlockPos sourcePlantPos, BlockPos destinationPlantPos, Random random) {
      IBlockState sourcePlant = world.getBlockState(sourcePlantPos);
      if (sourcePlant.getBlock() instanceof BlockDoublePlant) {
        BlockDoublePlant.EnumPlantType plantTypeToClone = Blocks.double_plant.getVariant(world, sourcePlantPos);
        return trySpawnNewPlant(world, destinationPlantPos, plantTypeToClone);
      }
      return false;
    }

    private boolean trySpawnNewPlant(World world, BlockPos blockPos, BlockDoublePlant.EnumPlantType plantToPlace) {
      boolean success = false;
      if (world.isAirBlock(blockPos) && Blocks.double_plant.canPlaceBlockAt(world, blockPos)) {
        Blocks.double_plant.placeAt(world, blockPos, plantType, SET_BLOCKSTATE_FLAG);
        success = true;
      }
      return success;
    }

    private BlockDoublePlant.EnumPlantType plantType;
  }

  // copied from WorldGenTallGrass
  public static class TallGrassSpawner implements NewPlantSpawner {
    public TallGrassSpawner(BlockTallGrass.EnumType enumPlantType) {
      grassToPlace = Blocks.tallgrass.getDefaultState().withProperty(BlockTallGrass.TYPE, enumPlantType);
    }

    @Override
    public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
      return trySpawnNewPlant(world, blockPos, grassToPlace);
    }

    @Override
    public Collection<Block> getSpawnablePlants() {
      return ImmutableList.of((Block) Blocks.tallgrass);
    }

    @Override
    public boolean tryClonePlant(World world, BlockPos sourcePlantPos, BlockPos destinationPlantPos, Random random) {
      IBlockState sourcePlant = world.getBlockState(sourcePlantPos);
      if (sourcePlant.getBlock() == Blocks.tallgrass) {
        return trySpawnNewPlant(world, destinationPlantPos, sourcePlant);
      }
      return false;
    }

    private TallGrassSpawner() {
    }

    private boolean trySpawnNewPlant(World world, BlockPos blockPos, IBlockState grassIBlockState) {
      boolean success = false;
      if (world.isAirBlock(blockPos) && Blocks.tallgrass.canBlockStay(world, blockPos, grassIBlockState)) {
        world.setBlockState(blockPos, grassIBlockState, 2);
        success = true;
      }
      return success;
    }

    private IBlockState grassToPlace;
  }

  public static class NothingSpawner implements NewPlantSpawner {
    @Override
    public boolean trySpawnNewPlant(World world, BlockPos blockPos, Random random) {
      boolean success = true;
      return success;
    }

    @Override
    public Collection<Block> getSpawnablePlants() {
      return Collections.emptyList();
    }

    @Override
    public boolean tryClonePlant(World world, BlockPos sourcePlantPos, BlockPos destinationPlantPos, Random random) {
      return false;
    }

  }

  final static int SET_BLOCKSTATE_FLAG = 3;  // update flag setting to use for setBlockState
  final static Collection<NewPlantSpawner> allSpawners = ImmutableList.of(aa)
}
