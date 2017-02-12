package info.ata4.minecraft.dragon.server;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.awt.*;

/**
 * Created by TGG on 3/07/2015.
 * The Dragon Orb.
 */
public class ItemDragonOrb extends Item {

  public ItemDragonOrb()
  {
    final int MAXIMUM_NUMBER_OF_ORBS = 1;
    this.setMaxStackSize(MAXIMUM_NUMBER_OF_ORBS);
    this.setCreativeTab(CreativeTabs.MISC);   // the item will appear on the Miscellaneous tab in creative
  }

  static boolean errorPrinted = false;

  /**
   * returns the action that specifies what animation to play when the items is being used
   */
  @Override
  public EnumAction getItemUseAction(ItemStack stack)
  {
    return EnumAction.NONE;
  }

  public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
  {
    playerIn.setActiveHand(hand);
    return new ActionResult(EnumActionResult.PASS, itemStackIn);
  }

  /**
   * How long to hold the block action for
   */
  @Override
  public int getMaxItemUseDuration(ItemStack stack)
  {
    return 72000;
  }

// move to DragonOrbColour class
//  // cycling glow effect for the orb jewel by varying the layer brightness
//  @Override
//  public int getColorFromItemStack(ItemStack stack, int layerNumber)
//  {
//    switch (layerNumber) {
//      case 0:  {  // claw
//        return Color.WHITE.getRGB();
//      }
//      case 1: {   // orb jewel
//        final long GLOW_CYCLE_PERIOD_SECONDS = 4;
//        final float MIN_GLOW_BRIGHTNESS = 0.4F;
//        final float MAX_GLOW_BRIGHTNESS = 1.0F;
//        final long NANO_SEC_PER_SEC = 1000L * 1000L * 1000L;
//
//        long cyclePosition = System.nanoTime() % (GLOW_CYCLE_PERIOD_SECONDS * NANO_SEC_PER_SEC);
//        double cyclePosRadians = 2 * Math.PI * cyclePosition / (double)(GLOW_CYCLE_PERIOD_SECONDS * NANO_SEC_PER_SEC);
//        final float BRIGHTNESS_MIDPOINT = (MIN_GLOW_BRIGHTNESS + MAX_GLOW_BRIGHTNESS) / 2.0F;
//        final float BRIGHTNESS_AMPLITUDE = (MAX_GLOW_BRIGHTNESS - BRIGHTNESS_MIDPOINT);
//        float brightness = BRIGHTNESS_MIDPOINT + BRIGHTNESS_AMPLITUDE * MathX.sin((float)cyclePosRadians);
//        int brightnessInt = MathHelper.clamp_int((int) (255 * brightness), 0, 255);
//        Color orbBrightness = new Color(brightnessInt, brightnessInt, brightnessInt);
//        return orbBrightness.getRGB();
//      }
//      default: {
//        if (!errorPrinted) {
//          System.out.println("invalid layer number");
//          errorPrinted = true;
//        }
//        return Color.WHITE.getRGB();
//      }
//    }
//  }

}
