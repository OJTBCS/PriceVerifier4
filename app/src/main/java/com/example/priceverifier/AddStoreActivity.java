package com.example.priceverifier;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddStoreActivity extends AppCompatActivity {
    private Button backButton;
    private Button updateButton;
    private Button saveButton;
    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addstore);

        Button backButton = findViewById(R.id.backButton);
        Button updateButton = findViewById(R.id.updateButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button deleteButton = findViewById(R.id.deleteButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStore();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStore();
            }
        });

        deleteButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteStore();
            }
        }));
    }

    private void updateStore() {
        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void saveStore() {
        Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void deleteStore() {
        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
    }
}


