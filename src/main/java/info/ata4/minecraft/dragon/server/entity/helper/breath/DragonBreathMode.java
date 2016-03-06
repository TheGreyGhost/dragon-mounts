package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.entity.DataWatcher;

/** The dragon breath mode - eg for forest; whether the breath is burning or not.  Not used by most breath types.
 * Created by TGG on 5/03/2016.
 */
public class DragonBreathMode {

  public static DragonBreathMode FOREST_NOT_BURNING = new DragonBreathMode(0);
  public static DragonBreathMode FOREST_BURNING = new DragonBreathMode(1);
  public static DragonBreathMode DEFAULT = new DragonBreathMode(0);

  public static DragonBreathMode createFromDataWatcher(DataWatcher dataWatcher, int dataWatcherIndex)
  {
    Integer mode = dataWatcher.getWatchableObjectInt(dataWatcherIndex);
    return new DragonBreathMode(mode);
  }

  public void writeToDataWatcher(DataWatcher dataWatcher, int dataWatcherIndex)
  {
    dataWatcher.updateObject(dataWatcherIndex, breathMode);
  }

  @Override
  public boolean equals(Object comparison)
  {
    if (!(comparison instanceof DragonBreathMode)) return false;
    DragonBreathMode comparisonDBM = (DragonBreathMode)comparison;
    return this.breathMode == comparisonDBM.breathMode;
  }

  @Override
  public int hashCode()
  {
    return breathMode;
  }

//  public int getIntValue() {return breathMode;}

  private DragonBreathMode(int i_breathModeInt)
  {
    breathMode = i_breathModeInt;
  }

  private int breathMode;
}
