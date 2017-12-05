package com.fahamutech.cs243networkdesign;

/**
 * Created by joshua , 12/4/17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class DataStorageSqlite extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "HtmlNotesData.db";
    public static final String NOTES_TABLE_NAME = "notes";
    public static final String NOTES_COLUMN_VERSION = "version";
    public static final String NOTES_COLUMN_UNIT = "unit";
    public static final String NOTES_COLUMN_CONTENTS = "content";
    public static HashMap<String, byte[]> hp;

    public DataStorageSqlite(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table notes " +
                        "(id integer primary key autoincrement, " +
                        "unit text,version interger,content blob)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
    }

    public boolean insertNotes (String unit, int version, InputStream content) {
        try {
            BufferedInputStream bis = new BufferedInputStream(content);
            ByteArrayOutputStream baf=new ByteArrayOutputStream();

            byte[] data=new byte[content.available()];
            int current;
            while ((current = bis.read(data,0,data.length)) != -1) {
                baf.write(data,0,current);
            }

            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "INSERT INTO notes (unit,version,content) VALUES(?,?,?)";
            SQLiteStatement insertStmt = db.compileStatement(sql);
            insertStmt.clearBindings();
            insertStmt.bindString(1, unit);
            insertStmt.bindLong(2,version);
            insertStmt.bindBlob(3,baf.toByteArray() );
            insertStmt.executeInsert();

        } catch (IOException e) {
            Log.d("Html insert", "Error: " + e.toString());
        }
        return true;
    }

    public int getFileVerionNumber(String unit) {
        SQLiteDatabase db = this.getReadableDatabase();
        int version=0;
        Cursor res =  db.rawQuery( "select version from notes where unit=\""+unit+"\"",
                null );
        res.moveToFirst();

        while (!res.isAfterLast()){
            version=res.getInt(res.getColumnIndex(NOTES_COLUMN_VERSION));
        }
        return version;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, NOTES_TABLE_NAME);
        return numRows;
    }

    public boolean updateNotes (String unit, int version, Blob content) {
        try {

            SQLiteDatabase db = this.getWritableDatabase();
            InputStream is = content.getBinaryStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream baf = new ByteArrayOutputStream();

            byte[] data = new byte[is.available()];
            int current;
            while ((current = bis.read(data, 0, data.length)) != -1) {
                baf.write(data, 0, current);
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put("unit", unit);
            contentValues.put("version",version);
            contentValues.put("content", baf.toByteArray());

            db.update("notes",
                    contentValues, "unit = ? ", new String[]{unit});

        }catch (SQLException s){
            Log.d("Update Table","Error :"+s.getMessage());
        } catch (IOException e) {
            Log.d("Update Table","Error :"+e.getMessage());
        }
        return true;
    }

    public Integer deleteNotes (String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("notes", "unit = ? ", new String[] { unit });
    }

    public ArrayList<String> getAllNotes() {
        ArrayList<String> array_list = new ArrayList<>();

        hp = new HashMap<String, byte[]>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select unit,content from notes", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(NOTES_COLUMN_UNIT)));
            hp.put(res.getString(res.getColumnIndex(NOTES_COLUMN_UNIT)),
                    res.getBlob(res.getColumnIndex(NOTES_COLUMN_CONTENTS)));
            res.moveToNext();
        }

        return array_list;
    }
}
