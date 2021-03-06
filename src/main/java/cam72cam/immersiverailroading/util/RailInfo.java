package cam72cam.immersiverailroading.util;

import java.util.*;

import cam72cam.immersiverailroading.Config.ConfigDamage;
import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.items.ItemTrackBlueprint;
import cam72cam.immersiverailroading.items.nbt.ItemGauge;
import cam72cam.immersiverailroading.items.nbt.RailSettings;
import cam72cam.immersiverailroading.library.SwitchState;
import cam72cam.immersiverailroading.library.ChatText;
import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.track.*;
import cam72cam.immersiverailroading.track.BuilderCubicCurve;
import cam72cam.immersiverailroading.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class RailInfo {
	public final World world;
	public final RailSettings settings;
	public final PlacementInfo placementInfo;
	public final PlacementInfo customInfo;

	// Used for tile rendering only
	public final SwitchState switchState;
	public final double tablePos;
	public final String uniqueID;


	public RailInfo(World world, RailSettings settings, PlacementInfo placementInfo, PlacementInfo customInfo, SwitchState switchState, double tablePos) {
		if (customInfo == null) {
			customInfo = placementInfo;
		}

		this.world = world;
		this.settings = settings;
		this.placementInfo = placementInfo;
		this.customInfo = customInfo;
		this.switchState = switchState;
		this.tablePos = tablePos;

		Object[] props = new Object [] {
				this.settings.type,
				this.settings.length,
				this.settings.quarters,
				this.settings.railBed,
				this.settings.gauge,
				this.switchState,
				this.tablePos,
				this.placementInfo.yaw,
				this.placementInfo.direction,
				this.customInfo.yaw,
				this.customInfo.direction,
		};
		String id = Arrays.toString(props);
		if (!placementInfo.placementPosition.equals(customInfo.placementPosition)) {
			id += placementInfo.placementPosition;
			id += customInfo.placementPosition;
		}
		uniqueID = id;
	}

	public RailInfo(World world, ItemStack settings, PlacementInfo placementInfo, PlacementInfo customInfo) {
		this(world, ItemTrackBlueprint.settings(settings), placementInfo, customInfo, SwitchState.NONE, 0);
	}

	public RailInfo(World world, BlockPos pos, NBTTagCompound nbt) {
		this(
				world,
				new RailSettings(nbt.getCompoundTag("settings")),
				new PlacementInfo(nbt.getCompoundTag("placement"), pos),
				new PlacementInfo(nbt.getCompoundTag("custom"), pos),
				SwitchState.values()[nbt.getInteger("switchState")],
				nbt.getDouble("tablePos")
		);
	}

	public NBTTagCompound toNBT(BlockPos pos) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("settings", settings.toNBT());
		nbt.setTag("placement", placementInfo.toNBT(pos));
		nbt.setTag("custom", customInfo.toNBT(pos));
		nbt.setInteger("switchState", switchState.ordinal());
		nbt.setDouble("tablePos", tablePos);
		return nbt;
	}

	@Override
	public RailInfo clone() {
		return new RailInfo(world, settings, placementInfo, customInfo, switchState, tablePos);
	}

	public RailInfo withLength(int length) {
		RailSettings settings = this.settings.withLength(length);
		return new RailInfo(world, settings, placementInfo, customInfo, switchState, tablePos);
	}

	public RailInfo withType(TrackItems type) {
		RailSettings settings = this.settings.withType(type);
		return new RailInfo(world, settings, placementInfo, customInfo, switchState, tablePos);
	}

	public Map<BlockPos, BuilderBase> builders = new HashMap<>();
	public BuilderBase getBuilder(BlockPos pos) {
		if (builders.containsKey(pos)) {
			return builders.get(pos);
		}
		builders.put(pos, constructBuilder(pos));
		return builders.get(pos);
	}
	private BuilderBase constructBuilder(BlockPos pos) {
		switch (settings.type) {
		case STRAIGHT:
			return new BuilderStraight(this, pos);
		case CROSSING:
			return new BuilderCrossing(this, pos);
		case SLOPE:
			return new BuilderSlope(this, pos);
		case TURN:
			return new BuilderTurn(this, pos);
		case SWITCH:
			return new BuilderSwitch(this, pos);
		case TURNTABLE:
			return new BuilderTurnTable(this, pos);
		case CUSTOM:
			return new BuilderCubicCurve(this, pos);
		}
		return null;
	}

	public BuilderBase getBuilder() {
		return getBuilder(BlockPos.ORIGIN);
	}

	public boolean build(EntityPlayer player) {
		BuilderBase builder = getBuilder(new BlockPos(placementInfo.placementPosition));

		if (player.capabilities.isCreativeMode && ConfigDamage.creativePlacementClearsBlocks) {
			if (!world.isRemote) {
				builder.clearArea();
			}
		}


		if (builder.canBuild()) {
			if (!world.isRemote) {
				if (player.capabilities.isCreativeMode) {
					builder.build();
					return true;
				}

				// Survival check

				int ties = 0;
				int rails = 0;
				int bed = 0;
				int fill = 0;

				for (ItemStack playerStack : player.inventory.mainInventory) {
					if (playerStack.getItem() == IRItems.ITEM_RAIL && ItemGauge.get(playerStack) == settings.gauge) {
						rails += playerStack.stackSize;
					}
					if (OreHelper.IR_TIE.matches(playerStack, false)) {
						ties += playerStack.stackSize;
					}
					if (settings.railBed.getItem() != null && settings.railBed.getItem() == playerStack.getItem() && settings.railBed.getItemDamage() == playerStack.getItemDamage()) {
						bed += playerStack.stackSize;
					}
					if (settings.railBedFill.getItem() != null && settings.railBedFill.getItem() == playerStack.getItem() && settings.railBedFill.getItemDamage() == playerStack.getItemDamage()) {
						fill += playerStack.stackSize;
					}
				}

				boolean isOk = true;

				if (ties < builder.costTies()) {
					player.addChatMessage(ChatText.BUILD_MISSING_TIES.getMessage(builder.costTies() - ties));
					isOk = false;
				}

				if (rails < builder.costRails()) {
					player.addChatMessage(ChatText.BUILD_MISSING_RAILS.getMessage(builder.costRails() - rails));
					isOk = false;
				}

				if (settings.railBed.getItem() != null && bed < builder.costBed()) {
					player.addChatMessage(ChatText.BUILD_MISSING_RAIL_BED.getMessage(builder.costBed() - bed));
					isOk = false;
				}

				if (settings.railBedFill.getItem() != null && fill < builder.costFill()) {
					player.addChatMessage(ChatText.BUILD_MISSING_RAIL_BED_FILL.getMessage(builder.costFill() - fill));
					isOk = false;
				}

				if (!isOk) {
					return false;
				}

				ties = builder.costTies();
				rails = builder.costRails();
				bed = builder.costBed();
				fill = builder.costFill();
				List<ItemStack> drops = new ArrayList<ItemStack>();

				for (ItemStack playerStack : player.inventory.mainInventory) {
					if (playerStack.getItem() == IRItems.ITEM_RAIL && ItemGauge.get(playerStack) == settings.gauge) {
						if (rails > playerStack.stackSize) {
							rails -= playerStack.stackSize;
							ItemStack copy = playerStack.copy();
							copy.stackSize = playerStack.stackSize;
							drops.add(copy);
							playerStack.stackSize = 0;
						} else if (rails != 0) {
							ItemStack copy = playerStack.copy();
							copy.stackSize = rails;
							drops.add(copy);
							playerStack.stackSize = playerStack.stackSize - rails;
							rails = 0;
						}
					}
					if (OreHelper.IR_TIE.matches(playerStack, false)) {
						if (ties > playerStack.stackSize) {
							ties -= playerStack.stackSize;
							ItemStack copy = playerStack.copy();
							copy.stackSize = playerStack.stackSize;
							drops.add(copy);
							playerStack.stackSize = 0;
						} else if (ties != 0) {
							ItemStack copy = playerStack.copy();
							copy.stackSize = ties;
							drops.add(copy);
							playerStack.stackSize = playerStack.stackSize - ties;
							ties = 0;
						}
					}
					if (settings.railBed.getItem() != null && settings.railBed.getItem() == playerStack.getItem() && settings.railBed.getItemDamage() == playerStack.getItemDamage()) {
						if (bed > playerStack.stackSize) {
							bed -= playerStack.stackSize;
							ItemStack copy = playerStack.copy();
							copy.stackSize = playerStack.stackSize;
							drops.add(copy);
							playerStack.stackSize = 0;
						} else if (bed != 0) {
							ItemStack copy = playerStack.copy();
							copy.stackSize = bed;
							drops.add(copy);
							playerStack.stackSize = playerStack.stackSize - bed;
							bed = 0;
						}
					}
					if (settings.railBedFill.getItem() != null&& settings.railBedFill.getItem() == playerStack.getItem() && settings.railBedFill.getItemDamage() == playerStack.getItemDamage()) {
						if (fill > playerStack.stackSize) {
							fill -= playerStack.stackSize;
							ItemStack copy = playerStack.copy();
							copy.stackSize = playerStack.stackSize;
							//drops.add(copy);
							playerStack.stackSize = 0;
						} else if (fill != 0) {
							ItemStack copy = playerStack.copy();
							copy.stackSize = fill;
							//drops.add(copy);
							playerStack.stackSize = playerStack.stackSize - fill;
							fill = 0;
						}
					}
				}
				builder.setDrops(drops);
				builder.build();
				return true;
			}
		}
		return false;
	}

}
