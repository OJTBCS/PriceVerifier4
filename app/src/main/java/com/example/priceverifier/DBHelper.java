package com.example.priceverifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CsvDB";
    private static final int DATABASE_VERSION = 1;
    protected static final String TABLE_NAME = "items";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + "(" +
                "Flag VARCHAR," +
                "PLU INTEGER NOT NULL," +
                "Barcode VARCHAR," +
                "Item_Description VARCHAR," +
                "Unit_Size VARCHAR," +
                "Unit_Measure VARCHAR," +
                "Unit_Price VARCHAR," +
                "Currency VARCHAR," +
                "Manufacturer VARCHAR," +
                "Store_Code VARCHAR," +
                "Item_Type VARCHAR" +
                ")";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades here if needed
    }
}
