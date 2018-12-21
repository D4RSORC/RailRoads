package cam72cam.immersiverailroading.blocks;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.util.math.BlockPos;
import cam72cam.immersiverailroading.util.math.NonNullList;

public class BlockRail extends BlockRailBase {
	public static final String NAME = "block_rail";

	public BlockRail() {
        setBlockName(ImmersiveRailroading.MODID + ":" + NAME);
        //setRegistryName(new ResourceLocation(ImmersiveRailroading.MODID, NAME));
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return null;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return true;
	}
	
	@Override
	public int getRenderType() {
		// TESR Renderer
		return 1;
	}

	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int meta) {
		return new TileRail();
	}
}
