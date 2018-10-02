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
package tsl.knowledge.engine;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tsl.documentanalysis.document.HeaderContent;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;
import tsl.knowledge.ontology.umls.UMLSOntology;
import tsl.knowledge.ontology.umls.UMLSTypeConstant;
import tsl.startup.StartupParameters;
import tsl.utilities.VUtils;

public class KnowledgeEngine {
	private String name = null;
	private KnowledgeBase rootKnowledgeBase = null;
	private KnowledgeBase lastKnowledgeBase = null;
	private KnowledgeBase currentKnowledgeBase = rootKnowledgeBase;
	private Ontology currentOntology = null;
	private Ontology lastOntology = null;
	private UMLSOntology umlsOntology = null;
	private Vector<Ontology> ontologies = null;
	private boolean isInitialized = false;
	// private Properties properties = null;
	private FileIO fileIO = null;
	private String[] javaPathnames = null;
	private Hashtable<String, KnowledgeBase> knowledgeBaseHash = new Hashtable();
	private StartupParameters startupParameters = null;

	public static KnowledgeEngine currentKnowledgeEngine = null;
	private static KnowledgeEngine staticKnowledgeEngine = null;
	private static boolean DoQueryDebug = false;
	private static boolean BreakAtFirstProof = false;
	private static boolean StoredBreakAtFirstProof = false;
	private static String JavaPathnameList = "JavaPackages";
	private static String ParameterDelimiter = ",";
	private static int MaxExpressionProofWrapperSize = 1000;
	public static String DefaultTSLPropertiesFileName = "tsl.properties";

	public static void main(String[] args) {
		StartKnowledgeEngine();
	}

	public KnowledgeEngine() {
		currentKnowledgeEngine = this;
		this.currentKnowledgeBase = this.rootKnowledgeBase = new KnowledgeBase();
	}

	public KnowledgeEngine(String name, boolean loadFiles,
			String propertyFileName) {
		initialize(name, loadFiles, propertyFileName);
	}

	public void initialize(String name, boolean loadFiles,
			String propertyFileName) {
		try {
			if (!this.isInitialized) {
				System.out.println("LOADING TSL KNOWLEDGE ENGINE");
				this.isInitialized = true;
				this.name = name;
				currentKnowledgeEngine = this;
				this.startupParameters = new StartupParameters(propertyFileName);
				// readProperties();

				String str = this.startupParameters
						.getPropertyValue(JavaPathnameList);
				if (str != null) {
					this.javaPathnames = str.split(ParameterDelimiter);
				}

				this.currentKnowledgeBase = this.rootKnowledgeBase = new KnowledgeBase(
						"root", this, null);
				UMLSTypeConstant.initialize();
				if (loadFiles) {
					this.doLoadFiles();
				}
				HeaderContent.initialize();
				System.out.println("FINISHED LOADING KNOWLEDGE ENGINE");
			}
		} catch (Exception e) {
			System.out.println("TSL Knowledge Engine:  Error Encountered");
			e.printStackTrace();
		}
	}

	public void doLoadFiles() {
		try {
			this.fileIO = new FileIO();
			this.fileIO.loadAllKBFiles();
		} catch (Exception e) {
			System.out
					.println("TSL Knowledge Engine Load Files:  Error Encountered");
			e.printStackTrace();
		}
	}

	public void setCurrentKnowledgeBase(KnowledgeBase kb) {
		this.lastKnowledgeBase = this.currentKnowledgeBase;
		this.currentKnowledgeBase = kb;
	}

	public void resetCurrentKnowledgeBase() {
		this.currentKnowledgeBase = this.lastKnowledgeBase;
		this.lastKnowledgeBase = null;
	}

	public static KnowledgeEngine getCurrentKnowledgeEngine(
			String propertyFileName) {
		return getCurrentKnowledgeEngine(false, propertyFileName);
	}
	
	public static KnowledgeEngine getCurrentKnowledgeEngine() {
		return getCurrentKnowledgeEngine(false, DefaultTSLPropertiesFileName);
	}
	
	public static KnowledgeEngine getCurrentKnowledgeEngine(boolean loadFiles) {
		return getCurrentKnowledgeEngine(loadFiles, DefaultTSLPropertiesFileName);
	}

	public static KnowledgeEngine getCurrentKnowledgeEngine(boolean loadFiles,
			String propertyFileName) {
		if (currentKnowledgeEngine == null) {
			currentKnowledgeEngine = new KnowledgeEngine("main", loadFiles,
					propertyFileName);
		}
		return currentKnowledgeEngine;
	}

	public Ontology findOrCreateOntology(String name) {
		Ontology ontology = findOntology(name);
		if (ontology == null) {
			ontology = new Ontology(name);
			currentKnowledgeEngine.ontologies = VUtils.add(
					currentKnowledgeEngine.ontologies, ontology);
			this.setCurrentOntology(ontology);
		}
		return ontology;
	}

	public UMLSOntology getUMLSOntology() {
		if (this.umlsOntology == null) {
			this.umlsOntology = new UMLSOntology();
		}
		return this.umlsOntology;
	}

	public Ontology getOntology() {
		return this.currentOntology;
	}

	public void setOntology(Ontology ontology) {
		this.currentOntology = ontology;
	}

	public Ontology getCurrentOntology() {
		return this.currentOntology;
	}

	public Vector<Ontology> getOntologies() {
		return this.ontologies;
	}

