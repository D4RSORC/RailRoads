package cam72cam.immersiverailroading.util;

import java.util.List;

import cam72cam.immersiverailroading.entity.EntityMoveableRollingStock;
import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.items.nbt.ItemGauge;
import cam72cam.immersiverailroading.items.nbt.ItemTextureVariant;
import cam72cam.immersiverailroading.library.ChatText;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.ItemComponentType;
import cam72cam.immersiverailroading.physics.MovementSimulator;
import cam72cam.immersiverailroading.physics.TickPos;
import cam72cam.immersiverailroading.Config.ConfigDebug;
import cam72cam.immersiverailroading.entity.EntityBuildableRollingStock;
import cam72cam.immersiverailroading.entity.EntityCoupleableRollingStock.CouplerType;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.util.math.BlockPos;
import cam72cam.immersiverailroading.util.math.EnumFacing;
import cam72cam.immersiverailroading.util.math.Vec3d;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import trackapi.lib.ITrack;
import trackapi.lib.Util;

public class SpawnUtil {
	public static void placeStock(EntityPlayer player, World worldIn, BlockPos pos, EntityRollingStockDefinition def, List<ItemComponentType> list) {
		ITrack initte = Util.getTileEntity(worldIn, new Vec3d(pos.add(0, 0.7, 0)).getVec(), true);
		if (initte == null) {
			return;
		}
		double trackGauge = initte.getTrackGauge();
		Gauge gauge = Gauge.from(trackGauge);
		
		
		if (!player.capabilities.isCreativeMode && gauge != ItemGauge.get(player.getHeldItem())) {
			player.addChatMessage(ChatText.STOCK_WRONG_GAUGE.getMessage());
			return;
		}
		
		double offset = def.getCouplerPosition(CouplerType.BACK, gauge) - ConfigDebug.couplerRange;
		float yaw = player.rotationYawHead;
		TickPos tp = new MovementSimulator(worldIn, new TickPos(0, Speed.ZERO, new Vec3d(pos.add(0, 0.7, 0)).addVector(0.5, 0, 0.5), yaw, yaw, yaw, 0, false), def.getBogeyFront(gauge), def.getBogeyRear(gauge), gauge.value()).nextPosition(offset);
		
		if (!tp.isOffTrack) {
			if (!worldIn.isRemote) {
				String texture = ItemTextureVariant.get(player.getHeldItem());
				EntityRollingStock stock = def.spawn(worldIn, tp.position, EnumFacing.fromAngle(player.rotationYawHead), gauge, texture);
				
				if (stock instanceof EntityBuildableRollingStock) {
					((EntityBuildableRollingStock)stock).setComponents(list);
				}
				
				if (stock instanceof EntityMoveableRollingStock) {
					// snap to track
					EntityMoveableRollingStock mrs = (EntityMoveableRollingStock)stock;
					tp.speed = Speed.ZERO;
					mrs.initPositions(tp);
				}
			}
			if (!player.capabilities.isCreativeMode) {
				ItemStack stack = player.getHeldItem();
				stack.stackSize = stack.stackSize-1;
				player.setCurrentItemOrArmor(0,stack);
			}
			return;
		}
		return;
	}
}
