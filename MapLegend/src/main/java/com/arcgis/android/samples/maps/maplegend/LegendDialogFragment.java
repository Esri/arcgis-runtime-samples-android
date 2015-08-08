/* Copyright 2015 Esri
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

package com.arcgis.android.samples.maps.maplegend;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.core.map.Legend;

import java.util.Collections;
import java.util.List;

/**
 * A dialog that shows the legend of a ArcGISDynamicMapServiceLayer.
 */
public class LegendDialogFragment extends DialogFragment {

    public static final String TAG = LegendDialogFragment.class.getSimpleName();
    LinearLayout mLinearLayout;
    private ArcGISDynamicMapServiceLayer mLayer;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLinearLayout = (LinearLayout) inflater.inflate(R.layout.legend_dialog_fragment_layout, null);

        getDialog().setTitle(getActivity().getString(R.string.legend));

        mLayer = ((MainActivity) getActivity()).getLayer();

        // before we can show the legend we have to fetch the legend info asynchronously
        new FetchLegendTask().execute();

        return mLinearLayout;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof MainActivity)) {
            throw new IllegalStateException("Hosting activity needs to be of type MainActivity");
        }
    }

    /**
     * Populates the list of legend items for each sub-layer of an ArcGISDynamicMapServiceLayer.
     */
    private class LayerLegendAdapter extends BaseAdapter {

        private final List<Legend> mLegends;

        public LayerLegendAdapter(List<Legend> legends) {
            mLegends = legends != null ? legends : Collections.EMPTY_LIST;
        }

        public int getCount() {
            return mLegends.size();
        }

        public Object getItem(int position) {
            return mLegends.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.layer_legend_item_layout, null);
            }

            Legend legend = mLegends.get(position);
            TextView textView = (TextView) view;
            textView.setText(legend.getLabel());

            Bitmap bitmap = legend.getImage();
            BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            textView.setCompoundDrawables(drawable, null, null, null);

            return view;
        }
    }

    /**
     * Retrieves the legend information asynchronously from the ArcGISDynamicMapServiceLayer.
     */
    private class FetchLegendTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mLayer.retrieveLegendInfo();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            for (ArcGISLayerInfo layerInfo : mLayer.getLayers()) {
                View view = getActivity().getLayoutInflater().inflate(R.layout.layer_legend_layout, null);
                populateLegendView(view, layerInfo);

                mLinearLayout.addView(view);
            }
        }

        private View populateLegendView(View view, ArcGISLayerInfo layerInfo) {
            if (layerInfo != null) {
                TextView textView = (TextView) view.findViewById(R.id.layer_legend_title_textview);
                ListView listView = (ListView) view.findViewById(R.id.layer_legend_symbols_listview);

                textView.setText(layerInfo.getName());
                listView.setAdapter(new LayerLegendAdapter(layerInfo.getLegend()));
            }
            return view;
        }
    }

}
