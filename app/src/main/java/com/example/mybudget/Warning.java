package com.example.mybudget;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Warning extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView warningText;
    private static final String TAG = "WarningActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        warningText = findViewById(R.id.warning_text);

        String uid = mAuth.getCurrentUser().getUid();

        Log.d(TAG, "Loading categories and checking limits for the current month...");

        try {
            checkCategoryLimitsForCurrentMonth(uid);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }

    private void checkCategoryLimitsForCurrentMonth(String uid) {
        // Obține luna curentă
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1;

        try {
            db.collection("Categorii")
                    .whereEqualTo("uid", uid)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                String category = document.getString("Nume categorie");
                                String categoryLimitString = document.getString("Suma limita");

                                // Verificați dacă Suma limita nu este nulă și nu este un șir gol
                                if (categoryLimitString != null && !categoryLimitString.isEmpty()) {
                                    double categoryLimit = Double.parseDouble(categoryLimitString);

                                    db.collection("Cheltuieli")
                                            .whereEqualTo("uid", uid)
                                            .whereEqualTo("Categorie", category)
                                            .whereGreaterThanOrEqualTo("Data", getStartOfMonth(currentMonth))
                                            .whereLessThanOrEqualTo("Data", getEndOfMonth(currentMonth))
                                            .get()
                                            .addOnSuccessListener(cheltuieliQueryDocumentSnapshots -> {
                                                double totalExpense = 0;
                                                Map<String, Integer> expensesMap = new HashMap<>(); // Aici vom stoca sumele cheltuite pentru fiecare categorie

                                                for (QueryDocumentSnapshot cheltuiala : cheltuieliQueryDocumentSnapshots) {
                                                    String sumaCheltuialaString = cheltuiala.getString("Suma");

                                                    // Verificați dacă Suma nu este nulă și nu este un șir gol
                                                    if (sumaCheltuialaString != null && !sumaCheltuialaString.isEmpty()) {
                                                        double sumaCheltuiala = Double.parseDouble(sumaCheltuialaString);
                                                        totalExpense += sumaCheltuiala;

                                                        // Verificați dacă această sumă și categorie au fost deja înregistrate
                                                        String key = category + "-" + sumaCheltuialaString;
                                                        if (expensesMap.containsKey(key)) {
                                                            // Afișați un mesaj de avertizare
                                                            String warningMessage = "Același total cheltuit pentru categoria " + category + " și suma " + sumaCheltuialaString + "\n";
                                                            appendToWarningText(warningMessage);
                                                            sendNotification("Avertizare buget", "Același total cheltuit pentru categoria " + category);
                                                        } else {
                                                            // Altfel, adăugați această sumă și categorie în map
                                                            expensesMap.put(key, 1);
                                                        }
                                                    }
                                                }

                                                if (totalExpense > categoryLimit) {
                                                    String warningMessage = "Depășire limită pentru categoria: " + category + "\n";
                                                    appendToWarningText(warningMessage);
                                                    sendNotification("Avertizare buget", "Depășire limită pentru categoria: " + category);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error getting expenses for category: " + category, e);
                                            });
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing category: " + document.getId(), e);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting categories: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in checkCategoryLimitsForCurrentMonth: " + e.getMessage(), e);
        }
    }

    private long getStartOfMonth(int month) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.MONTH, month - 1);
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfMonth(int month) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.MONTH, month - 1);
        calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calendar.set(java.util.Calendar.MINUTE, 59);
        calendar.set(java.util.Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }

    private void appendToWarningText(String message) {
        String currentText = warningText.getText().toString();
        currentText += message;
        warningText.setText(currentText);

    }

    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Iconița notificării (poate fi schimbată)
                .setContentTitle(title) // Titlul notificării
                .setContentText(message) // Mesajul notificării
                .setAutoCancel(true); // Faceți notificarea anulabilă atunci când este atinsă

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(0, builder.build()); // 0 este un ID unic pentru notificare
        }
    }
}
