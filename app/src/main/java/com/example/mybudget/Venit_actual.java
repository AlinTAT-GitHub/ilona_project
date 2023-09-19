package com.example.mybudget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Venit_actual extends AppCompatActivity {

    EditText nume_venit;
    EditText suma_venit;
    Button save_button;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venit_actual);
        db = FirebaseFirestore.getInstance();
        nume_venit = findViewById(R.id.editTextTextnume);
        suma_venit = findViewById(R.id.editTextTextsuma);
        save_button = findViewById(R.id.button3);

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Venit = nume_venit.getText().toString();
                String Suma = suma_venit.getText().toString();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Obțineți data actuală din sistem
                Map<String, Object> user = new HashMap<>();
                user.put("Nume venit", Venit);
                user.put("Suma venit", Suma);

                // Adăugați data actuală în document
                user.put("Data", FieldValue.serverTimestamp());

                user.put("uid", uid);

                db.collection("Venit")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(Venit_actual.this, "Succes", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), Income.class);
                                startActivity(intent);
                                finish();
                            }

                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Venit_actual.this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
