package com.lsr.sensordatacollection.sensorcharthandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class WiFiChartHandler extends BroadcastReceiver {

    WifiManager wifiManager;
    Context mainAppContext;
    TableLayout scanTable;

    BufferedWriter readingsLog;
    IntentFilter scanResultsIntentFilter;

    public WiFiChartHandler(Context appContext, TableLayout scanTable, BufferedWriter logWriter) {

        mainAppContext = appContext;
        readingsLog = logWriter;
        this.scanTable = scanTable;

        wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        scanResultsIntentFilter = new IntentFilter();
        scanResultsIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

    }

    public void registerScanListener() {
        if (mainAppContext != null) {
            mainAppContext.registerReceiver(this, scanResultsIntentFilter);
        }

        wifiManager.startScan();
    }

    public void unregisterScanListener() {
        if (mainAppContext != null) {
            mainAppContext.unregisterReceiver(this);
        }
    }

    private TableRow createTableHeader() {

        TableRow scanTableHeader = new TableRow(mainAppContext);
        scanTableHeader.setBackgroundColor(Color.parseColor("#0079D6"));

        // Create header text views
        TextView idView = new TextView(mainAppContext);
        idView.setText("ID");

        TextView rssView = new TextView(mainAppContext);
        rssView.setText("RSS [dBm]");

        TextView channelView = new TextView(mainAppContext);
        channelView.setText("Channel");

        // Add TextViews to header
        scanTableHeader.addView(idView);
        scanTableHeader.addView(rssView);
        scanTableHeader.addView(channelView);

        return scanTableHeader;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
        if (success) {

            // Remove old table data
            scanTable.removeAllViews();

            // Add header row to table
            scanTable.addView(createTableHeader());

            // Create scan rows
            List<ScanResult> results = wifiManager.getScanResults();
            for (ScanResult scan: results) {
                TableRow scanRow = new TableRow(mainAppContext);
                scanRow.setBackgroundColor(Color.parseColor("#DAE8FC"));

                TextView idView = new TextView(mainAppContext);
                TextView rssView = new TextView(mainAppContext);
                TextView channelView = new TextView(mainAppContext);

                idView.setText(scan.BSSID);
                rssView.setText(String.valueOf(scan.level));
                channelView.setText(String.valueOf(getChannel(scan.frequency)));

                scanRow.addView(idView);
                scanRow.addView(rssView);
                scanRow.addView(channelView);

                // Don't forget to add the views to the table
                scanTable.addView(scanRow);
            }

            // Start a new scan for future results
            wifiManager.startScan();

            try {
                sensorDataToXMLEntry(results);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sensorDataToXMLEntry(List<ScanResult> scanResults) throws IOException {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:MM");
        String timestamp = sdf.format(System.currentTimeMillis());

        readingsLog.write("<wr st=\"" + timestamp + "\" >\n");
        for (ScanResult result : scanResults) {
            readingsLog.write("<r b=\"" + result.BSSID + "\" s=\"" + String.valueOf(result.level) +
                    "\" c=\"" + String.valueOf(getChannel(result.frequency)) + "\" />\n");
        }
        readingsLog.write("</wr>\n");
    }

    private int getChannel(int freq) {

        if (freq == 2412) return 1;
        if (freq == 2437) return 6;
        if (freq == 2462) return 11;

        if (freq == 2417) return 2;
        if (freq == 2422) return 3;
        if (freq == 2427) return 4;
        if (freq == 2432) return 5;

        if (freq == 2442) return 7;
        if (freq == 2447) return 8;
        if (freq == 2452) return 9;
        if (freq == 2457) return 10;

        if (freq == 2467) return 12;
        if (freq == 2472) return 13;
        if (freq == 2484) return 14;

        if (freq == 4915) return 183;
        if (freq == 4920) return 184;
        if (freq == 4925) return 185;
        if (freq == 4935) return 187;
        if (freq == 4940) return 188;
        if (freq == 4945) return 189;
        if (freq == 4960) return 192;
        if (freq == 4980) return 196;
        if (freq == 5035) return -7;
        if (freq == 5040) return -8;
        if (freq == 5045) return -9;
        if (freq == 5055) return -11;
        if (freq == 5060) return -12;
        if (freq == 5080) return 16;
        if (freq == 5170) return 34;
        if (freq == 5180) return 36;
        if (freq == 5190) return 38;
        if (freq == 5200) return 40;
        if (freq == 5210) return 42;
        if (freq == 5220) return 44;
        if (freq == 5230) return 46;
        if (freq == 5240) return 48;
        if (freq == 5260) return 52;
        if (freq == 5280) return 56;
        if (freq == 5300) return 60;
        if (freq == 5320) return 64;
        if (freq == 5500) return 100;
        if (freq == 5520) return 104;
        if (freq == 5540) return 108;
        if (freq == 5560) return 112;
        if (freq == 5580) return 116;
        if (freq == 5600) return 120;
        if (freq == 5620) return 124;
        if (freq == 5640) return 128;
        if (freq == 5660) return 132;
        if (freq == 5680) return 136;
        if (freq == 5700) return 140;
        if (freq == 5745) return 149;
        if (freq == 5765) return 153;
        if (freq == 5785) return 157;
        if (freq == 5805) return 161;
        if (freq == 5825) return 165;
        return -99;
    }
}
