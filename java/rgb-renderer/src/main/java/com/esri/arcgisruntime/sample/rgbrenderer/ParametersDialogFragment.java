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

package com.esri.arcgisruntime.sample.rgbrenderer;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Class which handles the RGBRenderer parameters dialog.
 */

public class ParametersDialogFragment extends DialogFragment {

  private Integer mMinR;
  private Integer mMaxR;
  private Integer mMinG;
  private Integer mMaxG;
  private Integer mMinB;
  private Integer mMaxB;
  private Integer mPercentClipMin;
  private Integer mPercentClipMax;
  private Integer mStdDevFactor;
  private MainActivity.StretchType mStretchType;

  private TextView mMinRedTextView;
  private TextView mMaxRedTextView;
  private TextView mMinGreenTextView;
  private TextView mMaxGreenTextView;
  private TextView mMinBlueTextView;
  private TextView mMaxBlueTextView;
  private TextView mPercentClipMinTextView;
  private TextView mPercentClipMaxTextView;
  private TextView mStdDevTextView;
  private TextView mCurrMinRedTextView;
  private TextView mCurrMaxRedTextView;
  private TextView mCurrMinGreenTextView;
  private TextView mCurrMaxGreenTextView;
  private TextView mCurrMinBlueTextView;
  private TextView mCurrMaxBlueTextView;
  private TextView mCurrPercentClipMinTextView;
  private TextView mCurrPercentClipMaxTextView;
  private TextView mCurrStdDevTextView;
  private SeekBar mMinRedSeekBar;
  private SeekBar mMaxRedSeekBar;
  private SeekBar mMinGreenSeekBar;
  private SeekBar mMaxGreenSeekBar;
  private SeekBar mMinBlueSeekBar;
  private SeekBar mMaxBlueSeekBar;
  private SeekBar mPercentClipMinSeekBar;
  private SeekBar mPercentClipMaxSeekBar;
  private SeekBar mStdDevSeekBar;

  /**
   * Builds parameter dialog with values pulled through from MainActivity.
   *
   * @param savedInstanceState
   * @return create parameter dialog box
   */
  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService
        (Context.LAYOUT_INFLATER_SERVICE);
    Bundle rgbParameters = getArguments();
    if (rgbParameters != null) {
      mMinR = rgbParameters.getInt("minR");
      mMaxR = rgbParameters.getInt("maxR");
      mMinG = rgbParameters.getInt("minG");
      mMaxG = rgbParameters.getInt("maxG");
      mMinB = rgbParameters.getInt("minB");
      mMaxB = rgbParameters.getInt("maxB");
      mPercentClipMin = rgbParameters.getInt("percent_clip_min");
      mPercentClipMax = rgbParameters.getInt("percent_clip_max");
      mStdDevFactor = rgbParameters.getInt("std_dev_factor");
      mStretchType = (MainActivity.StretchType) rgbParameters.getSerializable("stretch_type");
      Log.d("Incoming Parameters", "min r: " +mMinR+ " max r: " +mMaxR+ " min g: " +mMinG+ " max g: " +mMaxG+ " min b: " +mMinB+ " max b: " +mMaxB);
    }

