package de.hub.srcanalysis.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hub.srcanalysis.datamodel.FileDependency;

/**
 * 
 * @author george
 * 
 */
public class DependencyMatrix {

    private Integer[][] depMatrix = null;

    /**
     * 
     * @param rawDependencyData
     */
    public DependencyMatrix(Map<String, ArrayList<FileDependency>> rawDependencyData) {
	this.calculateDependencyMatrix(rawDependencyData);
    }

    /**
     * 
     * @param rawDependencyData
     */
    private void calculateDependencyMatrix(Map<String, ArrayList<FileDependency>> rawDependencyData) {
	int dimension = rawDependencyData.size();
	depMatrix = new Integer[dimension][dimension];

	// we need to map Strings (Map-Keys) to indices first
	HashMap<String, Integer> indexMapPathToIndex = new HashMap<String, Integer>();
	int index = 0;
	Iterator<String> iterator = rawDependencyData.keySet().iterator();
	while (iterator.hasNext()) {
	    String key = iterator.next();

	    if (indexMapPathToIndex.get(key) == null) {
		indexMapPathToIndex.put(key, index);
		index++;
	    }
	}

	// now generate the dependencyMatrix
	iterator = rawDependencyData.keySet().iterator();
	while (iterator.hasNext()) {
	    String key = iterator.next();
	    Iterator<FileDependency> it = rawDependencyData.get(key).iterator();
	    while (it.hasNext()) {
		FileDependency dep = it.next();

		int x = indexMapPathToIndex.get(dep.getTargetDependency());
		int y = indexMapPathToIndex.get(dep.getSourceDependency());

		depMatrix[x][y] = 1;
	    }
	}
    }

    /**
     * 
     * @return The propagation cost in percent
     */
    public double getPropagationCost() {
	double ret = 0.0;
	long rowSum = 0;

	int matriceSize = this.depMatrix.length;

	for (int y = 0; y < matriceSize; y++) {
	    for (int x = 0; x < matriceSize; x++) {
		if(this.depMatrix[x][y] != null) {
		    rowSum += this.depMatrix[x][y];    
		}
		   
	    }
	    
	    ret += rowSum;
	    rowSum = 0;
	}
	System.out.println("ret: "+ ret + " of matrice size: "+ matriceSize);
	
	if(ret == 0 || matriceSize == 0)
	    return 0;
	
	ret = (ret / (matriceSize * matriceSize)) * 100;
	return ret;
    }
}
