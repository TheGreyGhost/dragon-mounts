/*
 ** 2012 August 13
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.model.anim.DragonAnimatorCommon;
import info.ata4.minecraft.dragon.server.entity.ai.DragonBodyHelper;
import info.ata4.minecraft.dragon.server.entity.breeds.DragonBreed;
import info.ata4.minecraft.dragon.server.entity.helper.*;
import info.ata4.minecraft.dragon.server.entity.helper.breath.DragonBreathHelper;
import info.ata4.minecraft.dragon.server.util.DebugFreezeAnimator;
import info.ata4.minecraft.dragon.server.util.ItemUtils;
import info.ata4.minecraft.dragon.util.reflection.PrivateFields;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Here be dragons.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityTameableDragon extends EntityFlyingTameable {

    private static final Logger L = LogManager.getLogger();

    // base attributes
    public static final double BASE_SPEED_GROUND = 0.3;
    public static final double BASE_SPEED_AIR = 1.5;
    public static final double BASE_DAMAGE = 8;
    public static final double BASE_HEALTH = 60;
    public static final float BASE_WIDTH = 2.75f;
    public static final float BASE_HEIGHT = 2.75f;
    public static final int HOME_RADIUS = 256;
    public static final Item FAVORITE_FOOD = Items.fish;

    // data value IDs
    private static final int INDEX_SADDLED = 20;
    private static final int INDEX_BREEDER = 21;
    private static final int INDEX_BREED = 22;
    private static final int INDEX_REPRO_COUNT = 23;
    private static final int INDEX_TICKS_SINCE_CREATION = 24;
    private static final int INDEX_BREATH_WEAPON_TARGET = 25;
    private static final int INDEX_BREATH_MODE = 26;  // used by some dragons for the current mode of the breath

    // data NBT IDs
    private static final String NBT_SADDLED = "Saddle";

    // server/client delegates
    private Map<Class, DragonHelper> helpers;

//    private DragonHeadPositionHelper dragonHeadPositionHelper;
//    public DragonHeadPositionHelper getDragonHeadPositionHelper() { return dragonHeadPositionHelper;}
    private DragonAnimatorCommon animator;

    // server-only flags
    private BitSet controlFlags;

    public EntityTameableDragon(World world) {
        super(world);

        // override EntityBodyHelper field, which is private and has no setter
        // required to fixate body while sitting. also slows down rotation while standing.
        try {
            ReflectionHelper.setPrivateValue(EntityLiving.class, this, new DragonBodyHelper(this), PrivateFields.ENTITYLIVING_BODYHELPER);
        } catch (Exception ex) {
            L.warn("Can't override EntityBodyHelper", ex);
        }

        // override EntityLookHelper field, which is private and has no setter
        try {
            ReflectionHelper.setPrivateValue(EntityLiving.class, this, new DragonLookHelper(this), PrivateFields.ENTITYLIVING_LOOKHELPER);
        } catch (Exception ex) {
            L.warn("Can't override EntityLookHelper", ex);
        }

        moveHelper = new DragonMoveHelper(this);

        // set base size
        setSize(BASE_WIDTH, BASE_HEIGHT);

        // enables walking over blocks
        stepHeight = 1;
        this.ignoreFrustumCheck = true; // makes sure that dragon renders even when its bounding box (which encloses its body only) is out of view.

    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(INDEX_SADDLED, (byte) 0);

        addHelper(new DragonBreedHelper(this, INDEX_BREED));
        addHelper(new DragonLifeStageHelper(this, INDEX_TICKS_SINCE_CREATION));
        addHelper(new DragonReproductionHelper(this, INDEX_BREEDER, INDEX_REPRO_COUNT));
        addHelper(new DragonParticleHelper(this));

        if (DragonMounts.instance.getConfig().isDebug()) {
            addHelper(new DragonDebug(this));
        }

        animator = new DragonAnimatorCommon(this);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        setAttributes();

        for (DragonHelper helper : helpers.values()) {
            helper.applyEntityAttributes();
        }
    }

    private void setAttributes() {
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(BASE_SPEED_GROUND);
        getEntityAttribute(MOVE_SPEED_AIR).setBaseValue(BASE_SPEED_AIR);
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(BASE_HEALTH);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(BASE_DAMAGE);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setBoolean(NBT_SADDLED, isSaddled());

        for (DragonHelper helper : helpers.values()) {
            helper.writeToNBT(nbt);
        }
    }

    @Override
    public void setNoAI(boolean disableAI) {
        super.setNoAI(disableAI);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        setSaddled(nbt.getBoolean(NBT_SADDLED));

        for (DragonHelper helper : helpers.values()) {
            helper.readFromNBT(nbt);
        }

    // override attributes loaded from NBT, they were set and handled
        // incorrectly in older versions
        setAttributes();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    public void setRotationYawHead(float rotation) {
        super.setRotationYawHead(rotation);
    }

    /**
     * returns the pitch of the dragon's body
     *
     * @return
     */
    public float getBodyPitch() {
        return animator.getBodyPitch();
    }

    @Override
    public void onLivingUpdate() {
//        System.out.format("motionY: %.2f\n", motionY);
        if (!DebugFreezeAnimator.isFrozen()) {
            for (DragonHelper helper : helpers.values()) {
                helper.onLivingUpdate();
            }
            animator.setOnGround(!isFlying());

            if (isServer()) {
                final float DUMMY_MOVETIME = 0;
                final float DUMMY_MOVESPEED = 0;
                animator.setMovement(DUMMY_MOVETIME, DUMMY_MOVESPEED);
                float netYawHead = getRotationYawHead() - renderYawOffset;
                animator.setLook(netYawHead, rotationPitch);
                animator.setTicksExisted(ticksExisted);
                animator.tickingUpdate();
                animator.animate();

        // set home position near owner when tamed
                //  setHomeArea renamed to EntityCreature.func_175449_a()
                if (isTamed()) {
                    Entity owner = getOwner();
                    if (owner != null) {
                        BlockPos ownerPosition = new BlockPos(owner.posX, owner.posY, owner.posZ);
                        func_175449_a(ownerPosition, HOME_RADIUS);
                    }
                }
            } else {
                animator.tickingUpdate();  // all other animator parameters are set by the model renderer
            }
            super.onLivingUpdate();
        }
    }

    /**
     * Handles entity death timer, experience orb and particle creation
     */
    @Override
    protected void onDeathUpdate() {
        for (DragonHelper helper : helpers.values()) {
            helper.onDeathUpdate();
        }

        // unmount any riding entity
        if (riddenByEntity != null) {
            riddenByEntity.mountEntity(null);
        }

        // freeze at place
        motionX = motionY = motionZ = 0;
        rotationYaw = prevRotationYaw;
        setRotationYawHead(prevRotationYawHead);

        if (isEgg()) {
            setDead();
        } else {
            if (deathTime >= getMaxDeathTime()) {
                setDead();
            }
        }

        deathTime++;
    }

    @Override
    public void setDead() {
        for (DragonHelper helper : helpers.values()) {
            helper.onDeath();
        }
        super.setDead();
    }

    // hijack swingItem to be used for attacking (jaw animation on attack)
    //  normally only used for swinging held items; attackEntityAsMob is overridden to send packet S0BPacketAnimation
    @Override
    public void swingItem() {
        ticksSinceLastAttack = 0;
    }

    @Override
    public String getName() {
        // return custom name if set
        if (hasCustomName()) {
            return getCustomNameTag();
        }

        // return default breed name otherwise
        String entName = EntityList.getEntityString(this);
        String breedName = getBreed().getName().toLowerCase();
        return StatCollector.translateToLocal("entity." + entName + "." + breedName + ".name");
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
    protected String getLivingSound() {
        if (isEgg()
                || isFlying()
                || getBreathHelper().getCurrentBreathState() != DragonBreathHelper.BreathState.IDLE) {
            return null;
        } else {
            return getBreed().getLivingSound(this);
        }
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
    protected String getHurtSound() {
        if (isEgg()) {
            return "mob.zombie.wood";
        } else {
            return getBreed().getHurtSound(this);
        }
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected String getDeathSound() {
        if (isEgg()) {
            return "mob.zombie.woodbreak";
        } else {
            return getBreed().getDeathSound(this);
        }
    }

    /**
     * Plays living's sound at its position
     */
    @Override
    public void playLivingSound() {
        String sound = getLivingSound();
        if (sound == null) {
            return;
        }

        float v = getSoundVolume();
        float p = getSoundPitch();

        // lower pitch and masterVolume for breathing sounds
        if (sound.endsWith("breathe")) {
            v *= 0.5;
            p *= 0.5;
        }

        playSound(sound, v, p);
    }

    /**
     * Plays step sound at given x, y, z for the entity
     */
    @Override
    protected void playStepSound(BlockPos entityPos, Block block) {
        if (isEgg() || inWater) {
            // no sounds for eggs or underwater action
        } else if (isHatchling()) {
            // play default step sound for babies
            super.playStepSound(entityPos, block);
        } else {
            // play stomping for bigger dragons
            worldObj.playSoundAtEntity(this, DragonMounts.AID + ":mob.enderdragon.step", 0.5f, 1);
        }
    }

    /**
     * Returns the masterVolume for the sounds this mob makes.
     */
    @Override
    protected float getSoundVolume() {
        return 2 - getScale();
    }

    /**
     * Gets the pitch of living sounds in living entities.
     */
    @Override
    protected float getSoundPitch() {
        return super.getSoundPitch() * (1.0F + 4.0F * (1 - getScale()));
    }

    /**
     * Get number of ticks, at least during which the living entity will be
     * silent.
     */
    @Override
    public int getTalkInterval() {
        return 240;
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return getBreed().getCreatureAttribute();
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow,
     * gets into the saddle on a pig.
     */
    @Override
    public boolean interact(EntityPlayer player) {
        ItemStack playerItem = player.inventory.getCurrentItem();

        // duplicate dragon with entity egg
        if (playerItem != null && playerItem.getItem() == Items.spawn_egg) {
            return super.interact(player);
        }

        // don't interact with eggs!
        if (isEgg()) {
            return false;
        }

        if (isTamed() || isChild()) {
            // heal dragon with food
            ItemFood food = null;

            // eat only if hurt
            if (getHealthRelative() < 1) {
                food = (ItemFood) ItemUtils.consumeEquipped(player, FAVORITE_FOOD,
                        Items.porkchop, Items.beef, Items.chicken);
            }

            // heal only if the food was actually consumed
            if (food != null) {
                heal(food.getHealAmount(playerItem));
                float volume = getSoundVolume() * 0.7f;
                float pitch = getSoundPitch();
                worldObj.playSoundAtEntity(this, "random.eat", volume, pitch);
                return true;
            }

            if (!isOwner(player)) {
                if (isServer()) {
                    // that's not your dragon!
                    player.addChatMessage(new ChatComponentTranslation("dragon.owned"));
                }
            } else if (!isChild() && riddenByEntity == null) {
                if (!isSaddled() && ItemUtils.consumeEquipped(player, Items.saddle)) {
                    if (isServer()) {
                        // put on a saddle
                        setSaddled(true);
                    }
                } else if (ItemUtils.hasEquipped(player, Items.bone)) {
                    if (isServer()) {
                        // toggle sitting state with the bone item
                        aiSit.setSitting(!isSitting());
                        isJumping = false;
                        navigator.clearPathEntity();  // replacement for setPathToEntity(null);
                    }
                } else if (getReproductionHelper().canReproduce() && ItemUtils.consumeEquipped(player, FAVORITE_FOOD)) {
                    // activate love mode with favorite food if it hasn't reproduced yet
                    if (isClient()) {
                        getParticleHelper().spawnBodyParticles(EnumParticleTypes.HEART);
                    }

                    setInLove(player);
                } else if (isSaddled() && !ItemUtils.hasEquippedUsable(player)) {
                    if (isServer()) {
                        // mount dragon when saddled and not already mounted
                        setRidingPlayer(player);
                    }
                }
            }
        } else {
            if (isServer()) {
                if (ItemUtils.consumeEquipped(player, FAVORITE_FOOD)) {
                    // tame dragon with favorite food and a random chance
                    tamedFor(player, rand.nextInt(3) == 0);
                }
            }

            return true;
        }

        return false;
    }

    public void tamedFor(EntityPlayer player, boolean successful) {
        if (successful) {
            setTamed(true);
            navigator.clearPathEntity();  // replacement for setPathToEntity(null);
            setAttackTarget(null);
            setOwnerId(player.getUniqueID().toString());
            playTameEffect(true);
            worldObj.setEntityState(this, (byte) 7);
        } else {
            playTameEffect(false);
            worldObj.setEntityState(this, (byte) 6);
        }
    }

    @Override
    protected void updateAITasks() {
        // disable AI on eggs
        if (!isEgg()) {
            super.updateAITasks();
        }
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed
     * it (wheat, carrots or seeds depending on the animal type)
     */
    @Override
    public boolean isBreedingItem(ItemStack item) {
        // breeding items are handled in interact(), this is just for EntityAnimal.interact()
        return false;
    }

    /**
     * Returns the height of the eyes. Used for looking at other entities.
     */
    @Override
    public float getEyeHeight() {
        float eyeHeight = super.getEyeHeight();

        if (isSitting()) {
            eyeHeight *= 0.8f;
        }

        return eyeHeight;
    }

    /**
     * Returns the Y offset from the entity's position for any entity riding
     * this one.
     */
    @Override
    public double getMountedYOffset() {
        return (isSitting() ? 1.7f : 2.2f) * getScale();
    }

    /**
     * Returns render size modifier
     */
    @Override
    public float getRenderSizeModifier() {
        return getScale();
    }

    @Override
    protected boolean isGroundAIEnabled() {
        return super.isGroundAIEnabled() && !isEgg();
    }

    /**
     * Returns true if this entity should push and be pushed by other entities
     * when colliding.
     */
    @Override
    public boolean canBePushed() {
        return super.canBePushed() && isEgg();
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    protected boolean canDespawn() {
        return false;
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    @Override
    public boolean isOnLadder() {
        // this better doesn't happen...
        return false;
    }

    /**
     * Drop 0-2 items of this living's type. @param par1 - Whether this entity
     * has recently been hit by a player. @param par2 - Level of Looting used to
     * kill this mob.
     */
    @Override
    protected void dropFewItems(boolean par1, int par2) {
        super.dropFewItems(par1, par2);

        // drop saddle if equipped
        if (isSaddled()) {
            dropItem(Items.saddle, 1);
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity victim) {
        // code copied from EntityMob::attackEntityAsMob

        float attackDamage = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        int knockback = 0;

        if (victim instanceof EntityLivingBase) {  // not actually required for a dragon?  leave in anyway
            attackDamage += EnchantmentHelper.func_152377_a(this.getHeldItem(), ((EntityLivingBase) victim).getCreatureAttribute());
            knockback += EnchantmentHelper.getKnockbackModifier(this);
        }

        boolean attacked = victim.attackEntityFrom(DamageSource.causeMobDamage(this), attackDamage);

        if (attacked) {
            if (knockback > 0) {
                double vx = -Math.sin(Math.toRadians(rotationYaw)) * knockback * 0.5;
                double vy = 0.1;
                double vz = Math.cos(Math.toRadians(rotationYaw)) * knockback * 0.5;
                victim.addVelocity(vx, vy, vz);

                motionX *= 0.6;
                motionZ *= 0.6;
            }

            int fireAspect = EnchantmentHelper.getFireAspectModifier(this);

            if (fireAspect > 0) {
                victim.setFire(fireAspect * 4);
            }

            if (victim instanceof EntityLivingBase) {
                EnchantmentHelper.func_151384_a((EntityLivingBase) victim, this);
            }

            EnchantmentHelper.func_151385_b(this, victim);

            setLastAttacker(victim);

            // play eating sound
            float volume = getSoundVolume() * 0.7f;
            float pitch = getSoundPitch();
            worldObj.playSoundAtEntity(this, "random.eat", volume, pitch);

            if (this.worldObj instanceof WorldServer) {
                ((WorldServer) this.worldObj).getEntityTracker().sendToAllTrackingEntity(this, new S0BPacketAnimation(this, 0));
            }

        }

        return attacked;
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource src, float par2) {
        if (isInvulnerableTo(src)) {
            return false;
        }

        // don't just sit there!
        aiSit.setSitting(false);

        return super.attackEntityFrom(src, par2);
    }

    /**
     * Return whether this entity should be rendered as on fire.
     */
    @Override
    public boolean canRenderOnFire() {
        return super.canRenderOnFire() && !getBreedHelper().getBreed().isImmuneToDamage(DamageSource.inFire);
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    @Override
    public boolean canMateWith(EntityAnimal mate) {
        return getReproductionHelper().canMateWith(mate);
    }

    /**
     * This function is used when two same-species animals in 'love mode' breed
     * to generate the new baby animal.
     */
    @Override
    public EntityAgeable createChild(EntityAgeable mate) {
        return getReproductionHelper().createChild(mate);
    }

    public DragonAnimatorCommon getAnimator() {
        return animator;
    }

    private void addHelper(DragonHelper helper) {
        L.trace("addHelper({})", helper.getClass().getName());
        if (helpers == null) {
            helpers = new HashMap<Class, DragonHelper>();
        }
        helpers.put(helper.getClass(), helper);
    }

    public <T extends DragonHelper> T getHelper(Class<T> clazz) {
        return (T) helpers.get(clazz);
    }

    public DragonBreedHelper getBreedHelper() {
        return getHelper(DragonBreedHelper.class);
    }

    public DragonLifeStageHelper getLifeStageHelper() {
        return getHelper(DragonLifeStageHelper.class);
    }

    public DragonReproductionHelper getReproductionHelper() {
        return getHelper(DragonReproductionHelper.class);
    }

    private DragonParticleHelper getParticleHelper() {
        return getHelper(DragonParticleHelper.class);
    }

    public DragonBreathHelper getBreathHelper() {
      if (isEgg()) {  // can't breathe yet; makes no sense to ask for BreathHelper.  Breed may still change,
        throw new IllegalStateException("Asked for DragonBreathHelper but dragon is still egg stage");
      }

      DragonBreathHelper dragonHelper = getHelper(DragonBreathHelper.class);
      if (dragonHelper == null) { // lazy initialisation
          dragonHelper = new DragonBreathHelper(this, INDEX_BREATH_WEAPON_TARGET, INDEX_BREATH_MODE);
        addHelper(dragonHelper);
      }

        return dragonHelper;
    }

    public boolean getBooleanData(int index) {
        return (dataWatcher.getWatchableObjectByte(index) & 1) != 0;
    }

    public void setBooleanData(int index, boolean value) {
        dataWatcher.updateObject(index, Byte.valueOf(value ? (byte) 1 : (byte) 0));
    }

    /**
     * Returns true if the dragon is saddled.
     */
    public boolean isSaddled() {
        return getBooleanData(INDEX_SADDLED);
    }

    /**
     * Set or remove the saddle of the dragon.
     */
    public void setSaddled(boolean saddled) {
        L.trace("setSaddled({})", saddled);
        setBooleanData(INDEX_SADDLED, saddled);
    }

    /**
     * Returns the breed for this dragon.
     *
     * @return breed
     */
    public DragonBreed getBreed() {
        return getBreedHelper().getBreed();
    }

    /**
     * Sets the new breed for this dragon.
     *
     * @param breed new breed
     */
    public void setBreed(DragonBreed breed) {
        getBreedHelper().setBreed(breed);
    }

    public EntityPlayer getRidingPlayer() {
        if (riddenByEntity instanceof EntityPlayer) {
            return (EntityPlayer) riddenByEntity;
        } else {
            return null;
        }
    }

    public void setRidingPlayer(EntityPlayer player) {
        L.trace("setRidingPlayer({})", player.getName());
        player.rotationYaw = this.rotationYaw;
        player.rotationPitch = this.rotationPitch;
        player.mountEntity(this);
    }

    public void setControlFlags(BitSet flags) {
        controlFlags = flags;
    }

    public BitSet getControlFlags() {
        return controlFlags;
    }

    @Override
    public void updateRiderPosition() {
        if (riddenByEntity != null) {
            double px = posX;
            double py = posY + getMountedYOffset() + riddenByEntity.getYOffset();
            double pz = posZ;

      // dragon position is the middle of the model and the saddle is on
            // the shoulders, so move player forwards on Z axis relative to the
            // dragon's rotation to fix that
            Vec3 pos = new Vec3(0, 0, 0.8 * getScale());
            pos = pos.rotateYaw((float) Math.toRadians(-renderYawOffset)); // oops
            px += pos.xCoord;
            py += pos.yCoord;
            pz += pos.zCoord;

            riddenByEntity.setPosition(px, py, pz);

            // fix rider rotation
            if (riddenByEntity instanceof EntityLiving) {
                EntityLiving rider = ((EntityLiving) riddenByEntity);
                rider.prevRotationPitch = rider.rotationPitch;
                rider.prevRotationYaw = rider.rotationYaw;
                rider.renderYawOffset = renderYawOffset;
            }
        }
    }

    public boolean isInvulnerableTo(DamageSource src) {
        Entity srcEnt = src.getEntity();
        if (srcEnt != null) {
            // ignore own damage
            if (srcEnt == this) {
                return true;
            }

            // ignore damage from rider
            if (srcEnt == riddenByEntity) {
                return true;
            }
        }

        // don't drown as egg
        if (src.damageType.equals("drown") && isEgg()) {
            return true;
        }

        return getBreed().isImmuneToDamage(src);
    }

    /**
     * Returns the entity's health relative to the maximum health.
     *
     * @return health normalized between 0 and 1
     */
    public double getHealthRelative() {
        return getHealth() / (double) getMaxHealth();
    }

    public int getDeathTime() {
        return deathTime;
    }

    public int getMaxDeathTime() {
        return 120;
    }

    /**
     * Client side method for wing animations. Plays wing flapping sounds.
     *
     * @param speed wing animation playback speed
     */
    public void onWingsDown(float speed) {
        if (!inWater) {
            // play wing sounds
            float pitch = getSoundPitch() + (1 - speed);
            float volume = getSoundVolume() * 0.3f + (1 - speed) * 0.2f;
            worldObj.playSound(posX, posY, posZ, "mob.enderdragon.wings", volume, pitch, false);
        }
    }

    public void setImmuneToFire(boolean isImmuneToFire) {
        L.trace("setImmuneToFire({})", isImmuneToFire);
        this.isImmuneToFire = isImmuneToFire;
    }

    public void setAttackDamage(double damage) {
        L.trace("setAttackDamage({})", damage);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(damage);
    }

    public double getAttackDamage() {
        return getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
    }

    /**
     * Public wrapper for protected final setScale(), used by
     * DragonLifeStageHelper.
     *
     * @param scale
     */
    public void setScalePublic(float scale) {
        double posXTmp = posX;
        double posYTmp = posY;
        double posZTmp = posZ;

        setScale(scale);

    // workaround for a vanilla bug; the position is apparently not set correcty
        // after changing the entity size, causing asynchronous server/client positioning
        setPosition(posXTmp, posYTmp, posZTmp);
    }

    @Override
    public void setScaleForAge(boolean p_98054_1_) {
    // SetGrowingAge calls this to switch between half and full scale based
        // on isChild(), but the scale is managed in DragonLifeStageHelper, so
        // this is no-op here
    }

//    Comparison of 1.7.10 and 1.8 Render::renderShadow shows that this isn't needed any more
//    @SideOnly(Side.CLIENT)
//    @Override
//    public float getShadowSize() {
//        // must be 0 or the shadows will be rendered incorrectly
//        // (misleading method name, should be getShadowYOffset())
//        return 0;
//    }
    /**
     * Returns the size multiplier for the current age.
     *
     * @return scale
     */
    public float getScale() {
        return getLifeStageHelper().getScale();
    }

    public boolean isEgg() {
        return getLifeStageHelper().isEgg();
    }

    public boolean isHatchling() {
        return getLifeStageHelper().isHatchling();
    }

    public boolean isJuvenile() {
        return getLifeStageHelper().isJuvenile();
    }

    public boolean isAdult() {
        return getLifeStageHelper().isAdult();
    }

    public void setDragonAvoidWater(boolean avoidWater) {
        PathNavigate pathNavigate = this.getNavigator();
        if (pathNavigate instanceof PathNavigateGround) {
            PathNavigateGround pathNavigateGround = (PathNavigateGround) pathNavigate;
            pathNavigateGround.func_179690_a(avoidWater);
        }
    }

    /**
     * when the dragon rotates its head left-right (yaw), how fast does it move?
     *
     * @return max yaw speed in degrees per tick
     */
    public float getHeadYawSpeed() {
        return 10.0F;
    }

    /**
     * when the dragon rotates its head up-down (pitch), how fast does it move?
     *
     * @return max pitch speed in degrees per tick
     */
    public float getHeadPitchSpeed() {
        return 40;
    }

    @Override
    public int getVerticalFaceSpeed() {
        return (int) getHeadPitchSpeed();
    }

}
