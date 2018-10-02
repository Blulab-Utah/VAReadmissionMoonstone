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
package io.knowtator;

import java.util.Vector;

import org.jdom.Element;

import tsl.utilities.JDomUtils;
import tsl.utilities.VUtils;

public class KTSimpleInstance {

	Element node = null;
	KnowtatorIO kt = null;
	String name = null;
	String id = null;
	String level = "snippet";
	boolean visited = false;
	Vector ktvector = null;

	static int currentID = 0;

	public KTSimpleInstance(KnowtatorIO kt) throws Exception {
		this.kt = kt;
		kt.simpleInstances = VUtils.add(kt.simpleInstances, this);
	}

	public KTSimpleInstance(KnowtatorIO kt, String name, Element node)
			throws Exception {
		this.kt = kt;
		this.name = name;
		this.node = node;
		this.id = "SimpleInstance_" + currentID++;
		if (name != null) {
			kt.addHashItem(name, this);
		}
		kt.simpleInstances = VUtils.add(kt.simpleInstances, this);
	}

	public KTSimpleInstance(KnowtatorIO kt, String name) throws Exception {
		this.kt = kt;
		this.name = name;
		if (name != null) {
			kt.addHashItem(name, this);
		}
		kt.simpleInstances = VUtils.add(kt.simpleInstances, this);
	}

	public KTSimpleInstance(KnowtatorIO kt, String name, Vector v)
			throws Exception {
		this.kt = kt;
		this.name = name;
		this.ktvector = v;
		if (name != null) {
			kt.addHashItem(name, this);
		}
		kt.simpleInstances = VUtils.add(kt.simpleInstances, this);
	}

	static void extractSimpleInstances(String filename, Element root,
			KnowtatorIO kt) throws Exception {
		if (kt.isOriginalXMLFormat()) {
			extractSimpleInstancesOriginal(root, kt);
		} else if (kt.isSHARPXMLFormat()) {
			extractSimpleInstancesSHARP(filename, root, kt);
		} else if (kt.isLispFormat()) {

		}
	}

	static void extractSimpleInstancesLisp(Vector<Vector> v, KnowtatorIO kt,
			String fname) throws Exception {
		if (v != null) {

			for (Vector sv : v) {
				String type = sv.elementAt(2).toString().toLowerCase();
				String name = sv.firstElement().toString();
				if ("knowtator+class+mention".equals(type)) {
					new KTClassMention(kt, name, sv);
				} else if ("knowtator+complex+slot+mention".equals(type)) {
					new KTComplexSlotMention(kt, name, sv);
				} else if ("knowtator+annotation".equals(type)) {
					new KTAnnotation(kt, sv, fname);
				} else if ("knowtator+human+annotator".equals(type)) {
					new KTAnnotator(kt, name, sv);
				} else if ("knowtator+annotator+team".equals(type)) {
					new KTAnnotator(kt, name, sv);
				} else if ("knowtator+string+slot+mention".equals(type)) {
					new KTStringSlotMention(kt, name, sv);
				} else if ("knowtator+configuration".equals(type)) {
					kt.extractConfigurationInfo(sv);
				}
			}
		}
	}

	static void extractSimpleInstancesOriginal(Element root, KnowtatorIO kt)
			throws Exception {
		Vector<Element> nodes = JDomUtils.getElementsByName(root,
				"simple_instance");
		if (nodes != null) {
			for (Element node : nodes) {
				Element cnode = JDomUtils.getElementByName(node, "name");
				String name = cnode.getText();
				cnode = JDomUtils.getElementByName(node, "type");
				String type = cnode.getText();
				if ("knowtator class mention".equals(type)) {
					new KTClassMention(kt, name, node);
				} else if ("knowtator complex slot mention".equals(type)) {
					new KTComplexSlotMention(kt, name, node);
				} else if ("knowtator annotation".equals(type)) {
					new KTAnnotation(kt, name, node);
				} else if ("knowtator human annotator".equals(type)) {
					new KTAnnotator(kt, name, node);
				} else if ("knowtator string slot mention".equals(type)) {
					new KTStringSlotMention(kt, name, node);
				}
			}
		}
	}

	static void extractSimpleInstancesSHARP(String filename, Element root,
			KnowtatorIO kt) throws Exception {
		Element anode = JDomUtils.getElementByName(root, "annotations");
		if (anode != null) {
			kt.textSource = anode.getAttributeValue("textSource");
			Vector<Element> nodes = JDomUtils.getElementsByName(root,
					"annotation");
			if (nodes != null) {
				for (Element node : nodes) {
					new KTAnnotation(kt, null, node);
				}
			}
			nodes = JDomUtils.getElementsByName(root, "classMention");
			if (nodes != null) {
				for (Element node : nodes) {
					String name = node.getAttributeValue("id");
					new KTClassMention(kt, name, node);
				}
			}
			nodes = JDomUtils.getElementsByName(root, "stringSlotMention");
			if (nodes != null) {
				for (Element node : nodes) {
					String name = node.getAttributeValue("id");
					new KTStringSlotMention(kt, name, node);
				}
			}
			nodes = JDomUtils.getElementsByName(root, "complexSlotMention");
			if (nodes != null) {
				for (Element node : nodes) {
					String name = node.getAttributeValue("id");
					new KTComplexSlotMention(kt, name, node);
				}
			}
		}
	}

	public void extractInformation() throws Exception {
		if (kt.isOriginalXMLFormat()) {
			this.extractInformationXMLFormatOriginal();
		} else if (kt.isSHARPXMLFormat()) {
			this.extractInformationXMLFormatSHARP();
		} else if (kt.isLispFormat()) {
			this.extractInformationLispFormat(this.ktvector);
		}
		this.node = null;
	}

	public void extractInformationLispFormat(Vector v) throws Exception {

	}

	public void extractInformationXMLFormatOriginal() throws Exception {

	}

	public void extractInformationXMLFormatSHARP() throws Exception {

	}

	public void resolveReferences() throws Exception {
	}

	public String toString() {
		return "<SI:" + this.name + ">";
	}

	public String getID() {
		return this.id;
	}

	public String getLevel() {
		return this.level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public KnowtatorIO getKt() {
		return kt;
	}

}
