package com.example.priceverifier;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class ImportActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 123;
    private Button importFileButton;
    private Button saveButton;
    private Button deleteButton;
    private Uri selectedFileUri;
    private DBHelper dbHelper;
    private List<String> requiredColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        dbHelper = new DBHelper(this);

        importFileButton = findViewById(R.id.importFileButton);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);

        importFileButton.setOnClickListener(v -> openFileChooser());

        saveButton.setOnClickListener(v -> {
            if (validateFile()) {
                showData();
                saveFile();
            } else {
                Toast.makeText(ImportActivity.this, "Invalid file format or missing required columns", Toast.LENGTH_SHORT).show();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllItems();
                showData();
            }
        });

        requiredColumns = Arrays.asList(
                "Flag", "PLU", "Barcode", "Item_Description", "Unit_Size",
                "Unit_Measure", "Unit_Price", "Currency", "Manufacturer",
                "Store_Code", "Item_Type"
        );
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE);
    }

    private boolean validateFile() {
        if (selectedFileUri != null) {
            String fileExtension = getFileExtension(selectedFileUri);
            if (fileExtension != null && fileExtension.equals("csv")) {
                return isValidFileContent(selectedFileUri);
            }
        }
        return false;
    }

    private boolean isValidFileContent(Uri fileUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line = reader.readLine();
                    if (line != null) {
                        String[] columns = line.split(",", -1);

                        for (String requiredColumn : requiredColumns) {
                            boolean found = false;
                            for (String column : columns) {
                                if (column.trim().equalsIgnoreCase(requiredColumn.trim())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveFile() {
        try (InputStream inputStream = getContentResolver().openInputStream(selectedFileUri)) {
            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    boolean isFirstLine = true;

                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.beginTransaction();

                    while ((line = reader.readLine())!= null) {
                        String[] data = line.split(",", -1);

                        if (isFirstLine) {
                            // Skip the first line (header row)
                            isFirstLine = false;
                            continue;
                        }

                        ContentValues values = new ContentValues();
                        values.put("Flag", data[0].trim());
                        values.put("PLU", data[1].trim());
                        values.put("Barcode", data[2].trim());
                        values.put("Item_Description", data[3].trim());
                        values.put("Unit_Size", data[4].trim());
                        values.put("Unit_Measure", data[5].trim());
                        values.put("Unit_Price", data[6].trim());
                        values.put("Currency", data[7].trim());
                        values.put("Manufacturer", data[8].trim());
                        values.put("Store_Code", data[9].trim());
                        values.put("Item_Type", data[10].trim());

                        db.insert("items", null, values);
                    }

                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.close();

                    Toast.makeText(ImportActivity.this, "File saved successfully", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showData() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_NAME, null);

            TableLayout tableLayout = findViewById(R.id.tableLayout);
            tableLayout.removeAllViews(); // Clear previous data

            // Add table header row
            TableRow headerRow = new TableRow(this);
            headerRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            for (String column : requiredColumns) {
                ScrollView headerTextView = createScrollView(column, true);
                headerRow.addView(headerTextView);
            }

            tableLayout.addView(headerRow);

            if (cursor.moveToFirst()) {
                do {
                    TableRow dataRow = new TableRow(this);
                    dataRow.setLayoutParams(new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));

                    for (String column : requiredColumns) {
                        int columnIndex = cursor.getColumnIndex(column);
                        String value = cursor.getString(columnIndex);
                        ScrollView dataTextView = createScrollView(value, false);
                        dataRow.addView(dataTextView);
                    }

                    tableLayout.addView(dataRow);

                    // Debug logs
                    Log.d("DB", "Added data row: " + dataRow.toString());
                } while (cursor.moveToNext());
            } else {
                Log.d("DB", "No data found in the database");
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error retrieving data from the database", Toast.LENGTH_SHORT).show();
        }
    }


    private ScrollView createScrollView(String text, boolean isHeader) {
        ScrollView scrollView = new ScrollView(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        int padding = getResources().getDimensionPixelSize(R.dimen.cell_padding);
        scrollView.setLayoutParams(layoutParams);

        TextView textView = new TextView(this);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setPadding(padding, padding, padding, padding);
        textView.setText(text);

        scrollView.addView(textView);

        if (isHeader) {
            textView.setBackgroundColor(getResources().getColor(R.color.header_background));
            textView.setTextColor(getResources().getColor(R.color.header_text_color));
        }

        return scrollView;
    }


    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedFileUri = data.getData();
            Toast.makeText(ImportActivity.this, "File selected: " + selectedFileUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeAllItems() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.TABLE_NAME, null, null);
        db.close();
        Toast.makeText(ImportActivity.this, "All items removed from the database", Toast.LENGTH_SHORT).show();
    }

}
