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
        titleTextView.setText("Venituri fixe:");
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20); // Dimensiune mare a textului
        titleTextView.setTypeface(null, Typeface.BOLD); // Text bold
        titleTextView.setTextColor(Color.BLACK); // Culoare text negru
        titleTextView.setPadding(8, 8, 8, 16); // Padding

        container.addView(titleTextView);

        // Obțineți datele din Firestore pentru Venit_Fix și afișați-le în TextView-uri
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Venit_Fix")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String numeVenit = document.getString("Nume venit");
                            String sumaVenit = document.getString("Suma venit");
                            String detalii = document.getString("Detalii");

                            // Crearea unui TextView pentru fiecare înregistrare și aplicarea stilurilor
                            TextView textView = new TextView(IncomeActivity.this);

                            // Aplicarea stilurilor direct în cod
                            textView.setText("Nume venit (fix): " + numeVenit + "\nSuma venit: " + sumaVenit + "\nDetalii: " + detalii);
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // Setarea dimensiunii textului în sp
                            textView.setTextColor(Color.BLACK); // Setarea culorii textului la negru
                            textView.setPadding(8, 8, 8, 8); // Setarea padding-ului

                            // Adăugarea TextView în LinearLayout
                            container.addView(textView);
                        }

                        // După ce ați afișat veniturile fixe, afișați veniturile normale
                        displayNormalIncome();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Tratarea erorilor la efectuarea interogării
                        Toast.makeText(IncomeActivity.this,"Lista de venit fix este goala ! : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Metodă pentru afișarea veniturilor normale
    private void displayNormalIncome() {
        // Obțineți datele din Firestore pentru Venit și afișați-le în TextView-uri
        container = findViewById(R.id.container);

        // Adăugarea unui TextView pentru titlu
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Ultimele venituri :");
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20); // Dimensiune mare a textului
        titleTextView.setTypeface(null, Typeface.BOLD); // Text bold
        titleTextView.setTextColor(Color.BLACK); // Culoare text negru
        titleTextView.setPadding(8, 8, 8, 16); // Padding

        container.addView(titleTextView);

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

                            // Crearea unui TextView pentru fiecare înregistrare și aplicarea stilurilor
                            TextView textView = new TextView(IncomeActivity.this);

                            // Aplicarea stilurilor direct în cod
                            textView.setText("Nume venit: " + numeVenit + "\nSuma venit: " + sumaVenit + "\nDetalii: " + detalii);
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // Setarea dimensiunii textului în sp
                            textView.setTextColor(Color.BLACK); // Setarea culorii textului la negru
                            textView.setPadding(8, 8, 8, 8); // Setarea padding-ului

                            // Adăugarea TextView în LinearLayout
                            container.addView(textView);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Tratarea erorilor la efectuarea interogării
                        Toast.makeText(IncomeActivity.this,"Lista de venituri actuale este goala ! : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

