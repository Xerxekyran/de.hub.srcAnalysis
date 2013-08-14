package de.hub.srcanalysis.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
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
    /**
     * 
     */
    private static final long serialVersionUID = -4370752563010488523L;

    private Map<String, ArrayList<FileDependency>> dataSet = null;    
    private HashMap<String, LinkedList<FileDependency>> indexMapIndexToFileDependency = null;

    /**
     * 
     * @param s
     * @param dataSet
     */
    public ScatterPlot(String s, Map<String, ArrayList<FileDependency>> dataSet) {
	super(s);
	this.dataSet = dataSet;

	JPanel jpanel = createPanel(dataSet, s);
	jpanel.setPreferredSize(new Dimension(800, 600));
	((ChartPanel) jpanel).addChartMouseListener(new ChartMouseListener() {

	    @Override
	    public void chartMouseMoved(ChartMouseEvent event) {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public void chartMouseClicked(ChartMouseEvent event) {
		ChartEntity entity = event.getEntity();
		if (entity instanceof XYItemEntity) {
		    // we got an element
		    XYItemEntity xyitem = (XYItemEntity) entity;

		    XYDataset dataset = (XYDataset) xyitem.getDataset(); // get
									 // data
									 // set

		    double x = dataset.getXValue(xyitem.getSeriesIndex(), xyitem.getItem());
		    double y = dataset.getYValue(xyitem.getSeriesIndex(), xyitem.getItem());

		    System.out.println("On position (" + x + " / " + y + ") are the following dependencies: ");
		    LinkedList<FileDependency> fileDependency = getFileDependency(Double.valueOf(x).intValue(), Double.valueOf(y).intValue());

		    Iterator<FileDependency> it = fileDependency.iterator();
		    while (it.hasNext()) {
			FileDependency next = it.next();
			System.out.print("--> ");
			System.out.println(next);
		    }

		    // System.out.println(xyitem.getItem() + " item of " +
		    // xyitem.getSeriesIndex() + "series");
		    // System.out.println(x);
		    // System.out.println(y);

		}
	    }
	});
	setContentPane(jpanel);
    }

    /**
     * 
     * @param dataSet
     * @param title
     * @return
     */
    public JPanel createPanel(Map<String, ArrayList<FileDependency>> dataSet, String title) {

	JFreeChart jfreechart = ChartFactory.createScatterPlot(title, "Source", "Dependency Target", convertDataSet(dataSet),
		PlotOrientation.VERTICAL, true, true, false);

	Shape cross = ShapeUtilities.createDiagonalCross(3, 1);
	XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
	XYItemRenderer renderer = xyPlot.getRenderer();
	renderer.setBaseShape(cross);
	renderer.setBasePaint(Color.green);
	renderer.setSeriesShape(0, cross);

	xyPlot.setDomainCrosshairVisible(true);
	xyPlot.setRangeCrosshairVisible(true);

	return new ChartPanel(jfreechart);
    }

    /**
     * 
     * @param dataSet
     * @return
     */
    private XYDataset convertDataSet(Map<String, ArrayList<FileDependency>> dataSet) {
	XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
	XYSeries unknownSeries = new XYSeries("Unknown");
	XYSeries functionCallSeries = new XYSeries("Function Call");
	XYSeries importSeries = new XYSeries("Import");

	HashMap<String, Integer> indexMapPathToIndex = new HashMap<String, Integer>();
	int index = 0;

	Iterator<String> iterator = dataSet.keySet().iterator();
	while (iterator.hasNext()) {
	    String key = iterator.next();

	    if (indexMapPathToIndex.get(key) == null) {
		indexMapPathToIndex.put(key, index);
		index++;
	    }
	}

	indexMapIndexToFileDependency = new HashMap<String, LinkedList<FileDependency>>();

	index = 0;

	// now add the dependency data points
	Iterator<String> it = dataSet.keySet().iterator();

	while (it.hasNext()) {
	    String key = it.next();
	    ArrayList<FileDependency> dependencies = dataSet.get(key);

	    Iterator<FileDependency> depIt = dependencies.iterator();
	    while (depIt.hasNext()) {
		FileDependency dependence = depIt.next();

		Integer xVal = indexMapPathToIndex.get(key);
		Integer yVal = indexMapPathToIndex.get(dependence.getTargetDependency());

		if (dependence.getDependecyType().equals(DependencyType.FunctionCall)) {
		    functionCallSeries.add(xVal, yVal);
		} else if (dependence.getDependecyType().equals(DependencyType.Unknown)) {
		    unknownSeries.add(xVal, yVal);
		} else if (dependence.getDependecyType().equals(DependencyType.Import)) {
		    importSeries.add(xVal, yVal);
		}

		LinkedList<FileDependency> fileDeps = indexMapIndexToFileDependency.get(xVal + "_" + yVal);
		if (fileDeps == null) {
		    LinkedList<FileDependency> newList = new LinkedList<FileDependency>();
		    newList.add(dependence);
		    indexMapIndexToFileDependency.put(xVal + "_" + yVal, newList);
		} else {
		    fileDeps.add(dependence);
		}

		System.out.println("adding (" + xVal + " / " + yVal + ") :: " + key + " -> " + dependence.getTargetDependency() + " ["
			+ dependence.getDependecyType() + "]");
	    }
	}

	xySeriesCollection.addSeries(unknownSeries);
	xySeriesCollection.addSeries(functionCallSeries);
	xySeriesCollection.addSeries(importSeries);
	return xySeriesCollection;
    }

    /**
     * 
     * @param x
     * @param y
     * @return
     */
    public LinkedList<FileDependency> getFileDependency(Integer x, Integer y) {
	// indexMapIndexToFileDependency.get(xyKey);
	// System.out.println("Getting "+ x + " | "+ y);
	return indexMapIndexToFileDependency.get(x + "_" + y);
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
