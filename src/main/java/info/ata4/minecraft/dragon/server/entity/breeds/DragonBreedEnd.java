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

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;
import info.ata4.minecraft.dragon.server.entity.helper.breath.*;
import info.ata4.minecraft.dragon.util.Pair;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedEnd extends DragonBreed {

    DragonBreedEnd() {
        super("ender", 0xab39be);
        
        addImmunity(DamageSource.magic);
        
        addHabitatBlock(Blocks.END_STONE);
        addHabitatBlock(Blocks.OBSIDIAN);
        addHabitatBlock(Blocks.END_BRICKS);
        
        addHabitatBiome(Biomes.SKY);
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
    public BreathWeapon getBreathWeapon(EntityTameableDragon dragon)
    {
        return new BreathWeaponEnder(dragon);
    }

    @Override
    public BreathWeaponSpawnType getBreathWeaponSpawnType(EntityTameableDragon dragon)
    {
        return BreathWeaponSpawnType.PROJECTILE;
    }

    @Override
    public BreathProjectileFactory getBreathProjectileFactory(EntityTameableDragon dragon)
    {
        return new EntityBreathProjectileEnder.BreathProjectileFactoryEnder();
    }

    @Override
    public Pair<Float, Float> getBreathWeaponRange(DragonLifeStage dragonLifeStage)
    {
        return new Pair<Float, Float>(10F, 40F);
    }


}
