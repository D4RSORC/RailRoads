package cam72cam.immersiverailroading.util;

import cam72cam.immersiverailroading.util.math.EnumParticleTypes;
import cam72cam.immersiverailroading.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleUtil {
	private ParticleUtil() {
		// Disable construction since java does not have static classes
	}
	
	public static void spawnParticle(World world, EnumParticleTypes type, Vec3d position) {
		world.spawnParticle(type.getParticleName(), position.x, position.y, position.z, 0, 0, 0);
	}
}
