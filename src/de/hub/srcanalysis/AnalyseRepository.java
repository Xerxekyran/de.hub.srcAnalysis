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
import de.hub.emffrag.util.Extensions;
import de.hub.srcanalysis.datamodel.FileDependency;
import de.hub.srcanalysis.gitModelVisitor.JavaClassAnalysisGitModelVisitor;
import de.hub.srcanalysis.gui.ScatterPlot;
import de.hub.srcrepo.emffrag.extensions.ExtensionsPackage;
import de.hub.srcrepo.emffrag.extensions.ImportLog;
import de.hub.srcrepo.gitmodel.Commit;
import de.hub.srcrepo.gitmodel.EmfFragSourceRepository;
import de.hub.srcrepo.gitmodel.Ref;
import de.hub.srcrepo.gitmodel.emffrag.metadata.GitModelPackage;
import de.hub.srcrepo.gitmodel.util.GitModelUtil;

/**
 * 
 * @author george
 * 
 */
public class AnalyseRepository {

    private static Logger log = Logger.getRootLogger();

    // private static String repoUri = "hbase://localhost/srcrepo.example.bin";
//    private static String repoUri = "hbase://localhost/emffrag.bin";
     private static String repoUri = "hbase://localhost/srcrepo.example2.bin";

    private FragmentedModel model = null;
    private Model javaModel = null;
    private EmfFragSourceRepository gitModel = null;

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
	JavaClassAnalysisGitModelVisitor visitor = new JavaClassAnalysisGitModelVisitor(null);
	GitModelUtil.visitCommitHierarchy(gitModel.getRootCommit(), visitor);
	return visitor.getJavaClasses();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	AnalyseRepository analyzer = new AnalyseRepository();

	analyzer.init();

	HashMap<String, CompilationUnit> javaClasses = analyzer.findJavaClassesForCommit(analyzer.getHeadCommit());

	Iterator<CompilationUnit> iterator = javaClasses.values().iterator();
	while (iterator.hasNext()) {
	    CompilationUnit next = iterator.next();
	    ImportLog importLog = Extensions.get(next, ImportLog.class);
	    if (importLog != null) {
		System.out.println("Error in Log: " + importLog.getEntries().toString());
	    } else {
		System.out.println(next.getOriginalFilePath() + " :: " + next.getName());
	    }
	}

	System.out.println("#################");
	System.out.println("Number of compilation units: " + javaClasses.size());

	JavaClassCouplingAnalysis javaClassCouplingAnalysis = new JavaClassCouplingAnalysis();
	javaClassCouplingAnalysis.calculateCouplings(javaClasses);

	TreeMap<String, ArrayList<FileDependency>> couplings = javaClassCouplingAnalysis.getCouplings();

	analyzer.persistData(couplings, "output.txt");
	
	ScatterPlot scatterplot = new ScatterPlot("File based dependency analysis", couplings);
	scatterplot.pack();
	RefineryUtilities.centerFrameOnScreen(scatterplot);
	scatterplot.setVisible(true);

    }

    /**
     * 
     * @param data
     * @param fileName
     */
    private void persistData(Map<String, ArrayList<FileDependency>> data, String fileName) {
	File file = new File(fileName);
	try {
	    FileWriter writer = new FileWriter(file, false);

	    Iterator<String> iterator = data.keySet().iterator();
	    while (iterator.hasNext()) {
		String key = iterator.next();
		Iterator<FileDependency> values = data.get(key).iterator();
		while (values.hasNext()) {
		    FileDependency val = values.next();
		    writer.write(key + "," + val.getTargetDependency());
		    writer.write(System.getProperty("line.separator"));
		}
	    }

	    writer.flush();

	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
