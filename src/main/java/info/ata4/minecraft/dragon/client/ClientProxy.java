/*
 ** 2012 August 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client;

import com.sun.javaws.exceptions.InvalidArgumentException;
import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.gui.GuiDragonDebug;
import info.ata4.minecraft.dragon.client.handler.*;
import info.ata4.minecraft.dragon.client.render.*;
import info.ata4.minecraft.dragon.server.CommonProxy;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.breeds.EnumDragonBreed;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathGhost;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileEnder;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileGhost;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileNether;
import info.ata4.minecraft.dragon.test.StartupClientOnly;
import info.ata4.minecraft.dragon.test.StartupCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import java.io.File;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ClientProxy extends CommonProxy {
  private static final int DEFAULT_ITEM_SUBTYPE = 0;

    @Override
    public void onPreInit(FMLPreInitializationEvent evt) {
        super.onPreInit(evt);
        DragonMounts.instance.getConfig().clientInit();
        MinecraftForge.EVENT_BUS.register(new TextureStitcherBreathFX());

      // register dragon entity renderer
      RenderingRegistry.registerEntityRenderingHandler(
              EntityTameableDragon.class, DragonRenderer::new);

      // register item renderer for dragon egg block variants
      ResourceLocation eggModelItemLoc = new ResourceLocation(DragonMounts.AID, "dragon_egg");
      Item itemBlockDragonEgg = Item.REGISTRY.getObject(eggModelItemLoc);
      EnumDragonBreed.META_MAPPING.forEach((breed, meta) -> {
        ModelResourceLocation eggModelLoc = new ModelResourceLocation(DragonMounts.AID + ":dragon_egg", "breed=" + breed.getName());
        ModelLoader.setCustomModelResourceLocation(itemBlockDragonEgg, meta, eggModelLoc);
      });

      RenderingRegistry.registerEntityRenderingHandler(EntityBreathProjectileNether.class,
                                                       new BreathEntityRendererNether.BERNetherFactory(1.0F));
      RenderingRegistry.registerEntityRenderingHandler(EntityBreathProjectileEnder.class,
              new BreathEntityRendererEnder.BEREnderFactory(1.0F));
      RenderingRegistry.registerEntityRenderingHandler(EntityBreathGhost.class,
              BreathEntityRendererGhost::new);
      RenderingRegistry.registerEntityRenderingHandler(EntityBreathProjectileGhost.class,         // dummy - renders blank
              NullEntityRenderer::new);

      ModelResourceLocation itemModelResourceLocation =
              new ModelResourceLocation("dragonmounts:dragonorb", "inventory");
      ModelLoader.setCustomModelResourceLocation(itemDragonOrb, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);

      StartupClientOnly.preInitClientOnly();
    }

    @Override
    public void onInit(FMLInitializationEvent evt) {
      super.onInit(evt);

      StartupClientOnly.initClientOnly();
    }

    @Override
    public void onPostInit(FMLPostInitializationEvent event) {
        super.onPostInit(event);
        if (DragonMounts.instance.getConfig().isDebug()) {
            MinecraftForge.EVENT_BUS.register(new GuiDragonDebug());
        }

    FMLCommonHandler.instance().bus().register(new DragonControl(getNetwork()));
    DragonOrbControl.createSingleton(getNetwork());
    DragonOrbControl.initialiseInterceptors();
    FMLCommonHandler.instance().bus().register(DragonOrbControl.getInstance());
    MinecraftForge.EVENT_BUS.register(new TargetHighlighter());
    FMLCommonHandler.instance().bus().register(new DragonEntityWatcher());

    Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new DragonOrbColour(), DragonMounts.proxy.itemDragonOrb);

    StartupClientOnly.postInitClientOnly();
  }

    /**
     * returns the EntityPlayerSP if this is the client, otherwise returns null.
     *
     * @return
     */
    @Override
    public Entity getClientEntityPlayerSP() {
        return Minecraft.getMinecraft().thePlayer;
    }

    @Override
    public File getDataDirectory() {
        return Minecraft.getMinecraft().mcDataDir;
    }

  @Override
  public void spawnCustomEntityFX(CustomEntityFXTypes entityFXtype,
                                  World world, double x, double y, double z,
                                  double velocityX, double velocityY, double velocityZ)
  {
    Particle entityFXToSpawn;
    switch (entityFXtype) {
      case ENDERTRAIL: {
        entityFXToSpawn = new EntityFXEnderTrail(world, x, y, z, velocityX, velocityY, velocityZ);
        break;
      }
      default: {
        throw new IllegalArgumentException(entityFXtype.toString());
      }
    }

    Minecraft.getMinecraft().effectRenderer.addEffect(entityFXToSpawn);
  }

}
