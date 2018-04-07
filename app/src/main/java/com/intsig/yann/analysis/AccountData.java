package com.intsig.yann.analysis;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by wo on 2018/4/7.
 */

public class AccountData implements BaseColumns {

    public static String TABLE_NAME = "account_data";
    public static String SMALL_IMG = "small_img";
    public static String BIG_IMG = "big_img";
    public static String CREATE_DATE = "create_date";
    public static String ACCOUNT_NAME = "account_name";
    public static String TABLE_PATH = "account_data";
    public static String TABLE_PATH_WITH_NAME = "account_data/#";
    public static Uri CONTENT_URI = Uri.parse("content://" + AnalysisDataProvider.AUTHORITY + "/" + TABLE_PATH);
    public static Uri CONTENT_URI_NAME = Uri.parse("content://" + AnalysisDataProvider.AUTHORITY + "/" + TABLE_PATH_WITH_NAME);
}
