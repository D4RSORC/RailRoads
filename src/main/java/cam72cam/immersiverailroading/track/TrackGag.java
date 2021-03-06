package cam72cam.immersiverailroading.track;

import net.minecraft.tileentity.TileEntity;
import cam72cam.immersiverailroading.IRBlocks;
import cam72cam.immersiverailroading.tile.TileRailGag;
import cam72cam.immersiverailroading.util.math.BlockPos;

public class TrackGag extends TrackBase {
	public TrackGag(BuilderBase builder, BlockPos rel) {
		super(builder, rel, IRBlocks.BLOCK_RAIL_GAG);
	}

	@Override
	public TileEntity placeTrack() {
		TileRailGag tileGag = (TileRailGag) super.placeTrack();
		
		tileGag.setFlexible(isFlexible());
		
		return tileGag;
	}
}
