/*
 ** 2013 October 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.breeds;

import info.ata4.minecraft.dragon.client.render.BreathWeaponFXEmitter;
import info.ata4.minecraft.dragon.client.render.BreathWeaponFXEmitterIce;
import info.ata4.minecraft.dragon.client.sound.SoundController;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeapon;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeaponFire;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeaponIce;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.breath.*;
import net.minecraft.block.Block;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedIce extends DragonBreed {
    
    DragonBreedIce() {
        super("ice", 0x6fc3ff);
        
        addImmunity(DamageSource.magic);
        
        addHabitatBlock(Blocks.SNOW);
        addHabitatBlock(Blocks.SNOW_LAYER);
        addHabitatBlock(Blocks.ICE);
        
        addHabitatBiome(Biomes.FROZEN_OCEAN);
        addHabitatBiome(Biomes.FROZEN_RIVER);
        addHabitatBiome(Biomes.ICE_MOUNTAINS);
        addHabitatBiome(Biomes.ICE_PLAINS);
    }
    
    @Override
    protected float getFootprintChance() {
        return 0.1f;
    }
    
    @Override
    protected void placeFootprintBlock(EntityTameableDragon dragon, BlockPos blockPos) {
        // place snow layer blocks, but only if the biome is cold enough
        World world = dragon.worldObj;
        
        if (world.getBiome(blockPos).getFloatTemperature(blockPos) > 0.8f) {
            return;
        }
        
        Block footprint = Blocks.SNOW_LAYER;
        if (!footprint.canPlaceBlockAt(world, blockPos)) {
            return;
        }
        
        world.setBlockState(blockPos, footprint.getDefaultState());
    }

    @Override
    public void onEnable(EntityTameableDragon dragon) {
    }

    @Override
    public void onDisable(EntityTameableDragon dragon) {
    }

    @Override
    public void onDeath(EntityTameableDragon dragon) {
    }

  /** return a new fire breathweapon FX emitter
   * @return
   */
  @Override
  public BreathWeaponFXEmitter getBreathWeaponFXEmitter(EntityTameableDragon dragon)
  {
    return new BreathWeaponFXEmitterIce();
  }

  /** return a new BreathWeapon based on breed
   * @return
   */
  @Override
  public BreathWeapon getBreathWeapon(EntityTameableDragon dragon)
  {
    return new BreathWeaponIce(dragon);
  }

  @Override
  public BreathWeaponSpawnType getBreathWeaponSpawnType(EntityTameableDragon dragon)
  {
    return BreathWeaponSpawnType.NODES;
  }


  @Override
  public BreathNodeFactory getBreathNodeFactory(EntityTameableDragon dragon)
  {
    return new BreathNodeIce.BreathNodeIceFactory();
  }

  @Override
  public SoundEffectBreathWeapon getSoundEffectBreathWeapon(SoundController i_soundController,
                                                            SoundEffectBreathWeapon.WeaponSoundUpdateLink i_weaponSoundUpdateLink) {
    return new SoundEffectBreathWeaponIce(i_soundController, i_weaponSoundUpdateLink);
  }

}
