package cam72cam.immersiverailroading.blocks;

import java.util.Random;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.library.GuiTypes;
import cam72cam.immersiverailroading.tile.TileRailPreview;
import cam72cam.immersiverailroading.util.PlacementInfo;
import cam72cam.immersiverailroading.util.math.BlockPos;
import cpw.mods.fml.common.registry.GameRegistry.ItemStackHolder;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRailPreview extends Block {
	public static final String NAME = "block_rail_preview";
	public BlockRailPreview() {
		super(Material.carpet);
		
		setBlockName(ImmersiveRailroading.MODID + ":" + NAME);
       //setRegistryName(new ResourceLocation(ImmersiveRailroading.MODID, NAME));
	}

	public static boolean tryBreakPreview(World world, BlockPos pos, EntityPlayer entityPlayer) {
		if (entityPlayer.isSneaking()) {
			TileRailPreview tr = TileRailPreview.get(world, pos);
			if (tr != null) {
				world.setBlockToAir(pos.getX(),pos.getY(),pos.getZ());
				tr.getRailRenderInfo().build(entityPlayer);
				return true;
			}
		}
		return false;
	}
	
	@Override
	@Deprecated //Forge: State sensitive version
    public float getExplosionResistance(Entity exploder) {
        return 2000;
    }
	
	@Override
	public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer playerIn, int meta, float hitX, float hitY, float hitZ) {
		BlockPos pos = new BlockPos(x,y,z);
		if (playerIn.isSneaking()) {
			if (!worldIn.isRemote) {
				TileRailPreview te = TileRailPreview.get(worldIn, pos);
				if (te != null) {
					te.setPlacementInfo(new PlacementInfo(te.getItem(), playerIn.rotationYawHead, pos, hitX, hitY, hitZ));
				}
			}
			return false;
		} else {
			if (playerIn.getHeldItem().getItem() == IRItems.ITEM_GOLDEN_SPIKE) {
				return false;
			}
			playerIn.openGui(ImmersiveRailroading.instance, GuiTypes.RAIL_PREVIEW.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
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
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		BlockPos pos = new BlockPos(x,y,z);
		TileRailPreview te = TileRailPreview.get(world, pos);
		if (te != null) {
			return te.getItem();
		}
		return null;
	}

	@Override
	public boolean hasTileEntity(int state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int state) {
		return new TileRailPreview();
	}

	@Override
	public int getRenderType() {
		return 0;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
		float height = 0.125F;
		return AxisAlignedBB.getBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, height+0.1, 1.0F);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		float height = 0.125F;
		return AxisAlignedBB.getBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, height, 1.0F);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
	{
		return  getCollisionBoundingBox(state, worldIn, pos).expand(0, 0.1, 0).offset(pos);
	}
	
	
	/*
	 * Fence, glass override
	 */
	@Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
		return false;
	}
	@Deprecated
	@Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess p_193383_1_, IBlockState p_193383_2_, BlockPos p_193383_3_, EnumFacing p_193383_4_)
    {
        return BlockFaceShape.UNDEFINED;
    }
}
