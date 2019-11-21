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

package com.esri.arcgisruntime.sample.readsymbolsmobilestylefile;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.symbology.SymbolStyleSearchResult;

/**
 * {@link RecyclerView.Adapter} subclass that displays symbols
 */
class SymbolAdapter extends RecyclerView.Adapter<SymbolAdapter.ViewHolder> {

  private static final String TAG = SymbolAdapter.class.getSimpleName();

  private final ArrayList<SymbolStyleSearchResult> mSymbols = new ArrayList<>();
  private final OnSymbolPreviewTapListener mOnSymbolPreviewTapListener;

  public SymbolAdapter(OnSymbolPreviewTapListener onSymbolPreviewTapListener) {
    mOnSymbolPreviewTapListener = onSymbolPreviewTapListener;
  }

  @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    return new ViewHolder(
        LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_symbol_adapter_item, viewGroup, false));
  }

  @Override public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
    viewHolder.bind(mSymbols.get(i), mOnSymbolPreviewTapListener);
  }

  @Override public int getItemCount() {
    return mSymbols.size();
  }

  void addSymbol(SymbolStyleSearchResult symbol) {
    mSymbols.add(symbol);
    notifyItemInserted(mSymbols.size() - 1);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    private final ImageView mImageView;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      mImageView = itemView.findViewById(R.id.imageView);
    }

    private void bind(SymbolStyleSearchResult symbol, OnSymbolPreviewTapListener onSymbolPreviewTapListener) {
      // get the Future to create the swatch of the multi layer symbol
      ListenableFuture<Bitmap> bitmapFuture = symbol.getSymbol()
          .createSwatchAsync(itemView.getContext(), Color.TRANSPARENT);
      bitmapFuture.addDoneListener(() -> {
        try {
          // wait for the Future to complete and get the result
          Bitmap bitmap = bitmapFuture.get();
          mImageView.setImageBitmap(bitmap);
        } catch (InterruptedException | ExecutionException e) {
          Log.e(TAG, itemView.getContext().getString(R.string.error_loading_symbol_bitmap_failed, e.getMessage()));
          Toast.makeText(itemView.getContext(),
              itemView.getContext().getString(R.string.error_loading_symbol_bitmap_failed, e.getMessage()),
              Toast.LENGTH_LONG).show();
        }
        itemView.setOnClickListener(v -> onSymbolPreviewTapListener.onSymbolPreviewTap(symbol));
      });
    }
  }
}
