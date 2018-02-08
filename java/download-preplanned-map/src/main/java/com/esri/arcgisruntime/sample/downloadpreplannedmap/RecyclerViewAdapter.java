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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.PreviewAreaHolder> {

  private ArrayList<PreplannedAreaPreview> preplannedAreaPreviews;

  private OnItemClicked onClick;

  public interface OnItemClicked {
    void onItemClick(int position);
  }

  public RecyclerViewAdapter(ArrayList<PreplannedAreaPreview> preplannedAreaPreviews) {
    this.preplannedAreaPreviews = preplannedAreaPreviews;
    Log.d("recyclerAdapter", String.valueOf(this.preplannedAreaPreviews.size()));
  }
  @Override
  public PreviewAreaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view;
    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_item, parent, false);
    return new PreviewAreaHolder(view);
  }
  @Override
  public void onBindViewHolder(PreviewAreaHolder holder, int position) {
    holder.itemView.setOnClickListener(v -> onClick.onItemClick(position));
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
  public static class PreviewAreaHolder extends RecyclerView.ViewHolder {
    TextView title;
    ImageView preview;
    public PreviewAreaHolder(View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.sectorTextView);
      preview = itemView.findViewById(R.id.areaPreview);
    }
  }

  public void setOnClick(OnItemClicked onClick)
  {
    this.onClick=onClick;
  }
}