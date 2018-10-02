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
package moonstone.io.readmission;

public class ReadmissionProjectSchema {

	public String[] relevantTypes = null;
	public String[] relevantAttributes = null;

	// Maps types to attributes
	public String[][] relevantTypeAttributeMap = null;

	// contains lists of types, and values for each type.
	public String[][] typeAttributeValueMap = null;
	
	//
	public String[] targetBinaryConcepts = null;

	public String[] targetConcepts = null;

	// Maps "type:value" pairs to Moonstone concepts.
	public String[][] conceptConversionMap = null;

	public String[][] translationalConceptMap = null;
	public String[][] negatedConceptMap = null;

	// 3/28/2016
	public String[][] defaultAttributeValueMap = null;

	public String[] getRelevantTypes() {
		return relevantTypes;
	}

	public String[] getRelevantAttributes() {
		return relevantAttributes;
	}

	public String[][] getRelevantTypeAttributeMap() {
		return relevantTypeAttributeMap;
	}

	public String[][] getTypeAttributeValueMap() {
		return typeAttributeValueMap;
	}

	public String[] getTargetBinaryConcepts() {
		return targetBinaryConcepts;
	}

	public String[] getTargetConcepts() {
		return targetConcepts;
	}

	public String[][] getConceptConversionMap() {
		return conceptConversionMap;
	}

	public String[][] getTranslationalConceptMap() {
		return translationalConceptMap;
	}

	public String[][] getNegatedConceptMap() {
		return negatedConceptMap;
	}

}
