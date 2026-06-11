package com.fasting.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 体重记录数据库辅助类
 */
public class WeightDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weight_records.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_WEIGHT = "weight_records";
    private static final String COL_ID = "id";
    private static final String COL_DATE = "record_date";
    private static final String COL_WEIGHT = "weight_kg";

    private static WeightDatabaseHelper instance;

    public static synchronized WeightDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new WeightDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private WeightDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_WEIGHT + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DATE + " TEXT NOT NULL UNIQUE, "
                + COL_WEIGHT + " REAL NOT NULL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT);
        onCreate(db);
    }

    /**
     * 体重数据模型
     */
    public static class WeightRecord {
        public long id;
        public String date;   // yyyy-MM-dd
        public float weight;  // kg

        public WeightRecord(long id, String date, float weight) {
            this.id = id;
            this.date = date;
            this.weight = weight;
        }
    }

    /**
     * 添加或更新体重记录（同一天只保留一条）
     */
    public void insertOrUpdateWeight(String date, float weight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_WEIGHT, weight);

        // 尝试更新已存在的记录
        int rows = db.update(TABLE_WEIGHT, values, COL_DATE + " = ?", new String[]{date});
        if (rows == 0) {
            // 不存在则插入
            db.insert(TABLE_WEIGHT, null, values);
        }
    }

    /**
     * 获取所有体重记录（按日期升序）
     */
    public List<WeightRecord> getAllRecords() {
        List<WeightRecord> records = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_WEIGHT,
                null,
                null,
                null,
                null,
                null,
                COL_DATE + " ASC"
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
            float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COL_WEIGHT));
            records.add(new WeightRecord(id, date, weight));
        }
        cursor.close();
        return records;
    }

    /**
     * 获取第一条体重记录（初始体重）
     */
    public WeightRecord getFirstRecord() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_WEIGHT,
                null,
                null,
                null,
                null,
                null,
                COL_DATE + " ASC",
                "1"
        );

        WeightRecord record = null;
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
            float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COL_WEIGHT));
            record = new WeightRecord(id, date, weight);
        }
        cursor.close();
        return record;
    }

    /**
     * 删除指定日期的记录
     */
    public void deleteRecord(String date) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_WEIGHT, COL_DATE + " = ?", new String[]{date});
    }
}
