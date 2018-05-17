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

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.LatitudeLongitudeGrid;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.MgrsGrid;
import com.esri.arcgisruntime.mapping.view.UsngGrid;
import com.esri.arcgisruntime.mapping.view.UtmGrid;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private CheckBox mLabelsCheckBox;
  private Spinner mGridSpinner;
  private Button mMenuButton;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate views from layout
    mMapView = findViewById(R.id.mapView);
    mMenuButton = findViewById(R.id.menu_button);

    // set up AlertDialog to display grid options
    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    final View view = getLayoutInflater().inflate(R.layout.popup_menu,null);
    mGridSpinner = view.findViewById(R.id.layer_spinner);
    mLabelsCheckBox = view.findViewById(R.id.labels_checkBox);
    builder.setView(view);
    final AlertDialog dialog = builder.create();

    // create drop-down list of different grids
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,
        getResources().getStringArray(R.array.layers_array));
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mGridSpinner.setAdapter(adapter);

    // create a map with imagery basemap
    ArcGISMap map = new ArcGISMap(Basemap.createImagery());
    // set viewpoint
    final Point center = new Point(-7702852.905619,6217972.345771,23227,SpatialReference.create(3857));
    map.setInitialViewpoint(new Viewpoint(center,23227));
    // set the map to be displayed in this view
    mMapView.setMap(map);

    // set defaults on grid
    mMapView.setGrid(new LatitudeLongitudeGrid());
    mLabelsCheckBox.setChecked(true);

    // add available grids to drop-down menu
    mGridSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // set the grid type
        switch (position) {
          case 0:
            mMapView.setGrid(new LatitudeLongitudeGrid());
            break;
          case 1:
            mMapView.setGrid(new MgrsGrid());
            break;
          case 2:
            mMapView.setGrid(new UtmGrid());
            break;
          case 3:
            mMapView.setGrid(new UsngGrid());
            break;
        }
        setLabelVisibility();
      }
      @Override public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(MainActivity.this, "Nothing Selected", Toast.LENGTH_SHORT).show();
      }
    });

    // turn labels on/off when the checkbox is clicked
    mLabelsCheckBox.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        setLabelVisibility();
      }
    });

    // display pop-up box when button is clicked
    mMenuButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        dialog.show();
      }
    });
  }

  public void setLabelVisibility(){
    if (mLabelsCheckBox.isChecked()){
      mMapView.getGrid().setLabelVisible(true);
    } else{
      mMapView.getGrid().setLabelVisible(false);
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