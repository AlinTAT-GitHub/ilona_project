package com.example.mybudget;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Statistics extends AppCompatActivity {

    View balanta;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistici);

        balanta=findViewById(R.id.view5);

        balanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Statistics.this,Balanta.class);
                startActivity(intent);
                finish();
            }
        });
    }
}