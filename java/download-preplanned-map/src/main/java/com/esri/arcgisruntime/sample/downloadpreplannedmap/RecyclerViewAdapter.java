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

package com.esri.arcgisruntime.sample.downloadpreplannedmap;

import java.util.ArrayList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.PreviewAreaHolder> {

  private final ArrayList<PreplannedAreaPreview> preplannedAreaPreviews;

  private OnAreaClicked onAreaClicked;

  public RecyclerViewAdapter(ArrayList<PreplannedAreaPreview> preplannedAreaPreviews) {
    this.preplannedAreaPreviews = preplannedAreaPreviews;
  }

  @Override
  public PreviewAreaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view;
    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_item, parent, false);
    return new PreviewAreaHolder(view);
  }

  @Override
  public void onBindViewHolder(PreviewAreaHolder holder, int position) {
    holder.setIsRecyclable(false);
    holder.itemView.setOnClickListener(v -> onAreaClicked.onAreaClick(position));
    holder.title.setText(preplannedAreaPreviews.get(position).getTitle());
    holder.preview.setImageBitmap(preplannedAreaPreviews.get(position).getBitmapThumbnail());
  }

  @Override
  public int getItemCount() {
    if (preplannedAreaPreviews != null) {
      return preplannedAreaPreviews.size();
    } else {
      return 0;
    }
  }

  public void setOnAreaClicked(OnAreaClicked onAreaClicked) {
    this.onAreaClicked = onAreaClicked;
  }

  public interface OnAreaClicked {
    void onAreaClick(int position);
  }

  public static class PreviewAreaHolder extends RecyclerView.ViewHolder {
    final TextView title;
    final ImageView preview;

    public PreviewAreaHolder(View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.sectorTextView);
      preview = itemView.findViewById(R.id.areaPreview);
    }
  }
}