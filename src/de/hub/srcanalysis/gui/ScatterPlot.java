package de.hub.srcanalysis.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

import de.hub.srcanalysis.datamodel.DependencyType;
import de.hub.srcanalysis.datamodel.FileDependency;

public class ScatterPlot extends ApplicationFrame {
    public ScatterPlot(String s, Map<String, ArrayList<FileDependency>> dataSet) {
	super(s);
	JPanel jpanel = createDemoPanel(dataSet, s);
	jpanel.setPreferredSize(new Dimension(500, 270));
	setContentPane(jpanel);
    }

    public static JPanel createDemoPanel(Map<String, ArrayList<FileDependency>> dataSet, String title) {

	JFreeChart jfreechart = ChartFactory.createScatterPlot(title, "Source", "Dependency Target", convertDataSet(dataSet),
		PlotOrientation.VERTICAL, true, true, false);

	Shape cross = ShapeUtilities.createDiagonalCross(3, 1);
	XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
	XYItemRenderer renderer = xyPlot.getRenderer();
	renderer.setBaseShape(cross);
	renderer.setBasePaint(Color.green);
	renderer.setSeriesShape(0, cross);

	// changing the Renderer to XYDotRenderer
	// xyPlot.setRenderer(new XYDotRenderer());
	// XYDotRenderer xydotrenderer = new XYDotRenderer();
	// xyPlot.setRenderer(xydotrenderer);
	// xydotrenderer.setSeriesShape(0, cross);

	xyPlot.setDomainCrosshairVisible(true);
	xyPlot.setRangeCrosshairVisible(true);

	return new ChartPanel(jfreechart);
    }

    /**
     * 
     * @param dataSet
     * @return
     */
    private static XYDataset convertDataSet(Map<String, ArrayList<FileDependency>> dataSet) {
	XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
	XYSeries unknownSeries = new XYSeries("File Dependencies Unknown");
	XYSeries functionCallSeries = new XYSeries("File Dependencies Function Call");
	XYSeries importSeries = new XYSeries("File Dependencies Imports");

	HashMap<String, Integer> indexMapper = new HashMap<String, Integer>();
	int index = 0;

	Iterator<String> iterator = dataSet.keySet().iterator();
	while (iterator.hasNext()) {
	    String key = iterator.next();

	    if (indexMapper.get(key) == null) {
		indexMapper.put(key, index);
		index++;
	    }
	}

	index = 0;

	// now add the dependency data points
	Iterator<String> it = dataSet.keySet().iterator();
	while (it.hasNext()) {
	    String key = it.next();
	    ArrayList<FileDependency> dependencies = dataSet.get(key);

	    Iterator<FileDependency> depIt = dependencies.iterator();
	    while (depIt.hasNext()) {
		FileDependency dependence = depIt.next();

		Integer xVal = indexMapper.get(key);
		Integer yVal = indexMapper.get(dependence.getTargetDependency());
		
		if(dependence.getDependecyType().equals(DependencyType.FunctionCall)) {		    
		    functionCallSeries.add(xVal, yVal);
		} else if(dependence.getDependecyType().equals(DependencyType.Unknown)) {
		    unknownSeries.add(xVal, yVal);		
	    	} else if(dependence.getDependecyType().equals(DependencyType.Import)) {
		    importSeries.add(xVal, yVal);
		}
		
		System.out.println("adding (" + xVal + " / " + yVal + ") :: " + key + " -> " + dependence.getTargetDependency());
	    }
	}

	xySeriesCollection.addSeries(unknownSeries);
	xySeriesCollection.addSeries(functionCallSeries);
	xySeriesCollection.addSeries(importSeries);
	return xySeriesCollection;
    }

    public static void main(String args[]) {
	TreeMap<String, ArrayList<FileDependency>> data = new TreeMap<String, ArrayList<FileDependency>>();

	ArrayList<FileDependency> dependencyFiles = new ArrayList<FileDependency>();
	dependencyFiles.add(new FileDependency("File1", "File2", DependencyType.Import));
	data.put("File1", dependencyFiles);

	dependencyFiles = new ArrayList<FileDependency>();
	dependencyFiles.add(new FileDependency("File2", "File1", DependencyType.Import));
	dependencyFiles.add(new FileDependency("File2", "File3", DependencyType.Import));	
	data.put("File2", dependencyFiles);

	dependencyFiles = new ArrayList<FileDependency>();
	dependencyFiles.add(new FileDependency("File3", "File1", DependencyType.Import));	
	data.put("File3", dependencyFiles);

	dependencyFiles = new ArrayList<FileDependency>();
	dependencyFiles.add(new FileDependency("File4", "File1", DependencyType.Import));	
	data.put("File4", dependencyFiles);

	ScatterPlot scatterplotdemo4 = new ScatterPlot("Scatter Plot Demo 4", data);
	scatterplotdemo4.pack();
	RefineryUtilities.centerFrameOnScreen(scatterplotdemo4);
	scatterplotdemo4.setVisible(true);
    }
}
