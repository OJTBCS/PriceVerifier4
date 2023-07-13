package com.example.priceverifier;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImportActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 123;
    private Button saveButton, deleteButton, importFileButton, searchButton, backButton;
    private EditText searchEditText;
    private Uri selectedFileUri;
    private DBHelper dbHelper;
    private List<String> dataView;
    private List<String[]> getData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        dbHelper = new DBHelper(this);

        importFileButton = findViewById(R.id.chooseFileButton);
        saveButton = findViewById(R.id.importButton);
        deleteButton = findViewById(R.id.deleteButton);
        searchButton = findViewById(R.id.searchButton);
        searchEditText = findViewById(R.id.searchEditText);
        backButton = findViewById(R.id.backButton);

        importFileButton.setOnClickListener(v -> openFileChooser());


        saveButton.setOnClickListener(v -> {
            if (validateFile()) {
                saveFile();
                showData();
            } else {
                Toast.makeText(ImportActivity.this, "Invalid file format or missing required columns", Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(v -> {
            removeAllItems();
            showData();
        });

        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            getData(query);
        });

        backButton.setOnClickListener(v -> finish());

        dataView = Arrays.asList(
                "Flag", "PLU", "Barcode", "Item_Description", "Unit_Size",
                "Unit_Measure", "Unit_Price", "Currency", "Manufacturer",
                "Store_Code", "Item_Type"
        );
    }

    private void getData(String query) {
        getData = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_NAME, null);

        while (cursor.moveToNext()) {
            boolean matchFound = false;
            for (String column : dataView) {
                int columnIndex = cursor.getColumnIndex(column);
                String value = cursor.getString(columnIndex);

                if (value != null && value.toLowerCase().contains(query.toLowerCase())) {
                    matchFound = true;
                    break;
                }
            }

            if (matchFound) {
                String[] rowData = new String[dataView.size()];
                for (int i = 0; i < dataView.size(); i++) {
                    int columnIndex = cursor.getColumnIndex(dataView.get(i));
                    rowData[i] = cursor.getString(columnIndex);
                }
                getData.add(rowData);
            }
        }
        cursor.close();
        db.close();

        displayFilteredData();
    }

    private void displayFilteredData() {
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        tableLayout.removeAllViews();

        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        for (String column : dataView) {
            TextView headerTextView = createTextView(column, true, 18);
            headerRow.addView(headerTextView);
        }
        tableLayout.addView(headerRow);

        for (String[] rowData : getData) {
            TableRow dataRow = new TableRow(ImportActivity.this);
            dataRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            for (String value : rowData) {
                ScrollView dataTextView = createScrollView(value, true, 18);
                dataRow.addView(dataTextView);
            }
            tableLayout.addView(dataRow);
            Log.d("DB", "Added data row: " + dataRow.toString());
        }
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

                        for (String requiredColumn : dataView) {
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
                    int batchSize = 1000;

                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.beginTransaction();

                    List<ContentValues> contentValuesList = new ArrayList<>();

                    while ((line = reader.readLine()) != null) {
                        String[] data = line.split(",", -1);

                        if (isFirstLine) {
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

                        contentValuesList.add(values);

                        if (contentValuesList.size() >= batchSize) {
                            for (ContentValues contentValues : contentValuesList) {
                                db.insert("items", null, contentValues);
                            }
                            contentValuesList.clear();
                        }
                    }

                    for (ContentValues contentValues : contentValuesList) {
                        db.insert("items", null, contentValues);
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

    private ScrollView createScrollView(String text, boolean isHeader, int textSize) {
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
        textView.setTextSize(textSize);

        scrollView.addView(textView);

        if (isHeader) {
            textView.setTextColor(getResources().getColor(R.color.header_text_color));
        }

        return scrollView;
    }

    private void showData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_NAME, null);

        TableLayout tableLayout = findViewById(R.id.tableLayout);
        tableLayout.removeAllViews();

        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        for (String column : dataView) {
            TextView headerTextView = createTextView(column, true, 18);
            headerRow.addView(headerTextView);
        }
        tableLayout.addView(headerRow);

        while (cursor.moveToNext()) {
            TableRow dataRow = new TableRow(this);
            dataRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            for (String column : dataView) {
                int columnIndex = cursor.getColumnIndex(column);
                String value = cursor.getString(columnIndex);

                TextView dataTextView = createTextView(value, false, 18);
                dataRow.addView(dataTextView);
            }

            tableLayout.addView(dataRow);
        }

        cursor.close();
        db.close();
    }
    private TextView createTextView(String text, boolean isHeader, float textSize) {
        TextView textView = new TextView(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        int padding = getResources().getDimensionPixelSize(R.dimen.cell_padding);
        textView.setLayoutParams(layoutParams);
        textView.setPadding(padding, padding, padding, padding);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

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

    private void removeAllItems() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.TABLE_NAME, null, null);
        db.close();
        Toast.makeText(ImportActivity.this, "All items removed from the database", Toast.LENGTH_SHORT).show();
    }
}
