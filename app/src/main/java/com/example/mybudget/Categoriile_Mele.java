package com.example.mybudget;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

public class Categoriile_Mele extends AppCompatActivity {

    private LinearLayout container;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categoriile_mele);

        container=findViewById(R.id.containercat);

        // Adăugarea unui TextView pentru titlu
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Categorii:");
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30); // Dimensiune mare a textului
        titleTextView.setTypeface(null, Typeface.BOLD); // Text bold
        titleTextView.setTextColor(Color.BLACK); // Culoare text negru
        titleTextView.setPadding(8, 8, 8, 16); // Padding

        container.addView(titleTextView);

        // Obțineți datele din Firestore pentru categorii și afișați-le în TextView-uri
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Categorii")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String numeCategorie = document.getString("Nume categorie");

                            // Crearea unui TextView pentru fiecare categorie și aplicarea stilurilor
                            TextView textView = new TextView(Categoriile_Mele.this);

                            // Aplicarea stilurilor direct în cod
                            textView.setText(numeCategorie);
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25); // Setarea dimensiunii textului în sp
                            textView.setTextColor(Color.BLACK); // Setarea culorii textului la negru
                            textView.setPadding(8, 8, 8, 8); // Setarea padding-ului

                            // Adăugarea TextView în LinearLayout
                            container.addView(textView);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Tratarea erorilor la efectuarea interogării
                        Toast.makeText(Categoriile_Mele.this, "Lista de categorii este goală! : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
