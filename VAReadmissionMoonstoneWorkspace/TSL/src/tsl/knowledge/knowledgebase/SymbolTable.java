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
package tsl.knowledge.knowledgebase;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.function.FunctionConstant;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class SymbolTable {

	private Hashtable<String, RelationConstant> relationConstantHash = new Hashtable();
	private Hashtable<String, FunctionConstant> functionConstantHash = new Hashtable();
	private Hashtable<String, TypeConstant> typeConstantHash = new Hashtable();
	private Hashtable<String, SyntacticTypeConstant> syntacticTypeConstantHash = new Hashtable();
	private Hashtable<String, ObjectConstant> objectConstantHash = new Hashtable();
	private Hashtable<String, PropertyConstant> propertyConstantHash = new Hashtable();
	private Hashtable<String, StringConstant> stringConstantHash = new Hashtable();
	private Hashtable<TypeConstant, Vector<Constant>> typedConstantHash = new Hashtable();

	public Vector<StringConstant> getAllStringConstants() {
		return HUtils.getElements(stringConstantHash);
	}

	// Problem: There are two indexes for each type, so this returns doubles.
	public Vector<TypeConstant> getAllTypeConstants() {
		Vector<TypeConstant> types = null;
		for (Enumeration<String> e = typeConstantHash.keys(); e.hasMoreElements();) {
			String name = e.nextElement();
			if (Character.isLowerCase(name.charAt(0))) {
				TypeConstant type = typeConstantHash.get(name);
				types = VUtils.add(types, type);
			}
		}
		return types;
	}

	public Vector<Constant> getAllTypedConstants(TypeConstant type) {
		return typedConstantHash.get(type);
	}

	public Vector<RelationConstant> getAllRelationConstants() {
		return HUtils.getElements(relationConstantHash);
	}

	public Vector<FunctionConstant> getAllFunctionConstants() {
		return HUtils.getElements(functionConstantHash);
	}

	public Vector<PropertyConstant> getAllPropertyConstants() {
		return HUtils.getElements(propertyConstantHash);
	}

	public Constant getConstant(String name) {
		Constant c = null;
		if (((c = getTypeConstant(name)) != null) || ((c = getTypeConstant(name)) != null)
				|| ((c = getStringConstant(name)) != null) || ((c = getRelationConstant(name)) != null)
				|| ((c = getFunctionConstant(name)) != null) || ((c = getPropertyConstant(name)) != null)
				|| ((c = getPropertyConstant(name)) != null)) {
			return c;
		}
		return null;
	}

	public TypeConstant getTypeConstant(String name) {
		if (name != null) {
			TypeConstant tc = typeConstantHash.get(name);
			return tc;
		}
		return null;
	}

	public SyntacticTypeConstant getSyntacticTypeConstant(String name) {
		if (name != null) {
			SyntacticTypeConstant tc = syntacticTypeConstantHash.get(name);
			return tc;
		}
		return null;
	}

	public RelationConstant getRelationConstant(String name) {
		if (name != null) {
			return relationConstantHash.get(name);
		}
		return null;
	}

	public FunctionConstant getFunctionConstant(String name) {
		if (name != null) {
			return functionConstantHash.get(name);
		}
		return null;
	}

	public ObjectConstant getObjectConstant(String name) {
		if (name != null) {
			return objectConstantHash.get(name);
		}
		return null;
	}

	public PropertyConstant getPropertyConstant(String name) {
		if (name != null) {
			return propertyConstantHash.get(name);
		}
		return null;
	}

	public StringConstant getStringConstant(String name) {
		if (name != null) {
			return stringConstantHash.get(name);
		}
		return null;
	}

	public void addTypeConstant(TypeConstant type) {
		typeConstantHash.put(type.getName(), type);
		typeConstantHash.put(type.getFormalName(), type);
	}
	
	public void addSyntacticTypeConstant(SyntacticTypeConstant type) {
		syntacticTypeConstantHash.put(type.getName(), type);
		syntacticTypeConstantHash.put(type.getFormalName(), type);
	}

	public void addRelationConstant(RelationConstant rc) {
		relationConstantHash.put(rc.getName(), rc);
	}

	public void addFunctionConstant(FunctionConstant fc) {
		functionConstantHash.put(fc.getName(), fc);
	}

	public void addObjectConstant(ObjectConstant oc) {
		objectConstantHash.put(oc.getName(), oc);
	}

	public void addPropertyConstant(PropertyConstant oc) {
		propertyConstantHash.put(oc.getName(), oc);
	}

	public void addStringConstant(StringConstant sc) {
		stringConstantHash.put(sc.getName(), sc);
		if (sc.getType() != null) {
			VUtils.pushHashVector(typedConstantHash, sc.getType(), sc);
		}
	}

	public void resolveConstants() {
		for (Enumeration<String> e = typeConstantHash.keys(); e.hasMoreElements();) {
			typeConstantHash.get(e.nextElement()).resolve();
		}
		for (Enumeration<String> e = syntacticTypeConstantHash.keys(); e.hasMoreElements();) {
			syntacticTypeConstantHash.get(e.nextElement()).resolve();
		}
		for (Enumeration<String> e = relationConstantHash.keys(); e.hasMoreElements();) {
			relationConstantHash.get(e.nextElement()).resolve();
		}
		for (Enumeration<String> e = functionConstantHash.keys(); e.hasMoreElements();) {
			functionConstantHash.get(e.nextElement()).resolve();
		}
	}

}
