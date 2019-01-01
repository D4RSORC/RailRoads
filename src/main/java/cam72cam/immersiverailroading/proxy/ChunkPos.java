package cam72cam.immersiverailroading.proxy;

import cam72cam.immersiverailroading.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ChunkPos {
	public final int dim;
	public final int chunkX;
	public final int chunkZ;
	
	public ChunkPos(World world, BlockPos pos) {
		dim = world.provider.dimensionId;
		Chunk chunk = world.getChunkFromBlockCoords(pos.getX(), pos.getZ());
		chunkX = chunk.xPosition;
		chunkZ = chunk.zPosition;
	}
	
	public ChunkPos(Entity entity) {
		this(entity.worldObj, new BlockPos(entity.posX,entity.posY,entity.posZ));
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ChunkPos) {
			ChunkPos other = (ChunkPos)o;
			return other.dim == dim && other.chunkX == chunkX && other.chunkZ == chunkZ;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return dim + chunkX + chunkZ;
	}
}