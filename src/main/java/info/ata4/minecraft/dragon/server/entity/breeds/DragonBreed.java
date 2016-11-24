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

import info.ata4.minecraft.dragon.DragonMountsSoundEvents;
import com.google.common.collect.Table;
import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.render.BreathWeaponFXEmitter;
import info.ata4.minecraft.dragon.client.sound.SoundController;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeapon;
import info.ata4.minecraft.dragon.client.sound.SoundEffectBreathWeaponNull;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNodeFactory;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathProjectileFactory;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathWeapon;
import info.ata4.minecraft.dragon.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for dragon breeds.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class DragonBreed {
    
    private final String skin;
    private final int color;
    private final Set<String> immunities = new HashSet<>();
    private final Set<Block> breedBlocks = new HashSet<>();
    private final Set<Biome> biomes = new HashSet<>();
    protected final Random rand = new Random();
    
    DragonBreed(String skin, int color) {
        this.skin = skin;
        this.color = color;
        
        // ignore suffocation damage
        addImmunity(DamageSource.drown);
        addImmunity(DamageSource.inWall);
        
        // assume that cactus needles don't do much damage to animals with horned scales
        addImmunity(DamageSource.cactus);
        
        // ignore damage from vanilla ender dragon
        addImmunity(DamageSource.dragonBreath);
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
    
    protected final void addImmunity(DamageSource dmg) {
        immunities.add(dmg.damageType);
    }
    
    public boolean isImmuneToDamage(DamageSource dmg) {
        if (immunities.isEmpty()) {
            return false;
        }
        
        return immunities.contains(dmg.damageType);
    }
    
    protected final void addHabitatBlock(Block block) {
        breedBlocks.add(block);
    }
    
    public boolean isHabitatBlock(Block block) {
        return breedBlocks.contains(block);
    }
    
    protected final void addHabitatBiome(Biome biome) {
        biomes.add(biome);
    }
    
    public boolean isHabitatBiome(Biome biome) {
        return biomes.contains(biome);
    }
    
    public boolean isHabitatEnvironment(EntityTameableDragon dragon) {
        return false;
    }
    
    public Item[] getFoodItems() {
        return new Item[] { Items.PORKCHOP, Items.BEEF, Items.CHICKEN };
    }
    
    public Item getBreedingItem() {
        return Items.FISH;
    }
    
    public void onUpdate(EntityTameableDragon dragon) {
        placeFootprintBlocks(dragon);
    }
    
    protected void placeFootprintBlocks(EntityTameableDragon dragon) {
        // only apply on server
        if (!dragon.isServer()) {
            return;
        }
        
        // only apply on adult dragons that don't fly
        if (!dragon.isAdult() || dragon.isFlying()) {
            return;
        }
        
        // only apply if footprints are enabled
        float footprintChance = getFootprintChance();
        if (footprintChance == 0) {
            return;
        }
        
        // footprint loop, from EntitySnowman.onLivingUpdate with slight tweaks
        World world = dragon.worldObj;
        for (int i = 0; i < 4; i++) {
            // place only if randomly selected
            if (world.rand.nextFloat() > footprintChance) {
                continue;
            }

            // get footprint position
            double bx = dragon.posX + (i % 2 * 2 - 1) * 0.25;
            double by = dragon.posY + 0.5;
            double bz = dragon.posZ + (i / 2 % 2 * 2 - 1) * 0.25;
            BlockPos pos = new BlockPos(bx, by, bz);

            // footprints can only be placed on empty space
            if (world.isAirBlock(pos)) {
                continue;
            }

            placeFootprintBlock(dragon, pos);
        }
    }
    
    protected void placeFootprintBlock(EntityTameableDragon dragon, BlockPos blockPos) {
    }
    
    protected float getFootprintChance() {
        return 0;
    }
    
    public abstract void onEnable(EntityTameableDragon dragon);
    
    public abstract void onDisable(EntityTameableDragon dragon);
    
    public abstract void onDeath(EntityTameableDragon dragon);
    
    public SoundEvent getLivingSound() {
        if (rand.nextInt(3) == 0) {
            return SoundEvents.ENTITY_ENDERDRAGON_GROWL;
        } else {
            return DragonMountsSoundEvents.ENTITY_DRAGON_MOUNT_BREATHE;
        }
    }
    
    public SoundEvent getHurtSound() {
        return SoundEvents.ENTITY_ENDERDRAGON_HURT;
    }
    
    public SoundEvent getDeathSound() {
        return DragonMountsSoundEvents.ENTITY_DRAGON_MOUNT_DEATH;
    }
    
    public SoundEvent getWingsSound() {
        return SoundEvents.ENTITY_ENDERDRAGON_FLAP;
    }
    
    public SoundEvent getStepSound() {
        return DragonMountsSoundEvents.ENTITY_DRAGON_MOUNT_STEP;
    }
    
    public SoundEvent getEatSound() {
        return SoundEvents.ENTITY_GENERIC_EAT;
    }
    
    public SoundEvent getAttackSound() {
        return SoundEvents.ENTITY_GENERIC_EAT;
    }

    public float getSoundPitch(SoundEvent sound) {
        return 1;
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

  /**
   * creates a SoundEffectBreathWeapon that creates the sound from the dragon's mouth when breathing
   * @return
   */
    public SoundEffectBreathWeapon getSoundEffectBreathWeapon(SoundController i_soundController,
                                                              SoundEffectBreathWeapon.WeaponSoundUpdateLink i_weaponSoundUpdateLink) {
      return new SoundEffectBreathWeaponNull(i_soundController, i_weaponSoundUpdateLink);
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
    public float getSoundVolume(SoundEvent sound) {
        return 1;
    }
}
