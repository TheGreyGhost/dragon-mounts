package info.ata4.minecraft.dragon.server.entity.helper.breath;

import com.google.common.collect.Maps;
import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.util.WeightedRandomChoices;
import info.ata4.minecraft.dragon.util.plants.*;
import info.ata4.minecraft.dragon.server.util.ItemUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by TGG on 7/12/2015.
 * effect on blocks: there are four kinds of effects:
 * 1) Grow: the block itself is a plant and will have a grow effect applied to it (eg crops)
 * 2) Transmute: the block is not a plant but will be modified by the breath (eg turn to mossy cobblestone)
 * 3) SpawnNew: the block is air or snow layer or flowing water, and the block underneath supports a plant -> spawn a plant here
 * 4) Flammable: the block sets fire to the breath
 *
 * Grow:
 * - crops (eg wheat) grow to max
 * - saplings to grow to full
 *
 * Transmute:
 * - dirt turns to grass
 * - cobblestone becomes mossy cobblestone
 *
 * SpawnNew:
 * - ploughed soil sprouts random crops
 * - living plants spawn on suitable blocks (dirt, grass, water, sand, etc)
 *
 * Flammable:
 * - torch, lava, or fire causes an explosion and causes the breath to catch fire
 *
 * Ideas not implemented:
 * - turns coal to wood
 * - wood objects grow leaves
 *
 * effect on entities:
 * - poison plus minor damage armor not protecting
 */
public class BreathWeaponForest extends BreathWeapon {
  public BreathWeaponForest(EntityTameableDragon i_dragon) {
    super(i_dragon);
  }

  @Override
  public BreathAffectedBlock affectBlock(World world, Vec3i blockPosition,
                                         BreathAffectedBlock currentHitDensity) {
    checkNotNull(world);
    checkNotNull(blockPosition);
    checkNotNull(currentHitDensity);
    initialiseStaticsLazily();

    if (dragon.getBreathHelper().getBreathMode().equals(DragonBreathMode.FOREST_BURNING)) {
      return burnBlock(world, blockPosition, currentHitDensity);
    }

    BlockPos blockPos = new BlockPos(blockPosition);
    IBlockState iBlockState = world.getBlockState(blockPos);
    Block block = iBlockState.getBlock();

    Random rand = new Random();

    if (block == null) return currentHitDensity;
    Material material = iBlockState.getMaterial();

    if (material == null) return currentHitDensity;

    // spawnNew
    if (material == Material.AIR ||
        block == Blocks.SNOW_LAYER ||
        block == Blocks.FLOWING_WATER) {
      BlockPos oneDown = blockPos.down();
      IBlockState groundBelow = world.getBlockState(oneDown);
      Material materialBelow = groundBelow.getMaterial();
      if (materialEffectTimeSpawnNew.containsKey(materialBelow)) {
        Integer spawnTime = materialEffectTimeSpawnNew.get(materialBelow);
        if (spawnTime != null
                && currentHitDensity.getMaxHitDensity() > spawnTime) {
          return spawnNew(currentHitDensity, world, blockPos, groundBelow, rand);
        }
        return currentHitDensity;
      }
    }

    // transmute
    if (materialEffectTimeTransmute.containsKey(material)) {
      Integer transmutationTime = materialEffectTimeTransmute.get(material);
      if (transmutationTime != null
              && currentHitDensity.getMaxHitDensity() > transmutationTime) {
        return transmute(currentHitDensity, world, blockPos, iBlockState);
      }
      return currentHitDensity;
    }

    // grow
    if (materialEffectTimeGrow.containsKey(material)) {
      Integer growTime = materialEffectTimeGrow.get(material);
      if (growTime != null
              && currentHitDensity.getMaxHitDensity() > growTime) {
        return grow(currentHitDensity, world, blockPos, iBlockState);
      }
      return currentHitDensity;
    }

    // ignite (flammable)

    if (block == Blocks.TORCH) {
      if (currentHitDensity.getMaxHitDensity() > 0) {
//        EnumFacing whichFaceTouched = currentHitDensity.getMaxHitDensityFace();
//        BlockPos adjacentFace = blockPos.offset(whichFaceTouched);
        ignite(world, blockPos);
        return new BreathAffectedBlock();
      }
      return currentHitDensity;
    }

    if (materialEffectTimeFlammable.containsKey(material) || block == Blocks.TORCH) {
      Integer igniteTime = materialEffectTimeFlammable.get(material);
      if (igniteTime != null
              && currentHitDensity.getMaxHitDensity() > igniteTime) {
//        EnumFacing whichFaceTouched = currentHitDensity.getMaxHitDensityFace();
//        BlockPos adjacentFace = blockPos.offset(whichFaceTouched);
        ignite(world, blockPos);
        return new BreathAffectedBlock();
      }
      return currentHitDensity;
    }

    return currentHitDensity;
  }


