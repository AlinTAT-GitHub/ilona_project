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

    private double venitFixTotal = 0.0;
    private double cheltuieliFixeTotal = 0.0;
    private double cheltuieliOcazionaleTotal = 0.0;

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

        // Obțineți venitul fix total
        getVenitFixTotal();
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

    private void getVenitFixTotal() {
        db.collection("Venit_Fix")
                .whereEqualTo("uid", currentUserUid) // Filtrați după utilizator
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        venitFixTotal = 0.0; // Resetați suma venitului fix total

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                String sumaVenitString = document.getString("Suma venit");
                                if (sumaVenitString != null) {
                                    double venitFixDocument = Double.parseDouble(sumaVenitString);
                                    venitFixTotal += venitFixDocument;
                                }
                            } catch (Exception e) {
                                Log.e("MyBudgetApp", "Eroare la procesarea venitului fix: " + e.getMessage());
                            }
                        }
                        Log.d("MyBudgetApp", "Venit fix total obținut: " + venitFixTotal);
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
                .whereEqualTo("uid", currentUserUid) // Filtrați după utilizator
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        double venitLunar = 0.0;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                Timestamp timestamp = document.getTimestamp("Data");
                                Date data = timestamp.toDate();
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(data);

                                int documentMonth = calendar.get(Calendar.MONTH);
                                int documentYear = calendar.get(Calendar.YEAR);

                                Calendar selectedCalendar = Calendar.getInstance();
                                selectedCalendar.setTime(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).parse(selectedMonth));
                                int selectedMonth = selectedCalendar.get(Calendar.MONTH);
                                int selectedYear = selectedCalendar.get(Calendar.YEAR);

                                if (documentMonth == selectedMonth && documentYear == selectedYear) {
                                    String sumaVenitString = document.getString("Suma venit");
                                    if (sumaVenitString != null) {
                                        double venitDocument = Double.parseDouble(sumaVenitString);
                                        venitLunar += venitDocument;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("MyBudgetApp", "Eroare la procesarea venitului: " + e.getMessage());
                            }
                        }

                        venitLunar += venitFixTotal;

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

        // Calcularea cheltuielilor fixe (pe toate lunile)
        db.collection("Cheltuieli")
                .whereEqualTo("uid", currentUserUid) // Filtrați după utilizator
                .whereEqualTo("CheltuialaOcazionala", false) // Adăugați filtrul pentru cheltuielile fixe
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        cheltuieliFixeTotal = 0.0;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                String sumaCheltuialaString = document.getString("Suma");
                                if (sumaCheltuialaString != null) {
                                    double cheltuialaFixaDocument = Double.parseDouble(sumaCheltuialaString);
                                    cheltuieliFixeTotal += cheltuialaFixaDocument;
                                }
                            } catch (Exception e) {
                                Log.e("MyBudgetApp", "Eroare la procesarea cheltuielii fixe: " + e.getMessage());
                            }
                        }

                        cheltuieliEditText.setText(String.valueOf(cheltuieliFixeTotal));
                        Log.d("MyBudgetApp", "Cheltuieli fixe calculate: " + cheltuieliFixeTotal);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Balanta.this, "Eroare la obținerea cheltuielilor fixe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("MyBudgetApp", "Eroare la obținerea cheltuielilor fixe: " + e.getMessage());
                    }
                });

        // Calcularea cheltuielilor ocazionale (doar pentru luna selectată)
        db.collection("Cheltuieli")
                .whereEqualTo("uid", currentUserUid) // Filtrați după utilizator
                .whereEqualTo("CheltuialaOcazionala", true) // Adăugați filtrul pentru cheltuielile ocazionale
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        cheltuieliOcazionaleTotal = 0.0;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                Timestamp timestamp = document.getTimestamp("Data");
                                Date data = timestamp.toDate();
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(data);

                                int documentMonth = calendar.get(Calendar.MONTH);
                                int documentYear = calendar.get(Calendar.YEAR);

                                Calendar selectedCalendar = Calendar.getInstance();
                                selectedCalendar.setTime(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).parse(selectedMonth));
                                int selectedMonth = selectedCalendar.get(Calendar.MONTH);
                                int selectedYear = selectedCalendar.get(Calendar.YEAR);

                                if (documentMonth == selectedMonth && documentYear == selectedYear) {
                                    String sumaCheltuialaString = document.getString("Suma");
                                    if (sumaCheltuialaString != null) {
                                        double cheltuialaOcazionalaDocument = Double.parseDouble(sumaCheltuialaString);
                                        cheltuieliOcazionaleTotal += cheltuialaOcazionalaDocument;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("MyBudgetApp", "Eroare la procesarea cheltuielii ocazionale: " + e.getMessage());
                            }
                        }

                        cheltuieliEditText.setText(String.valueOf(cheltuieliFixeTotal + cheltuieliOcazionaleTotal));
                        Log.d("MyBudgetApp", "Cheltuieli ocazionale calculate: " + cheltuieliOcazionaleTotal);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Balanta.this, "Eroare la obținerea cheltuielilor ocazionale: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("MyBudgetApp", "Eroare la obținerea cheltuielilor ocazionale: " + e.getMessage());
                    }
                });
    }


    private String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
}
