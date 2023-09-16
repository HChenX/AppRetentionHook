package com.hchen.hook.base;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.hchen.hook.R;
import moralnorm.appcompat.widget.CheckedTextView;

public class MultipleChoiceAdapter extends RecyclerView.Adapter<MultipleChoiceAdapter.ViewHolder> {

    private List<String> mList;
    private SparseBooleanArray mIsChecked;
    private OnCurWillCheckAllChangedListener mListener;
    private boolean curWillCheckAll = true;

    public MultipleChoiceAdapter(List<String> list) {
        mList = list;
        mIsChecked = new SparseBooleanArray();
        initCheckedArray();
    }

    private void initCheckedArray() {
        for (int i = 0; i < mList.size(); i++) {
            mIsChecked.put(i, false);
        }
    }

    public interface OnCurWillCheckAllChangedListener {
        void onCurWillCheckAllChanged(boolean curWillCheckAll);
    }

    public void setOnCurWillCheckAllChangedListener(OnCurWillCheckAllChangedListener listener) {
        this.mListener = listener;
    }

    public boolean isCurWillCheckAll() {
        return curWillCheckAll;
    }

    public SparseBooleanArray getCheckedArray() {
        return mIsChecked;
    }

    public void setCheckedArray(SparseBooleanArray isChecked) {
        mIsChecked = isChecked;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mutiplechoice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CheckedTextView mCheckBoxTitle = holder.mCheckBoxTitle;
        mCheckBoxTitle.setText(mList.get(position));
        mCheckBoxTitle.setChecked(mIsChecked.get(position));
        holder.itemView.setOnClickListener(v -> {
            mCheckBoxTitle.toggle();
            mIsChecked.put(position, mCheckBoxTitle.isChecked());
            updateCurWillCheckAll();
            if (mListener != null) {
                mListener.onCurWillCheckAllChanged(curWillCheckAll);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CheckedTextView mCheckBoxTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            mCheckBoxTitle = itemView.findViewById(android.R.id.text1);
        }
    }

    private void updateCurWillCheckAll() {
        curWillCheckAll = true;
        for (int i = 0; i < mIsChecked.size(); i++) {
            if (mIsChecked.valueAt(i)) {
                curWillCheckAll = false;
                break;
            }
        }
    }
}
