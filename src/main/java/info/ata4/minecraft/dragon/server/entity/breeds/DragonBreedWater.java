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
import info.ata4.minecraft.dragon.client.render.BreathWeaponFXEmitterWater;
import info.ata4.minecraft.dragon.client.sound.SoundController;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeapon;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeaponAir;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeaponWater;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.breath.*;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedWater extends DragonBreed {

  public DragonBreedWater() {
        super("sylphid", 0x4f69a8);
        
        addImmunity(DamageSource.drown);
        
        addHabitatBlock(Blocks.WATER);
        addHabitatBlock(Blocks.FLOWING_WATER);
        addHabitatBlock(Blocks.PRISMARINE);
        addHabitatBlock(Blocks.SEA_LANTERN);
        
        addHabitatBiome(Biomes.OCEAN);
        addHabitatBiome(Biomes.RIVER);
        addHabitatBiome(Biomes.SWAMPLAND);
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


    @Override
    public BreathWeaponFXEmitter getBreathWeaponFXEmitter(EntityTameableDragon dragon)
    {
        return new BreathWeaponFXEmitterWater();
    }

    @Override
    public BreathWeapon getBreathWeapon(EntityTameableDragon dragon)
    {
        return new BreathWeaponWater(dragon);
    }

  @Override
  public BreathWeaponSpawnType getBreathWeaponSpawnType(EntityTameableDragon dragon)
  {
    return BreathWeaponSpawnType.NODES;
  }

  @Override
    public BreathNodeFactory getBreathNodeFactory(EntityTameableDragon dragon)
    {
        return new BreathNodeWater.BreathNodeWaterFactory();
    }

  @Override
  public SoundEffectBreathWeapon getSoundEffectBreathWeapon(SoundController i_soundController,
                                                            SoundEffectBreathWeapon.WeaponSoundUpdateLink i_weaponSoundUpdateLink) {
    return new SoundEffectBreathWeaponWater(i_soundController, i_weaponSoundUpdateLink);
  }


}
