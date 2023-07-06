package com.example.priceverifier;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImportActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 123;

    private Button btnPickFile;
    private Button btnSave;
    private Uri selectedFileUri;
    private DBHelper dbHelper;
    private List<String> requiredColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        dbHelper = new DBHelper(this);

        btnPickFile = findViewById(R.id.btnPickFile);
        btnSave = findViewById(R.id.btnSave);

        btnPickFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFile()) {
                    saveFile();
                } else {
                    Toast.makeText(ImportActivity.this, "Invalid file format or missing required columns", Toast.LENGTH_SHORT).show();
                }
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
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                if ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",");

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void saveFile() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                boolean isFirstLine = true;
                int lineNumber = 1;

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                try {
                    while ((line = reader.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            lineNumber++;
                            continue;
                        }

                        String[] values = line.split(",");

                        if (values.length != requiredColumns.size()) {
                            throw new IllegalArgumentException("Invalid number of columns at line " + lineNumber);
                        }

                        ContentValues contentValues = new ContentValues();
                        try {
                            contentValues.put("Flag", values[0].trim());
                            contentValues.put("PLU", Long.parseLong(values[1].trim()));
                            contentValues.put("Barcode", values[2].trim());
                            contentValues.put("Item_Description", values[3].trim());
                            contentValues.put("Unit_Size", Float.parseFloat(values[4].trim()));
                            contentValues.put("Unit_Measure", values[5].trim());
                            contentValues.put("Unit_Price", Float.parseFloat(values[6].trim()));
                            contentValues.put("Currency", values[7].trim());
                            contentValues.put("Manufacturer", values[8].trim());
                            contentValues.put("Store_Code", values[9].trim());
                            contentValues.put("Item_Type", values[10].trim());

                            db.insert(DBHelper.TABLE_NAME, null, contentValues);
                        } catch (NumberFormatException e) {
                            db.endTransaction();
                            String errorMessage = String.format("Error parsing numeric value at line %d: %s", lineNumber, e.getMessage());
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        lineNumber++;
                    }

                    db.setTransactionSuccessful();
                    Toast.makeText(this, "File saved to the database", Toast.LENGTH_SHORT).show();
                } catch (IllegalArgumentException e) {
                    db.endTransaction();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    db.endTransaction();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving file to the database", Toast.LENGTH_SHORT).show();
        }
    }

    private ContentValues createContentValues(String[] values) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("Flag", values[0].trim());
        contentValues.put("PLU", Integer.parseInt(values[1].trim()));
        contentValues.put("Barcode", values[2].trim());
        contentValues.put("Item_Description", values[3].trim());
        contentValues.put("Unit_Size", Float.parseFloat(values[4].trim()));
        contentValues.put("Unit_Measure", values[5].trim());
        contentValues.put("Unit_Price", Float.parseFloat(values[6].trim()));
        contentValues.put("Currency", values[7].trim());
        contentValues.put("Manufacturer", values[8].trim());
        contentValues.put("Store_Code", values[9].trim());
        contentValues.put("Item_Type", values[10].trim());
        return contentValues;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                selectedFileUri = data.getData();
                btnPickFile.setVisibility(View.GONE);
                btnSave.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getFileExtension(Uri fileUri) {
        String extension = null;
        if (fileUri.getScheme().equals("content")) {
            String mimeType = getContentResolver().getType(fileUri);
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        } else {
            String filePath = fileUri.getPath();
            if (filePath != null) {
                extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
            }
        }
        return extension;
    }
}
