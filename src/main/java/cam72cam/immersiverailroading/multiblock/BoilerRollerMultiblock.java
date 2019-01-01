package cam72cam.immersiverailroading.multiblock;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.items.nbt.ItemComponent;
import cam72cam.immersiverailroading.items.nbt.ItemDefinition;
import cam72cam.immersiverailroading.items.nbt.ItemGauge;
import cam72cam.immersiverailroading.items.nbt.ItemPlateType;
import cam72cam.immersiverailroading.library.ItemComponentType;
import cam72cam.immersiverailroading.library.PlateType;
import cam72cam.immersiverailroading.tile.TileMultiblock;
import cam72cam.immersiverailroading.util.energy.IEnergyStorage;
import cam72cam.immersiverailroading.util.math.BlockPos;
import cam72cam.immersiverailroading.util.math.Rotation;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BoilerRollerMultiblock extends Multiblock {
	private static MultiblockComponent slab = new MultiblockComponent(Blocks.stone_slab);
	public static final String NAME = "BOILER_MACHINE";
	private static final BlockPos render = new BlockPos(2,0,0);
	private static final BlockPos power = new BlockPos(5,0,3);
	private static final BlockPos crafting = new BlockPos(2,0,4);
	
	private static MultiblockComponent[][][] componentGenerator() {
		MultiblockComponent[] bed = new MultiblockComponent[] {
				L_ENG(), L_ENG(), L_ENG(), L_ENG(), H_ENG(), H_ENG()
		};
		MultiblockComponent[] table = new MultiblockComponent[] {
				slab, slab, slab, slab, AIR, AIR
		};
		
		MultiblockComponent[][][] result = new MultiblockComponent[][][] {
			{ table },
			{ table },
			{ table },
			{ bed },
			{ bed },
			{ table },
			{ table },
			{ table },
		};
		return result;
	}

	public BoilerRollerMultiblock() {
		super(NAME, componentGenerator());
	}
	
	@Override
	public BlockPos placementPos() {
		return new BlockPos(2, 0, 0);
	}

	@Override
	protected MultiblockInstance newInstance(World world, BlockPos origin, Rotation rot) {
		return new BoilerRollerInstance(world, origin, rot);
	}
	public class BoilerRollerInstance extends MultiblockInstance {
		
		public BoilerRollerInstance(World world, BlockPos origin, Rotation rot) {
			super(world, origin, rot);
		}

		@Override
		public boolean onBlockActivated(EntityPlayer player, BlockPos offset) {
			if (world.isRemote) {
				return false;
			}
			
			if (!player.isSneaking()) {
				ItemStack held = player.getHeldItem();
				if (held == null) {
					TileMultiblock craftTe = getTile(crafting);
					if (craftTe == null) {
						return false;
					}
					
					ItemStack outstack = craftTe.getContainer().getStackInSlot(1);
					world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, outstack));
					
					craftTe.getContainer().setStackInSlot(1, null);
				} else if (held.getItem() == IRItems.ITEM_PLATE && ItemPlateType.get(held) == PlateType.BOILER) {
					TileMultiblock craftTe = getTile(crafting);
					if (craftTe == null) {
						return false;
					}
					if (craftTe.getContainer().getStackInSlot(0) == null) {
						ItemStack inputStack = held.copy();
						inputStack.stackSize = 1;
						craftTe.getContainer().setStackInSlot(0, inputStack);
						held.stackSize = held.stackSize - 1;
						player.inventory.mainInventory[player.inventory.currentItem] = held;
					}
				}
			}
			return false;
		}

		@Override
		public boolean isRender(BlockPos offset) {
			return offset.equals(render);
		}

		@Override
		public int getInvSize(BlockPos offset) {
			return offset.equals(crafting) ? 2 : 0;
		}

		@Override
		public void tick(BlockPos offset) {
			if (!offset.equals(crafting)) {
				return;
			}
			TileMultiblock craftTe = getTile(crafting);
			if (craftTe == null) {
				return;
			}
			
			TileMultiblock powerTe = getTile(power);
			if (powerTe == null) {
				return;
			}
			
			if (!hasPower()) {
				return;
			}
			
			if (world.isRemote) {
				if (craftTe.getRenderTicks() % 10 == 0 && craftTe.getCraftProgress() != 0) {
					world.playSound(craftTe.xCoord, craftTe.yCoord, craftTe.zCoord, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1.0f, 0.2f, false);
				}
				return;
			}
			
			// Decrement craft progress down to 0
			if (craftTe.getCraftProgress() != 0) {
				IEnergyStorage energy = powerTe.getCapability(CapabilityEnergy.ENERGY, null);
				energy.extractEnergy(32, false);
				craftTe.setCraftProgress(Math.max(0, craftTe.getCraftProgress() - 1));
			}
			
			float progress = craftTe.getCraftProgress();
			
			ItemStack input = craftTe.getContainer().getStackInSlot(0);
			ItemStack output = craftTe.getContainer().getStackInSlot(1);
			
			
			if (progress == 0) {
				// Try to start crafting
				if (input.getItem() == IRItems.ITEM_PLATE && ItemPlateType.get(input) == PlateType.BOILER && output.isEmpty()) {
					progress = 100;
					craftTe.setCraftProgress(100);
				}
			}
			
			if (progress == 1) {
				// Stop crafting
				ItemStack out = new ItemStack(IRItems.ITEM_ROLLING_STOCK_COMPONENT, 1);
				ItemGauge.set(out, ItemGauge.get(input));
				ItemDefinition.setID(out, ItemDefinition.getID(input));
				ItemComponent.setComponentType(out, ItemComponentType.BOILER_SEGMENT);
				craftTe.getContainer().setStackInSlot(1, out);
				input.shrink(1);
				craftTe.getContainer().setStackInSlot(0, input);;
			}
		}

		@Override
		public boolean canInsertItem(BlockPos pos, int slot, ItemStack stack) {
			//TODO
			return false;
		}

		@Override
		public boolean isOutputSlot(BlockPos pos, int slot) {
			//TODO
			return false;
		}

		@Override
		public int getSlotLimit(BlockPos offset, int slot) {
			return offset.equals(crafting) ? 1 : 0;
		}

		@Override
		public boolean canRecievePower(BlockPos offset) {
			return offset.equals(power);
		}

		public boolean hasPower() {
			TileMultiblock powerTe = getTile(power);
			if (powerTe == null) {
				return false;
			}
			IEnergyStorage energy = powerTe.getCapability(CapabilityEnergy.ENERGY, null);
			return energy.getEnergyStored() > 32;			
		}
		
		public boolean hasInput() {
			TileMultiblock craftTe = getTile(crafting);
			if (craftTe == null) {
				return false;
			}
			return !craftTe.getContainer().getStackInSlot(0).isEmpty();
		}

		public boolean hasOutput() {
			TileMultiblock craftTe = getTile(crafting);
			if (craftTe == null) {
				return false;
			}
			return !craftTe.getContainer().getStackInSlot(1).isEmpty();
		}

		
	}
}
