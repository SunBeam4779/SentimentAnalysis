package com.intsig.yann.analysis;

import android.app.Activity;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by yann_qiu on 2018/3/25.
 */

public class AnalysisDataViewHolder extends RecyclerView.ViewHolder {

    private ImageView smallImgImageView;
    private TextView statusDetailTextView;
    private TextView photoDateTextView;
    private long id;

    public AnalysisDataViewHolder(final View itemView) {
        super(itemView);
        smallImgImageView = (ImageView) itemView.findViewById(R.id.small_img_ImageView);
        statusDetailTextView = (TextView) itemView.findViewById(R.id.status_detail_TextView);
        photoDateTextView = (TextView) itemView.findViewById(R.id.photo_date_TextView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalysisDetailActivity.startActivity((Activity) itemView.getContext(), id,
                        ((AnalysisHolderActivity)itemView.getContext()).getAccountId());
            }
        });
    }

    public void initUi(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndex(AnalysisData._ID));
        String smallImg = cursor.getString(cursor.getColumnIndex(AnalysisData.SMALL_IMG));
        long date = cursor.getLong(cursor.getColumnIndex(AnalysisData.CREATE_DATE));
        String status = cursor.getString(cursor.getColumnIndex(AnalysisData.FATIGUE));
        smallImgImageView.setImageBitmap(Util.loadBitmap(smallImg));
        statusDetailTextView.setText(status);
        photoDateTextView.setText(itemView.getResources().getString(R.string.photo_data, Util.parseDateString(date)));
    }

}
