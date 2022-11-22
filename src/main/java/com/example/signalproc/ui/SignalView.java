package com.example.signalproc.ui;

import com.example.signalproc.audio.AudioProcessor;
import com.example.signalproc.audio.AudioSignal;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;


public class SignalView extends LineChart<Number, Number> {

    private XYChart.Series<Number, Number> series = new XYChart.Series<>();

    NumberAxis yAxis = new NumberAxis();
    public SignalView(NumberAxis xAxis, NumberAxis yAxis) {
        super(xAxis, yAxis);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);
        this.setAnimated(false);
        this.getData().add(series);
    }
}
