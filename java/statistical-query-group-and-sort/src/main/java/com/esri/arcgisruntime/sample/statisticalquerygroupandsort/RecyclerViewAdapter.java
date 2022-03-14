/* Copyright 2018 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.statisticalquerygroupandsort;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Flexible recycler view adapter which binds a field row either with or without a checkbox.
 */
class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int FIELD = 0;
  private static final int FIELDWITHCHECKBOX = 1;
  private final LayoutInflater mInflater;
  private final boolean mHasCheckbox;
  private final boolean[] mCheckedList;
  private final List<String> mFields;
  private int mSelectedPosition = 0;

  public RecyclerViewAdapter(Context context, List<String> fields, boolean hasCheckbox) {
    mInflater = LayoutInflater.from(context);
    mFields = fields;
    mHasCheckbox = hasCheckbox;
    mCheckedList = hasCheckbox ? new boolean[fields.size()] : null;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    RecyclerView.ViewHolder viewHolder;

    switch (viewType) {
      case FIELD:
        View view1 = mInflater.inflate(R.layout.field_row, parent, false);
        viewHolder = new ViewHolderField(view1);
        break;
      case FIELDWITHCHECKBOX:
        View view2 = mInflater.inflate(R.layout.field_checkbox_row, parent, false);
        viewHolder = new ViewHolderFieldCheckBox(view2);
        break;
      default:
        viewHolder = null;
        break;
    }
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    switch (holder.getItemViewType()) {
      case FIELD:
        ViewHolderField viewHolderField = (ViewHolderField) holder;
        viewHolderField.mRowTextView.setText(mFields.get(position));
        // give the selected row a gray background and make all others transparent
        holder.itemView.setBackgroundColor(mSelectedPosition == position ? Color.LTGRAY : Color.TRANSPARENT);
        break;
      case FIELDWITHCHECKBOX:
        ViewHolderFieldCheckBox viewHolderFieldCheckBox = (ViewHolderFieldCheckBox) holder;
        viewHolderFieldCheckBox.mRowTextView.setText(mFields.get(position));
        // prevent recycler view from occasionally resetting checkboxes
        viewHolderFieldCheckBox.mCheckBox.setOnCheckedChangeListener(null);
        // set checked status to known checked status
        viewHolderFieldCheckBox.mCheckBox.setChecked(mCheckedList[position]);
        // update checked array on check change
        viewHolderFieldCheckBox.mCheckBox.setOnCheckedChangeListener(
            (compoundButton, isChecked) -> mCheckedList[position] = !mCheckedList[position]);
        // give the selected row a gray background and make all others transparent
        holder.itemView.setBackgroundColor(mSelectedPosition == position ? Color.LTGRAY : Color.TRANSPARENT);
        break;
    }
  }

  @Override
  public int getItemCount() {
    return mFields.size();
  }

  @Override
  public int getItemViewType(int position) {
    int i = super.getItemViewType(position);
    return mHasCheckbox ? FIELDWITHCHECKBOX : FIELD;
  }

  public int getSelectedPosition() {
    return mSelectedPosition;
  }

  public String getItem(int id) {
    return mFields.get(id);
  }

  public boolean[] getCheckedList() {
    return mCheckedList;
  }

  class ViewHolderField extends RecyclerView.ViewHolder implements View.OnClickListener {
    final TextView mRowTextView;

    ViewHolderField(View itemView) {
      super(itemView);
      mRowTextView = itemView.findViewById(R.id.rowTextView);
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
      // notify change before and after selection so that both previous and current selection have their background
      // color changed
      notifyItemChanged(mSelectedPosition);
      mSelectedPosition = getAdapterPosition();
      notifyItemChanged(mSelectedPosition);
    }
  }

  class ViewHolderFieldCheckBox extends RecyclerView.ViewHolder implements View.OnClickListener {
    final TextView mRowTextView;
    final CheckBox mCheckBox;

    ViewHolderFieldCheckBox(View itemView) {
      super(itemView);
      mRowTextView = itemView.findViewById(R.id.rowTextView);
      mCheckBox = itemView.findViewById(R.id.rowCheckBox);
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
      // notify change before and after selection so that both previous and current selection have their background
      // color changed
      notifyItemChanged(mSelectedPosition);
      mSelectedPosition = getAdapterPosition();
      notifyItemChanged(mSelectedPosition);
    }
  }

}
