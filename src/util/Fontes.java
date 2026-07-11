package util;

import java.awt.Font;
import java.io.InputStream;

public class Fontes {
	private static final Font quantico = loadFont("/fonts/Quantico-Regular.ttf");
	private static final Font quantico_bold = loadFont("/fonts/Quantico-Bold.ttf");
	private static final Font quantico_italic = loadFont("/fonts/Quantico-Italic.ttf");
	private static final Font quantico_boldItalic = loadFont("/fonts/Quantico-BoldItalic.ttf");
	
	public static Font loadFont(String path) {
        try (InputStream is = Fontes.class.getResourceAsStream(path)) {
            if (is != null)
            	return Font.createFont(Font.TRUETYPE_FONT, is);

        } catch (Exception e) {
            return null;
        }
        return null;
	}
	
	public static Font quantico(float size) {
		return quantico.deriveFont(size);
	}
	
	public static Font quantico_bold(float size) {
		return quantico_bold.deriveFont(size);
	}
	
	public static Font quantico_italic(float size) {
		return quantico_italic.deriveFont(size);
	}
	
	public static Font quantico_boldItalic(float size) {
		return quantico_boldItalic.deriveFont(size);
	}
}
