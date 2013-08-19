package de.hub.srcanalysis.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import de.hub.srcanalysis.datamodel.TimeValue;

/**
 * Visualisation of the propagation costs over time
 * 
 * @author george
 * 
 */
public class PropagationCostPlot extends ApplicationFrame {

    /**
     * 
     */
    private static final long serialVersionUID = -6250806269163172779L;

    public PropagationCostPlot(String title, HashMap<String, ArrayList<TimeValue>> data) {
	super(title);
	
	// XYLineAndShapeRenderer renderer = new XYSplineRenderer();
	XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	// renderer.setSeriesShapesVisible(0, false);
	
	JFreeChart chart = createChart(createDataset(data));
	XYPlot xyplot = chart.getXYPlot();
	xyplot.setRenderer(renderer);
	
	ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }
    
    /**
     * Creates a sample chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return A sample chart.
     */
    private JFreeChart createChart(IntervalXYDataset dataset) {
        final JFreeChart chart = ChartFactory.createXYLineChart("XY Series", "Time", "Propagation Cost", dataset, PlotOrientation.VERTICAL, true, true, false);                       
        return chart;    
    }

    /**
     * Creates a sample dataset.
     * 
     * @return A sample dataset.
     */
    private IntervalXYDataset createDataset(HashMap<String, ArrayList<TimeValue>> data) {
	final XYSeriesCollection dataset = new XYSeriesCollection();
	
	Iterator<String> it = data.keySet().iterator();
	while(it.hasNext()) {
	    String key = it.next();
	    
	    final XYSeries series = new XYSeries(key);
	    
	    Iterator<TimeValue> valueIt = data.get(key).iterator();	    
	    while(valueIt.hasNext()) {		
		TimeValue val = valueIt.next();
		series.add(val.time, val.value);
	    }
	    
	    dataset.addSeries(series);
	}
        
        return dataset;
    }        
}