  @Override
  // return true ("wipe breath affected blocks & entities") if we're changing back to not burning.
  public boolean shouldResetOnBreathModeChange(DragonBreathMode newDragonBreathMode)
  {
    return (newDragonBreathMode.equals(DragonBreathMode.FOREST_NOT_BURNING));
  }

  /** if the gas has been ignited, set fire to flammable blocks
   *
   * @param world
   * @param blockPosition
   * @param currentHitDensity
   * @return
   */
  private BreathAffectedBlock burnBlock(World world, Vec3i blockPosition,
                                        BreathAffectedBlock currentHitDensity)
  {
    BlockPos blockPos = new BlockPos(blockPosition);
    IBlockState iBlockState = world.getBlockState(blockPos);
    Block block = iBlockState.getBlock();
    Random rand = new Random();

    // copied from BreathWeaponFire
    for (EnumFacing facing : EnumFacing.values()) {
      BlockPos sideToIgnite = blockPos.offset(facing);
      if (block.isFlammable(world, sideToIgnite, facing)) {
        int flammability = block.getFlammability(world, sideToIgnite, facing);
        float thresholdForIgnition = convertFlammabilityToHitDensityThreshold(flammability);
        float thresholdForDestruction = thresholdForIgnition * 10;
        float densityOfThisFace = currentHitDensity.getHitDensity(facing);
        if (densityOfThisFace >= thresholdForIgnition && world.isAirBlock(sideToIgnite)) {
          final float MIN_PITCH = 0.8F;
          final float MAX_PITCH = 1.2F;
          final float VOLUME = 1.0F;
          world.playSound(sideToIgnite.getX() + 0.5, sideToIgnite.getY() + 0.5, sideToIgnite.getZ() + 0.5,
                          SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.HOSTILE,
                          VOLUME, MIN_PITCH + rand.nextFloat() * (MAX_PITCH - MIN_PITCH), false);
          world.setBlockState(sideToIgnite, Blocks.FIRE.getDefaultState());
        }
        if (densityOfThisFace >= thresholdForDestruction) {
          world.setBlockToAir(blockPos);
        }
      }
    }
    return currentHitDensity;
  }

