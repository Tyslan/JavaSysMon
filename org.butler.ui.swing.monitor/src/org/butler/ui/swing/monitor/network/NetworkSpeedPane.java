package org.butler.ui.swing.monitor.network;

import java.util.LinkedList;

import javax.swing.JPanel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;

public class NetworkSpeedPane extends JPanel {
	private static final long serialVersionUID = 2477860885814641037L;

	private int nrOfValues;
	private LinkedList<Double> fifo;

	private XYChart chart;
	private JPanel chartPanel;

	public NetworkSpeedPane(String title, int nrOfValues) {
		chart = buildChart();
		chart.addSeries("speed", new double[] { 0 });
		styleChart();

		chartPanel = new XChartPanel<>(chart);

		this.nrOfValues = nrOfValues;
		fifo = new LinkedList<>();

		add(chartPanel);
	}

	private XYChart buildChart() {
		return new XYChartBuilder().width(200).height(50).build();
	}

	private void styleChart() {
		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setXAxisTicksVisible(false);
		chart.getStyler().setYAxisTicksVisible(false);
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Area);
		chart.getStyler().setMarkerSize(0);

		chart.getStyler().setChartPadding(0);
	}

	public void setSpeedData(double bps) {
		fifo.addLast(bps);
		if (fifo.size() > nrOfValues) {
			fifo.removeFirst();
		}
		chart.updateXYSeries("speed", null, fifo, null);
	}

	public void redraw() {
		chartPanel.revalidate();
		chartPanel.repaint();
	}
}
