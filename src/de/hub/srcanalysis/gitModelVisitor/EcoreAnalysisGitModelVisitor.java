package de.hub.srcanalysis.gitModelVisitor;

import de.hub.srcrepo.IGitModelVisitor;
import de.hub.srcrepo.gitmodel.Commit;
import de.hub.srcrepo.gitmodel.Diff;

public class EcoreAnalysisGitModelVisitor implements IGitModelVisitor {

    @Override
    public void onMerge(Commit mergeCommit, Commit branchCommit) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public boolean onStartCommit(Commit commit) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void onCompleteCommit(Commit commit) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void onCopiedFile(Diff diff) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void onRenamedFile(Diff diff) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void onAddedFile(Diff diff) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void onModifiedFile(Diff diff) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void onDeletedFile(Diff diff) {
	// TODO Auto-generated method stub
	
    }

}
