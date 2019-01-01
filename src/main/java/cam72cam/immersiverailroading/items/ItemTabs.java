package cam72cam.immersiverailroading.items;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.util.math.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemTabs {

	public static CreativeTabs MAIN_TAB = new CreativeTabs(ImmersiveRailroading.MODID) {
		@Override
		public Item getTabIconItem() {
			return IRItems.ITEM_LARGE_WRENCH;
		}
	};
	
	public static CreativeTabs LOCOMOTIVE_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".locomotive") {
		@Override
		public Item getTabIconItem() {
			NonNullList<ItemStack> items = NonNullList.create();
			IRItems.ITEM_ROLLING_STOCK.getSubItems(this, items);
			if (items.size() == 0) {
				return IRItems.ITEM_LARGE_WRENCH;
			}
			return items.get(0).getItem();
		}
	};
	
	public static CreativeTabs STOCK_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".stock") {
		@Override
		public Item getTabIconItem() {
			NonNullList<ItemStack> items = NonNullList.create();
			IRItems.ITEM_ROLLING_STOCK.getSubItems(this, items);
			if (items.size() == 0) {
				return IRItems.ITEM_LARGE_WRENCH;
			}
			return items.get(0).getItem();
		}
	};
	
	public static CreativeTabs PASSENGER_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".passenger") {
		@Override
		public Item getTabIconItem() {
			NonNullList<ItemStack> items = NonNullList.create();
			IRItems.ITEM_ROLLING_STOCK.getSubItems(this, items);
			if (items.size() == 0) {
				return IRItems.ITEM_LARGE_WRENCH;
			}
			return items.get(0).getItem();
		}
	};
	
	public static CreativeTabs COMPONENT_TAB = new CreativeTabs(ImmersiveRailroading.MODID + ".components") {
		@Override
		public Item getTabIconItem() {
			NonNullList<ItemStack> items = NonNullList.create();
			IRItems.ITEM_ROLLING_STOCK_COMPONENT.getSubItems(this, items);
			if (items.size() == 0) {
				return new ItemStack(IRItems.ITEM_LARGE_WRENCH, 1).getItem();
			}
			return items.get(0).getItem();
		}
	};

}
