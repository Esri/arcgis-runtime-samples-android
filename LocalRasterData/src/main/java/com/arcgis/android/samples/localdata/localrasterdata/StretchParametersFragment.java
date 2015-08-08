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

package com.arcgis.android.samples.localdata.localrasterdata;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.esri.core.renderer.RGBRenderer;
import com.esri.core.renderer.StretchParameters;
import com.esri.core.renderer.StretchParameters.ClipStretchParameters;
import com.esri.core.renderer.StretchParameters.HistogramStretchParamaeters;
import com.esri.core.renderer.StretchParameters.MinMaxStretchParameters;
import com.esri.core.renderer.StretchParameters.StdDevStretchParameters;
import com.esri.core.renderer.StretchRenderer;

/*
 * Dialog to allow users to select StretchType used in RGBRenderer and StretchRenderer.
 */
public class StretchParametersFragment extends DialogFragment implements OnClickListener {

  private SeekBar barStddev, barClipMin, barClipMax;
  private OnDialogDismissListener mDialogListener;
  private RendererType mType;
  private final static String KEY_INT = "code";
  private final static String KEY_STRETCH_TYPE ="type";
  private final static String KEY_MIN_CLIP = "minClip";
  private final static String KEY_MAX_CLIP = "maxClip";
  private final static String KEY_STANDARD_DEVIATION = "stdDev";
  private final static String KEY_GAMMA = "gamma";

