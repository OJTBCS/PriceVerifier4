package com.example.priceverifier;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;




import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImportActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 123;
    private static final int EXPECTED_COLUMN_COUNT = 11;

    private Button importFileButton;
    private Button cancel_button;
    private Uri selectedFileUri;
    private DBHelper dbHelper;
    private List<String> requiredColumns;

    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        dbHelper = new DBHelper(this);

        importFileButton = findViewById(R.id.importFileButton);
        cancel_button = findViewById(R.id.cancel_button);

        importFileButton.setOnClickListener(v -> openFileChooser());

        cancel_button.setOnClickListener(v -> {
            if (validateFile()) {
                saveFile();
                showData();
            } else {
                Toast.makeText(ImportActivity.this, "Invalid file format or missing required columns", Toast.LENGTH_SHORT).show();
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
                TextView headerTextView = createTextView(column, true);
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
                        TextView dataTextView = createTextView(value, false);
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




    private TextView createTextView(String text, boolean isHeader) {
        TextView textView = new TextView(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        int padding = getResources().getDimensionPixelSize(R.dimen.cell_padding);
        textView.setLayoutParams(layoutParams);
        textView.setPadding(padding, padding, padding, padding);
        textView.setText(text);
        if (isHeader) {
            textView.setBackgroundColor(getResources().getColor(R.color.header_background));
            textView.setTextColor(getResources().getColor(R.color.header_text_color));
        }
        return textView;
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
}
