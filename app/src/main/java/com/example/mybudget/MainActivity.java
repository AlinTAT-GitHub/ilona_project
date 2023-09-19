package com.example.mybudget;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;

    TextView textView;
    FirebaseUser user;

    ImageView venit;
    ImageView cheltuieli;

    View categorii;
    View statistici;

    Button buttonLogout; // Adăugați butonul de logout


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        auth = FirebaseAuth.getInstance();
        textView=findViewById(R.id.textView5);
        cheltuieli=findViewById(R.id.imageView5);
        categorii=findViewById(R.id.view7);
        statistici=findViewById(R.id.view3);

        user=auth.getCurrentUser();
        if(user==null){
            Intent intent = new Intent(getApplicationContext(), LogIn.class);
            startActivity(intent);
            finish();
        }
        else{
            textView.setText(user.getEmail());
        }

        venit=findViewById(R.id.imageView15);
        venit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,Income.class);
                startActivity(intent);
            }
        });

        cheltuieli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,Cheltuieli.class);
                startActivity(intent);
            }
        });

        categorii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,Categories.class);
                startActivity(intent);
            }
        });

        buttonLogout = findViewById(R.id.buttonlog); // Inițializați butonul de logout
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Deconectați utilizatorul și redirecționați-l către activitatea de autentificare
                auth.signOut();
                Intent intent = new Intent(getApplicationContext(), LogIn.class);
                startActivity(intent);
                finish();
            }
        });

        statistici.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,Statistics.class);
                startActivity(intent);
                finish();
            }
        });

    }
}