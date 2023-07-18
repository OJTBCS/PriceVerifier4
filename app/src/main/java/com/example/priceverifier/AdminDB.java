package com.example.priceverifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminDB{
    private static final String DATABASE_NAME = "adminDB";
    private static final int DATABASE_VERSION = 1;
    protected static final String TABLE_NAME = "admin";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private SQLiteDatabase database;

    public AdminDB(Context context) {
        AdminDBHelper dbHelper = new AdminDBHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        if(database != null && database.isOpen()){
            database.close();
        }
    }
    public void saveAdmin(String username, String password) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        database.insert(TABLE_NAME, null, values);
    }

    public List<Admin> getAllAdmin() {
        List<Admin> admins = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
                admins.add(new Admin(id, username, password));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return admins;
    }
    public boolean checkCredentials(String username, String password){
        SQLiteDatabase db = database;
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username,password};
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        boolean isMatchFound = cursor.getCount() > 0;
        cursor.close();
        return isMatchFound;
    }
    public boolean isUsernameExist(String username){
        SQLiteDatabase db = database;
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(TABLE_NAME, null, selection,selectionArgs,null,null,null);
        boolean isExist = cursor.getCount() > 0;
        cursor.close();
        return isExist;
    }

    private static class AdminDBHelper extends SQLiteOpenHelper {
        public AdminDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT)";
            db.execSQL(createTableQuery);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

    }

}

