package de.hub.srcanalysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.gmt.modisco.java.CompilationUnit;
import org.eclipse.gmt.modisco.java.Model;
import org.eclipse.gmt.modisco.java.emffrag.metadata.JavaPackage;
import org.jfree.ui.RefineryUtilities;

import de.hub.emffrag.EmfFragActivator;
import de.hub.emffrag.fragmentation.FragmentedModel;
import de.hub.emffrag.fragmentation.IndexBasedIdSemantics.IdBehaviour;
import de.hub.emffrag.fragmentation.NoReferencesIdSemantics;
import de.hub.emffrag.fragmentation.ReflectiveMetaModelRegistry;
import de.hub.emffrag.hbase.EmfFragHBaseActivator;
import de.hub.emffrag.model.emffrag.EmfFragPackage;
import de.hub.srcanalysis.couplingAnalysis.JavaClassCouplingAnalysis;
import de.hub.srcanalysis.datamodel.FileDependency;
import de.hub.srcanalysis.datamodel.TimeValue;
import de.hub.srcanalysis.gitModelVisitor.JavaClassAnalysisGitModelVisitor;
import de.hub.srcanalysis.gui.JavaClassCouplingScatterPlot;
import de.hub.srcanalysis.gui.PropagationCostPlot;
import de.hub.srcanalysis.metrics.DependencyMatrix;
import de.hub.srcrepo.emffrag.extensions.ExtensionsPackage;
import de.hub.srcrepo.gitmodel.Commit;
import de.hub.srcrepo.gitmodel.EmfFragSourceRepository;
import de.hub.srcrepo.gitmodel.ParentRelation;
import de.hub.srcrepo.gitmodel.Ref;
import de.hub.srcrepo.gitmodel.emffrag.metadata.GitModelPackage;
import de.hub.srcrepo.gitmodel.util.GitModelUtil;

/**
 * Testclass for an anylsis of an Git repository saved with EMF-Fragment
 * 
 * 
 * @author george
 * 
 */
public class AnalyseRepository {

    private static Logger log = Logger.getRootLogger();

    // private static String repoUri = "hbase://localhost/srcrepo.example.bin";
//    private static String repoUri = "hbase://localhost/emffrag.bin";
     private static String repoUri = "hbase://localhost/srcrepo.example2.bin";

    private static boolean SHOW_GRAPH = false;
    private static boolean PERSIST_COUPLING_DATA_TO_FILE = true;
    private static String PERSIST_COUPLING_FILENAME = "data/coupling_data_%TIME%.txt";
    
    private static boolean PERSIST_PROPAGATION_DATA_TO_FILE = true;
    private static String PERSIST_PROPAGATION_FILENAME = "data/prop_data.txt";
    
    
    private static boolean ANALYZE_RECURSIVE = true;
    private static boolean ANALYZE_PROPAGATION_COST = true;

    private static int CNT_SKIP_COMMITS = 0;

    private FragmentedModel model = null;
    private Model javaModel = null;
    private EmfFragSourceRepository gitModel = null;

    private HashMap<String, ArrayList<TimeValue>> propagationCosts = new HashMap<String, ArrayList<TimeValue>>();

    /**
     * 
     */
    private void init() {
	EmfFragActivator.standalone(JavaPackage.eINSTANCE, GitModelPackage.eINSTANCE, ExtensionsPackage.eINSTANCE);
	EmfFragActivator.instance.useBinaryFragments = true;
	EmfFragActivator.instance.idSemantics = new NoReferencesIdSemantics(IdBehaviour.defaultModel);
	EmfFragHBaseActivator.standalone();

	ResourceSet rs = new ResourceSetImpl();
	ReflectiveMetaModelRegistry.instance.registerUserMetaModel(EmfFragPackage.eINSTANCE);
	Resource resource = rs.createResource(URI.createURI(AnalyseRepository.repoUri));
	FragmentedModel model = (FragmentedModel) resource;
	EmfFragActivator.instance.defaultModel = model;

	this.model = model;
	this.gitModel = (EmfFragSourceRepository) this.model.getContents().get(0);
	this.javaModel = (Model) this.model.getContents().get(1);
    }

    /**
     * 
     * @return
     */
    public Commit getHeadCommit() {
	Commit ret = null;

	Iterator<Ref> iterator = this.gitModel.getAllRefs().iterator();
	while (iterator.hasNext()) {
	    Ref next = iterator.next();
	    if (next.getName().equals("HEAD")) {
		ret = next.getReferencedCommit();
		break;
	    }
	}

	if (ret == null)
	    System.out.println("WARNING: Could not find the Head Commit in the references of the GIT model");

	return ret;
    }

    /**
     * 
     * @param commit
     * @return
     */
    public HashMap<String, CompilationUnit> findJavaClassesForCommit(Commit commit) {
	JavaClassAnalysisGitModelVisitor visitor = new JavaClassAnalysisGitModelVisitor(commit);
	GitModelUtil.visitCommitHierarchy(gitModel.getRootCommit(), visitor);
	return visitor.getJavaClasses();
    }