	public void setCurrentOntology(Ontology ontology) {
		this.lastOntology = this.currentOntology;
		this.currentOntology = ontology;
	}

	public void resetCurrentOntology() {
		this.currentOntology = this.lastOntology;
	}

	public Ontology findOntology(String name) {
		if (this.ontologies != null) {
			for (Ontology ontology : this.ontologies) {
				if (name.equals(ontology.getName())) {
					return ontology;
				}
			}
		}
		return null;
	}

	// private void readProperties() {
	// InputStream is = null;
	// try {
	// if (this.properties == null) {
	// this.properties = new Properties();
	// is = getClass().getResourceAsStream("/startup.properties");
	// if (is == null) {
	// is = new FileInputStream("./startup.properties");
	// }
	// this.properties.load(is);
	// String value = this.properties.getProperty(JavaPathnameList);
	// if (value != null) {
	// this.javaPathnames = value.split(ParameterDelimiter);
	// }
	// is.close();
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// try {
	// is.close();
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	// }
	// }

	// public String getPropertyValue(String property) {
	// return this.properties.getProperty(property);
	// }
	//
	// public void setPropertyValue(String property, Object value) {
	// this.properties.put(property, value);
	// }

	// public boolean isPropertyTrue(String property) {
	// String value = this.
	// String value = this.properties.getProperty(property);
	// if (value != null && "true".equals(value.toString().toLowerCase())) {
	// return true;
	// }
	// return false;
	// }

	public String getName() {
		return this.name;
	}

	public String[] getJavaPathnames() {
		return this.javaPathnames;
	}

	public void setJavaPathnames(String[] javaPathnames) {
		this.javaPathnames = javaPathnames;
	}
	
	public static void StartKnowledgeEngine() {
		StartKnowledgeEngine(DefaultTSLPropertiesFileName);
	}

	public static void StartKnowledgeEngine(String propertyFileName) {
		try {
			if (staticKnowledgeEngine == null) {
				staticKnowledgeEngine = KnowledgeEngine
						.getCurrentKnowledgeEngine(propertyFileName);
			}
			KnowledgeEngineRunnableClass kerc = staticKnowledgeEngine.new KnowledgeEngineRunnableClass();
			Thread t = new Thread(kerc);
			t.start();
			while (true) {
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class KnowledgeEngineRunnableClass implements Runnable {

		KnowledgeEngineRunnableClass() {
		}

		public void run() {
			try {
				KnowledgeEngine.getCurrentKnowledgeEngine();
			} catch (Exception e) {
				String msg = e.getStackTrace().toString();
				JOptionPane.showMessageDialog(new JFrame(), msg);
				System.exit(-1);
			}
		}

	}

	public static boolean isDoQueryDebug() {
		return DoQueryDebug;
	}

	public static void setDoQueryDebug(boolean doQueryDebug) {
		DoQueryDebug = doQueryDebug;
	}

	public static boolean isBreakAtFirstProof() {
		return BreakAtFirstProof;
	}

	public static void setBreakAtFirstProof(boolean breakAtFirstProof) {
		StoredBreakAtFirstProof = BreakAtFirstProof;
		BreakAtFirstProof = breakAtFirstProof;
	}

	public static void restoreBreakAtFirstProof() {
		BreakAtFirstProof = StoredBreakAtFirstProof;
		StoredBreakAtFirstProof = false;
	}

	// KNOWLEDGE BASE FUNCTIONS: Use push/pop to set / reset the currentKB. Use
	// hashtable to store / find named KBs.

	public KnowledgeBase getKnowledgeBase() {
		return this.getCurrentKnowledgeBase();
	}

	public KnowledgeBase getCurrentKnowledgeBase() {
		return this.currentKnowledgeBase;
	}

	public KnowledgeBase getKnowledgeBaseHash(String name) {
		return this.knowledgeBaseHash.get(name);
	}

	public KnowledgeBase findOrCreateKnowledgeBase(String name) {
		KnowledgeBase kb = getKnowledgeBaseHash(name);
		if (kb == null) {
			kb = new KnowledgeBase(name);
		}
		return kb;
	}

	public void storeKnowledgeBaseHash(KnowledgeBase kb) {
		this.knowledgeBaseHash.put(kb.getName(), kb);
	}

	public void removeKnowledgeBaseHash(KnowledgeBase kb) {
		this.knowledgeBaseHash.remove(kb.getName());
	}

	public void pushKnowledgeBase(KnowledgeBase kb) {
		if (kb != null && this.currentKnowledgeBase != null
				&& kb != this.currentKnowledgeBase) {
			kb.setParentKB(this.currentKnowledgeBase);
			this.setCurrentKnowledgeBase(kb);
		}
	}

	public void popAndRemoveKnowledgeBase(KnowledgeBase kb) {
		this.popKnowledgeBase();
		this.removeKnowledgeBaseHash(kb);
	}

	public void popKnowledgeBase() {
		if (this.currentKnowledgeBase != null
				&& this.currentKnowledgeBase.getParentKB() != null) {
			this.currentKnowledgeBase = this.currentKnowledgeBase.getParentKB();
			this.lastKnowledgeBase = null;
		}
	}

	public StartupParameters getStartupParameters() {
		return startupParameters;
	}

	public void setStartupParameters(StartupParameters startupParameters) {
		this.startupParameters = startupParameters;
	}

	public String[] getJavaClassnames() {
		return this.javaPathnames;
	}

	public KnowledgeBase getRootKnowledgeBase() {
		return rootKnowledgeBase;
	}

}
