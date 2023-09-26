package com.example.mybudget;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class StatisticiMembrii extends AppCompatActivity {

    private Spinner memberSpinner;
    private BarChart barChart;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayList<String> memberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistici_membrii);

        memberSpinner = findViewById(R.id.member_spinner);
        barChart = findViewById(R.id.barChart);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        memberList = new ArrayList<>();

        // Populați Spinner-ul cu membrii
        populateMemberSpinner();
    }

    private void populateMemberSpinner() {
        // Obțineți lista de membri din Firebase și populați Spinner-ul
        db.collection("Utilizatori")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String memberId = document.getString("uid");
                            String memberName = document.getString("password"); // sau orice altceva identifică membrul

                            if (memberId != null && memberName != null) {
                                memberList.add(memberId); // Adăugați ID-ul membrului în listă
                            }
                        }

                        // Adăugați membrii în Spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(StatisticiMembrii.this,
                                android.R.layout.simple_spinner_item, memberList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        memberSpinner.setAdapter(adapter);

                        // Adăugați un ascultător pentru selecția membrilor
                        memberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                                String selectedMemberId = (String) adapterView.getItemAtPosition(position);
                                loadChartData(selectedMemberId);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });
                    }
                });
    }

    private void loadChartData(String memberId) {
        // Obțineți datele de cheltuieli pentru membrul selectat din Firebase
        db.collection("Cheltuieli")
                .whereEqualTo("uid", memberId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<BarDataSet> dataSets = new ArrayList<>();
                        ArrayList<String> categories = new ArrayList<>();
                        HashMap<String, Double> categoryTotals = new HashMap<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Timestamp timestamp = document.getTimestamp("Data");
                            Date data = timestamp.toDate();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(data);

                            int documentMonth = calendar.get(Calendar.MONTH);
                            int documentYear = calendar.get(Calendar.YEAR);

                            String categoria = document.getString("Categorie");
                            String sumaCheltuialaString = document.getString("Suma");

                            if (categoria != null && sumaCheltuialaString != null) {
                                double sumaCheltuialaDocument = Double.parseDouble(sumaCheltuialaString);

                                // Adăugați categoria la lista de categorii (dacă nu există deja)
                                if (!categories.contains(categoria)) {
                                    categories.add(categoria);
                                }

                                // Adăugați suma la totalul categoriei corespunzătoare
                                if (categoryTotals.containsKey(categoria)) {
                                    categoryTotals.put(categoria, categoryTotals.get(categoria) + sumaCheltuialaDocument);
                                } else {
                                    categoryTotals.put(categoria, sumaCheltuialaDocument);
                                }
                            }
                        }

                        for (String categoria : categories) {
                            double sumaTotala = categoryTotals.get(categoria);

                            ArrayList<BarEntry> entries = new ArrayList<>();
                            entries.add(new BarEntry(categories.indexOf(categoria), (float) sumaTotala));
                            BarDataSet dataSet = new BarDataSet(entries, categoria);
                            dataSet.setColor(Color.parseColor(getRandomColor()));
                            dataSets.add(dataSet);
                        }

                        BarData data = new BarData();
                        for (BarDataSet dataSet : dataSets) {
                            data.addDataSet(dataSet);
                        }

                        barChart.setData(data);
                        barChart.getDescription().setEnabled(false);
                        barChart.invalidate();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StatisticiMembrii.this, "Eroare la obținerea datelor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getRandomColor() {
        // Generați o culoare aleatoare pentru fiecare categorie
        Random random = new Random();
        return String.format("#%06x", random.nextInt(16777215));
    }
}