  public static StretchParametersFragment newInstance(int code, int stretchType, 
      double stdDev, double minClip, double maxClip, double gamma) {
    StretchParametersFragment fragment = new StretchParametersFragment();

    Bundle args = new Bundle();
    args.putInt(KEY_INT, code);
    args.putInt(KEY_STRETCH_TYPE, stretchType);
    args.putDouble(KEY_STANDARD_DEVIATION, stdDev);
    args.putDouble(KEY_MIN_CLIP, minClip);
    args.putDouble(KEY_MAX_CLIP, maxClip);
    args.putDouble(KEY_GAMMA, gamma);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    mDialogListener = (OnDialogDismissListener) getActivity();
    mType = RendererType.fromCode(getArguments().getInt(KEY_INT));

    // Create layout of the dialog
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    View view = createView();

    builder.setView(view);
    builder.setTitle(mType.getName())
        .setPositiveButton(R.string.rgbrenderer_parameters_ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int whichButton) {
            doPositiveClick();
          }
        }).setNegativeButton(R.string.rgbrenderer_parameters_cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int whichButton) {
            doNegativeClick();
          }
        });
    return builder.create();
  }

  @Override
  public void onClick(View view) {
    boolean checked = ((RadioButton) view).isChecked();
    // Check which radio button was clicked
    switch (view.getId()) {
    case R.id.radio_stretchtype_stddev:
      if (checked) {
        barStddev.setVisibility(View.VISIBLE);
        barClipMin.setVisibility(View.INVISIBLE);
        barClipMax.setVisibility(View.INVISIBLE);
      } else {
        barStddev.setVisibility(View.INVISIBLE);
      }
      break;
    case R.id.radio_stretchtype_pecentclip:
      if (checked) {
        barStddev.setVisibility(View.INVISIBLE);
        barClipMin.setVisibility(View.VISIBLE);
        barClipMax.setVisibility(View.VISIBLE);
      } else {
        barClipMin.setVisibility(View.INVISIBLE);
        barClipMax.setVisibility(View.INVISIBLE);
      }
      break;
    default:
      barStddev.setVisibility(View.INVISIBLE);
      barClipMin.setVisibility(View.INVISIBLE);
      barClipMax.setVisibility(View.INVISIBLE);
      break;
    }
  }

  private void checkStretchType(View view) {
    int index = getArguments().getInt(KEY_STRETCH_TYPE);
    barStddev = (SeekBar) view.findViewById(R.id.parameter_stddev);
    barStddev.setVisibility(View.INVISIBLE);
    barClipMin = (SeekBar) view.findViewById(R.id.parameter_minclip);
    barClipMin.setVisibility(View.INVISIBLE);
    barClipMax = (SeekBar) view.findViewById(R.id.parameter_maxclip);
    barClipMax.setVisibility(View.INVISIBLE);
    if ((index < 0) || (index > StretchParameters.StretchType.values().length))
      return;
    
    RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.rgbrenderer_parameters_group);
    switch (index) {
      case 0:
        radioGroup.check(R.id.radio_stretchtype_none);
        break;
      case 1:
        radioGroup.check(R.id.radio_stretchtype_minmax);
        break;
      case 2:
        radioGroup.check(R.id.radio_stretchtype_stddev);
        barStddev.setVisibility(View.VISIBLE);
        if (getArguments().getDouble(KEY_STANDARD_DEVIATION) != EditTextUtils.DEFAULT_DOUBLE_VALUE)
        {
          barStddev.setProgress((int) getArguments().getDouble(KEY_STANDARD_DEVIATION));
        }
        break;
      case 3:
        radioGroup.check(R.id.radio_stretchtype_histogram);
        break;
      case 4:
        radioGroup.check(R.id.radio_stretchtype_pecentclip);
        barClipMin.setVisibility(View.VISIBLE);
        if (getArguments().getDouble(KEY_MIN_CLIP) != EditTextUtils.DEFAULT_DOUBLE_VALUE) {
          barClipMin.setProgress((int) getArguments().getDouble(KEY_MIN_CLIP));
        }
        barClipMax.setVisibility(View.VISIBLE);
        if (getArguments().getDouble(KEY_MAX_CLIP) != EditTextUtils.DEFAULT_DOUBLE_VALUE) {
          barClipMax.setProgress((int) getArguments().getDouble(KEY_MAX_CLIP));
        }
        break;
      default:
        radioGroup.check(R.id.radio_stretchtype_none);
        break; 
    }
  }
  
  private View createView() {
    View view = getActivity().getLayoutInflater().inflate(R.layout.stretch_parameters, null);
    view.findViewById(R.id.radio_stretchtype_none).setOnClickListener(this);
    view.findViewById(R.id.radio_stretchtype_minmax).setOnClickListener(this);
    view.findViewById(R.id.radio_stretchtype_stddev).setOnClickListener(this);
    view.findViewById(R.id.radio_stretchtype_histogram).setOnClickListener(this);
    view.findViewById(R.id.radio_stretchtype_pecentclip).setOnClickListener(this);

    checkStretchType(view);
    EditTextUtils.setEditTextValue(view, R.id.parameter_stretch_gamma, getArguments().getDouble(KEY_GAMMA));
    return view;
  }
  
  private void doPositiveClick() {
    StretchParameters stretchParams = null;
    
    RadioGroup radioGroup = (RadioGroup) getDialog().findViewById(R.id.rgbrenderer_parameters_group);
    switch (radioGroup.getCheckedRadioButtonId()) {
    case R.id.radio_stretchtype_none:
      stretchParams = null;
      break;
    case R.id.radio_stretchtype_minmax:
      stretchParams = new MinMaxStretchParameters();
      break;
    case R.id.radio_stretchtype_stddev:
      StdDevStretchParameters stdDveStretch = new StdDevStretchParameters();
      if (barStddev.getVisibility() == View.VISIBLE) {
        stdDveStretch.setStdDev(barStddev.getProgress());
      }
      stretchParams = stdDveStretch;
      break;
    case R.id.radio_stretchtype_histogram:
      stretchParams = new HistogramStretchParamaeters();
      break;
    case R.id.radio_stretchtype_pecentclip:
      ClipStretchParameters clipStretch = new ClipStretchParameters();
      if (barClipMin.getVisibility() == View.VISIBLE) {
        clipStretch.setMinClip(barClipMin.getProgress());
      }
      if (barClipMax.getVisibility() == View.VISIBLE) {
        clipStretch.setMaxClip(barClipMax.getProgress());
      }
      stretchParams = clipStretch;
      break;
    }
    
    if (stretchParams != null) {
      EditText gammaText = (EditText) getDialog().findViewById(R.id.parameter_stretch_gamma);
      double gamma = EditTextUtils.getEditTextValue(gammaText);
      // default = -1
      stretchParams.setGamma(gamma);
    }

    if (mType == RendererType.RGB) {
      RGBRenderer rgbRenderer = new RGBRenderer();
      rgbRenderer.setStretchParameters(stretchParams);
      mDialogListener.onPositiveClicked(rgbRenderer);
    } else if (mType == RendererType.STRETCHED) {
      StretchRenderer renderer = new StretchRenderer();
      renderer.setStretchParameters(stretchParams);
      mDialogListener.onPositiveClicked(renderer);
    }
  }

  // Do nothing but dismiss the dialog.
  private void doNegativeClick() {

  }

}
