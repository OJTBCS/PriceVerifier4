package com.example.priceverifier;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AddStoreActivity extends AppCompatActivity {
    private EditText keyEditText;
    private EditText descriptionEditText;
    private Button saveButton;
    private Button updateButton;
    private Button deleteButton;
    private Button backButton;
    private TableLayout tableLayout;
    private StoreDB storeDB;
    private List<Store> storeList;
    private Store selectedStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addstore);

        keyEditText = findViewById(R.id.keyEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        saveButton = findViewById(R.id.saveButton);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        tableLayout = findViewById(R.id.tableLayout);
        backButton = findViewById(R.id.backButton);


        storeDB = new StoreDB(this);

        storeList = storeDB.getAllStores();

        displayStoresInTable();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = keyEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();

                if (!key.isEmpty() && !description.isEmpty()) {

                    storeDB.saveStore(key, description);
                    keyEditText.setText("");
                    descriptionEditText.setText("");

                    Toast.makeText(AddStoreActivity.this, "Store saved successfully", Toast.LENGTH_SHORT).show();
                    refreshTableLayout();
                } else {
                    Toast.makeText(AddStoreActivity.this, "Please enter both key and description", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedStore != null) {
                    String key = keyEditText.getText().toString().trim();
                    String description = descriptionEditText.getText().toString().trim();

                    if (!key.isEmpty() && !description.isEmpty()) {
                        storeDB.updateStore(selectedStore.getId(), key, description);

                        keyEditText.setText("");
                        descriptionEditText.setText("");
                        selectedStore = null;

                        Toast.makeText(AddStoreActivity.this, "Store updated successfully", Toast.LENGTH_SHORT).show();

                        refreshTableLayout();
                    } else {
                        Toast.makeText(AddStoreActivity.this, "Please enter both key and description", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddStoreActivity.this, "No store selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedStore != null) {

                    storeDB.deleteStore(selectedStore.getId());
                    selectedStore = null;

                    keyEditText.setText("");
                    descriptionEditText.setText("");

                    Toast.makeText(AddStoreActivity.this, "Store deleted successfully", Toast.LENGTH_SHORT).show();
                    refreshTableLayout();
                } else {
                    Toast.makeText(AddStoreActivity.this, "No store selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayStoresInTable() {
        tableLayout.removeAllViews();

        TableRow headerRow = new TableRow(this);
        TextView keyHeader = createHeaderTextView("Store Key");
        TextView descriptionHeader = createHeaderTextView("Description");
        headerRow.addView(keyHeader);
        headerRow.addView(descriptionHeader);
        tableLayout.addView(headerRow);

        for (Store store : storeList) {
            TableRow row = new TableRow(this);
            TextView keyTextView = createTextView(store.getKey());
            TextView descriptionTextView = createTextView(store.getDescription());
            row.addView(keyTextView);
            row.addView(descriptionTextView);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    keyEditText.setText(store.getKey());
                    descriptionEditText.setText(store.getDescription());
                    selectedStore = store;
                }
            });
            tableLayout.addView(row);
        }
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textView.setPadding(8, 8, 8, 8);
        textView.setText(text);
        textView.setTextSize(26);
        return textView;
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textView.setPadding(8, 8, 8, 8);
        textView.setText(text);
        textView.setTextSize(24);
        return textView;
    }

    private void refreshTableLayout() {
        storeList = storeDB.getAllStores();
        displayStoresInTable();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        storeDB.close();
    }
}
