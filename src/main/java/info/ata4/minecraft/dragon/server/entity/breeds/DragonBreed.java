/*
 ** 2013 March 18
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.breeds;

import com.google.common.collect.Table;
import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.render.BreathWeaponFXEmitter;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNodeFactory;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathProjectileFactory;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathWeapon;
import info.ata4.minecraft.dragon.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.util.DamageSource;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for dragon breeds.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class DragonBreed {
    
    private final String name;
    private final String skin;
    private final int color;
    private Set<String> immunities = new HashSet<String>();
    private Set<Block> breedBlocks = new HashSet<Block>();
    private Set<BiomeGenBase> biomes = new HashSet<BiomeGenBase>();
    
    public DragonBreed(String name, String skin, int color) {
        this.name = name;
        this.skin = skin;
        this.color = color;
        
        // ignore suffocation damage
        addImmunity(DamageSource.drown);
        addImmunity(DamageSource.inWall);
        
        // assume that cactus needles don't do much damage to animals with horned scales
        addImmunity(DamageSource.cactus);
    }
    
    public String getName() {
        return name;
    }

    public String getSkin() {
        return skin;
    }
    
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEFINED;
    }
    
    public int getColor() {
        return color;
    }
    
    public float getColorR() {
        return ((color >> 16) & 0xFF) / 255f;
    }
    
    public float getColorG() {
        return ((color >> 8) & 0xFF) / 255f;
    }
    
    public float getColorB() {
        return (color & 0xFF) / 255f;
    }
    
    protected void addImmunity(DamageSource dmg) {
        immunities.add(dmg.damageType);
    }
    
    public boolean isImmuneToDamage(DamageSource dmg) {
        if (immunities.isEmpty()) {
            return false;
        }
        
        return immunities.contains(dmg.damageType);
    }
    
    public void addHabitatBlock(Block block) {
        breedBlocks.add(block);
    }
    
    public boolean isHabitatBlock(Block block) {
        return breedBlocks.contains(block);
    }
    
    public void addHabitatBiome(BiomeGenBase biome) {
        biomes.add(biome);
    }
    
    public boolean isHabitatBiome(BiomeGenBase biome) {
        return biomes.contains(biome);
    }
    
    public boolean isHabitatEnvironment(EntityTameableDragon dragon) {
        return false;
    }
    
    public void onEnable(EntityTameableDragon dragon) {
    }
    
    public void onDisable(EntityTameableDragon dragon) {
    }
    
    public void onUpdate(EntityTameableDragon dragon) {
    }
    
    public void onDeath(EntityTameableDragon dragon) {
    }
    
    public String getLivingSound(EntityTameableDragon dragon) {
        if (dragon.getRNG().nextInt(3) == 0) {
            return "mob.enderdragon.growl";
        } else {
            return DragonMounts.AID + ":mob.enderdragon.breathe";
        }
    }
    
    public String getHurtSound(EntityTameableDragon dragon) {
        return "mob.enderdragon.hit";
    }
    
    public String getDeathSound(EntityTameableDragon dragon) {
        return DragonMounts.AID + ":mob.enderdragon.death";
    }

    @Override
    public String toString() {
        return name;
    }

    public int getNumberOfNeckSegments() {return 7;}

    public int getNumberOfTailSegments() {return 12;}

    public int getNumberOfWingFingers() {return 4;}

    public enum BreathWeaponSpawnType {PROJECTILE, NODES}
    // PROJECTILE = spawn a single Entity, similar to EntityFIreball for ghast
    // NODES = continuous stream of small nodes

    abstract public BreathWeaponSpawnType getBreathWeaponSpawnType(EntityTameableDragon dragon);
//    {
//      throw new UnsupportedOperationException();
//    }

    /** return a new Breath Weapon FX Emitter based on breed
     * @return
     */
    public  BreathWeaponFXEmitter getBreathWeaponFXEmitter(EntityTameableDragon dragon)
    {
      throw new UnsupportedOperationException();
    }

    /** return a new BreathWeapon based on breed
     * @return
     */
    abstract public BreathWeapon getBreathWeapon(EntityTameableDragon dragon);
//    {
//        throw new UnsupportedOperationException();
//    }

    public BreathNodeFactory getBreathNodeFactory(EntityTameableDragon dragon)
    {
        throw new UnsupportedOperationException();
    }

    public BreathProjectileFactory getBreathProjectileFactory(EntityTameableDragon dragon)
    {
      throw new UnsupportedOperationException();
    }

  /**
   * returns the range of the breath weapon
   * @param dragonLifeStage
   * @return  minimum range, maximum range, in blocks
   */
    public Pair<Float, Float> getBreathWeaponRange(DragonLifeStage dragonLifeStage)
    {
      return getBreathWeaponRangeDefault(dragonLifeStage);
    }

    private Pair<Float, Float> getBreathWeaponRangeDefault(DragonLifeStage dragonLifeStage) {
      float minAttackRange = 1.0F;
      float maxAttackRange = 1.0F;
      switch (dragonLifeStage) {
        case EGG:
          break;
        case HATCHLING: {
          minAttackRange = 2.0F;
          maxAttackRange = 4.0F;
          break;
        }
        case JUVENILE: {
          minAttackRange = 3.0F;
          maxAttackRange = 8.0F;
          break;
        }
        case ADULT: {
          minAttackRange = 5.0F;
          maxAttackRange = 25.0F;
          break;
        }
        default: {
          System.err.println("Unknown lifestage:" + dragonLifeStage);
          break;
        }
      }
      return new Pair<Float, Float>(minAttackRange, maxAttackRange);
    }
}
