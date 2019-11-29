/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.searchforwebmap;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esri.arcgisruntime.portal.PortalItem;

public class PortalItemAdapter extends RecyclerView.Adapter<PortalItemAdapter.PortalItemViewHolder> {

  public interface OnItemClickListener {
    void onItemClick(PortalItem portalItem);
  }

  private final List<PortalItem> mPortalItemList;
  private final OnItemClickListener mOnItemClickListener;

  static class PortalItemViewHolder extends RecyclerView.ViewHolder {

    final TextView mPortalItemTextView;

    PortalItemViewHolder(View itemView) {
      super(itemView);
      mPortalItemTextView = itemView.findViewById(R.id.webmapItem);
    }

    void bind(final PortalItem portalItem, final OnItemClickListener listener) {
      mPortalItemTextView.setText(portalItem.getTitle());
      itemView.setOnClickListener(v -> listener.onItemClick(portalItem));
    }
  }

  public PortalItemAdapter(List<PortalItem> portalItemNames, OnItemClickListener onItemClickListener) {
    mPortalItemList = portalItemNames;
    mOnItemClickListener = onItemClickListener;
  }

  @NonNull @Override
  public PortalItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View portalItemTextView = LayoutInflater.from(parent.getContext()).inflate(R.layout.webmap_row, parent, false);
    return new PortalItemViewHolder(portalItemTextView);
  }

  @Override
  public void onBindViewHolder(@NonNull PortalItemViewHolder holder, int position) {
    holder.bind(mPortalItemList.get(position), mOnItemClickListener);
  }

  @Override
  public int getItemCount() {
    return mPortalItemList.size();
  }
}
