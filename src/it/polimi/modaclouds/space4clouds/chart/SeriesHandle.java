package it.polimi.modaclouds.space4clouds.chart;

public class SeriesHandle {

	private final int position;
	private final Logger2JFreeChartImage logger;
	public SeriesHandle(int pos, Logger2JFreeChartImage logger) {
		this.position = pos;
		this.logger = logger;
	}
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}
	/**
	 * @return the logger
	 */
	public Logger2JFreeChartImage getLogger() {
		return logger;
	}
	

}
