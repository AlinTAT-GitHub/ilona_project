package com.example.mybudget;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Categories extends AppCompatActivity {

    private View adauga_categorii;

    private View view4;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categorii);

        adauga_categorii = findViewById(R.id.view9);
        view4=findViewById(R.id.view4);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();



        adauga_categorii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCategoryDialog();
            }
        });

        view4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Categories.this, Categoriile_Mele.class);
                startActivity(intent);
            }
        });

    }

    private void showAddCategoryDialog() {
        // Crearea unui LinearLayout pentru a conține elementele dialogului
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 0, 24, 0); // Adăugați un padding pentru a îmbunătăți aspectul

        // Crearea unui EditText pentru numele categoriei
        final EditText inputCategoryName = new EditText(this);
        inputCategoryName.setHint("Numele categoriei");
        layout.addView(inputCategoryName);

        // Crearea unui AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adăugare Categorie");
        builder.setView(layout);

        // Adăugare butoane pentru dialog
        builder.setPositiveButton("Adaugă", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = inputCategoryName.getText().toString().trim();
                if (!categoryName.isEmpty()) {
                    addCategoryToFirestore(categoryName);
                    dialog.dismiss();
                } else {
                    Toast.makeText(Categories.this, "Introduceți numele categoriei", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Anulează", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addCategoryToFirestore(String categoryName) {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> category = new HashMap<>();
        category.put("Nume categorie", categoryName);
        category.put("uid", uid);

        db.collection("Categorii")
                .add(category)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(Categories.this, "Categorie adăugată cu succes", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(Categories.this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


