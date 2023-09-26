package com.example.mybudget;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log; // Adăugat importul pentru Log
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ScanareFactura extends AppCompatActivity {

    private static final String TAG = "ScanareFactura"; // Adăugat un tag pentru log-uri

    Button btn_scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanare_factura);
        btn_scan=findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(v->{
            scanCode();
        });
        Log.d(TAG, "onCreate called"); // Adăugat un log pentru onCreate
    }

    private void scanCode() {
        try {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Volume up to flash on");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureAct.class);
            barLaucher.launch(options);
            Log.d(TAG, "scanCode called");
        } catch (Exception e) {
            Log.e(TAG, "Error in scanCode: " + e.getMessage(), e);
            // Aici puteți afișa un mesaj de eroare sau alte acțiuni corespunzătoare
        }
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result -> {
        try {
            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanareFactura.this);
                builder.setTitle("Result");
                builder.setMessage(result.getContents());
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
            Log.d(TAG, "barLaucher called");
        } catch (Exception e) {
            Log.e(TAG, "Error in barLaucher: " + e.getMessage(), e);
            // Aici puteți afișa un mesaj de eroare sau alte acțiuni corespunzătoare
        }
    });

}