    /**
     * 
     * @param commit
     */
    private TreeMap<String, ArrayList<FileDependency>> analyzeCommit(Commit commit) {
	HashMap<String, CompilationUnit> javaClasses = findJavaClassesForCommit(commit);

	// Iterator<CompilationUnit> iterator = javaClasses.values().iterator();
	// while (iterator.hasNext()) {
	// CompilationUnit next = iterator.next();
	// ImportLog importLog = Extensions.get(next, ImportLog.class);
	// if (importLog != null) {
	// System.out.println("Error in Log: " +
	// importLog.getEntries().toString());
	// } else {
	// System.out.println(next.getOriginalFilePath() + " :: " +
	// next.getName());
	// }
	// }
	// System.out.println("#################");
	// System.out.println("Number of compilation units: " +
	// javaClasses.size());

	// analyze thecoupling
	JavaClassCouplingAnalysis javaClassCouplingAnalysis = new JavaClassCouplingAnalysis();
	TreeMap<String, ArrayList<FileDependency>> couplings = javaClassCouplingAnalysis.calculateCouplings(javaClasses);

	// save as cvs file?
	if (AnalyseRepository.PERSIST_COUPLING_DATA_TO_FILE) {
	    persistCouplings(couplings, PERSIST_COUPLING_FILENAME);
	}

	// with graph?
	if (AnalyseRepository.SHOW_GRAPH) {
	    JavaClassCouplingScatterPlot scatterplot = new JavaClassCouplingScatterPlot("File based dependency analysis", couplings);
	    scatterplot.pack();
	    RefineryUtilities.centerFrameOnScreen(scatterplot);
	    scatterplot.setVisible(true);
	}

	return couplings;
    }

    public void doWork() {
	TreeMap<String, ArrayList<FileDependency>> tmpCouplings = null;

	Commit currentCommit = getHeadCommit();

	System.out.print("Commit [" + currentCommit.getTime() + "] ");
	tmpCouplings = analyzeCommit(currentCommit);

	if (AnalyseRepository.ANALYZE_PROPAGATION_COST) {
	    System.out.println(addPropagationCostValue(getRepoUri(), tmpCouplings, currentCommit.getTime().getTime()));
	}

	int skipCounter = 0;

	if (ANALYZE_RECURSIVE) {
	    // repeat the analysis
	    EList<ParentRelation> parentRelations = currentCommit.getParentRelations();
	    while (parentRelations.size() != 0) {
		// TODO: maybe follow all branches?
		currentCommit = parentRelations.get(0).getParent();
		if (currentCommit != null) {

		    if (skipCounter >= CNT_SKIP_COMMITS) {
			System.out.print("Commit [" + currentCommit.getTime() + "] ");
			tmpCouplings = analyzeCommit(currentCommit);

			if (AnalyseRepository.ANALYZE_PROPAGATION_COST) {
			    System.out.println(addPropagationCostValue(getRepoUri(), tmpCouplings, currentCommit.getTime().getTime()));
			}

			skipCounter = 0;
		    }
		    skipCounter++;

		    parentRelations = currentCommit.getParentRelations();
		} else {
		    break;
		}
	    }
	}

	if (AnalyseRepository.ANALYZE_PROPAGATION_COST) {
	    PropagationCostPlot demo = new PropagationCostPlot("Propagation Costs", propagationCosts);
	    demo.pack();
	    RefineryUtilities.centerFrameOnScreen(demo);
	    demo.setVisible(true);
	}
    }

    /**
     * 
     * @return
     */
    public static String getRepoUri() {
	return repoUri;
    }

    /**
     * 
     * @param repoName
     * @param couplings
     */
    private double addPropagationCostValue(String repoName, TreeMap<String, ArrayList<FileDependency>> couplings, Long time) {
	// calculate the DependencyMatrix
	DependencyMatrix dependencyMatrix = new DependencyMatrix(couplings);
	double propagationCost = dependencyMatrix.getPropagationCost();

	if (propagationCosts.get(repoName) == null) {
	    propagationCosts.put(repoName, new ArrayList<TimeValue>());
	}
	
	ArrayList<TimeValue> values = propagationCosts.get(repoName);
	values.add(new TimeValue(propagationCost, time));
	
	if(PERSIST_PROPAGATION_DATA_TO_FILE) {
	    persistPropagationCosts(PERSIST_PROPAGATION_FILENAME);
	}
	
	return propagationCost;
    }

    /**
     * 
     * @param fileName
     */
    private void persistPropagationCosts(String fileName) {
	File file = new File(fileName);
	try {
	    FileWriter writer = new FileWriter(file, false);

	    Iterator<String> iterator = propagationCosts.keySet().iterator();
	    while (iterator.hasNext()) {
		String key = iterator.next();
		Iterator<TimeValue> values = propagationCosts.get(key).iterator();
		while (values.hasNext()) {
		    TimeValue val = values.next();
		    writer.write(key + "," + val.time + "," + val.value);
		    writer.write(System.getProperty("line.separator"));
		}
	    }

	    writer.flush();

	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    /**
     * 
     * @param data
     * @param fileName
     */
    private void persistCouplings(Map<String, ArrayList<FileDependency>> data, String fileName) {
	File file = new File(replaceTimeStr(fileName));
	try {
	    FileWriter writer = new FileWriter(file, false);

	    Iterator<String> iterator = data.keySet().iterator();
	    while (iterator.hasNext()) {
		String key = iterator.next();
		Iterator<FileDependency> values = data.get(key).iterator();
		while (values.hasNext()) {
		    FileDependency val = values.next();
		    writer.write(key + "," + val.getTargetDependency() + "," + val.getDependecyType());		    
		    writer.write(System.getProperty("line.separator"));
		}
	    }
	    
	    writer.flush();

	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    /**
     * 
     * @param str
     * @return
     */
    private String replaceTimeStr(String str) {
	return str.replaceAll("%TIME%", "" + System.currentTimeMillis());
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	AnalyseRepository analyzer = new AnalyseRepository();
	analyzer.init();
	analyzer.doWork();
    }
}
