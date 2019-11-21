/*
 * Copyright 2015 Esri
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

package com.esri.arcgisruntime.sample.maprotation;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private TextView mRotationValueText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the Basemap.Type topographic
    ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 10);
    // set the map to be displayed in this view
    mMapView.setMap(map);
    // create TextView to show angle of rotation
    mRotationValueText = (TextView) findViewById(R.id.rotationValueText);
    // create SeekBar
    SeekBar mRotationSeekBar = (SeekBar) findViewById(R.id.rotationSeekBar);
    mRotationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int angle, boolean b) {
        // convert progress to double
        double dAngle = angle;
        // set the text to SeekBar value
        mRotationValueText.setText(String.valueOf(angle));
        // rotate MapView to double angle value
        mMapView.setViewpointRotationAsync(dAngle);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }
}
