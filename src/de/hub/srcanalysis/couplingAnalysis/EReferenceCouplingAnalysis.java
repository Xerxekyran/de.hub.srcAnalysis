package de.hub.srcanalysis.couplingAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.eclipse.gmt.modisco.java.CompilationUnit;

import de.hub.srcanalysis.datamodel.FileDependency;

/**
 * 
 * @author george
 *
 */
public class EReferenceCouplingAnalysis implements CouplingAnalysis {

    @Override
    public TreeMap<String, ArrayList<FileDependency>> calculateCouplings(HashMap<String, CompilationUnit> map) {

	return null;
    }

}
