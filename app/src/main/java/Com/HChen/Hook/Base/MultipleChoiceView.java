package com.hchen.hook.base;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.hchen.hook.R;

public class MultipleChoiceView extends LinearLayout implements MultipleChoiceAdapter.OnCurWillCheckAllChangedListener {

    private MultipleChoiceAdapter mAdapter;
    private List<String> mData;
    private RecyclerView mListView;
    private Button mAllSelectBtn;
    private Button mOkBtn;
    private OnCheckedListener mOnCheckedListener;
    private boolean curWillCheckAll = true;

    public MultipleChoiceView(Context context) {
        super(context);
        initView(context);
    }

    public MultipleChoiceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.view_multiple_choice, this);
        mListView = view.findViewById(R.id.list);
        mListView.setLayoutManager(new LinearLayoutManager(context));
        mListView.setHasFixedSize(true);
        mAllSelectBtn = view.findViewById(R.id.button2);
        mOkBtn = view.findViewById(R.id.button1);
        mAllSelectBtn.setText(curWillCheckAll ? getResources().getString(R.string.all) : getResources().getString(R.string.lla));

        OnCustomMultipleChoiceCheckedListener onCheckedListener = new OnCustomMultipleChoiceCheckedListener();
        mAllSelectBtn.setOnClickListener(onCheckedListener);
        mOkBtn.setOnClickListener(onCheckedListener);
    }

    public void setData(List<String> data, boolean[] isSelected) {
        if (data != null) {
            mData = data;
            mAdapter = new MultipleChoiceAdapter(data);
            mAdapter.setOnCurWillCheckAllChangedListener(this);

            if (isSelected != null) {
                if (isSelected.length != data.size()) {
                    throw new IllegalArgumentException("data's length not equal the isSelected's length");
                } else {
                    for (int i = 0; i < isSelected.length; i++) {
                        mAdapter.getCheckedArray().put(i, isSelected[i]);
                    }
                }
            }

            mListView.setAdapter(mAdapter);
        } else {
            throw new IllegalArgumentException("data is null");
        }
    }

    public void setOnCheckedListener(OnCheckedListener listener) {
        mOnCheckedListener = listener;
    }

    @Override
    public void onCurWillCheckAllChanged(boolean curWillCheckAll) {
        this.curWillCheckAll = curWillCheckAll;
        mAllSelectBtn.setText(curWillCheckAll ? getResources().getString(R.string.all) : getResources().getString(R.string.lla));
    }

    public interface OnCheckedListener {
        void onChecked(SparseBooleanArray sparseBooleanArray);
    }

    public void selectAll() {
        if (mData != null) {
            for (int i = 0; i < mData.size(); i++) {
                mAdapter.getCheckedArray().put(i, true);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public void deselectAll() {
        if (mData != null) {
            for (int i = 0; i < mData.size(); i++) {
                mAdapter.getCheckedArray().put(i, false);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public void reverseSelect() {
        if (mData != null) {
            for (int i = 0; i < mData.size(); i++) {
                mAdapter.getCheckedArray().put(i, !mAdapter.getCheckedArray().get(i));
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private class OnCustomMultipleChoiceCheckedListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button1:
                    if (mOnCheckedListener != null && mAdapter != null) {
                        mOnCheckedListener.onChecked(mAdapter.getCheckedArray());
                    }
                    break;
                case R.id.button2:
                    if (mData != null) {
                        if (curWillCheckAll) {
                            selectAll();
                        } else {
                            reverseSelect();
                        }
                        mAllSelectBtn.setText(curWillCheckAll ? getResources().getString(R.string.all) : getResources().getString(R.string.lla));
                        curWillCheckAll = !curWillCheckAll;
                    }
                    break;
            }
        }
    }
}
