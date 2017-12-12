package com.esri.arcgisruntime.sample.statisticalquery;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class RecyclerViewAdapterCheckBox extends RecyclerView.Adapter<RecyclerViewAdapterCheckBox.ViewHolder> {

  private final LayoutInflater mInflater;
  private List<String> mData = Collections.emptyList();
  private final boolean[] mCheckedList;
  private ItemClickListener mClickListener;
  private int mSelectedPosition = 0;

  public RecyclerViewAdapterCheckBox(Context context, List<String> data) {
    this.mInflater = LayoutInflater.from(context);
    this.mData = data;
    mCheckedList = new boolean[data.size()];
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = mInflater.inflate(R.layout.recyclerview_checkbox_row, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    String text = mData.get(position);
    holder.mRowTextView.setText(text);

    //in some cases, it will prevent unwanted situations
    holder.mCheckBox.setOnCheckedChangeListener(null);

    // set checked status to known checked status
    holder.mCheckBox.setChecked(mCheckedList[position]);

    // update checked array on check change
    holder.mCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> mCheckedList[position] = !mCheckedList[position]);

    // give the selected row a gray background and make all others transparent
    holder.itemView.setBackgroundColor(mSelectedPosition == position ? Color.LTGRAY : Color.TRANSPARENT);
  }

  @Override
  public int getItemCount() {
    return mData.size();
  }

  public int getSelectedPosition() {
    return mSelectedPosition;
  }

  public boolean[] getCheckedList() {
    return mCheckedList;
  }

  // convenience method for getting data at click position
  public String getItem(int id) {
    return mData.get(id);
  }

  // allows clicks events to be caught
  public void setClickListener(ItemClickListener itemClickListener) {
    this.mClickListener = itemClickListener;
  }

  // parent activity will implement this method to respond to click events
  public interface ItemClickListener {
    void onItemClick(View view, int position);
  }

  public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public final TextView mRowTextView;
    public final CheckBox mCheckBox;

    public ViewHolder(View itemView) {
      super(itemView);
      mRowTextView = itemView.findViewById(R.id.rowTextView);
      mCheckBox = itemView.findViewById(R.id.rowCheckBox);
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
      if (mClickListener != null) {
        mClickListener.onItemClick(view, getAdapterPosition());
      }
      notifyItemChanged(mSelectedPosition);
      mSelectedPosition = getAdapterPosition();
      notifyItemChanged(mSelectedPosition);
    }
  }
}
