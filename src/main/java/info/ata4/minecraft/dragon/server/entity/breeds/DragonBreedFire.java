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
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNodeFactory;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNodeFire;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathWeapon;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathWeaponFire;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.world.biome.BiomeGenBase;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedFire extends DragonBreed {

    public DragonBreedFire() {
        super("fire", "fire", 0x960b0f);
        
        addImmunity(DamageSource.inFire);
        addImmunity(DamageSource.onFire);
        addImmunity(DamageSource.lava);
        
        addHabitatBlock(Blocks.lava);
        addHabitatBlock(Blocks.flowing_lava);
        addHabitatBlock(Blocks.fire);
        addHabitatBlock(Blocks.lit_furnace);
        
        addHabitatBiome(BiomeGenBase.desert);
        addHabitatBiome(BiomeGenBase.desertHills);
    }

    @Override
    public void onEnable(EntityTameableDragon dragon) {
      dragon.setDragonAvoidWater(true);
    }

    @Override
    public void onDisable(EntityTameableDragon dragon) {
      dragon.setDragonAvoidWater(false);
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

}
