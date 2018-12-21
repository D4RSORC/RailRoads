package cam72cam.immersiverailroading.render.tile;

import cam72cam.immersiverailroading.IRItems;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;

import cam72cam.immersiverailroading.render.rail.RailRenderUtil;
import cam72cam.immersiverailroading.tile.TileRailPreview;
import cam72cam.immersiverailroading.util.GLBoolTracker;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.immersiverailroading.util.math.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TileRailPreviewRender extends TileEntitySpecialRenderer {
	
//	@Override
//	public boolean isGlobalRenderer(TileRailPreview te) {
//		return true;
//	}
	

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float partialTicks) {
		TileRailPreview te = (TileRailPreview) tileentity;
		RailInfo info = te.getRailRenderInfo();
		if (info == null) {
			// Still loading...
			return;
		}

		Minecraft.getMinecraft().mcProfiler.startSection("tile_rail_preview");
		GLBoolTracker blend = new GLBoolTracker(GL11.GL_BLEND, true);
		GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE);
		if (GLContext.getCapabilities().OpenGL14) {
			GL14.glBlendColor(1, 1, 1, 0.7f);
		}
		GL11.glPushMatrix();
		{
			// Move to specified position
			Vec3d placementPosition = info.placementInfo.placementPosition;
			placementPosition = placementPosition.subtract(new Vec3d(te.xCoord, te.yCoord, te.zCoord)).addVector(x, y, z);
			GL11.glTranslated(placementPosition.x, placementPosition.y, placementPosition.z);

			if (!te.isMulti()) {
				RailRenderUtil.render(info, true);
			}

			GL11.glTranslated(0, 0.5, 0);
			MinecraftForgeClient.getItemRenderer(new ItemStack(IRItems.ITEM_GOLDEN_SPIKE), ItemRenderType.ENTITY).renderItem(ItemRenderType.ENTITY, new ItemStack(IRItems.ITEM_GOLDEN_SPIKE));
		}
		GL11.glPopMatrix();
		blend.restore();
		Minecraft.getMinecraft().mcProfiler.endSection();
	}
}
