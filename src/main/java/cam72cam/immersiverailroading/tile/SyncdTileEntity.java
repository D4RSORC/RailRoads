package cam72cam.immersiverailroading.tile;

import cam72cam.immersiverailroading.util.math.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SyncdTileEntity extends TileEntity {
	public boolean hasTileData;
	
	public static SyncdTileEntity get(IBlockAccess world, BlockPos pos) {
		TileEntity te;
		if (world instanceof World) {
			te = ((World)world).getChunkFromBlockCoords(pos.getX(), pos.getZ()).func_150806_e(pos.getX(), pos.getY(), pos.getZ());
		} else {
			te = world.getTileEntity(pos.getX(),pos.getY(),pos.getZ());
		}
		
		if (te instanceof SyncdTileEntity) {
			return (SyncdTileEntity) te;
		}
		return null;
	}
	
	public boolean isLoaded() {
		return this.hasWorldObj() && (!worldObj.isRemote || hasTileData);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (!worldObj.isRemote) {
			worldObj.notifyBlockUpdate(getPos(), worldObj.getBlockState(getPos()), worldObj.getBlockState(getPos()), 1 + 2 + 8);
			worldObj.notifyNeighborsOfStateChange(pos, this.getBlockType(), true);
		}
	}
	
	public void writeUpdateNBT(NBTTagCompound nbt) {
	}
	public void readUpdateNBT(NBTTagCompound nbt) {
	}
	
	
	@Override
	public S35PacketUpdateTileEntity getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		this.writeUpdateNBT(nbt);
		
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.func_148857_g());
		this.readUpdateNBT(pkt.func_148857_g());
		super.onDataPacket(net, pkt);
		if (updateRerender()) {
			worldObj.markBlockRangeForRenderUpdate(this.xCoord, this.yCoord, this.zCoord, this.xCoord, this.yCoord, this.zCoord);
		}
		hasTileData = true;
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		this.writeToNBT(tag);
		this.writeUpdateNBT(tag);
		return tag;
	}
	
	public boolean updateRerender() {
		return false;
	}
	
	@Override 
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
		this.readUpdateNBT(tag);
		super.handleUpdateTag(tag);
		if (updateRerender()) {
			worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
		}
		hasTileData = true;
	}
}
