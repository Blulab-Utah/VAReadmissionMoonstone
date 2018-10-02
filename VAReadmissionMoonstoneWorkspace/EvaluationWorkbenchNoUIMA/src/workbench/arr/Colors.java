/*
Copyright 2018 Wendy Chapman (wendy.chapman\@utah.edu) & Lee Christensen (leenlp\@q.com)

Licensed under the Apache License, Version 2.0 (the \"License\");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an \"AS IS\" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package workbench.arr;

import java.awt.Color;

// TODO: Auto-generated Javadoc
/**
 * The Class Colors.
 */
public class Colors {

	/** The dark tan. */
	public static Color darkTan = new Color(0x8C8A71);

	/** The light tan. */
	public static Color lightTan = new Color(0xCCCAB0);

	/** The dark taupe. */
	public static Color darkTaupe = new Color(0xACBA9D);

	/** The light taupe. */
	public static Color lightTaupe = new Color(0xD3DBCA);

	/** The dark blue gray. */
	public static Color darkBlueGray = new Color(0xA6ADCC);
	
	public static Color darkGreen = new Color(0, 128, 0);
	
	public static Color darkYellow = new Color(128, 128, 0);

	/** The very dark blue gray. */
	public static Color veryDarkBlueGray = new Color(0x616781);

	/** The light blue gray. */
	public static Color lightBlueGray = new Color(0xD3D7E6);
	
	/** Extremely light gray. */
	public static Color extremelyLightGray = new Color(0xEEEEEE);

	/** The light purple. */
	public static Color lightPurple = new Color(0xE9C5E9);

	/** The dark purple. */
	public static Color darkPurple = new Color(0xC973C9);

	/** The very light gray. */
	public static Color veryLightGray = new Color(0xEBEBEB);

	/** The light pink. */
	public static Color lightPink = new Color(0xBF6D8C);

	/** The verified selected snippet annotation true. */
	public static Color verifiedSelectedSnippetAnnotationTrue = Color.green;

	/** The verified unselected snippet annotation true. */
	public static Color verifiedUnselectedSnippetAnnotationTrue = new Color(
			0x91FF00);
	
	/** The verified selected snippet annotation false. */
	public static Color verifiedSelectedSnippetAnnotationFalse = Color.yellow;

	/** The verified unselected snippet annotation false. */
	public static Color verifiedUnselectedSnippetAnnotationFalse = Color.orange;

	/** The verified selected document annotation true. */
	public static Color verifiedSelectedDocumentAnnotationTrue = new Color(
			0x548CBF);

	/** The verified selected document annotation false. */
	public static Color verifiedSelectedDocumentAnnotationFalse = new Color(
			0x7C8EBF);

	/** The verified unselected document annotation true. */
	public static Color verifiedUnselectedDocumentAnnotationTrue = new Color(
			0x2896BF);

	/** The verified unselected document annotation false. */
	public static Color verifiedUnselectedDocumentAnnotationFalse = new Color(
			0x97BFB7);

	public static Color getUnselectedClassificationPaneColor(
			typesystem.Annotation level) {
		if (level != null) {
			String levelName = level.getName().toLowerCase();
			if ("documentannotation".equals(levelName)) {
				return Color.white;
			}
			if ("snippetannotation".equals(levelName)) {
				return lightTan;
			}
		}
		return Color.white;
	}
	
	public static Color bluifyHue(Color c) {
		double red = c.getRed() + 0.05 * c.getRed();
		double green = c.getGreen() + 0.05 * c.getGreen();
		double blue = c.getBlue() + 0.2 * c.getBlue();
		if (red > 255) {
			red = 255;
		}
		if (green > 255) {
			green = 255;
		}
		if (blue > 255) {
			blue = 255;
		}
		Color newc = new Color((int) red, (int) green, (int) blue);
		return newc;
	}
	
	public static Color redifyHue(Color c) {
		double red = c.getRed() + 0.2 * c.getRed();
		double green = c.getGreen() + 0.0 * c.getGreen();
		double blue = c.getBlue() + 0.0 * c.getBlue();
		if (red > 255) {
			red = 255;
		}
		if (green > 255) {
			green = 255;
		}
		if (blue > 255) {
			blue = 255;
		}
		Color newc = new Color((int) red, (int) green, (int) blue);
		return newc;
	}
	
}
