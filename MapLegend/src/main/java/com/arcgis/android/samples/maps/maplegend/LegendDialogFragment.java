/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.arcgis.android.samples.maps.maplegend;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;

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

}
