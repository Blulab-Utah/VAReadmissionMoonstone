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

import java.util.Comparator;

import tsl.utilities.VUtils;

public class ReadmissionPatientClassificationTableObject {

	private ReadmissionPatientClassificationTable table = null;
	private String patientID = null;
	private int patientIDInt = -1;
	private String variable = null;
	private String classification = null;

	public static String Separator = "|";

	public ReadmissionPatientClassificationTableObject(
			ReadmissionPatientClassificationTable table, String pname,
			String vname, String cname) {
		try {
			this.table = table;
			this.patientID = pname;
			this.patientIDInt = Integer.parseInt(pname);
			this.variable = vname;
			this.classification = cname;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		String str = this.patientID + Separator + this.variable + Separator
				+ this.classification;
		return str;
	}

	public ReadmissionPatientClassificationTable getTable() {
		return table;
	}

	public String getPatientID() {
		return patientID;
	}

	public String getVariable() {
		return variable;
	}

	public String getClassification() {
		return classification;
	}

	public static class TableObjectSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			ReadmissionPatientClassificationTableObject to1 = (ReadmissionPatientClassificationTableObject) o1;
			ReadmissionPatientClassificationTableObject to2 = (ReadmissionPatientClassificationTableObject) o2;
			// int pval = to1.getPatientID().compareTo(to2.getPatientID());
			int pval = to1.patientIDInt - to2.patientIDInt;
			int vval = to1.getVariable().compareTo(to2.getVariable());
			int cval = to1.getClassification().compareTo(
					to2.getClassification());
			if (pval < 0) {
				return -1;
			} else if (pval > 0) {
				return 1;
			} else if (vval < 0) {
				return -1;
			} else if (vval > 0) {
				return 1;
			}
			if (cval < 0) {
				return -1;
			} else if (cval > 0) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}
