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

package com.esri.arcgisruntime.sample.hillshaderenderer;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.esri.arcgisruntime.raster.SlopeType;

/**
 * Class which handles the hillshade parameters dialog.
 */

public class ParametersDialogFragment extends DialogFragment {

    /**
     * Interface for passing dialog parameters back to MainActivity.
     */
    public interface ParametersListener {
        void returnParameters(int altitude, int azimuth, SlopeType slopeType);
    }

    private Integer mAltitude;
    private Integer mAzimuth;
    private SlopeType mSlopeType;
    private TextView mCurrAltitudeTextView;
    private TextView mCurrAzimuthTextView;

    /**
     * Builds a parameter dialog box with values pulled through from MainActivity.
     * @param savedInstanceState bundle holding previous activity's saved state
     * @return create parameter dialog box
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        Bundle hillshadeParameters = getArguments();
        if (hillshadeParameters != null) {
            mAltitude = hillshadeParameters.getInt("altitude");
            mAzimuth = hillshadeParameters.getInt("azimuth");
            mSlopeType = (SlopeType) hillshadeParameters.getSerializable("slope_type");
        }

        AlertDialog.Builder paramDialog = new AlertDialog.Builder(getContext());
        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.hillshade_dialog_box, null);
        paramDialog.setView(dialogView);
        paramDialog.setTitle(R.string.dialog_title);
        paramDialog.setNegativeButton("Cancel", (dialog, which) -> dismiss());
        paramDialog.setPositiveButton("Render", (dialog, which) -> {
            ParametersListener activity =
                    (ParametersListener) getActivity();
            activity.returnParameters(mAltitude, mAzimuth, mSlopeType);
        });

        mCurrAltitudeTextView = dialogView.findViewById(R.id.hillshade_curr_altitude_text);
        SeekBar altitudeSeekBar = dialogView.findViewById(R.id.hillshade_altitude_seek_bar);
        altitudeSeekBar.setMax(90); //altitude is restricted to 0 - 90
        //set initial altitude value
        updateAltitudeSeekBar(altitudeSeekBar);
        altitudeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAltitude = progress;
                updateAltitudeSeekBar(seekBar);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        mCurrAzimuthTextView = dialogView.findViewById(R.id.hillshade_curr_azimuth_text);
        SeekBar azimuthSeekBar = dialogView.findViewById(R.id.hillshade_azimuth_seek_bar);
        azimuthSeekBar.setMax(360); //azimuth measured in degrees 0 - 360
        //set initial azimuth value
        updateAzimuthSeekBar(azimuthSeekBar);
        azimuthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAzimuth = progress;
                updateAzimuthSeekBar(seekBar);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        List<String> slopeTypeArray = new ArrayList<>();
        slopeTypeArray.add("None");    //ordinals:0
        slopeTypeArray.add("Degree");           //1
        slopeTypeArray.add("Percent rise");     //2
        slopeTypeArray.add("Scaled");           //3

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.hillshade_spinner_text_view,
                slopeTypeArray);

        Spinner slopeTypeSpinner = dialogView.findViewById(R.id.hillshade_slope_type_spinner);
        slopeTypeSpinner.setAdapter(spinnerAdapter);
        slopeTypeSpinner.setSelection(mSlopeType.ordinal());
        slopeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mSlopeType = SlopeType.NONE;
                        break;
                    case 1:
                        mSlopeType = SlopeType.DEGREE;
                        break;
                    case 2:
                        mSlopeType = SlopeType.PERCENT_RISE;
                        break;
                    case 3:
                        mSlopeType = SlopeType.SCALED;
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        return paramDialog.create();
    }

    private void updateAltitudeSeekBar(SeekBar altitudeSeekBar) {
        altitudeSeekBar.setProgress(mAltitude);
        mCurrAltitudeTextView.setText(mAltitude.toString());
    }

    private void updateAzimuthSeekBar(SeekBar azimuthSeekBar) {
        azimuthSeekBar.setProgress(mAzimuth);
        mCurrAzimuthTextView.setText(mAzimuth.toString());
    }
}
