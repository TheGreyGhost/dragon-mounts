package info.ata4.minecraft.dragon.test;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import info.ata4.minecraft.dragon.server.entity.helper.breath.EntityBreathProjectileGhost;
import info.ata4.minecraft.dragon.test.testclasses.TestForestBreath;
import net.minecraft.block.BlockLadder;
import net.minecraft.command.CommandClone;
import net.minecraft.command.server.CommandTeleport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Vector;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Example test runner which is called when the player activate the testrunner item.
 * Has an example test of the ladder's behaviour.
 * Created by TGG on 4/01/2016.
 */
public class TestRunner
{
  public boolean runServerSideTest(World worldIn, EntityPlayer playerIn, int testNumber)
  {
    boolean success = false;
    switch (testNumber) {
      case 1: {
//        success = test1(worldIn, playerIn);  // todo restore
        TestForestBreath testForestBreath = new TestForestBreath();
        testForestBreath.test1(worldIn, playerIn);
        break;
      }
      case 2: {
        EntityTameableDragon dragon = new EntityTameableDragon(worldIn);
        Vec3 origin = new Vec3(0, 10, 0);
        Vec3 target = new Vec3(0, 11, 0);
        BreathNode.Power power = BreathNode.Power.SMALL;
        EntityBreathProjectileGhost entity = new EntityBreathProjectileGhost(worldIn, dragon, origin, target, power);
        worldIn.spawnEntityInWorld(entity);
        System.out.println("Lighting spawned: mouth at [x,y,z] = " + origin);
        break;
      }
      default: {
        System.out.println("Test Number " + testNumber + " does not exist on server side.");
        return false;
      }
    }

    System.out.println("Test Number " + testNumber + " called on server side:" + (success ? "success" : "failure"));
    return success;
  }

  public boolean runClientSideTest(World worldIn, EntityPlayer playerIn, int testNumber)
  {
    boolean success = false;

    switch (testNumber) {
      case -1: {  // dummy (do nothing) - can never be called, just to prevent unreachable code compiler error
        break;
      }
      default: {
        System.out.println("Test Number " + testNumber + " does not exist on client side.");
        return false;
      }
    }
    System.out.println("Test Number " + testNumber + " called on client side:" + (success ? "success" : "failure"));

    return success;
  }

  // dummy test: check the correct functioning of the ladder - to see which blocks it can stay attached to
  // The test region contains a ladder attached to a stone block.  We then replace it with different blocks and see
  //   whether the ladder remains or breaks appropriately; eg
  // testA - replace with wood
  // testB - replace with a glass block
  // testC - replace with diamond block
  private boolean test1(World worldIn, EntityPlayer playerIn)
  {
    BlockPos sourceRegionOrigin = new BlockPos(0, 204, 0);
    final int SOURCE_REGION_SIZE_X = 4;
    final int SOURCE_REGION_SIZE_Y = 2;
    final int SOURCE_REGION_SIZE_Z = 3;

    // put a stone block with attached ladder in the middle of our test region
    worldIn.setBlockState(sourceRegionOrigin.add(1, 0, 1), Blocks.stone.getDefaultState());
    worldIn.setBlockState(sourceRegionOrigin.add(2, 0, 1),
                            Blocks.ladder.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.EAST));

    BlockPos testRegionOriginA = new BlockPos(5, 204, 0);
    BlockPos testRegionOriginB = new BlockPos(10, 204, 0);
    BlockPos testRegionOriginC = new BlockPos(15, 204, 0);

    teleportPlayerToTestRegion(playerIn, testRegionOriginA.south(5));  // teleport the player nearby so you can watch

    // copy the test blocks to the destination region
    copyTestRegion(playerIn, sourceRegionOrigin, testRegionOriginA,
                          SOURCE_REGION_SIZE_X, SOURCE_REGION_SIZE_Y, SOURCE_REGION_SIZE_Z);
    copyTestRegion(playerIn, sourceRegionOrigin, testRegionOriginB,
                          SOURCE_REGION_SIZE_X, SOURCE_REGION_SIZE_Y, SOURCE_REGION_SIZE_Z);
    copyTestRegion(playerIn, sourceRegionOrigin, testRegionOriginC,
                          SOURCE_REGION_SIZE_X, SOURCE_REGION_SIZE_Y, SOURCE_REGION_SIZE_Z);

    boolean success = true;
    // testA: replace stone with wood; ladder should remain
    worldIn.setBlockState(testRegionOriginA.add(1, 0, 1), Blocks.log.getDefaultState());
    success &= worldIn.getBlockState(testRegionOriginA.add(2, 0, 1)).getBlock() == Blocks.ladder;

    // testB: replace stone with glass; ladder should be destroyed
    worldIn.setBlockState(testRegionOriginB.add(1, 0, 1), Blocks.glass.getDefaultState());
    success &= worldIn.getBlockState(testRegionOriginB.add(2, 0, 1)).getBlock() == Blocks.air;

    // testC: replace stone with diamond block; ladder should remain
    worldIn.setBlockState(testRegionOriginC.add(1, 0, 1), Blocks.diamond_block.getDefaultState());
    success &= worldIn.getBlockState(testRegionOriginC.add(2, 0, 1)).getBlock() == Blocks.ladder;

    return success;
  }

  /**
   * Teleport the player to the test region (so you can see the results of the test)
   * @param playerIn
   * @param location
   * @return
   */
  public static boolean teleportPlayerToTestRegion(EntityPlayer playerIn, BlockPos location)
  {
    String tpArguments = "@p " + location.getX() + " " + location.getY() + " " + location.getZ();
    String[] tpArgumentsArray = tpArguments.split(" ");

    CommandTeleport commandTeleport = new CommandTeleport();
    try {
      commandTeleport.execute(playerIn, tpArgumentsArray);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  /**
   * Copy a cuboid Test Region from one part of the world to another
   * The cuboid is x blocks wide, by y blocks high, by z blocks long
   * @param entityPlayer
   * @param sourceOrigin origin of the source region
   * @param destOrigin origin of the destination region
   * @param xCount >=1
   * @param yCount >=1
   * @param zCount >=1
   * @return true for success, false otherwise
   */
  public static boolean copyTestRegion(EntityPlayer entityPlayer,
                                 BlockPos sourceOrigin, BlockPos destOrigin,
                                 int xCount, int yCount, int zCount)
  {
    checkArgument(xCount >= 1);
    checkArgument(yCount >= 1);
    checkArgument(zCount >= 1);
    String [] args = new String[9];

    args[0] = String.valueOf(sourceOrigin.getX());
    args[1] = String.valueOf(sourceOrigin.getY());
    args[2] = String.valueOf(sourceOrigin.getZ());
    args[3] = String.valueOf(sourceOrigin.getX() + xCount - 1);
    args[4] = String.valueOf(sourceOrigin.getY() + yCount - 1);
    args[5] = String.valueOf(sourceOrigin.getZ() + zCount - 1);
    args[6] = String.valueOf(destOrigin.getX());
    args[7] = String.valueOf(destOrigin.getY());
    args[8] = String.valueOf(destOrigin.getZ());

    CommandClone commandClone = new CommandClone();
    try {
      commandClone.execute(entityPlayer, args);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

}
