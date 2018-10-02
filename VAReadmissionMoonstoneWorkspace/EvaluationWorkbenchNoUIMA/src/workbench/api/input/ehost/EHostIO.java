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
package workbench.api.input.ehost;

import java.awt.Color;
import java.util.Vector;

import org.jdom.Element;

import tsl.utilities.JDomUtils;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Classification;

public class EHostIO {

	public static boolean isEhostProjectSchema(Element root) {
		Element cnode = JDomUtils.getElementByName(root,
				"eHOST_Project_Configure");
		return cnode != null;
	}

	public static void extractTypes(workbench.api.typesystem.TypeSystem ts,
			Element root) {
		Vector<Element> cnodes = JDomUtils.getElementsByName(root, "classDef");
		if (cnodes != null) {
			for (Element cnode : cnodes) {
				Element tnnode = JDomUtils.getElementByName(cnode, "Name");
				String tname = tnnode.getText();
				
				String redstr = JDomUtils.getValueByName(cnode, "RGB_R");
				String greenstr = JDomUtils.getValueByName(cnode, "RGB_G");
				String bluestr = JDomUtils.getValueByName(cnode, "RGB_B");
				Color color = null;
				if (redstr != null && greenstr != null && bluestr != null) {
					int red = Integer.parseInt(redstr);
					int green = Integer.parseInt(greenstr);
					int blue = Integer.parseInt(bluestr);
					color = new Color(red, green, blue);
				}
				workbench.api.typesystem.Type type = new workbench.api.typesystem.Type(
						ts, null, tname, color); // How to get parent?
				Vector<Element> anodes = JDomUtils.getElementsByName(cnode,
						"attributeDef");
				if (anodes != null) {
					for (Element anode : anodes) {
						extractAttribute(type, anode);
					}
				}
			}
		}
		
		// 10/12/2015: Attach independently defined attributes to the root.
		// Later they can be moved to whatever type they are actually attached
		// to.
		// (Not sure if this is a good idea. I seem to have overwritten the code
		// I used previously...)
		Element panode = JDomUtils.getElementByName(root, "attributeDefs");
		if (panode != null) {
			Vector<Element> anodes = JDomUtils.getElementsByName(panode,
					"attributeDef");
			if (anodes != null) {
				for (Element anode : anodes) {
					extractAttribute(ts.getRootType(), anode);
				}
			}
		}
		
		// 7/20/2016:  Attach types to root if not previously attached.
		workbench.api.typesystem.Type rtype = ts.getRootType();
		for (workbench.api.typesystem.Type type : ts.getTypes()) {
			if (type.getParent() == null && !type.equals(rtype)) {
				type.setParent(rtype);
			}
		}
		
	}

	private static void extractAttribute(workbench.api.typesystem.Type type,
			Element anode) {
		workbench.api.typesystem.TypeSystem ts = type.getTypeSystem();
		Element annode = JDomUtils.getElementByName(anode, "Name");
		if (annode != null) {
			String aname = annode.getText();
			if (ts.isClassificationName(aname)) {
				new Classification(type, aname);
			} else {
				new Attribute(type, aname);
			}
		}
	}

	public static void extractTypes(typesystem.TypeSystem ts, Element root) {
		try {
			Vector<Element> cnodes = JDomUtils.getElementsByName(root,
					"classDef");
			if (cnodes != null) {
				for (Element cnode : cnodes) {
					Element tnnode = JDomUtils.getElementByName(cnode, "Name");
					String tname = tnnode.getText();
					typesystem.Annotation type = new typesystem.Annotation(ts,
							tname,
							Class.forName("annotation.SnippetAnnotation"), null);
					Vector<Element> anodes = JDomUtils.getElementsByName(cnode,
							"attributeDef");
					if (anodes != null) {
						for (Element anode : anodes) {
							Element annode = JDomUtils.getElementByName(cnode,
									"Name");
							String aname = annode.getText();
							typesystem.Attribute attr = new typesystem.Attribute(
									null, null, null, null);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
