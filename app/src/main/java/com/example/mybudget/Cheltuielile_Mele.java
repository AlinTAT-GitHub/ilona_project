package com.example.mybudget;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Cheltuielile_Mele extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheltuielile_mele);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Găsiți LinearLayout din XML pentru a adăuga cheltuielile
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) final LinearLayout container = findViewById(R.id.containerchelt);

        // Adăugați un TextView pentru titlu
        TextView titleTextView = new TextView(this);
        titleTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        titleTextView.setText("Cheltuielile mele:");
        titleTextView.setTextSize(30); // Mărimea textului
        titleTextView.setTypeface(null, Typeface.BOLD); // Îngroșați textul
        titleTextView.setTextColor(getResources().getColor(android.R.color.black)); // Culoarea textului
        container.addView(titleTextView);

        // Adăugați spațiu între titlu și înregistrări
        Space space = new Space(this);
        space.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                24)); // 16dp spațiu vertical
        container.addView(space);

        // Obțineți UID-ul utilizatorului curent
        String uid = mAuth.getCurrentUser().getUid();

        // Obțineți cheltuielile din Firestore pentru utilizatorul curent
        db.collection("Cheltuieli")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Obțineți informații despre cheltuială din document
                            String categorie = document.getString("Categorie");
                            String suma = document.getString("Suma");
                            String da;
                            boolean isCheltuialaOcazionala = document.getBoolean("CheltuialaOcazionala");

                            if (isCheltuialaOcazionala) {
                                da = "da";
                            } else {
                                da = "nu";
                            }

                            // Crearea unui TextView pentru fiecare înregistrare și aplicarea stilurilor
                            TextView textView = new TextView(Cheltuielile_Mele.this);
                            textView.setLayoutParams(new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));
                            textView.setText("Categorie: " + categorie + "\nSuma: " + suma + "\nCheltuiala Ocazionala: " + da);
                            textView.setTextSize(24); // Mărimea textului pentru înregistrări
                            textView.setTextColor(getResources().getColor(android.R.color.black)); // Culoarea textului
                            container.addView(textView);

                            // Adăugați spațiu între fiecare bloc de informație
                            Space innerSpace = new Space(Cheltuielile_Mele.this);
                            innerSpace.setLayoutParams(new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    23)); // 16dp spațiu vertical
                            container.addView(innerSpace);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Tratarea erorilor
                        Toast.makeText(Cheltuielile_Mele.this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
