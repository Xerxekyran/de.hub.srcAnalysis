package de.hub.srcanalysis.gui;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

public class FastScatterPlotter extends ApplicationFrame {

    /** A constant for the number of items in the sample dataset. */
    private static final int COUNT = 500000;

    /** The data. */
    private float[][] data = new float[2][COUNT];

    public FastScatterPlotter(String title, HashMap<String, ArrayList<String>> dataSet) {
	super(title);

	populateData(dataSet);

	final NumberAxis domainAxis = new NumberAxis("X");
	domainAxis.setAutoRangeIncludesZero(false);
	final NumberAxis rangeAxis = new NumberAxis("Y");
	rangeAxis.setAutoRangeIncludesZero(false);
	final FastScatterPlot plot = new FastScatterPlot(this.data, domainAxis, rangeAxis);
	final JFreeChart chart = new JFreeChart("Fast Scatter Plot", plot);

	// force aliasing of the rendered content..
	chart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	final ChartPanel panel = new ChartPanel(chart, true);
	panel.setPreferredSize(new java.awt.Dimension(1024, 768));
	// panel.setHorizontalZoom(true);
	// panel.setVerticalZoom(true);
	panel.setDomainZoomable(true);
	panel.setMinimumDrawHeight(10);
	panel.setMaximumDrawHeight(2000);
	panel.setMinimumDrawWidth(20);
	panel.setMaximumDrawWidth(2000);	 
	
	setContentPane(panel);

    }

    /**
     * Populates the data array with random values.
     */
    private void populateData(HashMap<String, ArrayList<String>> dataSet) {
	data = new float[2][5000];

	// first generate a file to index mapping
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
	    ArrayList<String> dependencies = dataSet.get(key);

	    Iterator<String> depIt = dependencies.iterator();
	    while (depIt.hasNext()) {
		String dependentFileName = depIt.next();

		Integer xVal = indexMapper.get(key);
		Integer yVal = indexMapper.get(dependentFileName);

		this.data[0][index] = xVal; 	// X
		this.data[1][index] = yVal; 	// Y
		index++;
		System.out.println("adding ("+ xVal +" / "+ yVal +")");
	    }
	}

	// for (int i = 0; i < this.data[0].length; i++) {
	// final float x = (float) i + 100000;
	// this.data[0][i] = x;
	// this.data[1][i] = 100000 + (float) Math.random() * COUNT;
	// }

    }

    /**
     * Starting point for the demonstration application.
     * 
     * @param args
     *            ignored.
     */
    public static void main(final String[] args) {

	HashMap<String, ArrayList<String>> data = new HashMap<String, ArrayList<String>>();

	ArrayList<String> dependencyFiles = new ArrayList<String>();
	dependencyFiles.add("File2");
	data.put("File1", dependencyFiles);

	dependencyFiles = new ArrayList<String>();
	dependencyFiles.add("File1");
	dependencyFiles.add("File3");
	data.put("File2", dependencyFiles);

	dependencyFiles = new ArrayList<String>();
	dependencyFiles.add("File1");
	data.put("File3", dependencyFiles);

	dependencyFiles = new ArrayList<String>();
	dependencyFiles.add("File1");
	data.put("File4", dependencyFiles);

	final FastScatterPlotter demo = new FastScatterPlotter("Fast Scatter Plot Demo", data);
	demo.pack();
	RefineryUtilities.centerFrameOnScreen(demo);
	demo.setVisible(true);

    }
}
