/*
 ** 2014 January 31
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.breeds;

import info.ata4.minecraft.dragon.client.render.BreathWeaponFXEmitter;
import info.ata4.minecraft.dragon.client.render.BreathWeaponFXEmitterForest;
import info.ata4.minecraft.dragon.client.sound.SoundController;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeapon;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeaponForest;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeaponIce;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.breath.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlower.EnumFlowerType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedForest extends DragonBreed {
	
    private static final int GRASS_LIGHT_THRESHOLD = 4;

  public DragonBreedForest() {
        super("forest", 0x2d6e00);
        
        addHabitatBlock(Blocks.LOG);
        addHabitatBlock(Blocks.LOG2);
        addHabitatBlock(Blocks.LEAVES);
        addHabitatBlock(Blocks.LEAVES2);
        addHabitatBlock(Blocks.YELLOW_FLOWER);
        addHabitatBlock(Blocks.RED_FLOWER);
        addHabitatBlock(Blocks.MOSSY_COBBLESTONE);
        addHabitatBlock(Blocks.VINE);
        addHabitatBlock(Blocks.SAPLING);
        
        addHabitatBiome(Biomes.FOREST);
        addHabitatBiome(Biomes.FOREST_HILLS);
        addHabitatBiome(Biomes.JUNGLE);
        addHabitatBiome(Biomes.JUNGLE_HILLS);
    }
    
    @Override
    protected void placeFootprintBlock(EntityTameableDragon dragon, BlockPos blockPos) {
        World world = dragon.worldObj;
        
        // grow mushrooms and plants
        BlockPos blockPosGround = blockPos.down();
        BlockPos blockPosSurface = blockPos;
        
        IBlockState blockStateUnderFoot = world.getBlockState(blockPosGround);
        Block blockUnderFoot = blockStateUnderFoot.getBlock();
        
        boolean plantFlower = false;
        boolean plantMushroom = false;
        
        if (blockUnderFoot == Blocks.GRASS) {
            // plant flowers on grass
            plantFlower = true;
        } else if (blockUnderFoot == Blocks.DIRT) {
            DirtType dirtType = blockStateUnderFoot.getValue(BlockDirt.VARIANT);
            if (dirtType != DirtType.DIRT) {
                // plant mushrooms on special dirt types
                plantMushroom = true;
            } else if (world.getLightFromNeighbors(blockPosSurface) >= GRASS_LIGHT_THRESHOLD) {
                // turn normal dirt green if there's enough light
                world.setBlockState(blockPosGround, Blocks.GRASS.getDefaultState());
                // also add flowers randomly
                plantFlower = world.rand.nextBoolean();
            }
        } else if (blockUnderFoot == Blocks.MYCELIUM) {
            // always plant mushrooms on mycelium
            plantMushroom = true;
        }
        
        // pick plant
        BlockBush blockPlant = null;
        IBlockState statePlant = null;
        
        if (plantFlower) {
            EnumFlowerType flower = world.getBiome(blockPosSurface)
                    .pickRandomFlower(world.rand, blockPosSurface);
            BlockFlower blockFlower = flower.getBlockType().getBlock();
            
            blockPlant = blockFlower;
            statePlant = blockFlower.getDefaultState()
                    .withProperty(blockFlower.getTypeProperty(), flower);
        }
        
        if (plantMushroom) {
            blockPlant = (world.rand.nextBoolean() ?
                    Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM);
            statePlant = blockPlant.getDefaultState();
        }
        
        // place plant if defined        
        if (blockPlant != null && blockPlant.canBlockStay(world, blockPosSurface, statePlant)) {
            world.setBlockState(blockPosSurface, statePlant);
        }
    }

    @Override
    public void onEnable(EntityTameableDragon dragon) {
    }

    @Override
    public void onDisable(EntityTameableDragon dragon) {
    }

  @Override
  public BreathWeaponFXEmitter getBreathWeaponFXEmitter(EntityTameableDragon dragon)
  {
    return new BreathWeaponFXEmitterForest();
  }

  @Override
  public BreathWeapon getBreathWeapon(EntityTameableDragon dragon)
  {
    return new BreathWeaponForest(dragon);
  }

  // breathmode != 0 --> the breath has been ignited

  @Override
  public BreathWeaponSpawnType getBreathWeaponSpawnType(EntityTameableDragon dragon)
  {
    return BreathWeaponSpawnType.NODES;
  }


  @Override
  public BreathNodeFactory getBreathNodeFactory(EntityTameableDragon dragon)
  {
    return new BreathNodeForest.BreathNodeForestFactory();
  }
  @Override
  public SoundEffectBreathWeapon getSoundEffectBreathWeapon(SoundController i_soundController,
                                                            SoundEffectBreathWeapon.WeaponSoundUpdateLink i_weaponSoundUpdateLink) {
    return new SoundEffectBreathWeaponForest(i_soundController, i_weaponSoundUpdateLink);
  }

    @Override
    public void onDeath(EntityTameableDragon dragon) {
    }
}

