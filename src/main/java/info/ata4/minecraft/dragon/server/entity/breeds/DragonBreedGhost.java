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
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedGhost extends DragonBreed {

    public DragonBreedGhost() {
        super("ghost", "undead", 0xbebebe);

        addImmunity(DamageSource.wither);

        addHabitatBlock(Blocks.web);
    }

    @Override
    public void onEnable(EntityTameableDragon dragon) {
        dragon.tasks.addTask(2, new EntityAIRestrictSun(dragon));
    }

    @Override
    public void onDisable(EntityTameableDragon dragon) {
        List<EntityAITaskEntry> taskEntries = (List<EntityAITaskEntry>) dragon.tasks.taskEntries;
        Iterator<EntityAITaskEntry> iterator = taskEntries.iterator();

        while (iterator.hasNext()) {
            EntityAITaskEntry taskEntry = iterator.next();

            if (taskEntry.action instanceof EntityAIRestrictSun) {
                taskEntry.action.resetTask();
                iterator.remove();
                break;
            }
        }
    }

//    @Override
//    public void onUpdate(EntityTameableDragon dragon) {
//        // start burning when in contact with sunlight
//        if (dragon.worldObj.isDaytime() && dragon.isServer()) {
//            float brightness = dragon.getBrightness(1);
//            float threshold = (brightness - 0.4f) * 2;
//            int bx = MathHelper.floor_double(dragon.posX);
//            int by = MathHelper.floor_double(dragon.posY);
//            int bz = MathHelper.floor_double(dragon.posZ);
//
//            if (brightness > 0.5f && dragon.worldObj.rand.nextFloat() * 30 < threshold && dragon.worldObj.canBlockSeeTheSky(bx, by, bz)) {
//                dragon.setFire(8);
//            }
//        }
//    }

    @Override
    public boolean isHabitatEnvironment(EntityTameableDragon dragon) {
        if (dragon.posY > dragon.worldObj.getHeight() * 0.25) {
            // woah dude, too high!
            return false;
        }

        int bx = MathHelper.floor_double(dragon.posX);
        int by = MathHelper.floor_double(dragon.posY);
        int bz = MathHelper.floor_double(dragon.posZ);

        BlockPos blockPos = new BlockPos(bx, by, bz);
        if (dragon.worldObj.canBlockSeeSky(blockPos)) { // sun is shining!
            return false;
        }
        if (dragon.worldObj.getLight(blockPos) > 4) { // too bright!
            return false;
        }
//        if (dragon.worldObj.canBlockSeeTheSky(bx, by, bz)) {
//            // sun is shining!
//            return false;
//        }
//        
//        if (dragon.worldObj.getBlockLightValue(bx, by, bz) > 4) {
//            // too bright!
//            return false;
//        }

        return true;
    }

    @Override
    public String getLivingSound(EntityTameableDragon dragon) {
        return "mob.skeleton.say";
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    public BreathWeapon getBreathWeapon(EntityTameableDragon dragon)
    {
        return new BreathWeaponGhost(dragon);
    }

    @Override
    public BreathWeaponSpawnType getBreathWeaponSpawnType(EntityTameableDragon dragon)
    {
        return BreathWeaponSpawnType.PROJECTILE;
    }

    @Override
    public BreathProjectileFactory getBreathProjectileFactory(EntityTameableDragon dragon)
    {
        return new EntityBreathProjectileGhost.BreathProjectileFactoryGhost();
    }

    @Override
    public Pair<Float, Float> getBreathWeaponRange(DragonLifeStage dragonLifeStage)
    {
        float minDistance = 5;
        float maxDistance = 0;
        switch (dragonLifeStage) {
          case EGG: {break;} //eh?
          case HATCHLING: {maxDistance = 10; break;}
          case JUVENILE: {maxDistance = 20; break;}
          case ADULT: {maxDistance = 40; break;}
        default: {System.err.println("Invalid dragonLifeStage" + dragonLifeStage); break;}
        }
        return new Pair<Float, Float>(minDistance, maxDistance);
    }


}
