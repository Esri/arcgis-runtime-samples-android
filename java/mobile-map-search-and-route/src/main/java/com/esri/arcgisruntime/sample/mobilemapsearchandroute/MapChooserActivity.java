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

package com.esri.arcgisruntime.sample.mobilemapsearchandroute;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Activity for choosing which map in the mobile map package to display on the main activity
 * (MainActivity.java). The activity features a recycler view which lists a series of
 * MapPreviews.
 */
public class MapChooserActivity extends AppCompatActivity {
    private RecyclerView mMapPreviewRecyclerView;
    private MapPreviewAdapter mMapPreviewAdapter;
    private List<MapPreview> mMapPreviews;
    private String mMMPkTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_chooser);
        //get intent extras
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            mMapPreviews = (List<MapPreview>) intentExtras.get("map_previews");
            mMMPkTitle = (String) intentExtras.get("MMPk_title");
        }
        TextView nameMMPkView = findViewById(R.id.MMPk_title);
        nameMMPkView.setText(mMMPkTitle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setup recycler view
        mMapPreviewRecyclerView = findViewById(R.id.map_preview_list);
        mMapPreviewRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        loadMapPreviews();
    }

    /**
     * Create and set adapter
     */
    private void loadMapPreviews() {
        if (mMapPreviewAdapter == null) {
            mMapPreviewAdapter = new MapPreviewAdapter(mMapPreviews);
            mMapPreviewRecyclerView.setAdapter(mMapPreviewAdapter);
        } else {
            mMapPreviewAdapter.setMapPreviews(mMapPreviews);
            mMapPreviewAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Class which extends the RecyclerView holder
     */
    private class MapPreviewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private MapPreview mMapPreview;
        private final TextView mTitleTextView;
        private final TextView mTransportView;
        private final TextView mGeotaggingView;
        private final TextView mDescTextView;
        private final ImageView mThumbnailImageView;

        /**
         * Inflate views within holder
         */
        private MapPreviewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.map_preview, parent, false));
            itemView.setOnClickListener(this);
            mTitleTextView = itemView.findViewById(R.id.mapTitle);
            mTransportView = itemView.findViewById(R.id.mapTransportNetwork);
            mGeotaggingView = itemView.findViewById(R.id.mapGeotagging);
            mDescTextView = itemView.findViewById(R.id.mapDesc);
            mThumbnailImageView = itemView.findViewById(R.id.mapThumbnail);
        }

        /**
         * Bind information from a mapPreview to views
         * @param mapPreview model class which holds information about maps
         */
        private void bind(MapPreview mapPreview) {
            mMapPreview = mapPreview;
            mTitleTextView.setText(mapPreview.getTitle());
            if (mapPreview.hasTransportNetwork()) {
                mTransportView.setText(R.string.has_transport);
            } else {
                mTransportView.setText(R.string.no_transport);
            }
            if (mapPreview.hasGeocoding()) {
                mGeotaggingView.setText(R.string.has_geotag);
            } else {
                mGeotaggingView.setText(R.string.no_geotag);
            }

            mDescTextView.setText(mapPreview.getDesc());
            // decode thumbnail from byte stream to bitmap
            Bitmap thumbnail = BitmapFactory.decodeByteArray(
                    mapPreview.getThumbnailByteStream(),
                    0,
                    mapPreview.getThumbnailByteStream().length);
            mThumbnailImageView.setImageBitmap(thumbnail);
        }

        @Override
        public void onClick(View view) {
            final int MAP_CHOSEN_RESULT = 1;
            Intent mapChosenIntent = new Intent(getApplicationContext(),
                    MainActivity.class);
            //pass map number chosen back to mobile map view activity
            mapChosenIntent.putExtra("map_num", mMapPreview.getMapNum());
            setResult(MAP_CHOSEN_RESULT, mapChosenIntent);
            finish();
        }
    }

    /**
     * Class which extends RecyclerView adapter
     */
    private class MapPreviewAdapter extends RecyclerView.Adapter<MapPreviewHolder> {
        private List<MapPreview> mMapPreviews;

        private MapPreviewAdapter(List<MapPreview> mapPreviews) {
            mMapPreviews = mapPreviews;
        }

        @Override
        public MapPreviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            return new MapPreviewHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(MapPreviewHolder holder, int position) {
            MapPreview mapPreview = mMapPreviews.get(position);
            holder.bind(mapPreview);
        }

        @Override
        public int getItemCount() {
            return mMapPreviews.size();
        }

        private void setMapPreviews(List<MapPreview> mapPreviews) {
            mMapPreviews = mapPreviews;
        }
    }
}
