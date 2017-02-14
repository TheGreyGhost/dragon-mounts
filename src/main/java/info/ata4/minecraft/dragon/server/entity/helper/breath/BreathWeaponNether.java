package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/**
 * Created by TGG on 20/03/2016.
 */
public class BreathWeaponNether extends BreathWeapon {
  public BreathWeaponNether(EntityTameableDragon i_dragon) {
    super(i_dragon);
  }

  @Override
  public BreathAffectedBlock affectBlock(World world, Vec3i blockPosition, BreathAffectedBlock currentHitDensity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BreathAffectedEntity affectEntity(World world, Integer entityID, BreathAffectedEntity currentHitDensity) {
    throw new UnsupportedOperationException();
  }
}
