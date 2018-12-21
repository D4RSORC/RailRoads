package cam72cam.immersiverailroading.render.entity;

import cam72cam.immersiverailroading.util.math.Vec3d;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class MagicEntity extends Entity {
	/*
	 * Minecraft does NOT support rendering entities which overlap with the field of view but don't exist in it
	 * 
	 * For large entities this breaks in awesome ways, like walking past the center of a rail car
	 * 
	 * To fix this we create an entity which is always rendered and render all of our stuff inside of that 
	 * 
	 */

	public MagicEntity(World worldIn) {
		super(worldIn);
		this.forceSpawn = true;
	}
	
	@Override
	public void onUpdate() {
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		if (player == null) {
			return;
		}
		Vec3d pos = player.getPositionEyes(0);
		pos = pos.add(player.getLookVec().scale(2));
		this.setPosition(pos.x, pos.y, pos.z);
	}

	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		return null;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}
	
	public boolean shouldRenderInPass(int pass)
    {
		return true;
    }
	
	@Override
	public boolean isInRangeToRender3d(double x, double y, double z) {
		return true;
	}

	@Override
	protected void entityInit() {
		this.setEntityId(-31415);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
	}
}
