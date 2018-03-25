package com.intsig.yann.analysis;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yann_qiu on 2018/3/25.
 */

public class Util {

    public static final String TAG = "Util";
    public static final String FILE_PROVIDER_AUTHORITIES = ".fileprovider.provider";
    public static final String ANALYSIS_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/analysis/";
    public static final String ORIGINAL_IMG = ANALYSIS_DIR + "ori";
    public static final String TEMP_IMG = ANALYSIS_DIR + "temp";
    public static final String THUMB_IMG = ANALYSIS_DIR + "thumb";

    public static String parseDateString(long time) {
        Date d = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(d);
    }

    public static void safeCloseCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }

    }

    public static Bitmap loadBitmap(String path) {
        File file = new File(path);
        if (file.exists() && file.length() == 0) {
            file.delete();
        }
        Bitmap b = null;
        boolean oom = false;
        int max = 2;
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (options.inSampleSize < 1) {
            options.inSampleSize = 1;
        }
        do {
            oom = false;
            max--;
            try {
                b = BitmapFactory.decodeFile(path, options);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "loadBitmap Failed: " + e.getMessage());
                b = null;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                Log.e(TAG, "loadBitmap Failed: " + e.getMessage());
                if (max > 0) {
                    oom = true;
                    options.inSampleSize += 1;
                }
                b = null;
            }
        } while (oom);
        return b;
    }

    public static String getDateAsName() {
        Calendar c = Calendar.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append(c.get(Calendar.YEAR)).append("-").append(c.get(Calendar.MONTH) + 1).append("-")
                .append(c.get(Calendar.DAY_OF_MONTH)).append("-").append(c.get(Calendar.HOUR_OF_DAY)).append("-")
                .append(c.get(Calendar.MINUTE)).append("-").append(c.get(Calendar.SECOND)).append("-")
                .append(c.get(Calendar.MILLISECOND));
        return sb.toString();
    }

    /**
     * 根据uri获取图片路径
     *
     * @param context
     * @param imageUri
     * @return
     */
    public static String getPathFromUri(Context context, Uri imageUri) {
        String imageFilename = null;
        if (imageUri == null) {
            return null;
        }
        String scheme = imageUri.getScheme();
        if ("file".equals(scheme)) {
            imageFilename = imageUri.getPath();
        } else if ("content".equals(scheme)) {
            String path = imageUri.getPath();
            if (path.contains("http") && path.contains(".com")) {
                return null;
            } else if (path.contains(context.getApplicationContext().getPackageName() + FILE_PROVIDER_AUTHORITIES)) {
                imageFilename = imageUri.getPath();
            } else {
                Cursor cursor = context.getContentResolver().query(imageUri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
                if ( null != cursor ) {
                    if ( cursor.moveToFirst() ) {
                        int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                        if ( index > -1 ) {
                            imageFilename = cursor.getString( index );
                        }
                    }
                    cursor.close();
                }
                if (TextUtils.isEmpty(imageFilename) && !TextUtils.isEmpty(path)) {
                    imageFilename = path;
                }
            }
        }
        return imageFilename;
    }

    public static boolean copyFile(final String src, final String dst) {
        try {
            copyFile(new File(src), new File(dst));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }
}
