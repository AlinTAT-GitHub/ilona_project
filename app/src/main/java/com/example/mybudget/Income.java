package com.example.mybudget;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Income extends AppCompatActivity {

    View venit_actual;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venituri);

        venit_actual=findViewById(R.id.venit_actual);

        venit_actual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Income.this,Venit_actual.class);
                startActivity(intent);
            }
        });
    }
}
