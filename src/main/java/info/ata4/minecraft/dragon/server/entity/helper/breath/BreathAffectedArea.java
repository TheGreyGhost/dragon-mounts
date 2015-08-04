package info.ata4.minecraft.dragon.server.entity.helper.breath;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.*;

/**
* Created by TGG on 30/07/2015.
* BreathAffectedArea base class
- generated by BreedHelper
- stores the area of effect; called every tick with the breathing direction; applies the effects of the breath weapon
- derived classes for each type of breath
Ctor
- update (not breathing) or update(start, finish)
- affectBlock for each block
- affectEntity for each entity
*/
public class BreathAffectedArea
{

  public void continueBreathing(World world, Vec3 origin, Vec3 destination, BreathNode.Power power)
  {
    Vec3 direction = destination.subtract(origin).normalize();

    EntityBreathNodeServer newNode = EntityBreathNodeServer.createEntityBreathNodeServer(
            world, origin.xCoord, origin.yCoord, origin.zCoord, direction.xCoord, direction.yCoord, direction.zCoord,
            power);

    entityBreathNodes.add(newNode);
  }

  public void updateTick() {
    List<NodeLineSegment> segments = new LinkedList<NodeLineSegment>();

    Iterator<EntityBreathNodeServer> it = entityBreathNodes.iterator();
    while (it.hasNext()) {
      EntityBreathNodeServer entity = it.next();
      if (entity.isDead) {
        it.remove();
      } else {
        float radius = entity.getCurrentRadius();
        Vec3 initialPosition = entity.getPositionVector();
        entity.onUpdate();
        Vec3 finalPosition = entity.getPositionVector();
        segments.add(new NodeLineSegment(initialPosition, finalPosition, radius));
      }
    }

    to be continued from here: collisions then actions

  }

  private void drawBreathNode(HashSet<BlockPos> blocksInBeam, HashSet<Integer> entitiesInBeam, Vec3 origin, Vec3 direction, float distance)
  {

  }

  private void moveNode()
  {

  }

  /**
   * Models the collision of the breath nodes on the world blocks and entities:
   * Each breathnode which contacts a world block will increase the corresponding 'hit density' by an amount proportional
   *   to the intensity of the node and the degree of overlap between the node and the block.
   * Likewise for the entities contacted by the breathnode
   * @param world
   * @param breathNodes the breathnodes in the breath weapon beam
   * @param affectedBlocks each block touched by the beam has an entry in this map.  The hitDensity (float) is increased
   *                       every time a node touches it.  blocks without an entry haven't been touched.
   * @param affectedEntities every entity touched by the beam has an entry in this map (entityID).  The hitDensity (float)
   *                         for an entity is increased every time a node touches it.  entities without an entry haven't
   *                         been touched.
   */
  private void compileAffected(World world, Map<NodeLineSegment, BreathNode> breathNodes,
                               HashMap<Vec3i, Float> affectedBlocks, HashMap<Integer, Float> affectedEntities)
  {
    if (breathNodes.isEmpty()) return;

    ArrayList<NodeLineSegment> nodeLineSegments = new ArrayList<NodeLineSegment>(breathNodes.keySet());

    final int NUMBER_OF_CLOUD_POINTS = 10;
    for (Map.Entry<NodeLineSegment, BreathNode> segment : breathNodes.entrySet()) {
      float intensity = segment.getValue().getCurrentIntensity();
      segment.getKey().addStochasticCloud(affectedBlocks, intensity, NUMBER_OF_CLOUD_POINTS);
    }

    AxisAlignedBB allAABB = NodeLineSegment.getAxisAlignedBoundingBoxForAll(nodeLineSegments);
    List<EntityLivingBase> allEntities = world.getEntitiesWithinAABB(EntityLivingBase.class, allAABB);

    Multimap<Vec3i, Integer> occupiedByEntities = ArrayListMultimap.create();
    Map<Integer, AxisAlignedBB> entityHitBoxes = new HashMap<Integer, AxisAlignedBB>();
    for (EntityLivingBase entityLivingBase : allEntities) {
      AxisAlignedBB aabb = entityLivingBase.getEntityBoundingBox();
      entityHitBoxes.put(entityLivingBase.getEntityId(), aabb);
      for (int x = (int)aabb.minX; x <= (int)aabb.maxX; ++x) {
        for (int y = (int)aabb.minY; y <= (int)aabb.maxY; ++y) {
          for (int z = (int)aabb.minZ; z <= (int)aabb.maxZ; ++z) {
            Vec3i pos = new Vec3i(x, y, z);
            occupiedByEntities.put(pos, entityLivingBase.getEntityId());
          }
        }
      }
    }

    final int NUMBER_OF_ENTITY_CLOUD_POINTS = 10;
    for (Map.Entry<NodeLineSegment, BreathNode> node : breathNodes.entrySet()) {
      Set<Integer> checkedEntities = new HashSet<Integer>();
      NodeLineSegment nodeLineSegment = node.getKey();
      AxisAlignedBB aabb = nodeLineSegment.getAxisAlignedBoundingBox();
      for (int x = (int)aabb.minX; x <= (int)aabb.maxX; ++x) {
        for (int y = (int)aabb.minY; y <= (int)aabb.maxY; ++y) {
          for (int z = (int)aabb.minZ; z <= (int)aabb.maxZ; ++z) {
            Vec3i pos = new Vec3i(x, y, z);
            Collection<Integer> entitiesHere = occupiedByEntities.get(pos);
            if (entitiesHere != null) {
              for (Integer entityID : entitiesHere) {
                if (!checkedEntities.contains(entityID)) {
                  checkedEntities.add(entityID);
                  float intensity = node.getValue().getCurrentIntensity();
                  float hitDensity = nodeLineSegment.collisionCheckAABB(aabb, intensity, NUMBER_OF_ENTITY_CLOUD_POINTS);
                  if (hitDensity > 0.0) {
                    Float currentDensity = affectedEntities.get(entityID);
                    if (currentDensity == null) {
                      currentDensity = 0.0F;
                    }
                    currentDensity += hitDensity;
                    affectedEntities.put(entityID, currentDensity);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private LinkedList<EntityBreathNodeServer> entityBreathNodes;

}
