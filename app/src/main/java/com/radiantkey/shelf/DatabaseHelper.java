package com.radiantkey.shelf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "shelf.db";
    public static final String R_TABLE_NAME = "record";
    public static final String R_COL_1 = "ID";
    public static final String R_COL_2 = "NAME";
    public static final String R_COL_3 = "POSITION";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        //SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + R_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT unique, POSITION INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + R_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public long insertData(String name, int cat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(R_COL_2, name);
        contentValues.put(R_COL_3, cat);
        Cursor cursor = getData(name);
        long res = db.insertOrThrow(R_TABLE_NAME, null, contentValues);
        return res;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + R_TABLE_NAME, null);
        return res;
    }

//    public boolean updateData(long id, String name, int position){
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(R_COL_1, id);
//        contentValues.put(R_COL_2, name);
//        contentValues.put(R_COL_3, position);
//        db.update(R_TABLE_NAME, contentValues, "ID = ?", new String[] {String.valueOf(id)});
//        return true;
//    }

    public Cursor getData(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(R_TABLE_NAME, new String[]{R_COL_3}, R_COL_2 + "=?", new String[]{name}, null,null,null);
        return cursor;
    }

    public boolean updatePosition(String name, int position){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(R_COL_2, name);
        contentValues.put(R_COL_3, position);
//        db.query(R_TABLE_NAME, new String[]{R_COL_1}, R_COL_2 + "=?", new String[]{name}, null,null,null);
        db.update(R_TABLE_NAME, contentValues, R_COL_2 + " = ?", new String[] {name});
        return true;
    }



    public Integer deleteData(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(R_TABLE_NAME, "ID = ?", new String[]{String.valueOf(id)});
    }
}