    final AlertDialog.Builder paramDialog = new AlertDialog.Builder(getContext());
    @SuppressLint("InflateParams")
    final View dialogView = inflater.inflate(R.layout.rgb_dialog_box, null);
    paramDialog.setView(dialogView);
    paramDialog.setTitle(R.string.rgb_rendering_parameters);
    paramDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dismiss();
      }
    });
    paramDialog.setPositiveButton("Render", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        ParametersListener activity = (ParametersListener) getActivity();
        activity.returnParameters(mMinR, mMaxR, mMinG, mMaxG, mMinB, mMaxB, mPercentClipMin, mPercentClipMax, mStdDevFactor, mStretchType);
      }
    });
    // min max ui elements
    mMinRedTextView = dialogView.findViewById(R.id.min_red_value_text_view);
    mMaxRedTextView = dialogView.findViewById(R.id.max_red_value_text_view);
    mMinGreenTextView = dialogView.findViewById(R.id.min_green_value_text_view);
    mMaxGreenTextView = dialogView.findViewById(R.id.max_green_value_text_view);
    mMinBlueTextView = dialogView.findViewById(R.id.min_blue_value_text_view);
    mMaxBlueTextView = dialogView.findViewById(R.id.max_blue_value_text_view);
    mMinRedSeekBar = dialogView.findViewById(R.id.min_red_seek_bar);
    mMaxRedSeekBar = dialogView.findViewById(R.id.max_red_seek_bar);
    mMinGreenSeekBar = dialogView.findViewById(R.id.min_green_seek_bar);
    mMaxGreenSeekBar = dialogView.findViewById(R.id.max_green_seek_bar);
    mMinBlueSeekBar = dialogView.findViewById(R.id.min_blue_seek_bar);
    mMaxBlueSeekBar = dialogView.findViewById(R.id.max_blue_seek_bar);
    mMinRedSeekBar.setMax(255);
    mMaxRedSeekBar.setMax(255);
    mMinGreenSeekBar.setMax(255);
    mMaxGreenSeekBar.setMax(255);
    mMinBlueSeekBar.setMax(255);
    mMaxBlueSeekBar.setMax(255);
    mCurrMinRedTextView = dialogView.findViewById(R.id.curr_min_red_text_view);
    mCurrMaxRedTextView = dialogView.findViewById(R.id.curr_max_red_text_view);
    mCurrMinGreenTextView = dialogView.findViewById(R.id.curr_min_green_text_view);
    mCurrMaxGreenTextView = dialogView.findViewById(R.id.curr_max_green_text_view);
    mCurrMinBlueTextView = dialogView.findViewById(R.id.curr_min_blue_text_view);
    mCurrMaxBlueTextView = dialogView.findViewById(R.id.curr_max_blue_text_view);
    //update seek bar positions with current mMinMax values
    updateSeekBar(mMinRedSeekBar, mMinR, mCurrMinRedTextView);
    updateSeekBar(mMaxRedSeekBar, mMaxR, mCurrMaxRedTextView);
    updateSeekBar(mMinGreenSeekBar, mMinG, mCurrMinGreenTextView);
    updateSeekBar(mMaxGreenSeekBar, mMaxG, mCurrMaxGreenTextView);
    updateSeekBar(mMinBlueSeekBar, mMinB, mCurrMinBlueTextView);
    updateSeekBar(mMaxBlueSeekBar, mMaxB, mCurrMaxBlueTextView);
    // percent clip ui elements
    mPercentClipMinTextView = dialogView.findViewById(R.id.percent_clip_min_value_text_view);
    mPercentClipMaxTextView = dialogView.findViewById(R.id.percent_clip_max_value_text_view);
    mPercentClipMinSeekBar = dialogView.findViewById(R.id.percent_clip_min_seek_bar);
    mPercentClipMaxSeekBar = dialogView.findViewById(R.id.percent_clip_max_seek_bar);
    mPercentClipMinSeekBar.setMax(99);
    mPercentClipMaxSeekBar.setMax(99);
    mCurrPercentClipMinTextView = dialogView.findViewById(R.id.curr_percent_clip_min_text_view);
    mCurrPercentClipMaxTextView = dialogView.findViewById(R.id.curr_percent_clip_max_text_view);
    //update seek bar positions with current PercentClip
    updateSeekBar(mPercentClipMinSeekBar, mPercentClipMin, mCurrPercentClipMinTextView);
    updateSeekBar(mPercentClipMaxSeekBar, mPercentClipMax, mCurrPercentClipMaxTextView);
    // standard deviation ui elements
    mStdDevTextView = dialogView.findViewById(R.id.std_dev_text_view);
    mStdDevSeekBar = dialogView.findViewById(R.id.std_dev_seek_bar);
    mStdDevSeekBar.setMax(3);
    mCurrStdDevTextView = dialogView.findViewById(R.id.curr_std_dev_text_view);
    //update seek bar position with current StandardDeviation
    updateSeekBar(mStdDevSeekBar, mStdDevFactor, mCurrStdDevTextView);

    // set ui to previous selection
    if (mStretchType == MainActivity.StretchType.MIN_MAX) {
      setMinMaxVisibility(true);
      setPercentClipVisibility(false);
      setStdDevVisibility(false);
    } else if (mStretchType == MainActivity.StretchType.PERCENT_CLIP) {
      setMinMaxVisibility(false);
      setPercentClipVisibility(true);
      setStdDevVisibility(false);
    } else if (mStretchType == MainActivity.StretchType.STANDARD_DEVIATION) {
      setMinMaxVisibility(false);
      setPercentClipVisibility(false);
      setStdDevVisibility(true);
    }
    // seek bar listeners
    mMinRedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mMinR = progress;
        updateSeekBar(mMinRedSeekBar, mMinR, mCurrMinRedTextView);
        // move max to march min if max goes below min
        if (mMaxR < mMinR) {
          mMaxR = mMinR;
          updateSeekBar(mMaxRedSeekBar, mMaxR, mCurrMaxRedTextView);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) { }
    });
    mMaxRedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mMaxR = progress;
        updateSeekBar(mMaxRedSeekBar, mMaxR, mCurrMaxRedTextView);
        // move min to match max if min goes above max
        if (mMinR > mMaxR) {
          mMinR = mMaxR;
          updateSeekBar(mMinRedSeekBar, mMinR, mCurrMinRedTextView);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) { }
    });
    mMinGreenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mMinG = progress;
        updateSeekBar(mMinGreenSeekBar, mMinG, mCurrMinGreenTextView);
        // move max to march min if max goes below min
        if (mMaxG < mMinG) {
          mMaxG = mMinG;
          updateSeekBar(mMaxGreenSeekBar, mMaxG, mCurrMaxGreenTextView);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) { }
    });
    mMaxGreenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mMaxG = progress;
        updateSeekBar(mMaxGreenSeekBar, mMaxG, mCurrMaxGreenTextView);
        // move min to match max if min goes above max
        if (mMinG > mMaxG) {
          mMinG = mMaxG;
          updateSeekBar(mMinGreenSeekBar, mMinG, mCurrMinGreenTextView);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) { }
    });
    mMinBlueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mMinB = progress;
        updateSeekBar(mMinBlueSeekBar, mMinB, mCurrMinBlueTextView);
        // move max to march min if max goes below min
        if (mMaxB < mMinB) {
          mMaxB = mMinB;
          updateSeekBar(mMaxBlueSeekBar, mMaxB, mCurrMaxBlueTextView);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) { }
    });
    mMaxBlueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mMaxB = progress;
        updateSeekBar(mMaxBlueSeekBar, mMaxB, mCurrMaxBlueTextView);
        // move min to match max if min goes above max
        if (mMinB > mMaxB) {
          mMinB = mMaxB;
          updateSeekBar(mMinBlueSeekBar, mMinB, mCurrMinBlueTextView);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) { }
    });
    
    mPercentClipMinSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mPercentClipMin = progress;
        updateSeekBar(mPercentClipMinSeekBar, mPercentClipMin, mCurrPercentClipMinTextView);
        if (mPercentClipMin + mPercentClipMax > 100) {
          // constrain min + max <= 100
          mPercentClipMax = 100 - mPercentClipMin;
          updateSeekBar(mPercentClipMaxSeekBar, mPercentClipMax, mCurrPercentClipMaxTextView);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) { }
    });

    mPercentClipMaxSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mPercentClipMax = progress;
        updateSeekBar(mPercentClipMaxSeekBar, mPercentClipMax, mCurrPercentClipMaxTextView);
        if (mPercentClipMin + mPercentClipMax > 100) {
          // constrain min + max <= 100
          mPercentClipMin = 100 - mPercentClipMax;
          updateSeekBar(mPercentClipMinSeekBar, mPercentClipMin, mCurrPercentClipMinTextView);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) { }
    });
    mStdDevSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mStdDevFactor = progress;
        updateSeekBar(mStdDevSeekBar, mStdDevFactor, mCurrStdDevTextView);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) { }
    });
    // stretch type spinner
    List<String> stretchTypeArray = new ArrayList<>();
    stretchTypeArray.add(MainActivity.StretchType.MIN_MAX.toString());   //ordinals:0
    stretchTypeArray.add(MainActivity.StretchType.PERCENT_CLIP.toString());       //1
    stretchTypeArray.add(MainActivity.StretchType.STANDARD_DEVIATION.toString()); //2
    ArrayAdapter<String> stretchTypeSpinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.rgb_spinner_text_view,
        stretchTypeArray);
    Spinner stretchTypeSpinner = dialogView.findViewById(R.id.stretch_type_spinner);
    stretchTypeSpinner.setAdapter(stretchTypeSpinnerAdapter);
    stretchTypeSpinner.setSelection(mStretchType.ordinal());
    stretchTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
          case 0:
            mStretchType = MainActivity.StretchType.MIN_MAX;
            setMinMaxVisibility(true);
            setPercentClipVisibility(false);
            setStdDevVisibility(false);
            break;
          case 1:
            mStretchType = MainActivity.StretchType.PERCENT_CLIP;
            setMinMaxVisibility(false);
            setPercentClipVisibility(true);
            setStdDevVisibility(false);
            break;
          case 2:
            mStretchType = MainActivity.StretchType.STANDARD_DEVIATION;
            setMinMaxVisibility(false);
            setPercentClipVisibility(false);
            setStdDevVisibility(true);
            break;
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
    return paramDialog.create();
  }

  private void setMinMaxVisibility(boolean visibility) {
    if (visibility) {
      mMinRedTextView.setVisibility(View.VISIBLE);
      mMinRedSeekBar.setVisibility(View.VISIBLE);
      mCurrMinRedTextView.setVisibility(View.VISIBLE);
      mMaxRedTextView.setVisibility(View.VISIBLE);
      mMaxRedSeekBar.setVisibility(View.VISIBLE);
      mCurrMaxRedTextView.setVisibility(View.VISIBLE);

      mMinGreenTextView.setVisibility(View.VISIBLE);
      mMinGreenSeekBar.setVisibility(View.VISIBLE);
      mCurrMinGreenTextView.setVisibility(View.VISIBLE);
      mMaxGreenTextView.setVisibility(View.VISIBLE);
      mMaxGreenSeekBar.setVisibility(View.VISIBLE);
      mCurrMaxGreenTextView.setVisibility(View.VISIBLE);

      mMinBlueTextView.setVisibility(View.VISIBLE);
      mMinBlueSeekBar.setVisibility(View.VISIBLE);
      mCurrMinBlueTextView.setVisibility(View.VISIBLE);
      mMaxBlueTextView.setVisibility(View.VISIBLE);
      mMaxBlueSeekBar.setVisibility(View.VISIBLE);
      mCurrMaxBlueTextView.setVisibility(View.VISIBLE);
    } else {
      mMinRedTextView.setVisibility(View.GONE);
      mMinRedSeekBar.setVisibility(View.GONE);
      mCurrMinRedTextView.setVisibility(View.GONE);
      mMaxRedTextView.setVisibility(View.GONE);
      mMaxRedSeekBar.setVisibility(View.GONE);
      mCurrMaxRedTextView.setVisibility(View.GONE);

      mMinGreenTextView.setVisibility(View.GONE);
      mMinGreenSeekBar.setVisibility(View.GONE);
      mCurrMinGreenTextView.setVisibility(View.GONE);
      mMaxGreenTextView.setVisibility(View.GONE);
      mMaxGreenSeekBar.setVisibility(View.GONE);
      mCurrMaxGreenTextView.setVisibility(View.GONE);

      mMinBlueTextView.setVisibility(View.GONE);
      mMinBlueSeekBar.setVisibility(View.GONE);
      mCurrMinBlueTextView.setVisibility(View.GONE);
      mMaxBlueTextView.setVisibility(View.GONE);
      mMaxBlueSeekBar.setVisibility(View.GONE);
      mCurrMaxBlueTextView.setVisibility(View.GONE);
    }
  }

  private void setPercentClipVisibility(boolean visibility) {
    if (visibility) {
      mPercentClipMinTextView.setVisibility(View.VISIBLE);
      mPercentClipMinSeekBar.setVisibility(View.VISIBLE);
      mCurrPercentClipMinTextView.setVisibility(View.VISIBLE);
      mPercentClipMaxTextView.setVisibility(View.VISIBLE);
      mPercentClipMaxSeekBar.setVisibility(View.VISIBLE);
      mCurrPercentClipMaxTextView.setVisibility(View.VISIBLE);
    } else {
      mPercentClipMinTextView.setVisibility(View.GONE);
      mPercentClipMinSeekBar.setVisibility(View.GONE);
      mCurrPercentClipMinTextView.setVisibility(View.GONE);
      mPercentClipMaxTextView.setVisibility(View.GONE);
      mPercentClipMaxSeekBar.setVisibility(View.GONE);
      mCurrPercentClipMaxTextView.setVisibility(View.GONE);
    }
  }

  private void setStdDevVisibility(boolean visibility) {
    if (visibility) {
      mStdDevTextView.setVisibility(View.VISIBLE);
      mStdDevSeekBar.setVisibility(View.VISIBLE);
      mCurrStdDevTextView.setVisibility(View.VISIBLE);
    } else {
      mStdDevTextView.setVisibility(View.GONE);
      mStdDevSeekBar.setVisibility(View.GONE);
      mCurrStdDevTextView.setVisibility(View.GONE);
    }
  }

  @SuppressLint("SetTextI18n")
  private void updateSeekBar(SeekBar seekBar, Integer progress, TextView textView) {
    seekBar.setProgress(progress);
    textView.setText(progress.toString());
  }

  /**
   * Interface for passing dialog parameters back to MainActivity.
   */
  interface ParametersListener {
    void returnParameters(int minR, int maxR, int minG, int maxG, int minB, int maxB, int percentClipMin,
        int percentClipMax, int stdDevFactor, MainActivity.StretchType stretchType);
  }
}
