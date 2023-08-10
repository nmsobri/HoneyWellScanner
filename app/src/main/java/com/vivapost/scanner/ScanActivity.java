package com.vivapost.scanner;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;

import java.util.List;
import java.util.Map;

public class ScanActivity extends AppCompatActivity implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener {
    private static BarcodeReader barcodeReader;
    private TextView textViewLabel;
    private TextView textViewValue;
    private static String LOG_TAG = "VIVAPOST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        textViewLabel = findViewById(R.id.scanLabel);
        textViewValue = findViewById(R.id.scanValue);
        barcodeReader = MainActivity.getBarcodeObject();

        if ( barcodeReader != null ) {
            barcodeReader.addBarcodeListener(this);

            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);
            } catch ( UnsupportedPropertyException e ) {
                Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
            }

            barcodeReader.addTriggerListener(this);

            String DEFAULT_PROFILE = "DEFAULT";
            List<String> profiles = this.barcodeReader.getProfileNames();
            Map<String, Object> barcodeReaderProperties = null;

            for ( String profile : profiles ) {
                if ( profile.contains(DEFAULT_PROFILE) && barcodeReader.loadProfile(profile) ) {
                    barcodeReaderProperties = barcodeReader.getAllProperties();

                    if ( barcodeReaderProperties != null && barcodeReaderProperties.size() > 0 ) {
                        barcodeReaderProperties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
                        barcodeReaderProperties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
                        barcodeReaderProperties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
                        barcodeReaderProperties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
                        barcodeReaderProperties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
                        barcodeReaderProperties.put("DPR_WEDGE", false);

                        barcodeReader.setProperties(barcodeReaderProperties);
                        Toast.makeText(this, "Use default scanning setting", Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        Toast.makeText(this, "Fail import scanner setting.Close App and start again", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }


    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewValue.setText(barcodeReadEvent.getBarcodeData());
            }
        });
    }


    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewValue.setText("No data decoded");
            }
        });
    }


    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) {
        try {
            barcodeReader.aim(triggerStateChangeEvent.getState());
            barcodeReader.light(triggerStateChangeEvent.getState());
            barcodeReader.decode(triggerStateChangeEvent.getState());
        } catch ( ScannerNotClaimedException e ) {
            Toast.makeText(this, "Scanner is not claimed", Toast.LENGTH_SHORT).show();
        } catch ( ScannerUnavailableException e ) {
            Toast.makeText(this, "Scanner is unavailable", Toast.LENGTH_SHORT).show();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if ( barcodeReader != null ) {
            try {
                // Claim it so we can use it
                barcodeReader.claim();
            } catch ( ScannerUnavailableException e ) {
                Toast.makeText(this, "Scanner is unavailable", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if ( barcodeReader != null ) {
            // Release the god damn scanner so we don't get any scanner notification
            // while we are idling..and let other app to use the scanner
            barcodeReader.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( barcodeReader != null ) {
            barcodeReader.removeBarcodeListener(this);
            barcodeReader.removeTriggerListener(this);
        }
    }
}