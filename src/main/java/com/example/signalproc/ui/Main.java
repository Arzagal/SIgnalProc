package com.example.signalproc.ui;

import com.example.signalproc.audio.AudioIO;
import com.example.signalproc.audio.AudioProcessor;
import com.example.signalproc.audio.AudioSignal;
import com.example.signalproc.audio.Complex;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.chart.XYChart;


import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.awt.event.ActionEvent;
import java.beans.EventHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.animation.AnimationTimer;

import java.util.concurrent.ConcurrentLinkedQueue;


public class Main extends Application {
    private boolean isInputOn;
    private boolean isOutputOn;
    private boolean isRunning;
    private String selectedMixerIn = "Pilote de capture audio principal";
    private String selectedMixerOut = "Haut-parleurs (Realtek(R) Audio)";
    private static final int MAX_DATA_POINTS = 2048;

    private int xSeriesDataIn = 0;
    private int xSeriesDataOut = 0;
    private int yCount =0;
    private ExecutorService executor;

    private XYChart.Series<Number, Number> seriesIn = new XYChart.Series<>();

    private XYChart.Series<Number, Number> seriesOut = new XYChart.Series<>();

    private NumberAxis xAxis1;
    private NumberAxis xAxis2;
    private ConcurrentLinkedQueue<Number> dataQ1 = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Number> dataQ2 = new ConcurrentLinkedQueue<>();

    private AudioProcessor AP = startAudioProc(selectedMixerIn, selectedMixerOut);

    public Main() throws LineUnavailableException {
    }

    public void start(Stage primaryStage) {
        try {
            BorderPane root = new BorderPane();
            root.setTop(createToolbar(AP,root));
            root.setBottom(createStatusbar());
            executor = Executors.newCachedThreadPool(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    return thread;
                }
            });

