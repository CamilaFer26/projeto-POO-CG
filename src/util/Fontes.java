package util;

import java.awt.Font;
import java.io.InputStream;

public class Fontes {
	private static final Font quantico = loadFont("/fonts/Quantico-Regular.ttf");
	private static final Font quantico_bold = loadFont("/fonts/Quantico-Bold.ttf");
	private static final Font quantico_italic = loadFont("/fonts/Quantico-Iatlic.ttf");
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
	    if (quantico == null) {
	        return new Font(Font.SANS_SERIF, Font.PLAIN, Math.round(size));
	    }

	    return quantico.deriveFont(size);
	}
	
	public static Font quantico_bold(float size) {
	    if (quantico_bold == null) {
	        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(size));
	    }

	    return quantico_bold.deriveFont(size);
	}
	
	public static Font quantico_italic(float size) {
	    if (quantico_italic == null) {
	        return new Font(Font.SANS_SERIF, Font.ITALIC, Math.round(size));
	    }

	    return quantico_italic.deriveFont(size);
	}
	
	public static Font quantico_boldItalic(float size) {
	    if (quantico_boldItalic == null) {
	        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(size));
	    }

	    return quantico_boldItalic.deriveFont(size);
	}
}
