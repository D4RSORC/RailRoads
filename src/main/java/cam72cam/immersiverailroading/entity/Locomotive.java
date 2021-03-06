package cam72cam.immersiverailroading.entity;

import java.util.UUID;

import com.google.common.base.Optional;

import cam72cam.immersiverailroading.Config;
import cam72cam.immersiverailroading.library.ChatText;
import cam72cam.immersiverailroading.library.GuiTypes;
import cam72cam.immersiverailroading.library.KeyTypes;
import cam72cam.immersiverailroading.registry.LocomotiveDefinition;
import cam72cam.immersiverailroading.util.Speed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public abstract class Locomotive extends FreightTank {

	private static DataParameter<Float> THROTTLE = EntityDataManager.createKey(Locomotive.class, DataSerializers.FLOAT);
	private static DataParameter<Float> AIR_BRAKE = EntityDataManager.createKey(Locomotive.class, DataSerializers.FLOAT);
	protected static DataParameter<Integer> HORN = EntityDataManager.createKey(Locomotive.class, DataSerializers.VARINT);
	protected static DataParameter<Optional<UUID>> HORN_PLAYER = EntityDataManager.createKey(Locomotive.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	

	private static final float throttleNotch = 0.04f;
	private static final float airBrakeNotch = 0.04f;
	
	private boolean deadMansSwitch;
	private int deadManChangeTimeout;


	public Locomotive(World world, String defID) {
		super(world, defID);

		this.getDataManager().register(THROTTLE, 0f);
		this.getDataManager().register(AIR_BRAKE, 0f);
		this.getDataManager().register(HORN, 0);
		this.getDataManager().register(HORN_PLAYER, Optional.absent());

		this.entityCollisionReduction = 0.99F;
	}
	
	/*
	 * 
	 * Stock Definitions
	 * 
	 */
	
	@Override
	public LocomotiveDefinition getDefinition() {
		return super.getDefinition(LocomotiveDefinition.class);
	}

	/*
	 * 
	 * EntityRollingStock Overrides
	 */

	@Override
	public GuiTypes guiType() {
		return null;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("throttle", getThrottle());
		nbttagcompound.setFloat("brake", getAirBrake());
		nbttagcompound.setBoolean("deadMansSwitch", deadMansSwitch);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		setThrottle(nbttagcompound.getFloat("throttle"));
		setAirBrake(nbttagcompound.getFloat("brake"));
		deadMansSwitch = nbttagcompound.getBoolean("deadMansSwitch");
	}
	
	@Override
	public void handleKeyPress(Entity source, KeyTypes key, boolean sprinting) {
		switch(key) {
		case HORN:
			setHorn(10, source.getPersistentID());
			break;
		case THROTTLE_UP:
			if (getThrottle() < 1) {
				setThrottle(getThrottle() + throttleNotch);
			}
			break;
		case THROTTLE_ZERO:
			setThrottle(0f);
			break;
		case THROTTLE_DOWN:
			if (getThrottle() > -1) {
				setThrottle(getThrottle() - throttleNotch);
			}
			break;
		case AIR_BRAKE_UP:
			if (getAirBrake() < 1) {
				setAirBrake(getAirBrake() + airBrakeNotch);
			}
			break;
		case AIR_BRAKE_ZERO:
			setAirBrake(0f);
			break;
		case AIR_BRAKE_DOWN:
			if (getAirBrake() > 0) {
				setAirBrake(getAirBrake() - airBrakeNotch);
			}
			break;
		case DEAD_MANS_SWITCH:
			if (deadManChangeTimeout == 0) { 
				deadMansSwitch = !deadMansSwitch;
				if (deadMansSwitch) {
					source.sendMessage(ChatText.DEADMANS_SWITCH_ENABLED.getMessage());
				} else {
					source.sendMessage(ChatText.DEADMANS_SWITCH_DISABLED.getMessage());
				}
				this.deadManChangeTimeout = 5;
			}
			break;
		default:
			super.handleKeyPress(source, key, sprinting);
			break;
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (!worldObj.isRemote) {
			if (deadManChangeTimeout > 0) {
				deadManChangeTimeout -= 1;
			}
			
			if (deadMansSwitch && !this.getCurrentSpeed().isZero()) {
				boolean hasDriver = false;
				for (Entity entity : this.getPassengers()) {
					if (entity instanceof EntityPlayer) {
						hasDriver = true;
						break;
					}
				}
				if (!hasDriver) {
					this.setThrottle(0);
					this.setAirBrake(1);
				}
			}
			if (this.getDataManager().get(HORN) > 0) {
				this.getDataManager().set(HORN, this.getDataManager().get(HORN)-1);
			} else if (this.getDataManager().get(HORN_PLAYER).isPresent()) {
				this.getDataManager().set(HORN_PLAYER, Optional.absent());
			}
		}
		
		simulateWheelSlip();
	}
	
	protected abstract int getAvailableHP();
	
	private double getAppliedTractiveEffort(Speed speed) {
		double locoEfficiency = 0.7f; //TODO config
		double outputHorsepower = Math.abs(Math.pow(getThrottle(), 3) * getAvailableHP());
		
		double tractiveEffortNewtons = (2650.0 * ((locoEfficiency * outputHorsepower) / Math.max(1.4, Math.abs(speed.metric()))));
		return tractiveEffortNewtons;
	}
	
	private void simulateWheelSlip() {
		double tractiveEffortNewtons = getAppliedTractiveEffort(getCurrentSpeed());
		double staticTractiveEffort = this.getDefinition().getStartingTractionNewtons(gauge) * slipCoefficient() * Config.ConfigBalance.tractionMultiplier;
		staticTractiveEffort *= 1.5; // Fudge factor
		double adhesionFactor = tractiveEffortNewtons / staticTractiveEffort;
		if (adhesionFactor > 1) {
			this.distanceTraveled += Math.copySign(Math.min((adhesionFactor-1)/10, 1), getThrottle());
		}
	}
	
	public double getTractiveEffortNewtons(Speed speed) {	
		if (!this.isBuilt()) {
			return 0;
		}
		
		double tractiveEffortNewtons = getAppliedTractiveEffort(speed);
		double staticTractiveEffort = this.getDefinition().getStartingTractionNewtons(gauge) * slipCoefficient() * Config.ConfigBalance.tractionMultiplier;
		staticTractiveEffort *= 1.5; // Fudge factor
		
		double adhesionFactor = tractiveEffortNewtons / staticTractiveEffort;
		
		if (adhesionFactor > 1) {
			// CRC Handbook of Physical Quantities. Boca Raton, FL: CRC Press, 1997: 145-156.
			double us = 0.74;
			double uk = 0.57;
			tractiveEffortNewtons = staticTractiveEffort * (uk/us) / adhesionFactor;
		}
		
		if (Math.abs(speed.minecraft()) > this.getDefinition().getMaxSpeed(gauge).minecraft()) {
			tractiveEffortNewtons = 0;
		}
		
		return Math.copySign(tractiveEffortNewtons, getThrottle());
	}

	/*
	 * 
	 * Misc Helper functions
	 */
	
	public float getThrottle() {
		return dataManager.get(THROTTLE);
	}
	public void setThrottle(float newThrottle) {
		if (this.getThrottle() != newThrottle) {
			dataManager.set(THROTTLE, newThrottle);
			triggerResimulate();
		}
	}
	
	public void setHorn(int val, UUID uuid) {
		UUID currentPlayer = this.getDataManager().get(HORN_PLAYER).isPresent() ? this.getDataManager().get(HORN_PLAYER).get() : null;
		if (currentPlayer == null && uuid != null) {
			currentPlayer = uuid;
			this.getDataManager().set(HORN_PLAYER, Optional.of(uuid));
		}
		if (currentPlayer == null || currentPlayer == uuid) {
			this.getDataManager().set(HORN, val);
		}
	}
	
	public float getAirBrake() {
		return dataManager.get(AIR_BRAKE);
	}
	public void setAirBrake(float newAirBrake) {
		if (this.getAirBrake() != newAirBrake) {
			dataManager.set(AIR_BRAKE, newAirBrake);
			triggerResimulate();
		}
	}
	
	public double slipCoefficient() {
		double slipMult = 1.0;
		World world = getEntityWorld();
		if (world.isRaining() && world.canSeeSky(getPosition())) {
			Biome biome = world.getBiome(getPosition());
			if (biome.canRain()) {
				slipMult = 0.6;
			}
			if (biome.isSnowyBiome()) {
				slipMult = 0.4;
			}
		}
		// Wheel balance messing with friction
		if (this.getCurrentSpeed().metric() != 0) {
			double balance = 1 - 0.004 * Math.abs(this.getCurrentSpeed().metric());
			slipMult *= balance;
		}
		return slipMult;
	}
	
	public float ambientTemperature() {
		float mctemp = worldObj.getBiome(this.getPosition()).getTemperature(getPosition());
		//https://www.reddit.com/r/Minecraft/comments/3eh7yu/the_rl_temperature_of_minecraft_biomes_revealed/ctex050/
		return (13.6484805403f*mctemp)+7.0879687222f;
	}
	
}