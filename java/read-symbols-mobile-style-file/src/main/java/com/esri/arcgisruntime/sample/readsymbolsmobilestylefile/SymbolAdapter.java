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

import android.content.Context;
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
import com.esri.arcgisruntime.symbology.Symbol;
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_symbol_adapter_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(mSymbols.get(i), mOnSymbolPreviewTapListener);
    }

    @Override
    public int getItemCount() {
        return mSymbols.size();
    }

    void addSymbol(SymbolStyleSearchResult symbol) {
        mSymbols.add(symbol);
        notifyItemInserted(mSymbols.size() - 1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mImageView;
        private Context mContext;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.imageView);
            mContext = itemView.getContext();
        }

        private void bind(SymbolStyleSearchResult symbolStyleSearchResult, OnSymbolPreviewTapListener onSymbolPreviewTapListener) {
            // get the Future to create the swatch of the multi layer symbol
            ListenableFuture<Symbol> symbolFuture = symbolStyleSearchResult.getSymbolAsync();
            symbolFuture.addDoneListener(() -> {
                try {
                    // get a reference to the symbol created
                    Symbol symbol = symbolFuture.get();
                    // create a bitmap swatch from the symbol
                    ListenableFuture<Bitmap> bitmapFuture = symbol.createSwatchAsync(itemView.getContext(), Color.TRANSPARENT);
                    bitmapFuture.addDoneListener(() -> {
                        try {
                            // get a reference to the bitmap
                            Bitmap bitmap = bitmapFuture.get();
                            // set the bitmap to the image view
                            mImageView.setImageBitmap(bitmap);
                            itemView.setOnClickListener(v -> onSymbolPreviewTapListener.onSymbolPreviewTap(symbolStyleSearchResult));
                        } catch (Exception e) {
                            Log.e(TAG, mContext.getString(R.string.error_loading_symbol_bitmap_failed, e.getMessage()));
                            Toast.makeText(mContext, mContext.getString(R.string.error_loading_symbol_bitmap_failed, e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, mContext.getString(R.string.error_loading_multilayer_symbol_failed, e.getMessage()));
                    Toast.makeText(mContext, mContext.getString(R.string.error_loading_multilayer_symbol_failed, e.getMessage()), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
