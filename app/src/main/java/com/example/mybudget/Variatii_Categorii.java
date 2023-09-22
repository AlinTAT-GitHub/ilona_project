package com.example.mybudget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

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
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Variatii_Categorii extends AppCompatActivity {

    private Spinner monthSpinner;
    private BarChart barChart;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_variatii_categorii);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserUid = mAuth.getCurrentUser().getUid();

        monthSpinner = findViewById(R.id.spinner);
        barChart = findViewById(R.id.barChart);

        createMonthSpinner();
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

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedMonth = (String) adapterView.getItemAtPosition(position);
                loadChartData(selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void loadChartData(String selectedMonth) {
        db.collection("Cheltuieli")
                .whereEqualTo("uid", currentUserUid)
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
                            boolean isOcazional = document.getBoolean("CheltuialaOcazionala");

                            Calendar selectedCalendar = Calendar.getInstance();
                            try {
                                selectedCalendar.setTime(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).parse(selectedMonth));
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                            int selectedMonth = selectedCalendar.get(Calendar.MONTH);
                            int selectedYear = selectedCalendar.get(Calendar.YEAR);

                            if ((isOcazional && documentMonth == selectedMonth && documentYear == selectedYear) || !isOcazional) {
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
                        Toast.makeText(Variatii_Categorii.this, "Eroare la obținerea datelor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("MyBudgetApp", "Eroare la obținerea datelor: " + e.getMessage());
                    }
                });
    }



    private String getRandomColor() {
        // Generați o culoare aleatoare pentru fiecare categorie
        Random random = new Random();
        return String.format("#%06x", random.nextInt(16777215));
    }
}
