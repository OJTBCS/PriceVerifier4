package com.example.priceverifier;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnAddStore = findViewById(R.id.addStoreButton);
        Button btnPriceVerify = findViewById(R.id.priceVerifierButton);
        Button btnImport = findViewById(R.id.importFileButton);
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImportActivity.class);
                startActivity(intent);
            }
        });

        btnPriceVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PriceVerifyActivity.class);
                startActivity(intent);
            }
        });
        btnAddStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddStoreActivity.class);
                startActivity(intent);
            }
        });

    }

}
