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

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esri.arcgisruntime.ogc.wfs.WfsLayerInfo;

/**
 * An adapter that displays {@link WfsLayerInfo}s
 */
public class WfsLayerInfoAdapter extends RecyclerView.Adapter<WfsLayerInfoAdapter.ViewHolder>
    implements OnItemSelectedListener {

  private final OnItemSelectedListener mOnItemSelectedListener;

  private final List<WfsLayerInfo> mWfsLayerInfos = new ArrayList<>();
  private WfsLayerInfo mSelectedWfsLayerInfo;

  WfsLayerInfoAdapter(OnItemSelectedListener onItemSelectedListener) {
    mOnItemSelectedListener = onItemSelectedListener;
  }

  @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    // inflate the layout for a WfsLayerInfo
    return new ViewHolder(
        LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_item_layer, viewGroup, false));
  }

  @Override public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
    viewHolder.bind(mWfsLayerInfos.get(i), mSelectedWfsLayerInfo == mWfsLayerInfos.get(i), this);
  }

  @Override public int getItemCount() {
    return mWfsLayerInfos.size();
  }

  /**
   * Add a {@link WfsLayerInfo} to the adapter
   *
   * @param wfsLayerInfo to display
   */
  void addLayer(WfsLayerInfo wfsLayerInfo) {
    if (!mWfsLayerInfos.contains(wfsLayerInfo)) {
      mWfsLayerInfos.add(wfsLayerInfo);
      notifyItemInserted(mWfsLayerInfos.size() - 1);
    }
  }

  @Override public void onItemSelected(WfsLayerInfo wfsLayerInfo) {
    int previousSelectedIndex = mWfsLayerInfos.indexOf(mSelectedWfsLayerInfo);
    mSelectedWfsLayerInfo = wfsLayerInfo;
    notifyItemChanged(previousSelectedIndex);
    notifyItemChanged(mWfsLayerInfos.indexOf(wfsLayerInfo));
    if (mOnItemSelectedListener != null) {
      mOnItemSelectedListener.onItemSelected(wfsLayerInfo);
    }
  }

  /**
   * Subclass of {@link ViewHolder} to display {@link View}s related to a {@link WfsLayerInfo}
   */
  class ViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTextView;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      mTextView = itemView.findViewById(R.id.layerNameTextView);
    }

    void bind(WfsLayerInfo wfsLayerInfo, boolean selected, OnItemSelectedListener onItemSelectedListener) {
      mTextView.setText(wfsLayerInfo.getTitle());

      itemView.setBackgroundColor(selected ?
          itemView.getContext().getColor(R.color.adapter_item_Selected_bg) :
          Color.WHITE
      );

      itemView.setOnClickListener(v -> {
        if (wfsLayerInfo != mSelectedWfsLayerInfo) {
          onItemSelectedListener.onItemSelected(wfsLayerInfo);
        }
      });
    }
  }
}

interface OnItemSelectedListener {
  void onItemSelected(WfsLayerInfo wfsLayerInfo);
}
