package cam72cam.immersiverailroading.util;

import net.minecraft.client.resources.I18n;

@SuppressWarnings("deprecation")
public class TextUtil {
	public static String translate(String name) {
		return I18n.format(name);
	}

	public static String translate(String name, Object[] objects) {
		return I18n.format(name, objects);
	}
}
