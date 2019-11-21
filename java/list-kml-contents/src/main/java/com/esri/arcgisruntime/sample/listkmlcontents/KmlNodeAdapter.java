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

package com.esri.arcgisruntime.sample.listkmlcontents;

import java.util.List;

import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class KmlNodeAdapter extends RecyclerView.Adapter<KmlNodeAdapter.KmlNodeViewHolder> {
  private final List<String> mNodeNames;
  private final List<BitmapDrawable> mKmlUxIcons;
  private final OnItemClickListener mOnItemClickListener;

  static class KmlNodeViewHolder extends RecyclerView.ViewHolder {
    final TextView textView;
    final ImageView imageView;
    KmlNodeViewHolder(View itemView) {
      super(itemView);
      itemView.setClickable(true);
      textView = itemView.findViewById(R.id.nodeRowTextView);
      imageView = itemView.findViewById(R.id.nodeRowImageView);
    }
  }

  public interface OnItemClickListener {
    void onItemClick(int position);
  }

  public KmlNodeAdapter(List<String> nodeNames, List<BitmapDrawable> kmlUxIcons, OnItemClickListener onItemClickListener) {
    mNodeNames = nodeNames;
    mKmlUxIcons = kmlUxIcons;
    mOnItemClickListener = onItemClickListener;
  }

  @NonNull @Override
  public KmlNodeAdapter.KmlNodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.node_row, parent, false);
    KmlNodeViewHolder kmlNodeViewHolder = new KmlNodeViewHolder(view);
    kmlNodeViewHolder.setIsRecyclable(false);
    return kmlNodeViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull KmlNodeViewHolder holder, int position) {
    holder.textView.setText(mNodeNames.get(position));
    if (position < mKmlUxIcons.size() && mKmlUxIcons.get(position) != null) {
      holder.imageView.setImageDrawable(mKmlUxIcons.get(position));
    }
    holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(position));
  }

  @Override
  public int getItemCount() {
    return mNodeNames.size();
  }
}