  /**
   * returns the hitDensity threshold for the given block flammability (0 - 300 as per Block.getFlammability)
   * Copied from BreathWeaponFire
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


  @Override
  public BreathAffectedEntity affectEntity(World world, Integer entityID, BreathAffectedEntity currentHitDensity) {
    // 1) if entity is burning, create explosion
    // 2) poison entity
    checkNotNull(world);
    checkNotNull(entityID);
    checkNotNull(currentHitDensity);
    initialiseStaticsLazily();

    if (entityID == dragon.getEntityId()) return null;

    Entity entity = world.getEntityByID(entityID);
    if (entity == null || !(entity instanceof EntityLivingBase) || entity.isDead) {
      return null;
    }

    if (entity instanceof EntityPlayer) {
      EntityPlayer entityPlayer = (EntityPlayer) entity;
      if (DragonMounts.instance.getConfig().isOrbHolderImmune()
              && ItemUtils.hasEquipped(entityPlayer, DragonMounts.proxy.itemDragonOrb)) {
        return null;
      }
    }

    boolean applyDamageThisTick = currentHitDensity.applyDamageThisTick();
    if (applyDamageThisTick && entity.isBurning()) {
      ignite(world, entity.getPosition());
    }

    final float POISON_THRESHOLD = 2.0F;
    final int POISON_DURATION_TICKS = 60;
    final int POISON_AMPLIFIER = 0;  // not sure of what this means but seems to be common

    float hitDensity = currentHitDensity.getHitDensity();
    if (entity instanceof EntityLivingBase && applyDamageThisTick && hitDensity > POISON_THRESHOLD) {
      EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
      entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.POISON, POISON_DURATION_TICKS, POISON_AMPLIFIER));
      currentHitDensity.resetHitDensity();
    }

    final int DELAY_UNTIL_DECAY = 5;
    final float DECAY_PERCENTAGE_PER_TICK = 10.0F;
    currentHitDensity.setDecayParameters(DECAY_PERCENTAGE_PER_TICK, DELAY_UNTIL_DECAY);

    return currentHitDensity;
  }

  @Override
  public void updateBreathWeaponMode()
  {
    // when the dragon stops breathing, reset mode to non-burning.
    // otherwise, leave the state unchanged.  burning is triggered in ignite().

    if (dragon.getBreathHelper().getCurrentBreathState() != DragonBreathHelper.BreathState.SUSTAIN) {
      dragon.getBreathHelper().setBreathMode(DragonBreathMode.FOREST_NOT_BURNING);
    }
  }

  private BreathAffectedBlock transmute(BreathAffectedBlock currentHitDensity, World world,
                                       BlockPos blockPos, IBlockState iBlockState)
  {
    Material material = iBlockState.getBlock().getMaterial(iBlockState);

    final int SET_BLOCKSTATE_FLAG = 3;  // update flag setting to use for setBlockState

    if (material == Material.GROUND) {
      world.setBlockState(blockPos, Blocks.GRASS.getDefaultState(), SET_BLOCKSTATE_FLAG);
      return new BreathAffectedBlock();
    } else if (material == Material.ROCK) {
      Block block = iBlockState.getBlock();
      if (block == Blocks.COBBLESTONE) {
        world.setBlockState(blockPos, Blocks.MOSSY_COBBLESTONE.getDefaultState(), SET_BLOCKSTATE_FLAG);
        return new BreathAffectedBlock();
      } else if (block == Blocks.COBBLESTONE_WALL) {
        world.setBlockState(blockPos,
                            iBlockState.withProperty(BlockWall.VARIANT, BlockWall.EnumType.MOSSY),
                            SET_BLOCKSTATE_FLAG);
        return new BreathAffectedBlock();
      }
    } else if (material == Material.AIR) {
      VinesPlant vinesPlant = new VinesPlant();
      Random random = new Random();
      boolean successfulSpawn = vinesPlant.trySpawnNewPlant(world, blockPos, random);
      if (successfulSpawn) {
        return new BreathAffectedBlock();
      }
    } else {
      throw new IllegalArgumentException("transmute called with Block:" + iBlockState);
    }

    return currentHitDensity;
  }

  private BreathAffectedBlock spawnNew(BreathAffectedBlock currentHitDensity,
                                       World world, BlockPos blockPos, IBlockState groundBlockState,
                                       Random rand)
  {
    WeightedRandomChoices<Plant> plantChoices = weightedSpawners.get(groundBlockState.getMaterial());
    Plant plant = plantChoices.pickRandom(rand);

    boolean spawnsuccess = plant.trySpawnNewPlant(world, blockPos, rand);
    return spawnsuccess ? new BreathAffectedBlock() : currentHitDensity;
  }

  private BreathAffectedBlock grow(BreathAffectedBlock currentHitDensity, World world,
                                   BlockPos blockPos, IBlockState iBlockState)
  {
    Plant plant = Plant.getPlantFromBlockState(iBlockState);

    if (plant == null) {
      return currentHitDensity;
    }

    final float GROWTH_PERCENTAGE_PER_DENSITY = 1;
    float growthPercentage = currentHitDensity.getMaxHitDensity() * GROWTH_PERCENTAGE_PER_DENSITY;
    plant.grow(world, blockPos, growthPercentage);
    return new BreathAffectedBlock();
  }

  private void ignite(World world, BlockPos blockPos)
  {
    dragon.getBreathHelper().setBreathMode(DragonBreathMode.FOREST_BURNING);

    final float EXPLOSION_SIZE = 6.0F;  // not sure of the units.  TNT is 4
    final boolean SET_FIRE_TO_BLOCKS = true;
    world.setBlockToAir(blockPos);
    world.createExplosion(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                          EXPLOSION_SIZE, SET_FIRE_TO_BLOCKS);
  }

  private static Map<Material, Integer> materialEffectTimeGrow = Maps.newHashMap();  // lazy initialisation
  private static Map<Material, Integer> materialEffectTimeSpawnNew = Maps.newHashMap();  // lazy initialisation
  private static Map<Material, Integer> materialEffectTimeTransmute = Maps.newHashMap();  // lazy initialisation
  private static Map<Material, Integer> materialEffectTimeFlammable = Maps.newHashMap();   // lazy initialisation

  private static Map<Material, WeightedRandomChoices<Plant>> weightedSpawners = Maps.newHashMap();

  private void initialiseStaticsLazily() {
    if (!materialEffectTimeGrow.isEmpty()) return;
    final int INSTANT = 0;
    final int MODERATE = 10;
    final int SLOW = 50;
    materialEffectTimeGrow.put(Material.LEAVES, MODERATE);
    materialEffectTimeGrow.put(Material.PLANTS, INSTANT);  // cocoa, flower, reed, bush
    materialEffectTimeGrow.put(Material.VINE, INSTANT);  // vine, deadbush, double plant, tallgrass
    materialEffectTimeGrow.put(Material.WEB, INSTANT);
    materialEffectTimeGrow.put(Material.GOURD, INSTANT); //melon, pumpkin
    materialEffectTimeGrow.put(Material.CACTUS, MODERATE);

    materialEffectTimeTransmute.put(Material.GROUND, INSTANT);
    materialEffectTimeTransmute.put(Material.ROCK, MODERATE);
    materialEffectTimeTransmute.put(Material.AIR, MODERATE);  // for vines

    materialEffectTimeFlammable.put(Material.LAVA, INSTANT);
    materialEffectTimeFlammable.put(Material.FIRE, MODERATE);

    materialEffectTimeSpawnNew.put(Material.GRASS, INSTANT);
    materialEffectTimeSpawnNew.put(Material.WATER, MODERATE);
    materialEffectTimeSpawnNew.put(Material.SAND, MODERATE);

    //------------
    weightedSpawners.put(Material.WATER, WeightedRandomChoices.ofEqualWeights(new WaterLilyPlant()));

    final int CROPS_WEIGHT_PART = 100;
    WeightedRandomChoices<Plant> ground = new WeightedRandomChoices<Plant>();
    ground.add(new CropsPlant(CropsPlant.CropType.CARROT), CROPS_WEIGHT_PART);
    ground.add(new CropsPlant(CropsPlant.CropType.POTATO), CROPS_WEIGHT_PART);
    ground.add(new CropsPlant(CropsPlant.CropType.WHEAT), CROPS_WEIGHT_PART * 8);
    weightedSpawners.put(Material.GROUND, ground);

    final int SAND_WEIGHT_PART = 100;
    WeightedRandomChoices<Plant> sand = new WeightedRandomChoices<Plant>();
    sand.add(new CactusPlant(), SAND_WEIGHT_PART);
    sand.add(new ReedsPlant(), 9 * SAND_WEIGHT_PART);
    weightedSpawners.put(Material.SAND, sand);


    WeightedRandomChoices<Plant> grass = new WeightedRandomChoices<Plant>();
    final int SHORT_GRASS_WEIGHT = 1000;
    final int SMALL_FLOWERS_WEIGHT = 50;
    final int LARGE_FLOWERS_WEIGHT = 10;
    final int SAPLING_WEIGHT = 1;
    grass.add(new TallGrassPlant(BlockTallGrass.EnumType.GRASS), SHORT_GRASS_WEIGHT);

    for (BlockFlower.EnumFlowerType flowerType : BlockFlower.EnumFlowerType.values()) {
      grass.add(new FlowersPlant(flowerType), SMALL_FLOWERS_WEIGHT);
    }
    grass.add(new TallGrassPlant(BlockTallGrass.EnumType.FERN), SMALL_FLOWERS_WEIGHT);

    grass.add(new PlantDoublePlant(BlockDoublePlant.EnumPlantType.GRASS), LARGE_FLOWERS_WEIGHT);
    grass.add(new PlantDoublePlant(BlockDoublePlant.EnumPlantType.FERN), LARGE_FLOWERS_WEIGHT);
    grass.add(new PlantDoublePlant(BlockDoublePlant.EnumPlantType.SUNFLOWER), LARGE_FLOWERS_WEIGHT);
    grass.add(new PlantDoublePlant(BlockDoublePlant.EnumPlantType.SYRINGA), LARGE_FLOWERS_WEIGHT);
    grass.add(new PlantDoublePlant(BlockDoublePlant.EnumPlantType.ROSE), LARGE_FLOWERS_WEIGHT);
    grass.add(new PlantDoublePlant(BlockDoublePlant.EnumPlantType.PAEONIA), LARGE_FLOWERS_WEIGHT);

    grass.add(new SaplingPlant(BlockPlanks.EnumType.ACACIA), SAPLING_WEIGHT);
    grass.add(new SaplingPlant(BlockPlanks.EnumType.OAK), SAPLING_WEIGHT);
    grass.add(new SaplingPlant(BlockPlanks.EnumType.SPRUCE), SAPLING_WEIGHT);
    grass.add(new SaplingPlant(BlockPlanks.EnumType.BIRCH), SAPLING_WEIGHT);
    grass.add(new SaplingPlant(BlockPlanks.EnumType.JUNGLE), SAPLING_WEIGHT);
    grass.add(new SaplingPlant(BlockPlanks.EnumType.DARK_OAK), SAPLING_WEIGHT);

    weightedSpawners.put(Material.GRASS, grass);
  }
}


