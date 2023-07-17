package com.example.priceverifier;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class PriceVerifyActivity extends AppCompatActivity {

    private EditText barcodeEditText;
    private Button checkButton, backButton;
    private TextView itemPriceValueTextView;
    private TextView itemDescriptionValueTextView;
    private TextView unitSizeValueTextView;
    private TextView unitMeasurementValueTextView;
    private TextView barcodeValueTextView;
    private ItemsDB itemsDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_priceverify);

        itemsDb = new ItemsDB(this);

        barcodeEditText = findViewById(R.id.barcodeEditText);
        checkButton = findViewById(R.id.checkButton);
        backButton = findViewById(R.id.backButton);
        itemPriceValueTextView = findViewById(R.id.itemPriceValueTextView);
        itemDescriptionValueTextView = findViewById(R.id.itemDescriptionValueTextView);
        unitSizeValueTextView = findViewById(R.id.unitSizeValueTextView);
        unitMeasurementValueTextView = findViewById(R.id.unitMeasurementValueTextView);
        barcodeValueTextView = findViewById(R.id.barcodeValueTextView);

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyBarcode();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void verifyBarcode() {
        String barcode = barcodeEditText.getText().toString();

        SQLiteDatabase db = itemsDb.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ItemsDB.TABLE_NAME + " WHERE Barcode=?", new String[]{barcode});

        if (cursor.moveToFirst()) {

            String itemPrice = cursor.getString(cursor.getColumnIndex("Unit_Price"));
            String itemDescription = cursor.getString(cursor.getColumnIndex("Item_Description"));
            String unitSize = cursor.getString(cursor.getColumnIndex("Unit_Size"));
            String unitMeasurement = cursor.getString(cursor.getColumnIndex("Unit_Measure"));
            String barcodeValue = barcode;


            itemPriceValueTextView.setText(itemPrice);
            itemDescriptionValueTextView.setText(itemDescription);
            unitSizeValueTextView.setText(unitSize);
            unitMeasurementValueTextView.setText(unitMeasurement);
            barcodeValueTextView.setText(barcodeValue);
        } else {
            Toast.makeText(PriceVerifyActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        db.close();
    }
}
