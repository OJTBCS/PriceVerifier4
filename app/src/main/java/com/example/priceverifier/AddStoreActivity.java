package com.example.priceverifier;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AddStoreActivity extends AppCompatActivity {
    private EditText keyEditText;
    private EditText descriptionEditText;
    private Button saveButton;
    private Button updateButton;
    private Button deleteButton;
    private Button backButton;
    private TableLayout tableLayout;

    private SQLiteDatabase database;
    private List<Store> storeList = new ArrayList<>();
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

        // Create or open the database
        database = openOrCreateDatabase("StoreDatabase", MODE_PRIVATE, null);

        // Create the "stores" table if it doesn't exist
        createStoresTable();

        // Retrieve store data from the database
        storeList = getAllStores();

        // Display store data in the table layout
        displayStoresInTable();

        // Set a click listener for the table rows
        setTableRowClickListener();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = keyEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();

                if (!key.isEmpty() && !description.isEmpty()) {
                    // Save the store data to the database
                    saveStore(key, description);

                    // Clear the input fields
                    keyEditText.setText("");
                    descriptionEditText.setText("");

                    // Display a success message
                    Toast.makeText(AddStoreActivity.this, "Store saved successfully", Toast.LENGTH_SHORT).show();

                    // Refresh the table layout with the updated store data
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
                        // Update the selected store data in the database
                        updateStore(selectedStore.getId(), key, description);

                        // Clear the input fields
                        keyEditText.setText("");
                        descriptionEditText.setText("");
                        selectedStore = null;

                        // Display a success message
                        Toast.makeText(AddStoreActivity.this, "Store updated successfully", Toast.LENGTH_SHORT).show();

                        // Refresh the table layout with the updated store data
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
                    // Delete the selected store from the database
                    deleteStore(selectedStore.getId());
                    selectedStore = null;

                    // Clear the input fields
                    keyEditText.setText("");
                    descriptionEditText.setText("");

                    // Display a success message
                    Toast.makeText(AddStoreActivity.this, "Store deleted successfully", Toast.LENGTH_SHORT).show();

                    // Refresh the table layout with the updated store data
                    refreshTableLayout();
                } else {
                    Toast.makeText(AddStoreActivity.this, "No store selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class Store{
        private int id;
        private String key;
        private String description;

        public Store(int id, String key, String description){
            this.id = id;
            this.key = key;
            this.description = description;
        }

        public int getId(){
            return id;
        }

        public String getKey(){
            return key;
        }

        public String getDescription(){
            return description;
        }
    }

    private void createStoresTable() {
        database.execSQL("CREATE TABLE IF NOT EXISTS stores (id INTEGER PRIMARY KEY AUTOINCREMENT, store_key TEXT, description TEXT)");
    }

    private void saveStore(String key, String description) {
        ContentValues values = new ContentValues();
        values.put("store_key", key);
        values.put("description", description);
        database.insert("stores", null, values);
    }

    private List<Store> getAllStores() {
        List<Store> stores = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM stores", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String key = cursor.getString(cursor.getColumnIndex("store_key"));
                String description = cursor.getString(cursor.getColumnIndex("description"));
                stores.add(new Store(id, key, description));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stores;
    }

    private void clearTableLayout(){
        int childCount = tableLayout.getChildCount();
        if (childCount > 1){
            tableLayout.removeViews(1, childCount - 1);
        }
    }

    private void displayStoresInTable() {
        clearTableLayout();
        for (Store store : storeList) {
            TableRow headerRow = new TableRow(this);
            headerRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT

            ));

            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            TextView keyTextView = new TextView(this);
            keyTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            keyTextView.setPadding(8, 8, 8, 8);
            keyTextView.setText(store.getKey());
            keyTextView.setTextSize(24);
            row.addView(keyTextView);

            TextView descriptionTextView = new TextView(this);
            descriptionTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            descriptionTextView.setPadding(8, 8, 8, 8);
            descriptionTextView.setText(store.getDescription());
            descriptionTextView.setTextSize(24);
            row.addView(descriptionTextView);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Set the clicked store data in the edit texts
                    keyEditText.setText(store.getKey());
                    descriptionEditText.setText(store.getDescription());

                    // Set the selected store
                    selectedStore = store;
                }
            });

            tableLayout.addView(row);
        }
    }


    private void setTableRowClickListener() {
        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            final TableRow row = (TableRow) tableLayout.getChildAt(i);
            final TextView keyTextView = (TextView) row.getChildAt(0);
            final TextView descriptionTextView = (TextView) row.getChildAt(1);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Set the clicked store data in the edit texts
                    keyEditText.setText(keyTextView.getText().toString());
                    descriptionEditText.setText(descriptionTextView.getText().toString());

                    // Set the selected store
                    for (Store store : storeList) {
                        if (store.getKey().equals(keyTextView.getText().toString()) &&
                                store.getDescription().equals(descriptionTextView.getText().toString())) {
                            selectedStore = store;
                            break;
                        }
                    }
                }
            });
        }
    }

    private void updateStore(int id, String key, String description) {
        ContentValues values = new ContentValues();
        values.put("store_key", key);
        values.put("description", description);
        database.update("stores", values, "id = " + id, null);
    }

    private void deleteStore(int id) {
        database.delete("stores", "id = " + id, null);
    }

    private void refreshTableLayout() {
        tableLayout.removeAllViews();

        // Retrieve updated store data from the database
        storeList = getAllStores();

        // Display updated store data in the table layout
        displayStoresInTable();

        // Set a click listener for the table rows
        setTableRowClickListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database connection
        database.close();
    }
}
