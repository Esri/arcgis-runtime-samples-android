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

package com.esri.arcgisruntime.sample.stretchrenderer;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Class which handles the stretch renderer parameters dialog.
 */

public class ParametersDialogFragment extends DialogFragment {

  private Integer mMin;
  private Integer mMax;
  private Integer mPercentClipMin;
  private Integer mPercentClipMax;
  private Integer mStdDevFactor;
  private MainActivity.StretchType mStretchType;

  private TextView mMinTextView;
  private TextView mMaxTextView;
  private TextView mPercentClipMinTextView;
  private TextView mPercentClipMaxTextView;
  private TextView mStdDevTextView;
  private TextView mCurrMinTextView;
  private TextView mCurrMaxTextView;
  private TextView mCurrPercentClipMinTextView;
  private TextView mCurrPercentClipMaxTextView;
  private TextView mCurrStdDevTextView;
  private SeekBar mMinSeekBar;
  private SeekBar mMaxSeekBar;
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
    Bundle stretchParameters = getArguments();
    if (stretchParameters != null) {
      mMin = stretchParameters.getInt("min");
      mMax = stretchParameters.getInt("max");
      mPercentClipMin = stretchParameters.getInt("percent_clip_min");
      mPercentClipMax = stretchParameters.getInt("percent_clip_max");
      mStdDevFactor = stretchParameters.getInt("std_dev_factor");
      mStretchType = (MainActivity.StretchType) stretchParameters.getSerializable("stretch_type");
    }

    final AlertDialog.Builder paramDialog = new AlertDialog.Builder(getContext());
    @SuppressLint("InflateParams")
    final View dialogView = inflater.inflate(R.layout.stretch_dialog_box, null);
    paramDialog.setView(dialogView);
    paramDialog.setTitle(R.string.stretch_rendering_parameters);
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
        activity.returnParameters(mMin, mMax, mPercentClipMin, mPercentClipMax, mStdDevFactor, mStretchType);
      }
    });
    // min max ui elements
    mMinTextView = dialogView.findViewById(R.id.min_value_text_view);
    mMaxTextView = dialogView.findViewById(R.id.max_value_text_view);
    mMinSeekBar = dialogView.findViewById(R.id.min_seek_bar);
    mMaxSeekBar = dialogView.findViewById(R.id.max_seek_bar);
    mMinSeekBar.setMax(255);
    mMaxSeekBar.setMax(255);
    mCurrMinTextView = dialogView.findViewById(R.id.curr_min_text_view);
    mCurrMaxTextView = dialogView.findViewById(R.id.curr_max_text_view);
    updateSeekBar(mMinSeekBar, mMin, mCurrMinTextView);
    updateSeekBar(mMaxSeekBar, mMax, mCurrMaxTextView);
    // percent clip ui elements
    mPercentClipMinTextView = dialogView.findViewById(R.id.percent_clip_min_value_text_view);
    mPercentClipMaxTextView = dialogView.findViewById(R.id.percent_clip_max_value_text_view);
    mPercentClipMinSeekBar = dialogView.findViewById(R.id.percent_clip_min_seek_bar);
    mPercentClipMaxSeekBar = dialogView.findViewById(R.id.percent_clip_max_seek_bar);
    mPercentClipMinSeekBar.setMax(99);
    mPercentClipMaxSeekBar.setMax(99);
    mCurrPercentClipMinTextView = dialogView.findViewById(R.id.curr_percent_clip_min_text_view);
    mCurrPercentClipMaxTextView = dialogView.findViewById(R.id.curr_percent_clip_max_text_view);
    updateSeekBar(mPercentClipMinSeekBar, mPercentClipMin, mCurrPercentClipMinTextView);
    updateSeekBar(mPercentClipMaxSeekBar, mPercentClipMax, mCurrPercentClipMaxTextView);
    // standard deviation ui elements
    mStdDevTextView = dialogView.findViewById(R.id.std_dev_text_view);
    mStdDevSeekBar = dialogView.findViewById(R.id.std_dev_seek_bar);
    mStdDevSeekBar.setMax(3);
    mCurrStdDevTextView = dialogView.findViewById(R.id.curr_std_dev_text_view);
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
    mMinSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mMin = progress;
        updateSeekBar(mMinSeekBar, mMin, mCurrMinTextView);
        // move max to march min if max goes below min
        if (mMax < mMin) {
          mMax = mMin;
          updateSeekBar(mMaxSeekBar, mMax, mCurrMaxTextView);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) { }
    });
    mMaxSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mMax = progress;
        updateSeekBar(mMaxSeekBar, mMax, mCurrMaxTextView);
        // move min to match max if min goes above max
        if (mMin > mMax) {
          mMin = mMax;
          updateSeekBar(mMinSeekBar, mMin, mCurrMinTextView);
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
    ArrayAdapter<String> stretchTypeSpinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.stretch_spinner_text_view,
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
      mMinTextView.setVisibility(View.VISIBLE);
      mMinSeekBar.setVisibility(View.VISIBLE);
      mCurrMinTextView.setVisibility(View.VISIBLE);
      mMaxTextView.setVisibility(View.VISIBLE);
      mMaxSeekBar.setVisibility(View.VISIBLE);
      mCurrMaxTextView.setVisibility(View.VISIBLE);
    } else {
      mMinTextView.setVisibility(View.GONE);
      mMinSeekBar.setVisibility(View.GONE);
      mCurrMinTextView.setVisibility(View.GONE);
      mMaxTextView.setVisibility(View.GONE);
      mMaxSeekBar.setVisibility(View.GONE);
      mCurrMaxTextView.setVisibility(View.GONE);
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
    void returnParameters(int min, int max, int percentClipMin, int percentClipMax, int stdDevFactor,
        MainActivity.StretchType stretchType);
  }
}
