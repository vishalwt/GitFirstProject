package com.example.admin.gitfirstproject.database;

/**
 * Created by Admin on 13-07-2017.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBAdapter {

    // Initial Configuration
    public static final String DB_NAME = "transporter";
    private static final int DATABASE_VER = 1;
    private static final String TAG = "DBAdapter";

    private final Context context;

    private DatabaseHelper DBHelper;
    public SQLiteDatabase db = null;


    public static final String TABLE_ENROLLMENT = "enroll_user";
    public static final String ID = "id";
    public static final String USER_EMAIL = "email_id";
    public static final String USER_PASSWORD = "password";
    public static final String FINGERPRINT = "fingerprint";
    public static final String USER_NAME = "name";



    private static final String CREATE_ENROLLMENT = "CREATE TABLE " + TABLE_ENROLLMENT + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
            + USER_EMAIL + " TEXT,"
            + USER_NAME + " TEXT,"
            + USER_PASSWORD + " TEXT,"
            + FINGERPRINT + " TEXT)";




    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VER);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_ENROLLMENT);


        }


        @Override

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            onCreate(db);

        }

    }

    public SQLiteDatabase getSQLiteDatabase() {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        return db;
    }


    public void deletefromtable(String tablename) {
        db.delete(tablename, null, null);
    }

    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;

    }


    public void resetDatabase() {
        SQLiteDatabase database = DBHelper.getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + CREATE_ENROLLMENT);
        database.execSQL(CREATE_ENROLLMENT);
        database.close();
    }


    public void close() {
        DBHelper.close();
    }



    public Cursor getFingerPrintByEmail(String email) throws SQLException {
        return db.query(TABLE_ENROLLMENT, null, USER_EMAIL + " = '" + email + "'",
                null, null, null, null, null);
    }


    public Cursor getAllUser() throws SQLException {
        return db.query(TABLE_ENROLLMENT, null,null,
                null, null, null, null, null);
    }



}
