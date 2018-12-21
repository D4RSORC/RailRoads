package cam72cam.immersiverailroading.blocks;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.tile.TileMultiblock;
import cam72cam.immersiverailroading.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BlockMultiblock extends Block {

	public static final String NAME = "multiblock";

	public BlockMultiblock() {
		super(Material.iron);
		setHardness(2.0F);
        setBlockName(ImmersiveRailroading.MODID + ":" + NAME);
        //setRegistryName(new ResourceLocation(ImmersiveRailroading.MODID, NAME));
	}
	
	@Override
	public void breakBlock(World worldIn, int x, int y, int z, Block block, int meta) {
		BlockPos pos = new BlockPos(x,y,z);
		TileMultiblock te = TileMultiblock.get(worldIn, pos);
		if (te != null) {
			// Multiblock break
			try {
				te.breakBlock();
			} catch (Exception ex) {
				ImmersiveRailroading.catching(ex);
				// Something broke
				// TODO figure out why
				worldIn.setBlockToAir(x, y, z);
			}
			worldIn.func_147480_a(x, y, z, true);
		} else {
			// Break during block restore
			super.breakBlock(worldIn, x, y, z, block, meta);
		}
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer playerIn, int facing, float hitX, float hitY, float hitZ) {
		BlockPos pos = new BlockPos(x, y, z);
		TileMultiblock te = TileMultiblock.get(worldIn, pos);
		if (te != null) {
			return te.onBlockActivated(playerIn);
		}
		return false;
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int meta) {
		return new TileMultiblock();
	}
	
	@Override
	public int getRenderType() {
		// TESR Renderer
		return 0;
	}


	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
}
