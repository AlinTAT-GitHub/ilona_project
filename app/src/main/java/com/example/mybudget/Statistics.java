package com.example.mybudget;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Statistics extends AppCompatActivity {

    View balanta;
    View sugestii;

    View variatii;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistici);

        balanta=findViewById(R.id.view5);
        sugestii=findViewById(R.id.view16);
        variatii=findViewById(R.id.view17);

        balanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Statistics.this,Balanta.class);
                startActivity(intent);

            }
        });

        sugestii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Statistics.this, Sugestii.class);
                startActivity(intent);
            }
        });

        variatii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Statistics.this,Variatii_Categorii.class);
                startActivity(intent);
            }
        });


    }
}