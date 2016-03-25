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

package com.esri.arcgisruntime.sample.arcgistiledlayerrendermode;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.ImageTiledLayer;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.VisibleAreaChangedEvent;
import com.esri.arcgisruntime.mapping.view.VisibleAreaChangedListener;
import com.esri.arcgisruntime.sample.android.widget.VerticalSeekBar;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private List<String> mRendermodeSpinnerList;
  private ArrayAdapter<String> mDataAdapter;
  private VerticalSeekBar mSeekBar;
  private TextView mSeekBarScale;
  private TextView mMapViewScale;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);

    final ArcGISTiledLayer tiledLayerBaseMap = new ArcGISTiledLayer(getResources().getString(R.string.world_topo_service));

    // set tiled layer as basemap
    Basemap basemap = new Basemap(tiledLayerBaseMap);
    // create a map with the basemap
    Map map = new Map(basemap);
    // set the map to be displayed in this view
    mMapView.setMap(map);

    // populate the spinner list with default bookmark names
    Spinner rendermodesSpinner = (Spinner) findViewById(R.id.rendermodesspinner);
    mRendermodeSpinnerList = new ArrayList<>();
    mRendermodeSpinnerList.add("RenderMode - DEFAULT");
    mRendermodeSpinnerList.add("RenderMode - AESTHETIC");
    mRendermodeSpinnerList.add("RenderMode - SCALE");

    // initialize the adapter for the bookmarks spinner
    mDataAdapter = new ArrayAdapter<>(this,
        R.layout.spinner_item, mRendermodeSpinnerList);
    rendermodesSpinner.setAdapter(mDataAdapter);

    // when an item is selected in the spinner set the mapview viewpoint to the selected
    // bookmark's viewpoint
    rendermodesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
          case 0 :
            tiledLayerBaseMap.setRenderMode(ImageTiledLayer.RenderMode.DEFAULT);
            break;
          case 1 :
            tiledLayerBaseMap.setRenderMode(ImageTiledLayer.RenderMode.AESTHETIC);
            break;
          case 2 :
            tiledLayerBaseMap.setRenderMode(ImageTiledLayer.RenderMode.SCALE);
            break;

        }

      }
      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    mSeekBar = (VerticalSeekBar) findViewById(R.id.seekBar);
    mSeekBarScale = (TextView) findViewById(R.id.seekbarscale);
    mMapViewScale = (TextView) findViewById(R.id.mapviewscale);

    mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (progress) {
          case 0:
            mMapView.setViewpointScaleAsync(5.91657527591555E8);
            mSeekBarScale.setText("Set Scale - "+ 5.91657527591555E8);
            break;
          case 1:
            mMapView.setViewpointScaleAsync(2.95828763795777E8);
            mSeekBarScale.setText("Set Scale - "+ 2.95828763795777E8);
          break;
          case 2:
            mMapView.setViewpointScaleAsync(1.47914381897889E8);
            mSeekBarScale.setText("Set Scale - "+ 1.47914381897889E8);
          break;
          case 3:
            mMapView.setViewpointScaleAsync(7.3957190948944E7);
            mSeekBarScale.setText("Set Scale - "+ 7.3957190948944E7);
          break;
          case 4:
            mMapView.setViewpointScaleAsync(3.6978595474472E7);
            mSeekBarScale.setText("Set Scale - "+ 3.6978595474472E7);
          break;
          case 5:
            mMapView.setViewpointScaleAsync(1.8489297737236E7);
            mSeekBarScale.setText("Set Scale - "+ 1.8489297737236E7);
          break;
          case 6:
            mMapView.setViewpointScaleAsync(9244648.868618);
            mSeekBarScale.setText("Set Scale - "+ 9244648.868618);
          break;
          case 7:
            mMapView.setViewpointScaleAsync(4622324.434309);
            mSeekBarScale.setText("Set Scale - "+ 4622324.434309);
          break;
          case 8:
            mMapView.setViewpointScaleAsync(2311162.217155);
            mSeekBarScale.setText("Set Scale - "+ 2311162.217155);
          break;
          case 9:
            mMapView.setViewpointScaleAsync(1155581.108577);
            mSeekBarScale.setText("Set Scale - "+ 1155581.108577);
          break;
          case 10:
            mMapView.setViewpointScaleAsync(577790.554289);
            mSeekBarScale.setText("Set Scale - "+ 577790.554289);
          break;
          case 11:
            mMapView.setViewpointScaleAsync(288895.277144);
            mSeekBarScale.setText("Set Scale - "+ 288895.277144);
          break;
          case 12:
            mMapView.setViewpointScaleAsync(144447.638572);
            mSeekBarScale.setText("Set Scale - "+ 144447.638572);
          break;
          case 13:
            mMapView.setViewpointScaleAsync(72223.819286);
            mSeekBarScale.setText("Set Scale - "+ 72223.819286);
          break;
          case 14:
            mMapView.setViewpointScaleAsync(36111.909643);
            mSeekBarScale.setText("Set Scale - "+ 36111.909643);
          break;
          case 15:
            mMapView.setViewpointScaleAsync(18055.954822);
            mSeekBarScale.setText("Set Scale - "+ 18055.954822);
          break;
          case 16:
            mMapView.setViewpointScaleAsync(9027.977411);
            mSeekBarScale.setText("Set Scale - "+ 9027.977411);
          break;
          case 17:
            mMapView.setViewpointScaleAsync(4513.988705);
            mSeekBarScale.setText("Set Scale - "+ 4513.988705);
          break;
          case 18:
            mMapView.setViewpointScaleAsync(2256.994353);
            mSeekBarScale.setText("Set Scale - "+ 2256.994353);
          break;
          case 19:
            mMapView.setViewpointScaleAsync(1128.497176);
            mSeekBarScale.setText("Set Scale - "+ 1128.497176);
          break;
          case 20:
            mMapView.setViewpointScaleAsync(564.248588);
            mSeekBarScale.setText("Set Scale - "+ 564.248588);
          break;
          case 21:
            mMapView.setViewpointScaleAsync(282.124294);
            mSeekBarScale.setText("Set Scale - "+ 282.124294);
          break;
          case 22:
            mMapView.setViewpointScaleAsync(141.062147);
            mSeekBarScale.setText("Set Scale - "+ 141.062147);
          break;
          case 23:
            mMapView.setViewpointScaleAsync(70.5310735);
            mSeekBarScale.setText("Set Scale - "+ 70.5310735);
          break;
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    mMapView.addVisibleAreaChangedListener(new VisibleAreaChangedListener() {
      @Override public void visibleAreaChanged(VisibleAreaChangedEvent visibleAreaChangedEvent) {
        double mapScale = mMapView.getMapScale();
        mMapViewScale.setText("Actual Scale - " + mapScale);
        if (mapScale == 5.91657527591555E8) {
          mSeekBar.setProgress(0);
          mSeekBarScale.setText("Set Scale - " + 5.91657527591555E8);
        } else if (mapScale == 2.95828763795777E8) {
          mSeekBar.setProgress(1);
          mSeekBarScale.setText("Set Scale - " + 2.95828763795777E8);
        } else if (mapScale == 1.47914381897889E8) {
          mSeekBar.setProgress(2);
          mSeekBarScale.setText("Set Scale - " + 1.47914381897889E8);
        } else if (mapScale == 7.3957190948944E7) {
          mSeekBar.setProgress(3);
          mSeekBarScale.setText("Set Scale - " + 7.3957190948944E7);
        } else if (mapScale == 3.6978595474472E7) {
          mSeekBar.setProgress(4);
          mSeekBarScale.setText("Set Scale - " + 3.6978595474472E7);
        } else if (mapScale == 1.8489297737236E7) {
          mSeekBar.setProgress(5);
          mSeekBarScale.setText("Set Scale - " + 1.8489297737236E7);
        } else if (mapScale == 9244648.868618) {
          mSeekBar.setProgress(6);
          mSeekBarScale.setText("Set Scale - " + 9244648.868618);
        } else if (mapScale == 4622324.434309) {
          mSeekBar.setProgress(7);
          mSeekBarScale.setText("Set Scale - " + 4622324.434309);
        } else if (mapScale == 2311162.217155) {
          mSeekBar.setProgress(8);
          mSeekBarScale.setText("Set Scale - " + 2311162.217155);
        } else if (mapScale == 1155581.108577) {
          mSeekBar.setProgress(9);
          mSeekBarScale.setText("Set Scale - " + 1155581.108577);
        } else if (mapScale == 577790.554289) {
          mSeekBar.setProgress(10);
          mSeekBarScale.setText("Set Scale - " + 577790.554289);
        } else if (mapScale == 288895.277144) {
          mSeekBar.setProgress(11);
          mSeekBarScale.setText("Set Scale - " + 288895.277144);
        } else if (mapScale == 144447.638572) {
          mSeekBar.setProgress(12);
          mSeekBarScale.setText("Set Scale - " + 144447.638572);
        } else if (mapScale == 72223.819286) {
          mSeekBar.setProgress(13);
          mSeekBarScale.setText("Set Scale - " + 72223.819286);
        } else if (mapScale == 36111.909643) {
          mSeekBar.setProgress(14);
          mSeekBarScale.setText("Set Scale - " + 36111.909643);
        } else if (mapScale == 18055.954822) {
          mSeekBar.setProgress(15);
          mSeekBarScale.setText("Set Scale - " + 18055.954822);
        } else if (mapScale == 9027.977411) {
          mSeekBar.setProgress(16);
          mSeekBarScale.setText("Set Scale - " + 9027.977411);
        } else if (mapScale == 4513.988705) {
          mSeekBar.setProgress(17);
          mSeekBarScale.setText("Set Scale - " + 4513.988705);
        } else if (mapScale == 2256.994353) {
          mSeekBar.setProgress(18);
          mSeekBarScale.setText("Set Scale - " + 2256.994353);
        } else if (mapScale == 1128.497176) {
          mSeekBar.setProgress(19);
          mSeekBarScale.setText("Set Scale - " + 1128.497176);
        } else if (mapScale == 564.248588) {
          mSeekBar.setProgress(20);
          mSeekBarScale.setText("Set Scale - " + 564.248588);
        } else if (mapScale == 282.124294) {
          mSeekBar.setProgress(21);
          mSeekBarScale.setText("Set Scale - " + 282.124294);
        } else if (mapScale == 141.062147) {
          mSeekBar.setProgress(22);
          mSeekBarScale.setText("Set Scale - " + 141.062147);
        } else if (mapScale == 70.5310735) {
          mSeekBar.setProgress(23);
          mSeekBarScale.setText("Set Scale - " + 70.5310735);
        }

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
}
