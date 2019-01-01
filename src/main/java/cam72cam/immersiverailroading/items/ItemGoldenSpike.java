package cam72cam.immersiverailroading.items;

import cam72cam.immersiverailroading.IRBlocks;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.tile.TileRailPreview;
import cam72cam.immersiverailroading.util.BlockUtil;
import cam72cam.immersiverailroading.util.PlacementInfo;
import cam72cam.immersiverailroading.util.math.BlockPos;
import cam72cam.immersiverailroading.util.nbt.NBTUtil;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class ItemGoldenSpike extends Item {
	public static final String NAME = "item_golden_spike";
	
	public ItemGoldenSpike() {
		super();
		setUnlocalizedName(ImmersiveRailroading.MODID + ":" + NAME);
		//setRegistryName(new ResourceLocation(ImmersiveRailroading.MODID, NAME));
        this.setCreativeTab(ItemTabs.MAIN_TAB);
	}
	
	public boolean onItemUse(EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack held = player.getHeldItem();
		if (world.getBlockState(pos).getBlock() == IRBlocks.BLOCK_RAIL_PREVIEW) {
			setPosition(held, pos);
			world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.5f, 0.2f, false);
		} else {
			pos = pos.up();
			
			BlockPos tepos = getPosition(held);
			if (tepos != null) {
				if (BlockUtil.canBeReplaced(world, pos.down(), true)) {
					pos = pos.down();
				}
				TileRailPreview tr = TileRailPreview.get(world, tepos);
				if (tr != null) {
					tr.setCustomInfo(new PlacementInfo(tr.getItem(), player.getRotationYawHead(), pos, hitX, hitY, hitZ));
				}
			}
		}
		//TODO
		return true;
	}

	public static BlockPos getPosition(ItemStack stack) {
		if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("pos")) { 
			return NBTUtil.getPosFromTag(stack.getTagCompound().getCompoundTag("pos"));
		} else {
			return null;
		}
	}
	
	public static void setPosition(ItemStack stack, BlockPos pos) {
		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setTag("pos", NBTUtil.createPosTag(pos));
	}
}
