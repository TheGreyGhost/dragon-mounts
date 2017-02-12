package info.ata4.minecraft.dragon.server.entity.helper.breath;

import com.google.common.collect.Maps;
import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.util.ItemUtils;
import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by TGG on 7/12/2015.
 */
public class BreathWeaponWater extends BreathWeapon
{
  public BreathWeaponWater(EntityTameableDragon i_dragon)
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
    // soft blocks such as sand, clay, etc get destroyed (washed away)
    // leaves, grass, flowers, plants, etc get washed away (destroyed)
    // lava turns to obsidian
    // extinguish fire and torches
    // coat blocks with water.  Gradually builds up and will coat everything

    if (block == null) return currentHitDensity;
    Material material = iBlockState.getMaterial();
    if (material == null) return currentHitDensity;

    if (materialDisintegrateTime.containsKey(material)) {
      Integer disintegrationTime = materialDisintegrateTime.get(material);
      if (disintegrationTime != null
              && currentHitDensity.getMaxHitDensity() > disintegrationTime) {
        final boolean DROP_BLOCK = true;
        world.destroyBlock(blockPos, DROP_BLOCK);
        return new BreathAffectedBlock();
      }
      return currentHitDensity;
    }

    if (material == Material.LAVA) {
      final float THRESHOLD_LAVA_QUENCH = 10;
      if (currentHitDensity.getMaxHitDensity() > THRESHOLD_LAVA_QUENCH) {
        quenchLava(world, blockPos);
        return new BreathAffectedBlock();
      }
      return currentHitDensity;
    }

    if (material == Material.FIRE) {
      final float THRESHOLD_FIRE_EXTINGUISH = 1;
      if (currentHitDensity.getMaxHitDensity() > THRESHOLD_FIRE_EXTINGUISH) {
        extinguishFire(world, blockPos);
        return new BreathAffectedBlock();
      }
      return currentHitDensity;
    }

    if (material == Material.WATER) {
      return deepenWater(world, block, blockPos, currentHitDensity);
    }
    if (block == Blocks.TORCH) {
      final float THRESHOLD_FIRE_EXTINGUISH = 1;
      if (currentHitDensity.getMaxHitDensity() > THRESHOLD_FIRE_EXTINGUISH) {
        final boolean DROP_BLOCK = true;
        world.destroyBlock(blockPos, DROP_BLOCK);
        return new BreathAffectedBlock();
      }
      return currentHitDensity;
    }

    if (material == Material.AIR) {
      final int THRESHOLD_DEEPEN_WATER_LAYER = 1;
      if (currentHitDensity.getMaxHitDensity() < THRESHOLD_DEEPEN_WATER_LAYER) {
        return currentHitDensity;
      }
      IBlockState blockUnderneath = world.getBlockState(blockPos.down());

      final int MINIMUM_DEPTH = 7;

      if (blockUnderneath.getMaterial().blocksMovement()) {
        world.setBlockState(blockPos,
                Blocks.FLOWING_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL, MINIMUM_DEPTH));
      }
    }

    return currentHitDensity;
  }

  // If this is a water block with level, make level deeper
  private BreathAffectedBlock deepenWater(World world, Block block, BlockPos blockPos,
                                          BreathAffectedBlock currentHitDensity)
  {
    IBlockState currentBlockState = world.getBlockState(blockPos);
    if (currentBlockState.getMaterial() == Material.WATER && block instanceof BlockDynamicLiquid) {
      final int THRESHOLD_DEEPEN_WATER_LEVEL = 1;
      if (currentHitDensity.getMaxHitDensity() < THRESHOLD_DEEPEN_WATER_LEVEL) {
        return currentHitDensity;
      }

      IBlockState newBlockState;
      Integer currentLevel = (Integer)currentBlockState.getValue(BlockDynamicLiquid.LEVEL);
      Integer newLevel = oneWaterLevelHigher(currentLevel);

      if (currentLevel != newLevel) {
        newBlockState = currentBlockState.withProperty(BlockDynamicLiquid.LEVEL, newLevel);
        world.setBlockState(blockPos, newBlockState);
      }
      return new BreathAffectedBlock();
    }
    return currentHitDensity;
  }

  // key parts copied from BlockLiquid
  private Integer oneWaterLevelHigher(Integer currentLevel)
  {
    if (currentLevel == null || currentLevel == 0 || currentLevel >= 8) {
      return currentLevel;
    }
    return --currentLevel;
  }

  // quench a lava and turn it to obsidian
  // copy from BlockLiquid.checkForMixing()
  private void quenchLava(World world, BlockPos blockPos)
  {
    world.setBlockState(blockPos, Blocks.OBSIDIAN.getDefaultState());
    double wx = blockPos.getX();
    double wy = blockPos.getY();
    double wz = blockPos.getZ();
    world.playSound(wx + 0.5D, wy + 0.5D, wz + 0.5D,
                    SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS,
                    0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F, false);

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
    world.playSound(wx + 0.5D, wy + 0.5D, wz + 0.5D,
                    SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS,
                    0.5F, 3.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F, false);
  }

  @Override
  public BreathAffectedEntity affectEntity(World world, Integer entityID, BreathAffectedEntity currentHitDensity)
  {
    // 1) extinguish fire on entity
    // 2) pushes entity in the direction of the water
    // 3) apply water damage
    // 4) cancels all potion effects

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


    final double FORCE_MULTIPLIER = 0.01;
    Vec3d waterForceDirection = currentHitDensity.getHitDensityDirection();
    Vec3d waterMotion = MathX.multiply(waterForceDirection, FORCE_MULTIPLIER);
    entity.addVelocity(waterMotion.xCoord, waterMotion.yCoord, waterMotion.zCoord);

    final float DAMAGE_PER_HIT_DENSITY = 0.1F;
    float hitDensity = currentHitDensity.getHitDensity();
    if (currentHitDensity.applyDamageThisTick()) {
      entity.attackEntityFrom(DamageSource.magic,
              hitDensity * DAMAGE_PER_HIT_DENSITY);
      currentHitDensity.resetHitDensity();

      if (entity instanceof EntityLivingBase) {
        EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
        entityLivingBase.clearActivePotions();
      }
    }

    return currentHitDensity;
  }

  private static Map<Material, Integer> materialDisintegrateTime = Maps.newHashMap();  // lazy initialisation

  private void initialiseStatics()
  {
    if (!materialDisintegrateTime.isEmpty()) return;
    final int INSTANT = 0;
    final int MODERATE = 10;
    final int SLOW = 50;
    materialDisintegrateTime.put(Material.LEAVES, INSTANT);
    materialDisintegrateTime.put(Material.PLANTS, INSTANT);
    materialDisintegrateTime.put(Material.VINE, INSTANT);
    materialDisintegrateTime.put(Material.WEB, INSTANT);
    materialDisintegrateTime.put(Material.GOURD, INSTANT);
    materialDisintegrateTime.put(Material.GRASS, MODERATE);
    materialDisintegrateTime.put(Material.SPONGE, MODERATE);
    materialDisintegrateTime.put(Material.SAND, MODERATE);
    materialDisintegrateTime.put(Material.ICE, MODERATE);
    materialDisintegrateTime.put(Material.PACKED_ICE, MODERATE);
    materialDisintegrateTime.put(Material.SNOW, MODERATE);
    materialDisintegrateTime.put(Material.CRAFTED_SNOW, MODERATE);
    materialDisintegrateTime.put(Material.CLAY, SLOW);
    materialDisintegrateTime.put(Material.CACTUS, SLOW);
    materialDisintegrateTime.put(Material.ROCK, SLOW);
  }

}
