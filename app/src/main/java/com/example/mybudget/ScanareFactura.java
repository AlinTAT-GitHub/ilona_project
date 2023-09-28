package com.example.mybudget;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;
import java.util.Map;

public class ScanareFactura extends AppCompatActivity {

    private static final String TAG = "ScanareFactura"; // Adăugat un tag pentru log-uri
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Definește un identificator de utilizator (uid)
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    Button btn_scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanare_factura);
        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(v -> {
            scanCode();
        });
        Log.d(TAG, "onCreate called"); // Adăugat un log pentru onCreate
    }

    private void scanCode() {
        try {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Buton volum sus pentru lanterna");
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
                // Extragerea sumei din codul de bare
                String codBare = result.getContents();
                String sumaPlata = extrageSumaPlata(codBare);

                // Construirea mesajului pentru alerta
                String message = "Cod de bare: " + codBare + "\nSuma: " + sumaPlata;

                // Crearea alertei
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanareFactura.this);
                builder.setTitle("Rezultat scanare");
                builder.setMessage(message);

                // Adaugarea butonului "Adaugă la cheltuieli"
                builder.setPositiveButton("Adaugă la cheltuieli", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        // Adăugarea datelor în Firestore
                        Map<String, Object> expenseData = new HashMap<>();
                        expenseData.put("Categorie", "Facturi");
                        expenseData.put("Suma", sumaPlata);
                        expenseData.put("CheltuialaOcazionala", false); // Presupunem că nu este o cheltuială ocazională
                        expenseData.put("uid", uid);

                        // Adăugarea câmpului "Data" cu data curentă în obiectul Map
                        expenseData.put("Data", FieldValue.serverTimestamp());

                        // Salvarea datelor în Firestore
                        db.collection("Cheltuieli")
                                .add(expenseData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // Succes: Datele au fost adăugate cu succes
                                        Log.d(TAG, "Datele au fost adăugate cu succes. Document ID: " + documentReference.getId());
                                        // Aici poți adăuga orice acțiuni suplimentare după adăugarea datelor.
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Eșec: Datele nu au putut fi adăugate
                                        Log.e(TAG, "Eroare la adăugarea datelor: " + e.getMessage(), e);
                                        // Aici poți afișa un mesaj de eroare sau alte acțiuni corespunzătoare.
                                    }
                                });
                    }
                });

                // Adaugarea butonului "Anulează"
                builder.setNegativeButton("Anulează", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        // Înapoi la activitatea principală sau altă acțiune corespunzătoare.
                    }
                });

                // Afișarea alertei
                builder.show();
            }
            Log.d(TAG, "barLaucher called");
        } catch (Exception e) {
            Log.e(TAG, "Error in barLaucher: " + e.getMessage(), e);
            // Aici poți afișa un mesaj de eroare sau alte acțiuni corespunzătoare.
        }
    });


    private String extrageSumaPlata(String codBare) {
        // Verificăm dacă codul de bare are cel puțin două cifre la sfârșit
        if (codBare.length() >= 2) {
            // Extragem ultimele două cifre din codul de bare
            String ultimeCifre = codBare.substring(codBare.length() - 2);

            // Verificăm dacă codul de bare are cel puțin trei cifre la sfârșit pentru zecimale
            if (codBare.length() >= 3) {
                // Extragem ultimele trei cifre din codul de bare (două zecimale și ultima cifră)
                ultimeCifre = codBare.substring(codBare.length() - 3);
            }

            // Transformăm cifrele în formatul sumei (de exemplu, 93437 devine 934.37)
            String sumaPlata = ultimeCifre.substring(0, ultimeCifre.length() - 2) + "." + ultimeCifre.substring(ultimeCifre.length() - 2);

            return sumaPlata;
        } else {
            // Codul de bare nu are suficiente cifre pentru a reprezenta suma
            return "Suma indisponibilă";
        }
    }

}
