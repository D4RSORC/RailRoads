package cam72cam.immersiverailroading.render.rail;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cam72cam.immersiverailroading.util.GLBoolTracker;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.immersiverailroading.util.math.BlockPos;
import cam72cam.immersiverailroading.util.math.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;

public class RailRenderUtil {
	public static void render(RailInfo info, boolean renderOverlay) {
		GLBoolTracker light = new GLBoolTracker(GL11.GL_LIGHTING, false);
		
		if (renderOverlay) {

			// Bind block textures to current context
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
			
			GL11.glPushMatrix();
			Vec3d pos = info.placementInfo.placementPosition;
			pos = pos.subtract(new Vec3d(new BlockPos(pos)));
			GL11.glTranslated(- pos.x, - pos.y, - pos.z);
			Minecraft.getMinecraft().mcProfiler.startSection("base");
			RailBaseRender.draw(info);
			Minecraft.getMinecraft().mcProfiler.endStartSection("overlay");
			RailBaseOverlayRender.draw(info);
			GL11.glPopMatrix();
			Minecraft.getMinecraft().mcProfiler.endSection();
		}
		
		Minecraft.getMinecraft().mcProfiler.startSection("rail");
        RailBuilderRender.renderRailBuilder(info);
		Minecraft.getMinecraft().mcProfiler.endSection();
		
		light.restore();
	}
	
	/*
	 *  From WorldVertexBufferUploader.draw
	 *  
	 *  Excludes the reset buffer at the end
	 */
	public static void draw(BufferBuilder vertexBufferIn) {
		if (vertexBufferIn == null) {
			return;
		}
        VertexFormat vertexformat = vertexBufferIn.getVertexFormat();
        int i = vertexformat.getNextOffset();
        ByteBuffer bytebuffer = vertexBufferIn.getByteBuffer();
        List<VertexFormatElement> list = vertexformat.getElements();

        for (int j = 0; j < list.size(); ++j)
        {
            VertexFormatElement vertexformatelement = list.get(j);
            bytebuffer.position(vertexformat.getOffset(j));

            // moved to VertexFormatElement.preDraw
            vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
        }

        GL11.glDrawArrays(vertexBufferIn.getDrawMode(), 0, vertexBufferIn.getVertexCount());
        int i1 = 0;

        for (int j1 = list.size(); i1 < j1; ++i1)
        {
            VertexFormatElement vertexformatelement1 = list.get(i1);
            // moved to VertexFormatElement.postDraw
            vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
        }

		try {
			ByteBuffer buffer = vertexBufferIn.getByteBuffer();
			Method cleanerMethod = buffer.getClass().getMethod("cleaner");
			cleanerMethod.setAccessible(true);
			Object cleaner = cleanerMethod.invoke(buffer);
			Method cleanMethod = cleaner.getClass().getMethod("clean");
			cleanMethod.setAccessible(true);
			cleanMethod.invoke(cleaner);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
