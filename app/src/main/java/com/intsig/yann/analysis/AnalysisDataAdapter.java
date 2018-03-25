package com.intsig.yann.analysis;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by yann_qiu on 2018/3/25.
 */

public class AnalysisDataAdapter extends RecyclerView.Adapter<AnalysisDataViewHolder> {

    private LayoutInflater layoutInflater;
    private Cursor cursor;

    public AnalysisDataAdapter(LayoutInflater layoutInflater, Cursor cursor) {
        this.layoutInflater = layoutInflater;
        this.cursor = cursor;
    }

    @Override
    public AnalysisDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AnalysisDataViewHolder(layoutInflater.inflate(R.layout.analysis_viewholder, parent, false));
    }

    @Override
    public void onBindViewHolder(AnalysisDataViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            holder.initUi(cursor);
        }
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public Cursor swapCursor(Cursor cursor) {
        Cursor cursorTemp = this.cursor;
        this.cursor = cursor;
        notifyDataSetChanged();
        return cursorTemp;
    }
}
