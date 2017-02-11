/*
 ** 2012 August 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.render.CustomEntityFXTypes;
import info.ata4.minecraft.dragon.server.block.BlockDragonBreedEgg;
import info.ata4.minecraft.dragon.server.cmd.CommandDragon;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileEnder;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileGhost;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileNether;
import info.ata4.minecraft.dragon.server.handler.DragonEggBlockHandler;
import info.ata4.minecraft.dragon.server.network.DragonTargetMessage;
import info.ata4.minecraft.dragon.server.network.DragonTargetMessageHandlerServer;
import info.ata4.minecraft.dragon.test.StartupCommon;
import info.ata4.minecraft.dragon.server.item.ItemDragonBreedEgg;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.io.File;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CommonProxy {

    private SimpleNetworkWrapper network;
    public final byte DCM_DISCRIMINATOR_ID = 35;  // arbitrary non-zero ID (non-zero makes troubleshooting easier)
    public final byte DOT_DISCRIMINATOR_ID = 73;  // arbitrary non-zero ID (non-zero makes troubleshooting easier)

  private final int ENTITY_TRACKING_RANGE = 80;
  private final int ENTITY_UPDATE_FREQ = 3;
  private final int ENTITY_ID = 0;
  private final boolean ENTITY_SEND_VELO_UPDATES = true;

    public SimpleNetworkWrapper getNetwork() {
        return network;
    }

    public ItemDragonOrb itemDragonOrb;

    public void onPreInit(FMLPreInitializationEvent evt) {
      itemDragonOrb = (ItemDragonOrb) (new ItemDragonOrb().setUnlocalizedName("dragonorb"));
      itemDragonOrb.setRegistryName("dragonorb");
      GameRegistry.register(itemDragonOrb);
      GameRegistry.register(BlockDragonBreedEgg.INSTANCE.setRegistryName("dragon_egg"));
      GameRegistry.register(ItemDragonBreedEgg.INSTANCE.setRegistryName("dragon_egg"));
//    MinecraftForge.EVENT_BUS.register(new EntitySpawnSuppressor());
      StartupCommon.preInitCommon();
    }

    public void onInit(FMLInitializationEvent evt) {
        registerEntities();

        if (DragonMounts.instance.getConfig().isEggsInChests()) {
            registerChestItems();
        }

        MinecraftForge.EVENT_BUS.register(new DragonEggBlockHandler());
        network = NetworkRegistry.INSTANCE.newSimpleChannel("DragonControls");
//        network.registerMessage(DragonControlMessageHandler.class, DragonControlMessage.class, //todo no longer needed?
//                DCM_DISCRIMINATOR_ID, Side.SERVER);
        network.registerMessage(DragonTargetMessageHandlerServer.class, DragonTargetMessage.class,
                DOT_DISCRIMINATOR_ID, Side.SERVER);
      StartupCommon.initCommon();

    }

    public void onPostInit(FMLPostInitializationEvent event) {
        //  Shaped recipe for the DragonOrb ender eye at the end of two blaze rods
        GameRegistry.addRecipe(new ItemStack(itemDragonOrb), new Object[]{
            ".E.",
            ".B.",
            ".B.",
            'E', Items.ENDER_EYE,
            'B', Items.BLAZE_ROD
        });
      StartupCommon.postInitCommon();
    }
    
    public void onServerStarting(FMLServerStartingEvent evt) {
        MinecraftServer server = evt.getServer();
        ServerCommandManager cmdman = (ServerCommandManager) server.getCommandManager(); 
        cmdman.registerCommand(new CommandDragon());
    }
    
    public void onServerStopped(FMLServerStoppedEvent evt) {
    }

    private void registerEntities() {
        final int TRACKING_RANGE = 80;
        final int UPDATE_FREQUENCY = 3;
        final int DRAGON_ENTITY_ID = 26;
        EntityRegistry.registerModEntity(EntityTameableDragon.class, "DragonMount", DRAGON_ENTITY_ID,
                DragonMounts.instance, TRACKING_RANGE, UPDATE_FREQUENCY, true);

        final int PROJECTILE_NETHER_ENTITY_ID = 27;
        EntityRegistry.registerModEntity(EntityBreathProjectileNether.class, "NetherFireball", PROJECTILE_NETHER_ENTITY_ID,
                DragonMounts.instance, TRACKING_RANGE, UPDATE_FREQUENCY, true);

        final int PROJECTILE_ENDER_ENTITY_ID = 28;
        EntityRegistry.registerModEntity(EntityBreathProjectileEnder.class, "EnderGlobe", PROJECTILE_ENDER_ENTITY_ID,
                DragonMounts.instance, TRACKING_RANGE, UPDATE_FREQUENCY, true);

        final int PROJECTILE_GHOST_ENTITY_ID = 29;
        EntityRegistry.registerModEntity(EntityBreathProjectileGhost.class, "GhostEntity", PROJECTILE_GHOST_ENTITY_ID,
                DragonMounts.instance, TRACKING_RANGE, UPDATE_FREQUENCY, true);
        final int WEATHER_EFFECT_GHOST_ENTITY_ID = 30;
        EntityRegistry.registerModEntity(EntityBreathProjectileGhost.class, "GhostWeatherEntity", WEATHER_EFFECT_GHOST_ENTITY_ID,
                DragonMounts.instance, TRACKING_RANGE, UPDATE_FREQUENCY, true);



      EntityRegistry.registerModEntity(EntityTameableDragon.class, "DragonMount",
              ENTITY_ID, DragonMounts.instance, ENTITY_TRACKING_RANGE, ENTITY_UPDATE_FREQ,
              ENTITY_SEND_VELO_UPDATES);

    }

    public void registerChestItems() {
      //todo replace ChestGenHooks with LootTableLoadEvent
//        ChestGenHooks chestGenHooksDungeon = ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST);
//        chestGenHooksDungeon.addItem(new WeightedRandomChestContent(new ItemStack(Blocks.DRAGON_EGG), 1, 1, 70));
//        // chance < saddle (1/16, ca. 6%, in max 8 slots -> 40% at least 1 egg, 0.48 eggs per chest): I think that's okay
//
//        ChestGenHooks chestGenHooksMineshaft = ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR);
//        chestGenHooksMineshaft.addItem(new WeightedRandomChestContent(new ItemStack(Blocks.DRAGON_EGG), 1, 1, 5));
//        // chance == gold ingot (1/18, ca. 6%, in 3-6 slots -> 23% at least 1 egg, 0.27 eggs per chest):
//        // exploring a random mine shaft in creative mode yielded 2 eggs out of about 10 chests in 1 hour
//
//        ChestGenHooks chestGenHooksJungleChest = ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST);
//        chestGenHooksJungleChest.addItem(new WeightedRandomChestContent(new ItemStack(Blocks.DRAGON_EGG), 1, 1, 15));
//        // chance == gold ingot (15/81, ca. 18%, in 2-5 slots -> 51% at least 1 egg, 0.65 eggs per chest, 1.3 eggs per temple):
//        // jungle temples are so rare, it should be rewarded
//
//        ChestGenHooks chestGenHooksDesertChest = ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST);
//        chestGenHooksDesertChest.addItem(new WeightedRandomChestContent(new ItemStack(Blocks.DRAGON_EGG), 1, 1, 10));
//        // chance == iron ingot (10/76, ca. 13%, in 2-5 slots -> 39% at least 1 egg, 0.46 eggs per chest, 1.8 eggs per temple):
//        // desert temples are so rare, it should be rewarded
    }

    /**
     * returns the EntityPlayerSP if this is the client, otherwise returns null.
     *
     * @return
     */
    public Entity getClientEntityPlayerSP() {
        return null;
    }

    public File getDataDirectory() {
        return FMLServerHandler.instance().getSavesDirectory();
    }

    public void spawnCustomEntityFX(CustomEntityFXTypes entityFXtype,
                                    World world, double x, double y, double z,
                                    double velocityX, double velocityY, double velocityZ) {};
}