            AddToQueue addToQueue = new AddToQueue();
            executor.execute(addToQueue);
            //-- Prepare Timeline
            prepareTimeline();
            Scene scene = new Scene(root, 1500, 800);
            primaryStage.setScene(scene);
            primaryStage.setTitle("The JavaFX audio processor");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Node createToolbar(AudioProcessor AP,BorderPane root) {

        Button buttonIn = new Button("Active input");
        Button buttonOut = new Button("Active output");
        Button buttonStart = new Button("Start");
        Button buttonStop = new Button("Stop");
        Button buttonFFT = new Button("Active FFT");
        ToolBar tb = new ToolBar(new Label("Activation des voies (choisir le mixer avant)"),buttonIn,buttonOut,new Separator(),
                new Label("choix du Mixer"), new Separator(), buttonStart, buttonStop, new Separator(),
                buttonFFT
        );
        buttonStart.setOnAction(event -> isRunning=true);
        buttonStop.setOnAction(event -> isRunning=false);
        buttonIn.setOnAction(event -> {
            try {
                root.setLeft(createMainContent(AP));
                isInputOn = true;
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        });
        buttonOut.setOnAction(event -> {
            try {
                root.setRight(createMainContent2(AP));
                isOutputOn = true;
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        });
        buttonFFT.setOnAction(event -> {
                root.setCenter(createFFT(AP));
                //isOutputOn = true;
        });

        ComboBox<String> cb = new ComboBox(FXCollections.observableArrayList(AudioIO.getAudioMixersIN()));
        cb.setOnAction(eventcb -> selectedMixerIn=cb.getValue());
        tb.getItems().add(cb);
        return tb;
    }

    private Node createStatusbar() {
        HBox statusbar = new HBox();
        statusbar.getChildren().addAll(new Label("Name:"), new TextField(" "));
        return statusbar;
    }

    private Node createMainContent(AudioProcessor AP) throws LineUnavailableException {
        Group g = new Group();
        // ici en utilisant g.getChildren().add(...) vous pouvez ajouter tout  ́el ́ement graphique souhait ́e de type Node
        xAxis1 = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis1.setForceZeroInRange(false);
        xAxis1.setAutoRanging(false);
        xAxis1.setTickLabelsVisible(false);
        xAxis1.setTickMarkVisible(false);
        xAxis1.setMinorTickVisible(false);
        NumberAxis yAxis = new NumberAxis();
        SignalView sV = new SignalView(xAxis1,yAxis);
        sV.setTitle("Entry signal");

        seriesIn.setName("Signal");
        AudioSignal entsig = AP.getInputSignal();
        double[] sig = entsig.getSampleBuffer();
        for (int i = 0; i < MAX_DATA_POINTS; i++) {
            seriesIn.getData().add(new XYChart.Data(i, sig[i]));
        }
        sV.getData().add(seriesIn);
        g.getChildren().add(sV);
        return g;
    }
    private Node createMainContent2(AudioProcessor AP) throws LineUnavailableException {
        Group g = new Group();
        // ici en utilisant g.getChildren().add(...) vous pouvez ajouter tout  ́el ́ement graphique souhait ́e de type Node
        xAxis2 = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis2.setForceZeroInRange(false);
        xAxis2.setAutoRanging(false);
        xAxis2.setTickLabelsVisible(false);
        xAxis2.setTickMarkVisible(false);
        xAxis2.setMinorTickVisible(false);
        NumberAxis yAxis = new NumberAxis();
        SignalView sV = new SignalView(xAxis1,yAxis);
        sV.setTitle("Exit signal");
        sV.setAnimated(false);
        sV.getData().add(seriesOut);
        g.getChildren().add(sV);
        seriesOut.setName("Signal");
        AudioSignal entsig = AP.getOutputSignal();
        double[] sig = entsig.getSampleBuffer();
        for (int i = 0; i < sig.length; i++) {
            seriesOut.getData().add(new XYChart.Data(i, sig[i]));
        }
        return g;
    }
    private Node createFFT(AudioProcessor AP){
        Group g = new Group();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        final LineChart<Number,Number> lineChart =
                new LineChart<Number,Number>(xAxis,yAxis);
        XYChart.Series series = new XYChart.Series();
        Complex[] sigfft = AP.getInputSignal().computeFFT();
        for(int i =0;i< sigfft.length;i++){
            series.getData().add(new XYChart.Data(i,sigfft[i].abs()));
        }
        lineChart.setTitle("FFT");
        lineChart.getData().add(series);
        g.getChildren().add(lineChart);
        return g;
    }
    private AudioProcessor startAudioProc(String selectedMixerIn, String selectedMixerOut) throws LineUnavailableException {
        TargetDataLine inLine = AudioIO.obtainAudioInput(selectedMixerIn, 16000);
        SourceDataLine outLine = AudioIO.obtainAudioOutput(selectedMixerOut, 16000);
        AudioProcessor as = new AudioProcessor(inLine, outLine, 2048);
        inLine.open();
        inLine.start();
        outLine.open();
        outLine.start();
        new Thread(as).start();
        return as;
    }

    private class AddToQueue implements Runnable {
        public void run() {
            try {
                double[] sigIn = AP.getInputSignal().getSampleBuffer();
                double[] sigOut = AP.getOutputSignal().getSampleBuffer();
                dataQ1.add(sigIn[yCount]);
                dataQ2.add(sigOut[yCount]);
                yCount++;
                if(yCount==sigIn.length){
                    AP.updateInput();
                    yCount=0;
                }


                Thread.sleep((long) 0.0125);
                executor.execute(this);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void prepareTimeline() {
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                addDataToSeries();
            }
        }.start();
    }

    private void addDataToSeries() {
        for (int i = 0; i < 100; i++) { //-- add 20 numbers to the plot+
            if (dataQ1.isEmpty()  || isRunning==false) break;
            seriesIn.getData().add(new XYChart.Data<>(xSeriesDataIn++, dataQ1.remove()));
            seriesOut.getData().add(new XYChart.Data<>(xSeriesDataOut++, dataQ2.remove()));
    }
        // remove points to keep us at no more than MAX_DATA_POINTS
        if (seriesIn.getData().size() > MAX_DATA_POINTS) {
            seriesIn.getData().remove(0, seriesIn.getData().size() - MAX_DATA_POINTS);
        }
        if (seriesOut.getData().size() > MAX_DATA_POINTS) {
            seriesOut.getData().remove(0, seriesOut.getData().size() - MAX_DATA_POINTS);
        }
        // update
        if(isInputOn==true) {
            xAxis1.setLowerBound(xSeriesDataIn - MAX_DATA_POINTS);
            xAxis1.setUpperBound(xSeriesDataIn - 1);
        }
        if(isOutputOn==true) {
            xAxis2.setLowerBound(xSeriesDataOut - MAX_DATA_POINTS);
            xAxis2.setUpperBound(xSeriesDataOut - 1);
        }
    }
}