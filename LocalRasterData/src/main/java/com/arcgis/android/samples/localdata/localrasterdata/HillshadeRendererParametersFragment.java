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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.esri.core.map.SlopeType;
import com.esri.core.renderer.BlendRenderer;
import com.esri.core.renderer.HillshadeRenderer;

/*
 * Dialog to allow users to input parameters used in HillshadeRenderer.
 */
public class HillshadeRendererParametersFragment extends DialogFragment {

  private OnDialogDismissListener mDialogListener;
  private RendererType mType;
  
  private final static String KEY_INT = "code";
  private final static String KEY_ALTITUDE = "altitude";
  private final static String KEY_AZIMUTH = "azimuth";
  private final static String KEY_ZFACTOR = "zfactor";
  private final static String KEY_SLOPE_TYPE = "slopeType";
  private final static String KEY_PIXEL_SIZE_FACTOR = "pixelSizeFactor";
  private final static String KEY_PIXEL_SIZE_POWER = "pixelSizePower";
  private final static String KEY_GAMMA = "gamma";

  public static HillshadeRendererParametersFragment newInstance(int code, double altitude, double azimuth, 
      double zfactor, int slotType, double pixelSizeFactor, double pixelSizePower, double gamma) {

    HillshadeRendererParametersFragment fragment = new HillshadeRendererParametersFragment();

    Bundle args = new Bundle();
    args.putInt(KEY_INT, code);
    args.putDouble(KEY_ALTITUDE, altitude);
    args.putDouble(KEY_AZIMUTH, azimuth);
    args.putDouble(KEY_ZFACTOR, zfactor);
    args.putInt(KEY_SLOPE_TYPE, slotType);
    args.putDouble(KEY_PIXEL_SIZE_FACTOR, pixelSizeFactor);
    args.putDouble(KEY_PIXEL_SIZE_POWER, pixelSizePower);
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
  
  private double getEditTextValue(EditText text) {
    double ret = EditTextUtils.DEFAULT_DOUBLE_VALUE;
    
    String textString = text.getText().toString();
    if (textString != null && textString.length() > 0) {
      ret = Double.parseDouble(textString);
    }
    
    return ret;
  }
  
  private void setEditText(View view, String key, int id) {
    double value = getArguments().getDouble(key);
    if (value != EditTextUtils.DEFAULT_DOUBLE_VALUE) {
      EditText editText = (EditText) view.findViewById(id);
      if (editText != null)  {
        editText.setText(Double.toString(value));
      }
    }
  }
  
  private View createView() {
    View view = getActivity().getLayoutInflater().inflate(R.layout.hillshade_renderer_parameters, null);
    setEditText(view, KEY_ALTITUDE, R.id.parameter_altitude);
    setEditText(view, KEY_AZIMUTH, R.id.parameter_azimuth);
    setEditText(view, KEY_ZFACTOR, R.id.parameter_zfactor);
    setEditText(view, KEY_PIXEL_SIZE_FACTOR, R.id.parameter_pixelsizefactor);
    setEditText(view, KEY_PIXEL_SIZE_POWER, R.id.parameter_pixelsizepower);
    
    Spinner spinner = (Spinner) view.findViewById(R.id.parameter_slopetype);
    ArrayAdapter<SlopeType> adapter = new ArrayAdapter<SlopeType>(view.getContext(), android.R.layout.simple_spinner_item, SlopeType.values());
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    int index = getArguments().getInt(KEY_SLOPE_TYPE);
    if ((index > -1) && (index < SlopeType.values().length)){
      spinner.setSelection(index);
    }
      
    if (mType == RendererType.BLEND) {
      LinearLayout ll = (LinearLayout) view.findViewById(R.id.parameter_gamma_group);
      ll.setVisibility(View.VISIBLE);
      setEditText(view, KEY_GAMMA, R.id.parameter_gamma);
    }
    
    return view;
  }
  
  private void doPositiveClick() {
    boolean shouldChange = false;
    HillshadeRenderer hillshdeRenderer = new HillshadeRenderer();
    
    Dialog dialog = getDialog();
    EditText altitudeText = (EditText) dialog.findViewById(R.id.parameter_altitude);
    double altitude = getEditTextValue(altitudeText);
    if (altitude >= 0 && altitude <= 90) {
      // [0, 90], default = 45
      hillshdeRenderer.setAltitude(altitude);
      shouldChange = true;
    }
    
    EditText azimuthText = (EditText) dialog.findViewById(R.id.parameter_azimuth);
    double azimuth = getEditTextValue(azimuthText);
    if (azimuth >= 0 && azimuth <= 360) {
      // [0, 360], default = 315
      hillshdeRenderer.setAzimuth(azimuth);
      shouldChange = true;
    }
    
    EditText zfacgtorText = (EditText) dialog.findViewById(R.id.parameter_zfactor);
    double zfactor = getEditTextValue(zfacgtorText);
    if (zfactor > 0) {
      // default = 1
      hillshdeRenderer.setZfactor(zfactor);
      shouldChange = true;
    }
    
    EditText pixelSizeFactorText = (EditText) dialog.findViewById(R.id.parameter_pixelsizefactor);
    double sizeFactor = getEditTextValue(pixelSizeFactorText);
    if (sizeFactor != EditTextUtils.DEFAULT_DOUBLE_VALUE) {
      // default = 1
      hillshdeRenderer.setPixelSizeFactor(sizeFactor);
      shouldChange = true;
    }
      
    EditText pixelSizePowerText = (EditText) dialog.findViewById(R.id.parameter_pixelsizepower);
    double sizePower = getEditTextValue(pixelSizePowerText);
    if (sizePower != EditTextUtils.DEFAULT_DOUBLE_VALUE) {
      // default = 1
      hillshdeRenderer.setPixelSizePower(sizePower);
      shouldChange = true;
    }
    
    Spinner spinner = (Spinner) dialog.findViewById(R.id.parameter_slopetype);
    hillshdeRenderer.setSlopeType(SlopeType.valueOf(spinner.getSelectedItem().toString()));
    if (mType == RendererType.BLEND) {
      BlendRenderer blendRenderer = new BlendRenderer();
      EditText gammaText = (EditText) dialog.findViewById(R.id.parameter_gamma);
      double gamma = getEditTextValue(gammaText);
      // default = -1
      blendRenderer.setGamma(gamma);
      
      blendRenderer.setAltitude(hillshdeRenderer.getAltitude());
      blendRenderer.setAzimuth(hillshdeRenderer.getAzimuth());
      blendRenderer.setZfactor(hillshdeRenderer.getZfactor());
      blendRenderer.setSlopeType(hillshdeRenderer.getSlopeType());
      blendRenderer.setPixelSizeFactor(hillshdeRenderer.getPixelSizeFactor());
      blendRenderer.setPixelSizePower(hillshdeRenderer.getPixelSizePower());
      
      mDialogListener.onPositiveClicked(blendRenderer);
    } else if (shouldChange) {
      mDialogListener.onPositiveClicked(hillshdeRenderer);
    }
  }

  private void doNegativeClick() {

  }

}
