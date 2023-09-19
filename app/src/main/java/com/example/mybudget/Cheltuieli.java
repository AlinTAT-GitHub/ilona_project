package com.example.mybudget;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cheltuieli extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private View cheltuieli_pe_categorii;
    private View cheltuielile_mele;
    private View membrii;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cheltuieli);

        cheltuieli_pe_categorii = findViewById(R.id.view10);
        cheltuielile_mele = findViewById(R.id.view15);
        membrii = findViewById(R.id.view13);

        cheltuieli_pe_categorii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddExpenseDialog();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cheltuielile_mele.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Cheltuieli.this, Cheltuielile_Mele.class);
                startActivity(intent);
            }
        });

        membrii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Cheltuieli.this, Cheltuieli_Membrii.class);
                startActivity(intent);
            }
        });
    }

    public void showAddExpenseDialog() {
        // Inițializarea dialogului de alertă
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adăugare Cheltuială");

        // Crearea unei viziuni personalizate pentru dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        // Inițializarea elementelor din dialog
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Spinner categoriesSpinner = dialogView.findViewById(R.id.spinner_categories);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText amountEditText = dialogView.findViewById(R.id.editText_expense_amount);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CheckBox isOccasionalCheckBox = dialogView.findViewById(R.id.checkBox_occasional_expense);

        // Configurarea Spinner-ului cu categoriile din Firestore
        setupCategoriesSpinner(categoriesSpinner);

        // Setarea butoanelor pentru dialog
        builder.setPositiveButton("Adaugă", (dialog, which) -> {
            // Obținerea valorilor din elementele dialogului
            String selectedCategory = categoriesSpinner.getSelectedItem().toString();
            String amount = amountEditText.getText().toString().trim();
            boolean isOccasional = isOccasionalCheckBox.isChecked();

            // Validarea datelor introduse
            if (amount.isEmpty()) {
                Toast.makeText(this, "Introduceți suma cheltuielii", Toast.LENGTH_SHORT).show();
                return;
            }

            // Salvarea cheltuielii ocazionale în Firestore
            saveExpenseToFirestore(selectedCategory, amount, isOccasional);

            // Închiderea dialogului
            dialog.dismiss();
        });

        builder.setNegativeButton("Anulează", (dialog, which) -> {
            // Închiderea dialogului
            dialog.dismiss();
        });

        // Afișarea dialogului de alertă
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setupCategoriesSpinner(Spinner spinner) {
        // Obțineți referința la Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obțineți ID-ul utilizatorului autentificat
        String uid = mAuth.getCurrentUser().getUid();

        // Obțineți colecția "Categorii" specifică pentru utilizatorul curent
        db.collection("Categorii")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Lista de categorii
                    List<String> categoriesList = new ArrayList<>();

                    // Parcurgeți rezultatele pentru a obține categoriile
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String category = document.getString("Nume categorie");
                        if (category != null) {
                            categoriesList.add(category);
                        }
                    }

                    // Creați un adaptor pentru Spinner cu lista de categorii
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    // Setați adaptorul pentru Spinner
                    spinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    // Tratarea erorilor dacă apar în timpul obținerii categoriilor din Firestore
                    Toast.makeText(this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveExpenseToFirestore(String category, String amount, boolean isOccasional) {
        // Obținerea ID-ului utilizatorului autentificat
        String uid = mAuth.getCurrentUser().getUid();

        // Crearea unui obiect Map pentru a stoca datele cheltuielii
        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("Categorie", category);
        expenseData.put("Suma", amount);
        expenseData.put("CheltuialaOcazionala", isOccasional); // Adăugare informații despre cheltuielile ocazionale
        expenseData.put("uid", uid);

        // Adăugarea câmpului "Data" cu data curentă în obiectul Map
        expenseData.put("Data", FieldValue.serverTimestamp());

        // Salvarea datelor în Firestore
        db.collection("Cheltuieli")
                .add(expenseData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Cheltuială adăugată cu succes", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
