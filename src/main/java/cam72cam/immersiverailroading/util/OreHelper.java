package cam72cam.immersiverailroading.util;

import java.util.ArrayList;

import cam72cam.immersiverailroading.util.math.NonNullList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreHelper {
	public static class OreAbstract {
		private ItemStack fallback;
		public final String def;
		public final String name;
		
		public OreAbstract(String name, ItemStack fallback) {
			this.name = name;
			this.def = null;
			this.fallback = fallback;
		}

		public OreAbstract(String name, String def, ItemStack fallback) {
			this.name = name;
			this.def = def;
			this.fallback = fallback;
		}
		
		public ArrayList<ItemStack> getOres() {
			ArrayList<ItemStack> ores = OreDictionary.getOres(name);
			// Someone added stuff to the IR dict
			if (ores.size() != 0) {
				return ores;
			}
			
			// Fallback/merge
			if (def != null) {
				ores = OreDictionary.getOres(def);
				if (ores.size() != 0) {
					return ores;
				}
			}
			
			ores = new ArrayList<ItemStack>();
			
			ores.add(fallback);
			
			return ores;
		}
		
		public void add(ItemStack stack) {
			OreDictionary.registerOre(name, stack);
		}

		public void add(Block block) {
			OreDictionary.registerOre(name, block);
		}
		
		public void add(Item item) {
			OreDictionary.registerOre(name, item);
		}
		
		public boolean matches(ItemStack stack, boolean strict) {
			return oreDictionaryContainsMatch(strict, getOres(), stack);
		}
		
		public ItemStack example() {
			return getOres().get(0);
		}
	}
	
	public static final OreAbstract IR_CASTING_CASING = new OreAbstract("irCastingCasing", new ItemStack(Blocks.nether_brick));
	public static final OreAbstract IR_LIGHT_ENG = new OreAbstract("irLightEngineering", new ItemStack(Blocks.iron_block));
	public static final OreAbstract IR_HEAVY_ENG = new OreAbstract("irHeavyEngineering", new ItemStack(Blocks.iron_block));
	public static final OreAbstract IR_SCAFFOLDING = new OreAbstract("irScaffolding", "scaffoldingSteel", new ItemStack(Blocks.iron_bars));
	public static final OreAbstract IR_STEEL_BLOCK = new OreAbstract("blockSteel", new ItemStack(Blocks.iron_block));
	public static final OreAbstract IR_STEEL_INGOT = new OreAbstract("ingotSteel", new ItemStack(Items.iron_ingot));
	public static final OreAbstract IR_TIE = new OreAbstract("irTie", "plankTreatedWood", new ItemStack(Blocks.planks, 1, OreDictionary.WILDCARD_VALUE));
	public static final OreAbstract IR_RAIL_BED = new OreAbstract("railBed", new ItemStack(Blocks.gravel));
	
	
	private static boolean oreDictionaryContainsMatch(boolean strict, ArrayList<ItemStack> arrayList, ItemStack playerStack) {
        for (ItemStack target : arrayList)
        {
            if (OreDictionary.itemMatches(target, playerStack, strict))
            {
                return true;
            }
        }
        return false;
	}
}
