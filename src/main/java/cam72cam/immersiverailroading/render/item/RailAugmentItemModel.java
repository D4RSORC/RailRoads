package cam72cam.immersiverailroading.render.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import cam72cam.immersiverailroading.items.nbt.ItemAugmentType;
import cam72cam.immersiverailroading.library.Augment;
import cam72cam.immersiverailroading.render.BakedScaledModel;
import net.minecraft.block.BlockColored;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class RailAugmentItemModel implements IBakedModel {
	private EnumDyeColor color;

	public RailAugmentItemModel() {
	}
	
	public RailAugmentItemModel(ItemStack stack, World world) {
		Augment augment = ItemAugmentType.get(stack);
		if (augment != null) {
			color = augment.tempColor();
		}
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		state = Blocks.CONCRETE.getDefaultState();
		if (color != null) {
			state = state.withProperty(BlockColored.COLOR, color);
		}
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
		return new BakedScaledModel(model, new Vec3d(1, 0.2f, 1), new Vec3d(0, 0.4, 0)).getQuads(state, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return null;
	}
	
	public class ItemOverrideListHack extends ItemOverrideList {
		public ItemOverrideListHack() {
			super(new ArrayList<ItemOverride>());
		}

		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
			return new RailAugmentItemModel(stack, world);
		}
	}

	@Override
	public ItemOverrideList getOverrides() {
		return new ItemOverrideListHack();
	}
}
