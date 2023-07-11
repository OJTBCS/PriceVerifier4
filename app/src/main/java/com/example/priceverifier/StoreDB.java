package com.example.priceverifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class StoreDB {
    private static final String DATABASE_NAME = "StoreDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "stores";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_KEY = "store_key";
    private static final String COLUMN_DESCRIPTION = "description";

    private SQLiteDatabase database;

    public StoreDB(Context context) {
        StoreDatabaseHelper dbHelper = new StoreDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        if(database != null && database.isOpen()){
            database.close();
        }
    }

    public void saveStore(String key, String description) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY, key);
        values.put(COLUMN_DESCRIPTION, description);
        database.insert(TABLE_NAME, null, values);
    }

    public List<Store> getAllStores() {
        List<Store> stores = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String key = cursor.getString(cursor.getColumnIndex(COLUMN_KEY));
                String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                stores.add(new Store(id, key, description));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stores;
    }

    public void updateStore(int id, String key, String description) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY, key);
        values.put(COLUMN_DESCRIPTION, description);
        database.update(TABLE_NAME, values, COLUMN_ID + " = " + id, null);
    }

    public void deleteStore(int id) {
        database.delete(TABLE_NAME, COLUMN_ID + " = " + id, null);
    }

    private static class StoreDatabaseHelper extends SQLiteOpenHelper {
        public StoreDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create the "stores" table
            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_KEY + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT)";
            db.execSQL(createTableQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
