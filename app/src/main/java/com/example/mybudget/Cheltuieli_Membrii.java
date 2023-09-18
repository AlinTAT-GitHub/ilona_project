package com.example.mybudget;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Cheltuieli_Membrii extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView cheltuieliTextView;
    private ListView cheltuieliListView; // ListView pentru afișarea cheltuielilor
    private List<String> cheltuieliList = new ArrayList<>(); // Listă pentru a stoca cheltuielile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheltuieli_membrii);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        cheltuieliTextView = findViewById(R.id.cheltuieli_text_view);
        cheltuieliListView = findViewById(R.id.cheltuieli_list_view); // Legați ListView din XML

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserId = currentUser.getUid();

        // Obțineți parola utilizatorului curent
        db.collection("Utilizatori")
                .document(currentUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String currentUserPassword = document.getString("password"); // Utilizăm "password" în loc de "parola"

                                // Afișați cheltuielile utilizatorilor cu aceeași parolă (excluzând cheltuielile dvs.)
                                findCheltuieliWithSamePassword(currentUserPassword, currentUserId);
                            } else {
                                // Documentul pentru utilizatorul curent nu există
                                cheltuieliTextView.setText("Utilizatorul curent nu există.");
                            }
                        } else {
                            // Tratați eroarea în caz de eșec
                            String errorMessage = "Eroare: " + task.getException().getMessage();
                            cheltuieliTextView.setText(errorMessage);
                            Log.e("Cheltuieli_Membrii", errorMessage); // Afișare în logcat
                            Toast.makeText(Cheltuieli_Membrii.this, errorMessage, Toast.LENGTH_SHORT).show(); // Afișare în Toast
                        }
                    }
                });
    }

    private void findCheltuieliWithSamePassword(String currentUserPassword, String currentUserId) {
        // Căutați toți utilizatorii cu aceeași parolă (excluzând utilizatorul curent)
        Query query = db.collection("Utilizatori")
                .whereEqualTo("password", currentUserPassword) // Utilizăm "password" în loc de "parola"
                .whereNotEqualTo("uid", currentUserId);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String userId = document.getString("uid");
                        String email = document.getString("email"); // Obțineți adresa de email

                        // Adăugați adresa de email în listă pentru a o afișa
                        cheltuieliList.add("Email: " + email);

                        // Afișați cheltuielile utilizatorului curent
                        db.collection("Cheltuieli")
                                .whereEqualTo("uid", userId)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> cheltuieliTask) {
                                        if (cheltuieliTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot cheltuialaDocument : cheltuieliTask.getResult()) {
                                                // Extrageți detaliile cheltuielii, de exemplu, suma
                                                String categorie = cheltuialaDocument.getString("Categorie");
                                                String suma = cheltuialaDocument.getString("Suma");

                                                // Adăugați detaliile la lista cheltuieliList
                                                String cheltuiala = "Categorie: " + categorie + ", Suma: " + suma;
                                                cheltuieliList.add(cheltuiala);
                                            }

                                            // Afișați cheltuielile acumulate în cheltuieliListView folosind un adaptor
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(Cheltuieli_Membrii.this,
                                                    android.R.layout.simple_list_item_1, cheltuieliList);
                                            cheltuieliListView.setAdapter(adapter);
                                        } else {
                                            // Tratați eroarea în caz de eșec
                                            String errorMessage = "Eroare: " + cheltuieliTask.getException().getMessage();
                                            Log.e("Cheltuieli_Membrii", errorMessage); // Afișare în logcat
                                            Toast.makeText(Cheltuieli_Membrii.this, errorMessage, Toast.LENGTH_SHORT).show(); // Afișare în Toast
                                        }
                                    }
                                });
                    }
                } else {
                    // Tratați eroarea în caz de eșec
                    String errorMessage = "Eroare: " + task.getException().getMessage();
                    Log.e("Cheltuieli_Membrii", errorMessage); // Afișare în logcat
                    Toast.makeText(Cheltuieli_Membrii.this, errorMessage, Toast.LENGTH_SHORT).show(); // Afișare în Toast
                }
            }
        });
    }
}
