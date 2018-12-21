package cam72cam.immersiverailroading.blocks;

import java.util.Random;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.tile.TileRailGag;

public class BlockRailGag extends BlockRailBase {

	public static final String NAME = "block_rail_gag";
	
	public BlockRailGag() {
		super();
        setBlockName(ImmersiveRailroading.MODID + ":" + NAME);
        //setRegistryName(new ResourceLocation(ImmersiveRailroading.MODID, NAME));
	}
	
	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return false;
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 0;
	}

	@Override
	public boolean hasTileEntity(int state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		return new TileRailGag();
	}

	@Override
	public int getRenderType() {
		return 0;
	}
}