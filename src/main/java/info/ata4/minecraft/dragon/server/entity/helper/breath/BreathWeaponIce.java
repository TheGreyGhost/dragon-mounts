package info.ata4.minecraft.dragon.server.entity.helper.breath;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.util.ItemUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by TGG on 7/12/2015.
 * TOD CHANGE TO ICE LATER
 */
public class BreathWeaponIce extends BreathWeapon
{
  public BreathWeaponIce(EntityTameableDragon i_dragon)
  {
    super(i_dragon);
    initialiseStatics();
  }

  @Override
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

    // effects- which occur after the block has been exposed for sufficient time
    // hard blocks such as stone, glass shatter
    // leaves, grass, flowers, plants, etc shatter (destroyed)
    // freeze water to ice - freeze deeper once a higher threshold is reached
    // lava turns to obsidian
    // extinguish fire and torches
    // coat blocks with snow.  Gradually builds up and will coat everything


    if (block == null) return currentHitDensity;
    Material material = block.getMaterial();
    if (material == null) return currentHitDensity;

    if (materialShatterTime.containsKey(material)) {
      Integer shatterTime = materialShatterTime.get(material);
      if (shatterTime != null
              && currentHitDensity.getMaxHitDensity() > shatterTime) {
        final boolean DROP_BLOCK = true;
        world.destroyBlock(blockPos, DROP_BLOCK);
        return new BreathAffectedBlock();
      }
      return currentHitDensity;
    }


    if (material == Material.water) {
      final float THRESHOLD_WATER_FREEZE = 10;
      if (currentHitDensity.getMaxHitDensity() > THRESHOLD_WATER_FREEZE) {
        world.setBlockState(blockPos, Blocks.packed_ice.getDefaultState());
        return new BreathAffectedBlock();
      }
      return currentHitDensity;
    }

    if (material == Material.lava) {
      final float THRESHOLD_LAVA_FREEZE = 10;
      if (currentHitDensity.getMaxHitDensity() > THRESHOLD_LAVA_FREEZE) {
        quenchLava(world, blockPos);
        return new BreathAffectedBlock();
      }
      return currentHitDensity;
    }

    if (material == Material.fire) {
      final float THRESHOLD_FIRE_EXTINGUISH = 1;
      if (currentHitDensity.getMaxHitDensity() > THRESHOLD_FIRE_EXTINGUISH) {
        extinguishFire(world, blockPos);
        return new BreathAffectedBlock();
      }
      return currentHitDensity;
    }

    if (material == Material.snow || material == Material.craftedSnow) {
      return thickenSnow(world, block, blockPos, currentHitDensity);
    }

    if (material == Material.ice || material == Material.packedIce) {
      return iceFreeze(world, block, blockPos, currentHitDensity);
    }

    if (block == Blocks.torch) {
      final float THRESHOLD_FIRE_EXTINGUISH = 1;
      if (currentHitDensity.getMaxHitDensity() > THRESHOLD_FIRE_EXTINGUISH) {
        final boolean DROP_BLOCK = true;
        world.destroyBlock(blockPos, DROP_BLOCK);
        return new BreathAffectedBlock();
      }
      return currentHitDensity;
    }

    if (material == Material.air) {
      final int THRESHOLD_DEEPEN_SNOW_LAYER = 1;
      if (currentHitDensity.getMaxHitDensity() < THRESHOLD_DEEPEN_SNOW_LAYER
          || !Blocks.snow_layer.canPlaceBlockAt(world, blockPos)) {
        return currentHitDensity;
      }
      world.setBlockState(blockPos, Blocks.snow_layer.getDefaultState());
    }

