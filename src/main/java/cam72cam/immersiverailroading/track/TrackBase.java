package cam72cam.immersiverailroading.track;

import cam72cam.immersiverailroading.Config;
import cam72cam.immersiverailroading.blocks.BlockRailBase;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.immersiverailroading.util.BlockUtil;
import cam72cam.immersiverailroading.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public abstract class TrackBase {
	public BuilderBase builder;

	protected BlockPos rel;
	private float bedHeight;
	private float railHeight;

	protected Block block;

	private boolean flexible = false;

	private BlockPos parent;

	public boolean solidNotRequired;

	public TrackBase(BuilderBase builder, BlockPos rel, Block block) {
		this.builder = builder;
		this.rel = rel;
		this.block = block;
	}

	@SuppressWarnings("deprecation")
	public boolean canPlaceTrack() {
		BlockPos pos = getPos();
		IBlockState down = builder.info.world.getBlockState(pos.down());
		boolean downOK = (down.isTopSolid() || !Config.ConfigDamage.requireSolidBlocks && !builder.info.world.isAirBlock(pos.down().getX(),pos.down().getY(),pos.down().getZ())) ||
				(BlockUtil.canBeReplaced(builder.info.world, pos.down(), false) && builder.info.settings.railBedFill.getItem() != null) ||
				solidNotRequired || BlockUtil.isIRRail(builder.info.world, pos);
		return BlockUtil.canBeReplaced(builder.info.world, pos, flexible || builder.overrideFlexible) && downOK;
	}

	public TileEntity placeTrack() {
		BlockPos pos = getPos();

		if (builder.info.settings.railBedFill.getItem() != null && BlockUtil.canBeReplaced(builder.info.world, pos.down(), false)) {
			builder.info.world.setBlockState(pos.down(), BlockUtil.itemToBlockState(builder.info.settings.railBedFill));
		}
		
		NBTTagCompound replaced = null;
		
		IBlockState state = builder.info.world.getBlockState(pos);
		Block removed = state.getBlock();
		TileRailBase te = null;
		if (removed != null) {
			if (removed instanceof BlockRailBase) {
				te = TileRailBase.get(builder.info.world, pos);
				if (te != null) {					
					replaced = te.serializeNBT();
				}
			} else {				
				removed.dropBlockAsItem(builder.info.world, pos, state, 0);
			}
		}
		
		if (te != null) {
			te.setWillBeReplaced(true);
		}
		builder.info.world.setBlockState(pos, getBlockState(), 3);
		if (te != null) {
			te.setWillBeReplaced(false);
		}
		
		TileRailBase tr = TileRailBase.get(builder.info.world, pos);
		tr.setReplaced(replaced);
		if (parent != null) {
			tr.setParent(parent);
		} else {
			tr.setParent(builder.getParentPos());
		}
		tr.setRailHeight(getRailHeight());
		tr.setBedHeight(getBedHeight());
		return tr;
	}
	public IBlockState getBlockState() {
		return block.getDefaultState();
	}

	public BlockPos getPos() {
		return builder.convertRelativePositions(rel);
	}

	public void setHeight(float height) {
		setBedHeight(height);
		setRailHeight(height);
	}
	public void setBedHeight(float height) {
		this.bedHeight = height;
	}
	public float getBedHeight() {
		return bedHeight;
	}
	public void setRailHeight(float height) {
		this.railHeight = height;
	}
	public float getRailHeight() {
		return railHeight;
	}

	public void setFlexible() {
		this.flexible  = true;
	}

	public boolean isFlexible() {
		return this.flexible;
	}

	public void overrideParent(BlockPos blockPos) {
		this.parent = blockPos;
	}
}
