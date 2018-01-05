/* Copyright 2017 Esri
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.RecyclerViewHolder> {
  private ArrayList<PreplannedAreaPreview> preplannedAreaPreviews;
  public DrawerAdapter(ArrayList<PreplannedAreaPreview> preplannedAreaPreviews) {
    this.preplannedAreaPreviews = preplannedAreaPreviews;
  }
  @Override
  public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view;
    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_item, parent, false);
    return new RecyclerViewHolder(view);
  }
  @Override
  public void onBindViewHolder(RecyclerViewHolder holder, int position) {
    holder.title.setText(preplannedAreaPreviews.get(position).getTitle());
    byte[] byteStream = preplannedAreaPreviews.get(position).getThumbnailByteStream();
    Bitmap thumbnail = BitmapFactory.decodeByteArray(byteStream, 0, byteStream.length);
    holder.preview.setImageBitmap(thumbnail);
  }
  @Override
  public int getItemCount() {
    if (preplannedAreaPreviews != null) {
      return preplannedAreaPreviews.size();
    } else {
      return 0;
    }
  }
  public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
    TextView title;
    ImageView preview;
    public RecyclerViewHolder(View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.title);
      preview = itemView.findViewById(R.id.areaPreview);
    }
  }
}