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
import info.ata4.minecraft.dragon.client.render.BreathWeaponFXEmitterFire;
import info.ata4.minecraft.dragon.client.sound.SoundController;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeapon;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeaponFire;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeaponNull;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNodeFactory;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNodeFire;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathWeapon;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathWeaponFire;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedFire extends DragonBreed {

    DragonBreedFire() {
        super("fire", 0x960b0f);
        
        addImmunity(DamageSource.inFire);
        addImmunity(DamageSource.onFire);
        addImmunity(DamageSource.lava);
        
        addHabitatBlock(Blocks.LAVA);
        addHabitatBlock(Blocks.FLOWING_LAVA);
        addHabitatBlock(Blocks.FIRE);
        addHabitatBlock(Blocks.LIT_FURNACE);
        
        addHabitatBiome(Biomes.DESERT);
        addHabitatBiome(Biomes.DESERT_HILLS);
    }

    @Override
    public void onEnable(EntityTameableDragon dragon) {
        dragon.getBrain().setAvoidsWater(true);
    }

    @Override
    public void onDisable(EntityTameableDragon dragon) {
        dragon.getBrain().setAvoidsWater(false);
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
        return new BreathWeaponFXEmitterFire();
    }

  @Override
  public BreathWeaponSpawnType getBreathWeaponSpawnType(EntityTameableDragon dragon)
  {
    return BreathWeaponSpawnType.NODES;
  }

  /** return a new BreathWeapon based on breed
     * @return
     */
    @Override
    public BreathWeapon getBreathWeapon(EntityTameableDragon dragon)
    {
        return new BreathWeaponFire(dragon);
    }

    @Override
    public BreathNodeFactory getBreathNodeFactory(EntityTameableDragon dragon)
    {
      return new BreathNodeFire.BreathNodeFireFactory();
    }

  @Override
  public SoundEffectBreathWeapon getSoundEffectBreathWeapon(SoundController i_soundController,
                                                            SoundEffectBreathWeapon.WeaponSoundUpdateLink i_weaponSoundUpdateLink) {
    return new SoundEffectBreathWeaponFire(i_soundController, i_weaponSoundUpdateLink);
  }

}