    return currentHitDensity;

//    // Flammable blocks: set fire to them once they have been exposed enough.  After sufficient exposure, destroy the
//    //   block (otherwise -if it's raining, the burning block will keep going out)
//    // Non-flammable blocks:
//    // 1) liquids (except lava) evaporate
//    // 2) If the block can be smelted (eg sand), then convert the block to the smelted version
//    // 3) If the block can't be smelted then convert to lava
//
//    for (EnumFacing facing : EnumFacing.values()) {
//      BlockPos sideToIgnite = blockPos.offset(facing);
//      if (block.isFlammable(world, sideToIgnite, facing)) {
//        int flammability = block.getFlammability(world, sideToIgnite, facing);
//        float thresholdForIgnition = convertFlammabilityToHitDensityThreshold(flammability);
//        float thresholdForDestruction = thresholdForIgnition * 10;
//        float densityOfThisFace = currentHitDensity.getHitDensity(facing);
//        if (densityOfThisFace >= thresholdForIgnition && world.isAirBlock(sideToIgnite)) {
//          final float MIN_PITCH = 0.8F;
//          final float MAX_PITCH = 1.2F;
//          final float VOLUME = 1.0F;
//          world.playSoundEffect(sideToIgnite.getX() + 0.5, sideToIgnite.getY() + 0.5, sideToIgnite.getZ() + 0.5,
//                  "fire.ignite", VOLUME, MIN_PITCH + rand.nextFloat() * (MAX_PITCH - MIN_PITCH));
//          world.setBlockState(sideToIgnite, Blocks.fire.getDefaultState());
//        }
//        if (densityOfThisFace >= thresholdForDestruction) {
//          world.setBlockToAir(blockPos);
//        }
//      }
//    }
//
//    BlockBurnProperties burnProperties = getBurnProperties(iBlockState);
//    if (burnProperties.burnResult == null
//        || currentHitDensity.getMaxHitDensity() < burnProperties.threshold) {
//      return currentHitDensity;
//    }
//    world.setBlockState(blockPos, burnProperties.burnResult);
//    return new BreathAffectedBlock();  // reset to zero
  }

  // as hit density increases, find adjacent water and freeze it too
  private BreathAffectedBlock iceFreeze(World world, Block block, BlockPos blockPos,
                                          BreathAffectedBlock breathAffectedBlock)
  {
    float previousHitDensity = breathAffectedBlock.getPreviousMaxHitDensity();
    float currentHitDensity = breathAffectedBlock.getMaxHitDensity();

    final float HIT_DENSITY_PER_BLOCK = 10;
    final int MAX_BLOCKS_IN_PATH = 5; // how far will the cold propagate?

    int previousBlocksInPath = MathHelper.floor_double(previousHitDensity / HIT_DENSITY_PER_BLOCK);
    int currentBlocksInPath = MathHelper.floor_double(currentHitDensity / HIT_DENSITY_PER_BLOCK);

    if (currentBlocksInPath == previousBlocksInPath) return breathAffectedBlock;
    if (currentBlocksInPath > MAX_BLOCKS_IN_PATH) {
      currentBlocksInPath = MAX_BLOCKS_IN_PATH;
    }

    floodFillWaterToIce(world, blockPos, currentBlocksInPath);
    return breathAffectedBlock;
  }

  /** flood fill from the starting block for the indicated number of blocks, turning water to packed ice
   * @param world
   * @param blockPos
   * @param maxPathLength maximum path length to flood fill to.
   */
  private void floodFillWaterToIce(World world, BlockPos blockPos, int maxPathLength)
  {
    HashSet<BlockPos> blocksToSearchFrom = new HashSet<BlockPos>();
    HashSet<BlockPos> blocksSearched = new HashSet<BlockPos>();

    blocksToSearchFrom.add(blockPos);
    for (int pathLength = 0; pathLength < maxPathLength; ++pathLength) {
      HashSet<BlockPos> blocksToSearchFromNext = new HashSet<BlockPos>();

      for (BlockPos whichBlock : blocksToSearchFrom) {
        for (EnumFacing whichDirection : EnumFacing.VALUES) {
          BlockPos adjacent = whichBlock.offset(whichDirection);
          if (!blocksSearched.contains(adjacent)) {
            blocksSearched.add(adjacent);

            IBlockState adjacentBlockState = world.getBlockState(adjacent);
            Material material = adjacentBlockState.getBlock().getMaterial();
            if (material == Material.water) {
              blocksToSearchFromNext.add(adjacent);
              world.setBlockState(adjacent, Blocks.packed_ice.getDefaultState());
            } else if (material == Material.ice || material == Material.packedIce) {
              blocksToSearchFromNext.add(adjacent);
            }
          }
        }
      }
    }
    return;
  }

  // If this is a snow block with layers, make layers thicker
  // if this is a full snow block, harden to ice - No, not any more - looks silly
  private BreathAffectedBlock thickenSnow(World world, Block block, BlockPos blockPos,
                                          BreathAffectedBlock currentHitDensity)
  {
    IBlockState currentBlockState = world.getBlockState(blockPos);
    if (block instanceof BlockSnow) {
      final int THRESHOLD_DEEPEN_SNOW_LAYER = 1;
      if (currentHitDensity.getMaxHitDensity() < THRESHOLD_DEEPEN_SNOW_LAYER) {
        return currentHitDensity;
      }

      IBlockState newBlockState;
      Integer layers = (Integer)currentBlockState.getValue(BlockSnow.LAYERS);
      final int MAX_LAYERS = 8; // copied from BlockSnow.LAYERS
      if (layers != null && layers < MAX_LAYERS) {
        Integer newLayerDepth = layers + 1;
        newBlockState = currentBlockState.withProperty(BlockSnow.LAYERS, newLayerDepth);
      } else {
        newBlockState = Blocks.snow.getDefaultState();
      }
      world.setBlockState(blockPos, newBlockState);
      return new BreathAffectedBlock();
    }

    final float THRESHOLD_SNOW_BECOMES_ICE = 10000; // arbitrarily high for now.  looks silly.
    if (currentHitDensity.getMaxHitDensity() < THRESHOLD_SNOW_BECOMES_ICE) {
      return currentHitDensity;
    }
    IBlockState newBlockState = Blocks.packed_ice.getDefaultState();
    world.setBlockState(blockPos, newBlockState);
    return new BreathAffectedBlock();

  }


  // quench a lava and turn it to
  // copy from BlockLiquid.checkForMixing()
  private void quenchLava(World world, BlockPos blockPos)
  {
    world.setBlockState(blockPos, Blocks.obsidian.getDefaultState());
    double wx = blockPos.getX();
    double wy = blockPos.getY();
    double wz = blockPos.getZ();
    world.playSoundEffect(wx + 0.5D, wy + 0.5D, wz + 0.5D, "random.fizz",
            0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

    for (int i = 0; i < 8; ++i) {
      world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, wx + Math.random(), wy + 1.2D, wz + Math.random(),
              0.0D, 0.0D, 0.0D, new int[0]);
    }
  }

  // extinguish any fire
  private void extinguishFire(World world, BlockPos blockPos)
  {
    world.setBlockToAir(blockPos);
    double wx = blockPos.getX();
    double wy = blockPos.getY();
    double wz = blockPos.getZ();
    world.playSoundEffect(wx + 0.5D, wy + 0.5D, wz + 0.5D, "random.fizz",
            0.5F, 3.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
  }

  @Override
  public BreathAffectedEntity affectEntity(World world, Integer entityID, BreathAffectedEntity currentHitDensity)
  {
    // 1) extinguish fire on entity
    // 2) apply cold damage.  The normal armour protection does not apply.  Instead, metal armor worsens damage.
    //   Leather and diamond armor protects.
    // 3) cancels invisibility, slows victim

    checkNotNull(world);
    checkNotNull(entityID);
    checkNotNull(currentHitDensity);

    if (entityID == dragon.getEntityId()) return null;

    Entity entity = world.getEntityByID(entityID);
    if (entity == null || !(entity instanceof EntityLivingBase) || entity.isDead) {
      return null;
    }

    if (entity instanceof EntityPlayer) {
      EntityPlayer entityPlayer = (EntityPlayer)entity;
      if (DragonMounts.instance.getConfig().isOrbHolderImmune()
          && ItemUtils.hasEquipped(entityPlayer, DragonMounts.proxy.itemDragonOrb)) {
        return null;
      }
    }

    if (entity.isBurning()) {
      entity.extinguish();
    }

    final float DAMAGE_PER_HIT_DENSITY = 0.1F;
    float armorDamageModifier = getArmorDamageModifier(entity);
    float hitDensity = currentHitDensity.getHitDensity();
    if (currentHitDensity.applyDamageThisTick()) {
      entity.attackEntityFrom(DamageSource.magic,
              hitDensity * DAMAGE_PER_HIT_DENSITY * armorDamageModifier);
      currentHitDensity.resetHitDensity();

      if (entity instanceof EntityLivingBase) {
        EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
        if (entityLivingBase.isPotionActive(Potion.invisibility.id)) {
          entityLivingBase.removePotionEffect(Potion.invisibility.id);
        }

        int duration = 10 * 20;          // 10 seconds
        final int EFFECT_AMPLIFIER = 3;  // not sure why this is 3; other vanilla uses that value
        PotionEffect slowDown = new PotionEffect(Potion.moveSlowdown.id, duration, EFFECT_AMPLIFIER);
        entityLivingBase.addPotionEffect(slowDown);
      }
    }

    return currentHitDensity;
  }


  private static Map<Material, Integer> materialShatterTime = Maps.newHashMap();  // lazy initialisation

//  private void affectBlock(Block whichBlock)
//  {
//    if (whichBlock == null) return;
//    Material material = whichBlock.getMaterial();
//    if (material == null) return;
//
//    if (materialShatterTime.containsKey(material)) {
//      Integer shatterTime = materialShatterTime.get(material);
//
//      return;
//    }
//
//    eventually, snow turns to ice
//
//    if (material == Material.water) {
//    }
//
//    if (material == Material.lava) {
//      if (material == Material.fire) {
//
//      }
//      if (material == Material.ice) {
//
//      }
//      if (material == Material.packedIce) {
//
//      }
//      if (material == Material.snow) {
//
//      }
//      if (material == Material.craftedSnow) {
//
//      }
//
//    }
//  }

  private void initialiseStatics()
  {
    if (!materialShatterTime.isEmpty()) return;
    final int INSTANT = 0;
    final int MODERATE = 10;
    final int SLOW = 50;
    materialShatterTime.put(Material.leaves, INSTANT);
    materialShatterTime.put(Material.plants, INSTANT);
    materialShatterTime.put(Material.vine, INSTANT);
    materialShatterTime.put(Material.web, INSTANT);
    materialShatterTime.put(Material.gourd, INSTANT);
    materialShatterTime.put(Material.sponge, MODERATE);
    materialShatterTime.put(Material.glass, MODERATE);
    materialShatterTime.put(Material.cactus, MODERATE);
    materialShatterTime.put(Material.rock, SLOW);
  }

//  private BlockBurnProperties getBurnProperties(IBlockState iBlockState)
//  {
//    Block block = iBlockState.getBlock();
//    if (blockBurnPropertiesCache.containsKey(block)) {
//      return  blockBurnPropertiesCache.get(block);
//    }
//
//    BlockBurnProperties blockBurnProperties = new BlockBurnProperties();
//    IBlockState result = getSmeltingResult(iBlockState);
//    blockBurnProperties.threshold = 20;
//    if (result == null) {
//      blockBurnProperties.threshold = 3;
//      result = getScorchedResult(iBlockState);
//    }
//    if (result == null) {
//      blockBurnProperties.threshold = 5;
//      result = getVaporisedLiquidResult(iBlockState);
//    }
//    if (result == null) {
//      blockBurnProperties.threshold = 100;
//      result = getMoltenLavaResult(iBlockState);
//    }
//    blockBurnProperties.burnResult = result;
//    blockBurnPropertiesCache.put(block, blockBurnProperties);
//    return blockBurnProperties;
//  }
//
//  /** if sourceBlock can be smelted, return the smelting result as a block
//   * @param sourceBlock
//   * @return the smelting result, or null if none
//   */
//  private static IBlockState getSmeltingResult(IBlockState sourceBlock)
//  {
//    Block block = sourceBlock.getBlock();
//    Item itemFromBlock = Item.getItemFromBlock(block);
//    ItemStack itemStack;
//    if (itemFromBlock != null && itemFromBlock.getHasSubtypes())     {
//      int metadata = block.getMetaFromState(sourceBlock);
//      itemStack = new ItemStack(itemFromBlock, 1, metadata);
//    } else {
//      itemStack = new ItemStack(itemFromBlock);
//    }
//
//    ItemStack smeltingResult = FurnaceRecipes.instance().getSmeltingResult(itemStack);
//    if (smeltingResult != null) {
//      Block smeltedResultBlock = Block.getBlockFromItem(smeltingResult.getItem());
//      if (smeltedResultBlock != null) {
//        IBlockState iBlockStateSmelted = smeltedResultBlock.getStateFromMeta(smeltingResult.getMetadata());
//        return iBlockStateSmelted;
//      }
//    }
//    if (block == Blocks.iron_ore) return Blocks.iron_block.getDefaultState();
//    if (block == Blocks.gold_ore) return Blocks.gold_block.getDefaultState();
//    if (block == Blocks.emerald_ore) return Blocks.emerald_block.getDefaultState();
//    if (block == Blocks.diamond_ore) return Blocks.diamond_block.getDefaultState();
//    if (block == Blocks.coal_ore) return Blocks.coal_block.getDefaultState();
//    if (block == Blocks.redstone_ore) return Blocks.redstone_block.getDefaultState();
//    if (block == Blocks.lapis_ore) return Blocks.lapis_block.getDefaultState();
//    if (block == Blocks.quartz_ore) return Blocks.quartz_block.getDefaultState();
//    return null;
//  }
//
//  /** if sourceBlock is a liquid or snow that can be molten or vaporised, return the result as a block
//   *
//   * @param sourceBlock
//   * @return the vaporised result, or null if none
//   */
//  private static IBlockState getVaporisedLiquidResult(IBlockState sourceBlock)
//  {
//    Block block = sourceBlock.getBlock();
//    Material material = block.getMaterial();
//
//    if (material == Material.water) {
//      return Blocks.air.getDefaultState();
//    } else if (material == Material.snow || material == Material.ice) {
//      final int SMALL_LIQUID_AMOUNT = 4;
//      return Blocks.flowing_water.getDefaultState().withProperty(BlockLiquid.LEVEL, SMALL_LIQUID_AMOUNT);
//    } else if (material == Material.packedIce || material == Material.craftedSnow) {
//      final int LARGE_LIQUID_AMOUNT = 1;
//      return Blocks.flowing_water.getDefaultState().withProperty(BlockLiquid.LEVEL, LARGE_LIQUID_AMOUNT);
//    }
//    return null;
//  }
//
//  /** if sourceBlock is a block that can be melted to lave, return the result as a block
//   * @param sourceBlock
//   * @return the molten lava result, or null if none
//   */
//  private static IBlockState getMoltenLavaResult(IBlockState sourceBlock)
//  {
//    Block block = sourceBlock.getBlock();
//    Material material = block.getMaterial();
//
//    if (material == Material.sand || material == Material.clay
//            || material == Material.glass || material == Material.iron
//            || material == Material.ground || material == Material.rock) {
//      final int LARGE_LIQUID_AMOUNT = 1;
//      return Blocks.lava.getDefaultState().withProperty(BlockLiquid.LEVEL, LARGE_LIQUID_AMOUNT);
//    }
//    return null;
//  }
//
//  /** if sourceBlock is a block that isn't flammable but can be scorched / changed, return the result as a block
//   * @param sourceBlock
//   * @return the scorched result, or null if none
//   */
//  private static IBlockState getScorchedResult(IBlockState sourceBlock)
//  {
//    Block block = sourceBlock.getBlock();
//    Material material = block.getMaterial();
//
//    if (material == Material.grass) {
//      return Blocks.dirt.getDefaultState();
//    }
//    return null;
//  }

  /**
   * How much does the entity's armour modify the damage?
   * Up to double for all-conductive armour; down to half for all-insulating armour
   * @param armoredEntity
   * @return multiplier (1.0= unmodified) for the cold damage
   */
  private static float getArmorDamageModifier(Entity armoredEntity)
  {
    final float UNMODIFIED = 1.0F;
    if (!(armoredEntity instanceof EntityLivingBase)) {
      return UNMODIFIED;
    }
    EntityLivingBase entityLivingBase = (EntityLivingBase)armoredEntity;

    final int FIRST_ARMOUR_INDEX = 1;
    final int LAST_ARMOUR_INDEX = 4;
    final float ARMOUR_SLOTS_COUNT = LAST_ARMOUR_INDEX - FIRST_ARMOUR_INDEX + 1;

    int conductorCount = 0;
    int insulatorCount = 0;
    for (int slot = FIRST_ARMOUR_INDEX; slot <= LAST_ARMOUR_INDEX; ++slot) {
      ItemStack itemStack = entityLivingBase.getEquipmentInSlot(slot);
      if (itemStack != null && itemStack.getItem() instanceof ItemArmor) {
        ItemArmor.ArmorMaterial material = ((ItemArmor) itemStack.getItem()).getArmorMaterial();
        switch (material) {
          case IRON:
          case CHAIN:
          case GOLD: {
            ++conductorCount;
            break;
          }

          case LEATHER:
          case DIAMOND: {
            ++insulatorCount;
            break;
          }

          default:{  // no effect
            break;
          }
        }
      }
    }
    float damageMultiplier = UNMODIFIED
                             * (1 + conductorCount / ARMOUR_SLOTS_COUNT)
                             / (1 + insulatorCount / ARMOUR_SLOTS_COUNT);
    return damageMultiplier;
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

  private HashMap<Block, BlockBurnProperties> blockBurnPropertiesCache = new HashMap<Block, BlockBurnProperties>();

  private static class BlockBurnProperties {
    public IBlockState burnResult = null;  // null if no effect
    public float threshold;
  }
}
