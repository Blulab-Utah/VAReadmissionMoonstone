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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class ReadmissionPatientClassificationTable {

	private ReadmissionPatientClassificationTables tables = null;
	private String annotator = null;
	Hashtable<String, ReadmissionPatientClassificationTableObject> patientVariableObjectHash = new Hashtable();
	private Vector<ReadmissionPatientClassificationTableObject> allTableObjects = null;
	private Hashtable<String, Vector<ReadmissionPatientClassificationTableObject>> variableObjectHash = new Hashtable();
	private Hashtable<String, String> patientHash = new Hashtable();
	public static String Separator = ":";

	public ReadmissionPatientClassificationTable(
			ReadmissionPatientClassificationTables tables, String annotator) {
		this.tables = tables;
		this.annotator = annotator;
	}

	public void addTableObject(String pname, String vname, String cname) {
		ReadmissionPatientClassificationTableObject to = new ReadmissionPatientClassificationTableObject(
				this, pname, vname, cname);
		String key = pname + Separator + vname;
		this.patientVariableObjectHash.put(key, to);
		VUtils.pushHashVector(this.variableObjectHash, vname, to);
		this.patientHash.put(pname, pname);
		this.allTableObjects = VUtils.add(this.allTableObjects, to);
	}

	public String getClassification(String pname, String vname) {
		String key = pname + Separator + vname;
		ReadmissionPatientClassificationTableObject to = this.patientVariableObjectHash
				.get(key);
		String pstr = this.patientHash.get(pname);
		if (to != null) {
			return to.getClassification();
		}
		return null;
	}

	public void extractTableObjectsFromEHost(ReadmissionEHostPatientResults repr) {
//		this.clear();
		for (String key : repr.patientVariableClassificationHash.keySet()) {
			String[] strs = key.split(":");
			String pname = strs[0];
			String vname = strs[1];
			String cname = repr.getPatientClassification(pname, vname);
			if (cname != null) {
				this.addTableObject(pname, vname, cname);
			}
		}
	}

	public void readTableFile(File f) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line = null;
			int lineoffset = 0;
			while ((line = in.readLine()) != null) {
				if (line.length() > 6) {
					String[] strs = line
							.split("\\|");
					if (strs != null && strs.length == 3) {
						String pname = strs[0];
						String vname = strs[1];
						String cname = strs[2];
						this.addTableObject(pname, vname, cname);
					}
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getAnnotator() {
		return annotator;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (ReadmissionPatientClassificationTableObject to : this.allTableObjects) {
			sb.append(to.toString() + "\n");
		}
		return sb.toString();
	}

	public void clear() {
		this.patientVariableObjectHash.clear();
		this.allTableObjects = null;
	}

	public Vector<String> getVariables() {
		Vector<String> v = HUtils.getKeys(this.variableObjectHash);
		if (v != null) {
			Collections.sort(v);
		}
		return v;
	}

	public Vector<String> getPatients() {
		Vector<String> v = HUtils.getKeys(this.patientHash);
		if (v != null) {
			Collections.sort(v);
		}
		return v;
	}

	public void sortTableObjects() {
		if (this.allTableObjects != null) {
			Collections
					.sort(this.allTableObjects,
							new ReadmissionPatientClassificationTableObject.TableObjectSorter());
		}
	}

}
