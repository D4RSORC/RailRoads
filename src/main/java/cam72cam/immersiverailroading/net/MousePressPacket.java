package cam72cam.immersiverailroading.net;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import cam72cam.immersiverailroading.entity.EntityRidableRollingStock;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class MousePressPacket implements IMessage {
	private int dimension;
	private int mouseButton;
	private int targetEntityID;
	
	public MousePressPacket() {
		// For Reflection
	}
	
	@SideOnly(Side.CLIENT)
	public MousePressPacket(int button, int dimension, int targetEntityID) {
		this.dimension = dimension;
		this.mouseButton = button;
		this.targetEntityID = targetEntityID;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.dimension = buf.readInt();
		this.mouseButton = buf.readInt();
		this.targetEntityID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(dimension);
		buf.writeInt(mouseButton);
		buf.writeInt(targetEntityID);
	}
	
	public static class Handler implements IMessageHandler<MousePressPacket, IMessage> {
		@Override
		public IMessage onMessage(MousePressPacket message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private void handle(MousePressPacket message, MessageContext ctx) {
			List<EntityRidableRollingStock> matches = ctx.getServerHandler().playerEntity.getServerWorld().getEntities(EntityRidableRollingStock.class, new Predicate<EntityRidableRollingStock>()
		    {
		        @Override
				public boolean apply(@Nullable EntityRidableRollingStock entity)
		        {
		            return entity != null && entity.getEntityId() == message.targetEntityID;
		        }
		    });

			if (matches.isEmpty()) {
				return;
			}
			
			// NetHandlerPlayerServer.processUseEntity
			if (message.mouseButton == 0) {
				ctx.getServerHandler().playerEntity.attackTargetEntityWithCurrentItem(matches.get(0));
			}
			if (message.mouseButton == 1) {
				ctx.getServerHandler().playerEntity.interactOn(matches.get(0), EnumHand.MAIN_HAND);
			}
		}
	}
}
