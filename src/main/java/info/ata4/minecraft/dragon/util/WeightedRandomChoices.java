package info.ata4.minecraft.dragon.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by TGG on 2/01/2016.
 *
 * A collection to hold objects with a weight for each object;
 * intended to be used to obtain an object randomly with a different weighted probability for each.
 */
public class WeightedRandomChoices <E> {

  /**
   * Add an object to the collection
   * @param element object to add
   * @param weight the weight of this object (1 - MAX_WEIGHT)
   */
  public void addObject(E element, int weight)
  {
    checkArgument(weight > 0);
    checkArgument(weight <= MAX_WEIGHT);
    weights.add(weight);
    runningSum += weight;
    cumulativeWeights.add(runningSum);
    objects.add(element);
  }

  /**
   * Pick one of the objects in the collection at random according to its weight
   * @param random
   * @return the randomly-chosen object
   */
  public E pickRandom(Random random)
  {
    int chosenWeight = random.nextInt(runningSum);
    int index = Collections.binarySearch(cumulativeWeights, chosenWeight);
    if (index < 0) {
      index = -(index + 1);
    }
    assert index < runningSum;
    return objects.get(index);
  }

  public final int MAX_WEIGHT = 1000;
  private int runningSum = 0;
  private ArrayList<Integer> weights = new ArrayList<Integer>();
  private ArrayList<Integer> cumulativeWeights = new ArrayList<Integer>();
  private ArrayList<E> objects = new ArrayList<E>();
}
