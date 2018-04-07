package com.intsig.yann.analysis;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yann_qiu on 2018/3/24.
 */

public class AnalysisData implements BaseColumns {

    public static String TABLE_NAME = "analysis_data";
    public static String SMALL_IMG = "small_img";
    public static String BIG_IMG = "big_img";
    public static String CREATE_DATE = "create_date";
    public static String FATIGUE = "fatigue";
    public static String ACCOUNT_ID = "account_id";
    public static String TABLE_PATH = "analysis_data";
    public static String TABLE_PATH_WITH_PARAM = "analysis_data/#";
    public static String TABLE_PATH_WITH_ACCOUNT = "analysis_data/account/#";
    public static Uri CONTENT_URI = Uri.parse("content://" + AnalysisDataProvider.AUTHORITY + "/" + TABLE_PATH);
    public static Uri CONTENT_URI_ID = Uri.parse("content://" + AnalysisDataProvider.AUTHORITY + "/" + TABLE_PATH_WITH_PARAM);
    public static Uri CONTENT_URI_ACCOUNT_ID = Uri.parse("content://" + AnalysisDataProvider.AUTHORITY + "/" + TABLE_PATH_WITH_ACCOUNT);

}
