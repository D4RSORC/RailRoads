package cam72cam.immersiverailroading.render.item;

import java.util.List;

import cam72cam.immersiverailroading.render.BakedScaledModel;
import cam72cam.immersiverailroading.util.math.Vec3d;
import net.minecraft.block.BlockColored;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IModelCustom;

public class PlateItemModel implements IBakedModel {
	public PlateItemModel() {
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		state = Blocks.CONCRETE.getDefaultState();
		state = state.withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
		return new BakedScaledModel(model, new Vec3d(1, 1, 0.03), new Vec3d(0, 0, 0.5)).getQuads(state, side, rand);
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

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}
}
