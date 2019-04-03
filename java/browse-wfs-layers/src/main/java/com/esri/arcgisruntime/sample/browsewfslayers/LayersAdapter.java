/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.browsewfslayers;

import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esri.arcgisruntime.ogc.wfs.WfsLayerInfo;

/**
 * An adapter that displays {@link WfsLayerInfo}s
 */
public class LayersAdapter extends RecyclerView.Adapter<LayersAdapter.ViewHolder> implements OnItemSelectedListener {

  private final OnItemSelectedListener mOnItemSelectedListener;

  private List<WfsLayerInfo> mLayers = new ArrayList<>();
  private WfsLayerInfo mSelectedLayer;

  LayersAdapter(OnItemSelectedListener onItemSelectedListener) {
    mOnItemSelectedListener = onItemSelectedListener;
  }

  @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    // inflate the layout for a Layer
    return new ViewHolder(
        LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_item_layer, viewGroup, false));
  }

  @Override public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
    viewHolder.bind(mLayers.get(i), mSelectedLayer == mLayers.get(i), this);
  }

  @Override public int getItemCount() {
    return mLayers.size();
  }

  /**
   * Add a {@link WfsLayerInfo} to the adapter
   *
   * @param layer
   */
  void addLayer(WfsLayerInfo layer) {
    if (!mLayers.contains(layer)) {
      mLayers.add(layer);
      notifyItemInserted(mLayers.size() - 1);
    }
  }

  @Override public void onItemSelected(WfsLayerInfo layer) {
    int previousSelectedIndex = mLayers.indexOf(mSelectedLayer);
    mSelectedLayer = layer;
    notifyItemChanged(previousSelectedIndex);
    notifyItemChanged(mLayers.indexOf(layer));
    if (mOnItemSelectedListener != null) {
      mOnItemSelectedListener.onItemSelected(layer);
    }
  }

  /**
   * Subclass of {@link ViewHolder} to display {@link View}s related to a {@link WfsLayerInfo}
   */
  class ViewHolder extends RecyclerView.ViewHolder {

    private TextView mTextView;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      mTextView = itemView.findViewById(R.id.layerNameTextView);
    }

    void bind(WfsLayerInfo layer, boolean selected, OnItemSelectedListener onItemSelectedListener) {
      mTextView.setText(layer.getTitle());

      itemView.setBackgroundColor(selected ?
          itemView.getResources().getColor(R.color.colorPrimaryDark) :
          itemView.getResources().getColor(R.color.colorPrimary));

      itemView.setOnClickListener(v -> onItemSelectedListener.onItemSelected(layer));
    }
  }
}

interface OnItemSelectedListener {
  void onItemSelected(WfsLayerInfo layer);
}