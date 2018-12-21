package cam72cam.immersiverailroading.util;

import cam72cam.immersiverailroading.util.math.BlockPos;
import cam72cam.immersiverailroading.util.math.EnumFacing;
import net.minecraft.world.World;

public class RedstoneUtil {

	public static int getPower(World world, BlockPos pos) {
		int power = 0;
		for (EnumFacing facing : EnumFacing.VALUES) {
			power = Math.max(power, world.getIndirectPowerLevelTo(pos.offset(facing).getX(),pos.offset(facing).getY(), pos.offset(facing).getZ(), facing.getIndex()));
		}
		return power;
	}

}
