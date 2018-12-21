package cam72cam.immersiverailroading.net;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import cam72cam.immersiverailroading.entity.EntityRidableRollingStock;
import cam72cam.immersiverailroading.library.KeyTypes;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;

public class KeyPressPacket implements IMessage {
	private int dimension;
	private int keyBindingOrdinal;
	private int sourceEntityID;
	private int targetEntityID;
	private boolean sprinting;
	
	public KeyPressPacket() {
		// For Reflection
	}
	
	@SideOnly(Side.CLIENT)
	public KeyPressPacket(KeyTypes binding, int dimension, int sourceEntityID, int targetEntityID, boolean sprinting) {
		this.dimension = dimension;
		this.keyBindingOrdinal = binding.ordinal();
		this.sourceEntityID = sourceEntityID;
		this.targetEntityID = targetEntityID;
		this.sprinting = sprinting;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.dimension = buf.readInt();
		this.keyBindingOrdinal = buf.readInt();
		this.sourceEntityID = buf.readInt();
		this.targetEntityID = buf.readInt();
		this.sprinting = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(dimension);
		buf.writeInt(keyBindingOrdinal);
		buf.writeInt(sourceEntityID);
		buf.writeInt(targetEntityID);
		buf.writeBoolean(sprinting);
	}
	
	public static class Handler implements IMessageHandler<KeyPressPacket, IMessage> {
		@Override
		public IMessage onMessage(KeyPressPacket message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private void handle(KeyPressPacket message, MessageContext ctx) {
			
			Entity source = ctx.getServerHandler().playerEntity.getServerForPlayer().getEntityByID(message.sourceEntityID);
			
			List<EntityRidableRollingStock> matches = ctx.getServerHandler().playerEntity.getServerForPlayer().getEntities(EntityRidableRollingStock.class, new Predicate<EntityRidableRollingStock>()
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
			
			matches.get(0).handleKeyPress(source, KeyTypes.values()[message.keyBindingOrdinal], message.sprinting);
		}
	}
}
