package com.example.mybudget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class IncomeActivity extends AppCompatActivity {

    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        container = findViewById(R.id.container);

        // Adăugarea unui TextView pentru titlu
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Ultimele venituri actuale:");
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20); // Dimensiune mare a textului
        titleTextView.setTypeface(null, Typeface.BOLD); // Text bold
        titleTextView.setTextColor(Color.BLACK); // Culoare text negru
        titleTextView.setPadding(8, 8, 8, 16); // Padding

        container.addView(titleTextView);

        // Obțineți datele din Firestore și afișați-le în TextView-uri
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Venit")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String numeVenit = document.getString("Nume venit");
                            String sumaVenit = document.getString("Suma venit");
                            String detalii = document.getString("Detalii");

                            // Crearea unui TextView pentru fiecare înregistrare și adăugarea acestuia în LinearLayout
                            TextView textView = new TextView(IncomeActivity.this);
                            textView.setText("Nume venit: " + numeVenit + "\nSuma venit: " + sumaVenit + "\nDetalii: " + detalii);
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // Setarea dimensiunii textului în sp
                            textView.setTextColor(Color.BLACK); // Setarea culorii textului
                            textView.setPadding(8, 8, 8, 8); // Setarea padding-ului

                            // Adăugarea TextView în LinearLayout
                            container.addView(textView);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(IncomeActivity.this,"Lista este goala ! : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

