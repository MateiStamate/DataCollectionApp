package com.lsr.sensordatacollection.sensorcharthandler;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


import java.io.BufferedWriter;
import java.io.IOException;

public abstract class SensorChartHandler {

    private BufferedWriter buffWrite;

    public SensorChartHandler(BufferedWriter logWriter) {
        this.buffWrite = logWriter;
    }

    public BufferedWriter getBuffWrite() {
        return buffWrite;
    }

    public abstract void sensorDataToXMLEntry(SensorEvent event) throws IOException;
}
