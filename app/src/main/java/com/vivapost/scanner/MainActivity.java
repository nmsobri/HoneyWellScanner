package com.vivapost.scanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.InvalidScannerNameException;

public class MainActivity extends AppCompatActivity {
    private static BarcodeReader barcodeReader;
    private static AidcManager aidcManager;
    private Button btnScan;
    private static String LOG_TAG = "VIVAPOST";

    static BarcodeReader getBarcodeObject() {
        return barcodeReader;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AidcManager.create(this, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager manager) {
                aidcManager = manager;

                try {
                    barcodeReader = manager.createBarcodeReader();
                } catch ( InvalidScannerNameException e ) {
                    e.printStackTrace();
                }
            }
        });

        btnScan = findViewById(R.id.btnScan);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( barcodeReader != null ) {
            barcodeReader.close();
            barcodeReader = null;
        }

        if ( aidcManager != null ) {
            aidcManager.close();
            aidcManager = null;
        }
    }

}