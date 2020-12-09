/* Copyright 2018 Esri
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
package com.esri.arcgisruntime.sample.displaygrid;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Grid;
import com.esri.arcgisruntime.mapping.view.LatitudeLongitudeGrid;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.MgrsGrid;
import com.esri.arcgisruntime.mapping.view.UsngGrid;
import com.esri.arcgisruntime.mapping.view.UtmGrid;
import com.esri.arcgisruntime.symbology.LineSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private CheckBox mLabelsCheckBox;
  private int mLineColor;
  private int mLabelColor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate views from activity_main
    mMapView = findViewById(R.id.mapView);
    Button mMenuButton = findViewById(R.id.menu_button);

    // set up a popup menu to manage grid settings
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    final View view = getLayoutInflater().inflate(R.layout.popup_menu, null);
    builder.setView(view);
    final AlertDialog dialog = builder.create();

    // inflate views from popup_menu
    Spinner mGridSpinner = view.findViewById(R.id.layer_spinner);
    mLabelsCheckBox = view.findViewById(R.id.labels_checkBox);
    Spinner mColorsSpinner = view.findViewById(R.id.line_color_spinner);
    Spinner mLabelColorSpinner = view.findViewById(R.id.label_color_spinner);

    // create drop-down list of different grids
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
        getResources().getStringArray(R.array.layers_array));
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mGridSpinner.setAdapter(adapter);

    // create drop-down list of different colors
    ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
        getResources().getStringArray(R.array.colors_array));
    colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mColorsSpinner.setAdapter(colorAdapter);

    // create drop-down list of different label colors
    ArrayAdapter<String> labelColorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
        getResources().getStringArray(R.array.colors_array));
    labelColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mLabelColorSpinner.setAdapter(labelColorAdapter);

    // create a map with imagery basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);
    // set the map to be displayed in this view
    mMapView.setMap(map);
    // set viewpoint
    final Point center = new Point(-7702852.905619, 6217972.345771, 23227, SpatialReference.create(3857));
    mMapView.setViewpoint(new Viewpoint(center, 23227));

    // set defaults on grid
    mMapView.setGrid(new LatitudeLongitudeGrid());
    mLabelsCheckBox.setChecked(true);

    // change different grids on the Map View
    mGridSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // set the grid type
        switch (position) {
          case 0:
            mMapView.setGrid(new LatitudeLongitudeGrid());
            mMapView.setViewpointScaleAsync(23227);
            break;
          case 1:
            mMapView.setGrid(new MgrsGrid());
            mMapView.setViewpointScaleAsync(23227);
            break;
          case 2:
            mMapView.setGrid(new UtmGrid());
             mMapView.setViewpointScaleAsync(10000000);
            break;
          case 3:
            mMapView.setGrid(new UsngGrid());
            mMapView.setViewpointScaleAsync(23227);
            break;
          default:
            Toast.makeText(MainActivity.this,"Unsupported option", Toast.LENGTH_SHORT).show();
            break;
        }
        // make sure settings persist on grid type change
        setLabelVisibility();
        changeGridColor(mLineColor);
        changeLabelColor(mLabelColor);
      }

      @Override public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    // change grid lines color
    mColorsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //set the color
        switch (position) {
          case 0:
            mLineColor = Color.RED;
            break;
          case 1:
            mLineColor = Color.WHITE;
            break;
          case 2:
            mLineColor = Color.BLUE;
            break;
          default:
            Toast.makeText(MainActivity.this,"Unsupported option", Toast.LENGTH_SHORT).show();
            break;
        }
        changeGridColor(mLineColor);
      }

      @Override public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    // change grid labels color
    mLabelColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // set the color
        switch (position) {
          case 0:
            mLabelColor = Color.RED;
            break;
          case 1:
            mLabelColor = Color.WHITE;
            break;
          case 2:
            mLabelColor = Color.BLUE;
            break;
          default:
            Toast.makeText(MainActivity.this,"Unsupported option", Toast.LENGTH_SHORT).show();
            break;
        }
        changeLabelColor(mLabelColor);
      }

      @Override public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    // hide and show label visibility when the checkbox is clicked
    mLabelsCheckBox.setOnClickListener(v -> setLabelVisibility());

    // display pop-up box when button is clicked
    mMenuButton.setOnClickListener(v -> dialog.show());
  }

  private void setLabelVisibility() {
    mMapView.getGrid().setLabelVisible(mLabelsCheckBox.isChecked());
  }

  private void changeGridColor(int color) {
    Grid grid = mMapView.getGrid();
    int gridLevels = grid.getLevelCount();
    for (int gridLevel = 0; gridLevel <= gridLevels - 1; gridLevel++) {
      LineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, color, gridLevel + 1);
      grid.setLineSymbol(gridLevel, lineSymbol);
    }
  }

  private void changeLabelColor(int color) {
    Grid grid = mMapView.getGrid();
    int gridLevels = grid.getLevelCount();
    for (int gridLevel = 0; gridLevel <= gridLevels - 1; gridLevel++) {
      TextSymbol textSymbol = new TextSymbol();
      textSymbol.setColor(color);
      textSymbol.setSize(14);
      textSymbol.setHorizontalAlignment(TextSymbol.HorizontalAlignment.LEFT);
      textSymbol.setVerticalAlignment(TextSymbol.VerticalAlignment.BOTTOM);
      textSymbol.setHaloColor(Color.WHITE);
      textSymbol.setHaloWidth(gridLevel + 1);
      grid.setTextSymbol(gridLevel, textSymbol);
    }
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
