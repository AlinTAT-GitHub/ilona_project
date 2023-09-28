package com.example.mybudget;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Venit_PDF extends AppCompatActivity {

    private Button extractPDFBtn;
    private static final int PICK_PDF_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venit_pdf);

        extractPDFBtn = findViewById(R.id.idBtnExtract);

        extractPDFBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAndExtractPDF();
            }
        });
    }

    private void selectAndExtractPDF() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri pdfUri = data.getData();

            if (pdfUri != null) {
                try {
                    PdfReader reader = new PdfReader(getContentResolver().openInputStream(pdfUri));
                    int n = reader.getNumberOfPages();

                    List<String> extractedTextLines = new ArrayList<>();

                    for (int i = 0; i < n; i++) {
                        String extractedText = PdfTextExtractor.getTextFromPage(reader, i + 1).trim();
                        String[] lines = extractedText.split("\n");
                        for (String line : lines) {
                            extractedTextLines.add(line);
                        }
                    }

                    reader.close();

                    List<String> extractedSums = new ArrayList<>();
                    Pattern pattern = Pattern.compile("(\\d+\\,\\d+) (\\d+\\,\\d+)");
                    for (String line : extractedTextLines) {
                        Matcher matcher = pattern.matcher(line);
                        while (matcher.find()) {
                            String suma1 = matcher.group(1);
                            String suma2 = matcher.group(2);

                            if (!suma2.equals("0,00")) {
                                suma2 = suma2.replace(",", "."); // înlocuim virgula cu punct
                                extractedSums.add(suma2);
                            }
                        }
                    }

                    // Afișăm sumele găsite în dialoguri separate și le adăugăm în Firestore la confirmare.
                    for (String suma : extractedSums) {
                        showSumDialog(suma);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Eroare la citirea PDF-ului", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showSumDialog(final String suma) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Suma venit");
        builder.setMessage(suma);

        builder.setPositiveButton("Adaugă la venituri", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addSumToFirestore(suma);
            }
        });

        builder.setNegativeButton("Anulează", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void addSumToFirestore(String suma) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> sumMap = new HashMap<>();
            sumMap.put("Nume venit", "Extras Bancar");
            sumMap.put("Suma venit", suma);
            sumMap.put("Data", FieldValue.serverTimestamp());
            sumMap.put("uid", uid);

            db.collection("Venit").add(sumMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Suma a fost adăugată cu succes în Firestore.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Eroare la adăugarea sumei în Firestore.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Utilizatorul nu este autentificat.", Toast.LENGTH_SHORT).show();
        }
    }
}
