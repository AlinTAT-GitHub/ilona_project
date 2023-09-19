package com.example.mybudget;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Balanta extends AppCompatActivity {

    private Spinner monthSpinner;
    private EditText venitEditText;
    private EditText cheltuieliEditText;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserUid;

    private double venitFix = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balanta);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserUid = mAuth.getCurrentUser().getUid();

        monthSpinner = findViewById(R.id.monthSpinner);
        venitEditText = findViewById(R.id.editTextText);
        cheltuieliEditText = findViewById(R.id.editTextText3);

        createMonthSpinner();

        getVenitFix();

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedMonth = (String) adapterView.getItemAtPosition(position);
                Log.d("MyBudgetApp", "Luna selectată: " + selectedMonth);
                calculateBalanceForMonth(selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void createMonthSpinner() {
        List<String> months = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        for (int i = 0; i < 12; i++) {
            months.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.MONTH, -1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
    }

    private void getVenitFix() {
        db.collection("Venit_Fix")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Double venitFixDouble = documentSnapshot.getDouble("Suma venit");
                            if (venitFixDouble != null) {
                                venitFix = venitFixDouble;
                                Log.d("MyBudgetApp", "Venit fix obținut: " + venitFix);
                            }
                        } else {
                            // Documentul "Venit_Fix" nu există pentru utilizatorul curent.
                            Log.d("MyBudgetApp", "Documentul Venit_Fix nu există.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Balanta.this, "Eroare la obținerea venitului fix: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("MyBudgetApp", "Eroare la obținerea venitului fix: " + e.getMessage());
                    }
                });
    }

    private void calculateBalanceForMonth(final String selectedMonth) {
        venitEditText.setText("");
        cheltuieliEditText.setText("");

        Log.d("MyBudgetApp", "Calculare pentru luna selectată: " + selectedMonth);

        // Calcularea veniturilor
        db.collection("Venit")
                .document(currentUserUid)
                .collection(selectedMonth)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        double venitLunar = 0.0;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Obțineți data din document
                            Timestamp data = document.getTimestamp("Data");
                            Log.d("MyBudgetApp", "Data venitului: " + data.toDate());

                            // Obțineți numele venitului din document
                            String numeVenit = document.getString("Nume venit");
                            Log.d("MyBudgetApp", "Nume venit: " + numeVenit);

                            // Obțineți suma venitului din document
                            String sumaVenitString = document.getString("Suma venit");
                            Log.d("MyBudgetApp", "Suma venit: " + sumaVenitString);

                            // Verificăm dacă data din document corespunde lunii selectate
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(data.toDate());
                            int documentMonth = calendar.get(Calendar.MONTH);
                            int selectedMonthIndex = monthSpinner.getSelectedItemPosition();
                            if (documentMonth == (11 - selectedMonthIndex)) {
                                if (sumaVenitString != null) {
                                    try {
                                        double venit = Double.parseDouble(sumaVenitString);
                                        venitLunar += venit;
                                    } catch (NumberFormatException e) {
                                        Log.e("MyBudgetApp", "Eroare la conversia sumei venitului: " + e.getMessage());
                                    }
                                }
                            }
                        }

                        venitLunar += venitFix;

                        venitEditText.setText(String.valueOf(venitLunar));
                        Log.d("MyBudgetApp", "Venit lunar calculat: " + venitLunar);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Balanta.this, "Eroare la obținerea veniturilor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("MyBudgetApp", "Eroare la obținerea veniturilor: " + e.getMessage());
                    }
                });

        // Calcularea cheltuielilor
        db.collection("Cheltuieli")
                .document(currentUserUid)
                .collection(selectedMonth)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        double cheltuieliLunare = 0.0;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Obțineți data din document
                            Timestamp data = document.getTimestamp("Data");
                            Log.d("MyBudgetApp", "Data cheltuielii: " + data.toDate());

                            // Obțineți numele cheltuielii din document
                            String numeCheltuiala = document.getString("Nume cheltuiala");
                            Log.d("MyBudgetApp", "Nume cheltuiala: " + numeCheltuiala);

                            // Obțineți suma cheltuielii din document
                            String sumaCheltuialaString = document.getString("Suma");
                            Log.d("MyBudgetApp", "Suma cheltuielă: " + sumaCheltuialaString);

                            // Verificăm dacă data din document corespunde lunii selectate
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(data.toDate());
                            int documentMonth = calendar.get(Calendar.MONTH);
                            int selectedMonthIndex = monthSpinner.getSelectedItemPosition();
                            if (documentMonth == (11 - selectedMonthIndex)) {
                                if (sumaCheltuialaString != null) {
                                    try {
                                        double cheltuiala = Double.parseDouble(sumaCheltuialaString);
                                        cheltuieliLunare += cheltuiala;
                                    } catch (NumberFormatException e) {
                                        Log.e("MyBudgetApp", "Eroare la conversia sumei cheltuielilor: " + e.getMessage());
                                    }
                                }
                            }
                        }

                        cheltuieliEditText.setText(String.valueOf(cheltuieliLunare));
                        Log.d("MyBudgetApp", "Cheltuieli lunare calculate: " + cheltuieliLunare);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Balanta.this, "Eroare la obținerea cheltuielilor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("MyBudgetApp", "Eroare la obținerea cheltuielilor: " + e.getMessage());
                    }
                });
    }
}
