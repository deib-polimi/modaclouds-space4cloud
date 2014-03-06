package it.polimi.modaclouds.space4clouds.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartCreator {

	private String charTitle;
	public String getCharTitle() {
		return charTitle;
	}

	public void setCharTitle(String charTitle) {
		this.charTitle = charTitle;
	}

	private XYSeriesCollection seriesCollection = new XYSeriesCollection();
	
	public JFreeChart getChart() {
		
		return ChartFactory.createXYLineChart(
                charTitle,
                "X", 
                "Y", 
                seriesCollection,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            );
	}

	public ChartCreator(String charTitle) {

		this.charTitle = charTitle;
		
	}
	
	public void addSeries(XYSeries s){
		seriesCollection.addSeries(s);
	}

	public void removeAllSeries(){
		seriesCollection.removeAllSeries();
	}

}
