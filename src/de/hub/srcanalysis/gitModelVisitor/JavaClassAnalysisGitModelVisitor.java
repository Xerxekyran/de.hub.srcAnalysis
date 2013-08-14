package de.hub.srcanalysis.gitModelVisitor;

import java.util.HashMap;

import org.eclipse.gmt.modisco.java.CompilationUnit;

import de.hub.srcrepo.IGitModelVisitor;
import de.hub.srcrepo.gitmodel.Commit;
import de.hub.srcrepo.gitmodel.Diff;
import de.hub.srcrepo.gitmodel.JavaDiff;

public class JavaClassAnalysisGitModelVisitor implements IGitModelVisitor {

    private HashMap<String, CompilationUnit> javaClasses = new HashMap<String, CompilationUnit>();
    private Commit haltOnCommit = null;

    /**
     * 
     */
    public JavaClassAnalysisGitModelVisitor() {
    }

    /**
     * Constructor that creates a visitor that will halt on the given commit
     * (starting from the root of the repo)
     * 
     * @param commit
     */
    public JavaClassAnalysisGitModelVisitor(Commit commit) {
	this.haltOnCommit = commit;
    }

    @Override
    public void onMerge(Commit mergeCommit, Commit branchCommit) {
    }

    @Override
    public boolean onStartCommit(Commit commit) {		
	if(commit.getTime().equals(this.haltOnCommit.getTime())) {
	    // System.out.println("Found the commit to stop analyzing. Now stopping.");
	    return false;
	}
	
	return true;
    }

    @Override
    public void onCompleteCommit(Commit commit) {	
    }

    @Override
    public void onCopiedFile(Diff diff) {
    }

    @Override
    public void onRenamedFile(Diff diff) {	
    }

    @Override
    public void onAddedFile(Diff diff) {	
	if (diff instanceof JavaDiff) {
	    JavaDiff jd = (JavaDiff) diff;	    
	    if(jd.getCompilationUnit() != null) {
		javaClasses.put(jd.getNewPath(), jd.getCompilationUnit());
	    } else {
		// System.out.println("WARN: Got a JavaDiff with a null as CompilationUnit ("+ diff +")");
	    }
	    
	}
    }

    @Override
    public void onModifiedFile(Diff diff) {
	onAddedFile(diff);
    }

    @Override
    public void onDeletedFile(Diff diff) {
	if (diff instanceof JavaDiff) {	    	    
	    JavaDiff jd = (JavaDiff) diff;
	    javaClasses.remove(jd.getOldPath());
	}

    }

    public HashMap<String, CompilationUnit> getJavaClasses() {
	return javaClasses;
    }
}
