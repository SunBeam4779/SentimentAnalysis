package com.intsig.yann.analysis;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yann_qiu on 2018/3/24.
 */

public class AnalysisDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "analysis.db";
    public static final int DATABASE_VERSION1 = 1;
    private static final int DATABASE_VERSION = DATABASE_VERSION1;
    private static AnalysisDatabaseHelper instance = null;

    private AnalysisDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + AnalysisData.TABLE_NAME + " (" + AnalysisData._ID + " INTEGER PRIMARY KEY,"
                + AnalysisData.SMALL_IMG + " TEXT," + AnalysisData.BIG_IMG + " TEXT," + AnalysisData.CREATE_DATE
                + " INTEGER DEFAULT 0," + AnalysisData.FATIGUE + " TEXT" + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS analysis_index_small_img" + " ON " + AnalysisData.TABLE_NAME + " ( "
                + AnalysisData.SMALL_IMG + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS analysis_index_big_img" + " ON " + AnalysisData.TABLE_NAME + " ( "
                + AnalysisData.BIG_IMG + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS analysis_index_create_date" + " ON " + AnalysisData.TABLE_NAME + " ( "
                + AnalysisData.CREATE_DATE + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS analysis_index_fatigue" + " ON " + AnalysisData.TABLE_NAME + " ( "
                + AnalysisData.FATIGUE + ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static AnalysisDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (AnalysisDatabaseHelper.class) {
                if (instance == null) {
                    instance = new AnalysisDatabaseHelper(context);
                }
                return instance;
            }
        }
        return instance;

    }
}
