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
package workbench.api.typesystem;

import java.util.Hashtable;
import java.util.Vector;

import tsl.expression.term.type.TypeConstant;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import workbench.api.Analysis;

public class TypeSystem {
	private Vector<Type> types = null;
	private Hashtable<String, Object> objectHash = new Hashtable();
	private Type rootType = null;
	private Analysis analysis = null;
	private Hashtable<String, String> classificationPropertyNameHash = new Hashtable();
	
	// 10/8/2015
	private Hashtable typeTypeConstantHash = new Hashtable();

	public TypeSystem() {
		int x = 1;
	}

	public TypeSystem(Analysis analysis) {
		this.analysis = analysis;
		if (analysis.getKnownledgeEngine() != null) {
			String dcstr = analysis.getKnownledgeEngine()
					.getStartupParameters()
					.getPropertyValue("DefaultClassificationProperties");
			if (dcstr != null) {
				Vector<String> cnames = StrUtils.stringList(dcstr, ',');
				for (String cname : cnames) {
					this.classificationPropertyNameHash.put(cname, cname);
				}
			}
		}
	}

	public void addType(Type type) {
		if (this.objectHash.get(type.getFullname()) == null) {
			this.objectHash.put(type.getFullname(), type);
			this.objectHash.put(type.getName(), type);
			this.types = VUtils.add(this.types, type);
		}
	}

	public Type addType(typesystem.Annotation ta) {
		typesystem.Annotation pa = (typesystem.Annotation) ta
				.getParentTypeObject();
		Type ptype = null;
		if (pa != null) {
			ptype = addType(pa);
		}
		Type type = (Type) this.objectHash.get(ta.getName());
		if (type == null) {
			type = new Type(this, ptype, ta.getName(), null);
		}
		return type;
	}

	public String toXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<workbench_type_system>\n");
		if (this.types != null) {
			for (Type type : this.types) {
				sb.append(type.toXML());
			}
		}
		sb.append("</workbench_type_system>\n");
		return sb.toString();
	}

	public void putObjectHash(String key, Object object) {
		this.objectHash.put(key, object);
	}
	
	// 10/12/2015
	public void removeObjectHash(String key) {
		this.objectHash.remove(key);
	}

	public Object getObjectHash(String key) {
		return objectHash.get(key);
	}

	public Type getOrCreateType(Type parent, String tname) {
		Type type = getType(tname);
		if (type == null) {
			getRootType();
			if (parent == null) {
				parent = this.rootType;
			}
			type = new Type(this, parent, tname, null);
		}
		return type;
	}

	public Type getType(String tname) {
		if (tname != null) {
			return (Type) this.objectHash.get(tname);
		}
		return null;
	}

	public Attribute getAttribute(String aname) {
		Attribute attribute = (Attribute) this.objectHash.get(aname);
		return attribute;
	}

	public String toString() {
		String str = "<TypeSystem: Types=" + this.types + ">";
		return str;
	}

	public static String eliminatePrepend(String aname) {
		int index = aname.lastIndexOf("$");
		if (index > 0) {
			aname = aname.substring(index + 1);
		}
		return aname;
	}

	public Type getRootType() {
		if (this.rootType == null) {
			this.rootType = new Type(this, (Type) null, "ROOT", null);
		}
		return this.rootType;
	}

	public boolean isClassificationName(String cname) {
		int index = cname.lastIndexOf(':');
		if (index > 0) {
			cname = cname.substring(index + 1);
		}
		if (this.classificationPropertyNameHash.get(cname) != null
				|| this.classificationPropertyNameHash.get(cname.toLowerCase()) != null) {
			return true;
		}
		return false;
	}

	public Vector<Type> getTypes() {
		return types;
	}

	public Hashtable<String, Object> getObjectHash() {
		return objectHash;
	}

	public Analysis getAnalysis() {
		return analysis;
	}
	
	// 10/8/2015
	public Type getType(TypeConstant tc) {
		return (Type) this.typeTypeConstantHash.get(tc);
	}
	
	public TypeConstant getTypeConstant(Type type) {
		return (TypeConstant) this.typeTypeConstantHash.get(type);
	}

}
