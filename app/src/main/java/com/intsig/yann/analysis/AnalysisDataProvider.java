package com.intsig.yann.analysis;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by yann_qiu on 2018/3/24.
 */

public class AnalysisDataProvider extends ContentProvider {

    public static String AUTHORITY = "com.intsig.yann.analysis.provider";

    private AnalysisDatabaseHelper openHelper;
    private static final UriMatcher sUriMatcher;
    private static final int ANALYSIS = 1;
    private static final int ANALYSIS_ID = 2;
    public static final int MSG_ANALYSIS = 0;
    private static final int NOTIFY_INTERAL = 3000;
    private long lastAnalysisNotifyTime = 0;
    private static Handler handler;
    static HandlerThread thread;
    class NotifyHandler extends Handler {

        public NotifyHandler(Looper looper) {
            super(looper);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            long time = System.currentTimeMillis();
            switch (what) {
                case MSG_ANALYSIS:
                    getContext().getContentResolver().notifyChange(AnalysisData.CONTENT_URI, null);
                    lastAnalysisNotifyTime = time;
                    break;
                default:
                    break;
            }
        }
    }
    //
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, AnalysisData.TABLE_PATH, ANALYSIS);
        sUriMatcher.addURI(AUTHORITY, AnalysisData.TABLE_PATH_WITH_PARAM, ANALYSIS_ID);
    }

    @Override
    public boolean onCreate() {
        openHelper = AnalysisDatabaseHelper.getInstance(getContext());
        thread = new HandlerThread("AnalysisDataProviderNotify");
        thread.setPriority(Thread.NORM_PRIORITY - 2);
        thread.start();
        handler = new NotifyHandler(thread.getLooper());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = sUriMatcher.match(uri);
        GetTableAndWhereOutParameter getTableAndWhereOutParameter = new GetTableAndWhereOutParameter();
        getTableAndWhere(uri, match, selection, getTableAndWhereOutParameter);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        Cursor cursor = db.query(getTableAndWhereOutParameter.table, projection, getTableAndWhereOutParameter.where, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private void getTableAndWhere(Uri uri, int match, String userWhere, GetTableAndWhereOutParameter out) {
        switch (match) {
            case ANALYSIS:
                out.table = AnalysisData.TABLE_NAME;
                out.where = userWhere;
                break;
            case ANALYSIS_ID:
                out.table = AnalysisData.TABLE_NAME;
                out.where = TextUtils.isEmpty(userWhere) ? AnalysisData._ID + " == " + uri.getLastPathSegment() :
                        AnalysisData._ID + " == " + uri.getLastPathSegment() + " AND " + userWhere;
                break;
            default:
                throw new IllegalStateException("Unknown URL" + uri);
        }


    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        ContentValues initialValues;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        if (values != null) {
            initialValues = new ContentValues(values);
        } else {
            initialValues = new ContentValues();
        }
        Uri newUri = null;
        switch (sUriMatcher.match(uri)) {
            case ANALYSIS:
            case ANALYSIS_ID: {
                if (!initialValues.containsKey(AnalysisData.CREATE_DATE)) {
                    initialValues.put(AnalysisData.CREATE_DATE, System.currentTimeMillis());
                }
                long rowId = db.insert(AnalysisData.TABLE_NAME, null, initialValues);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(AnalysisData.CONTENT_URI, rowId);
                }
                break;
            }
            default:
                throw new IllegalStateException("Unknown URL" + uri);

        }
        notifyUri(uri);
        return newUri;
    }

    private void notifyUri(Uri uri) {
        long thisTime = System.currentTimeMillis();
        int msg = -1;
        long lastNotifyTime = -1;
        if (uri.toString().contains(AnalysisData.CONTENT_URI.toString())) {// notify group
            msg = MSG_ANALYSIS;
            lastNotifyTime = lastAnalysisNotifyTime;
        }
        if (msg > 0) {
            if (thisTime - lastNotifyTime < NOTIFY_INTERAL) {
                if (!handler.hasMessages(msg)) {
                    handler.sendEmptyMessageDelayed(msg, NOTIFY_INTERAL);
                }
            } else {
                if (handler.hasMessages(msg)) {
                    handler.removeMessages(msg);
                    handler.sendEmptyMessage(msg);
                } else {
                    handler.sendEmptyMessage(msg);
                }
            }
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        GetTableAndWhereOutParameter gtw = new GetTableAndWhereOutParameter();
        SQLiteDatabase db = openHelper.getWritableDatabase();
        getTableAndWhere(uri, sUriMatcher.match(uri), selection, gtw);
        int count = db.delete(gtw.table, gtw.where, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            notifyUri(uri);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        GetTableAndWhereOutParameter gtw = new GetTableAndWhereOutParameter();
        getTableAndWhere(uri, sUriMatcher.match(uri), selection, gtw);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        int count = db.update(gtw.table, values, gtw.where, selectionArgs);
        if (count > 0) {
            notifyUri(uri);
        }
        return count;
    }

    private static class GetTableAndWhereOutParameter {
        public String table;
        public String where;
    }
}
