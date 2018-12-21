package cam72cam.immersiverailroading.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonObject;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.Config.ConfigDamage;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.StockDeathType;
import cam72cam.immersiverailroading.net.PaintSyncPacket;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.util.BufferUtil;
import cam72cam.immersiverailroading.util.math.Vec3d;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public abstract class EntityRollingStock extends Entity implements IEntityAdditionalSpawnData {
	
	protected String defID;
	public Gauge gauge;
	public String tag = "";
	public String texture;

	public EntityRollingStock(World world, String defID) {
		super(world);

		this.defID = defID;

		super.preventEntitySpawning = true;
		super.isImmuneToFire = true;
		super.entityCollisionReduction = 1F;
		super.ignoreFrustumCheck = true;
	}
	
	public Vec3d getPositionVector()
    {
        return new Vec3d(this.posX, this.posY, this.posZ);
    }
	
	@Override
	public String getCommandSenderName() {
		return this.getDefinition().name();
	}

	public EntityRollingStockDefinition getDefinition() {
		return this.getDefinition(EntityRollingStockDefinition.class);
	}
	public <T extends EntityRollingStockDefinition> T getDefinition(Class<T> type) {
		EntityRollingStockDefinition def = DefinitionManager.getDefinition(defID);
		if (def == null) {
			try {
				return type.getConstructor(String.class, JsonObject.class).newInstance(defID, (JsonObject)null);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return type.cast(def);
		}
	}
	public String getDefinitionID() {
		return this.defID;
	}
	
	@Override
	public void onUpdate() {
		if (!worldObj.isRemote && this.ticksExisted % 5 == 0) {
			EntityRollingStockDefinition def = DefinitionManager.getDefinition(defID);
			if (def == null) {
				worldObj.removeEntity(this);
			}
		}
	}

	/*
	 * 
	 * Data RW for Spawn and Entity Load
	 */

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		defID = BufferUtil.readString(additionalData);
		gauge = Gauge.from(additionalData.readDouble());
		tag = BufferUtil.readString(additionalData);
		if (additionalData.readBoolean()) {
			texture = BufferUtil.readString(additionalData);
		}
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		BufferUtil.writeString(buffer, defID);
		buffer.writeDouble(gauge.value());
		BufferUtil.writeString(buffer, tag);
		buffer.writeBoolean(texture != null);
		if (texture != null) {
			BufferUtil.writeString(buffer, texture);
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setString("defID", defID);
		nbttagcompound.setDouble("gauge", gauge.value());
		nbttagcompound.setString("tag", tag);
		
		if (this.texture != null) {
			nbttagcompound.setString("texture", texture);
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		defID = nbttagcompound.getString("defID");
		if (nbttagcompound.hasKey("gauge")) {
			gauge = Gauge.from(nbttagcompound.getDouble("gauge"));
		} else {
			gauge = Gauge.from(Gauge.STANDARD);
		}
		
		tag = nbttagcompound.getString("tag");
		
		if (nbttagcompound.hasKey("texture")) {
			texture = nbttagcompound.getString("texture");
		}
	}

	@Override
	protected void entityInit() {
	}

	/*
	 * Player Interactions
	 */
	
	@Override
	public boolean interactFirst(EntityPlayer player) {
		if (player.getHeldItem().getItem() == IRItems.ITEM_PAINT_BRUSH) {
			List<String> texNames = new ArrayList<String>(this.getDefinition().textureNames.keySet());
			if (texNames.size() > 1) {
				int idx = texNames.indexOf(this.texture);
				idx = (idx + (player.isSneaking() ? -1 : 1) + texNames.size()) % (texNames.size());
				this.texture = texNames.get(idx);
				this.sendToObserving(new PaintSyncPacket(this));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		// Needed for right click, probably a forge or MC bug
		return true;
	}
	
	public void onDeath(StockDeathType type) {
		setDead();
	}

	@Override
	public boolean attackEntityFrom(DamageSource damagesource, float amount) {
		if (worldObj.isRemote) {
			return false;
		}
		
		if (damagesource.isExplosion()) {
			if (amount > 5 && (ConfigDamage.trainMobExplosionDamage || !(damagesource.getSourceOfDamage() instanceof EntityMob))) {
				if (!this.isDead) {
					this.onDeath(amount > 20 ? StockDeathType.CATACYSM : StockDeathType.EXPLOSION);
				}
				worldObj.removeEntity(this);
				return false;
			}
		}
		
		if (damagesource.getSourceOfDamage() instanceof EntityPlayer && !damagesource.isProjectile()) {
			EntityPlayer player = (EntityPlayer) damagesource.getSourceOfDamage();
			if (player.isSneaking()) {
				if (!this.isDead) {
					this.onDeath(StockDeathType.PLAYER);
				}
				worldObj.removeEntity(this);
				return false;
			}
		}
		
		return false;
	}
	
//	@Override
//	public <T extends Entity> Collection<T> getRecursivePassengersByType(Class<T> entityClass) {
//		try {
//			throw new Exception("Hack the planet");
//		} catch (Exception ex) {
//			for (StackTraceElement tl : ex.getStackTrace()) {
//				if (tl.getFileName().contains("PlayerList.java")) {
//					return new ArrayList<T>();
//				}
//			}
//		}
//		return super.getRecursivePassengersByType(entityClass);
//	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	/**
	 * @return Stock Weight in Kg
	 */
	public double getWeight() {
		return this.getDefinition().getWeight(gauge);
	}

	/*
	 * Helpers
	 */

	public void sendToObserving(IMessage packet) {
		boolean found = false;
		for (Object player : worldObj.playerEntities) {
			
			if (new Vec3d(((EntityPlayer)player).posX, ((EntityPlayer)player).posY, ((EntityPlayer)player).posZ).distanceTo(this.getPositionVector()) < ImmersiveRailroading.ENTITY_SYNC_DISTANCE) {
				found = true;
				break;
			}
		}
		if (found) {
			ImmersiveRailroading.net.sendToAllAround(packet,
					new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, ImmersiveRailroading.ENTITY_SYNC_DISTANCE));
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double distance)
    {
        return true;
    }
	
	@Override
	public boolean shouldRenderInPass(int pass) {
		return false;
	}

	public void triggerResimulate() {
	}

	public void renderTick(float partialTicks) {
	}
	
	public Gauge soundGauge() {
		return this.getDefinition().shouldScalePitch() ? gauge : Gauge.from(Gauge.STANDARD);
	}
}