/* Copyright 2016 Esri
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

package com.esri.arcgisruntime.sample.analyzehotspots;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingJob;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingParameters;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingResult;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingString;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingTask;

public class MainActivity extends AppCompatActivity {

  private String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private GeoprocessingTask mGeoprocessingTask;
  private GeoprocessingJob mGeoprocessingJob;

  private EditText fromDateText;
  private EditText toDateText;

  private SimpleDateFormat mSimpleDateFormatter;
  private Date mMinDate;
  private Date mMaxDate;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSimpleDateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    try {
      mMinDate = mSimpleDateFormatter.parse("01/01/1998");
      mMaxDate = mSimpleDateFormatter.parse("31/05/1998");
    } catch (ParseException e) {
      e.printStackTrace();
    }

    final Button analyzeButton = (Button) findViewById(R.id.analyze_button);
    analyzeButton.setVisibility(View.GONE);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());

    //center for initial viewpoint
    Point center = new Point(-13671170, 5693633, SpatialReference.create(3857));

    //set initial viewpoint
    map.setInitialViewpoint(new Viewpoint(center, 57779));

    // set the map to the map view
    mMapView.setMap(map);

    // initialize geoprocessing task with the url of the service
    mGeoprocessingTask = new GeoprocessingTask(
        "http://sampleserver6.arcgisonline.com/arcgis/rest/services/911CallsHotspot/GPServer/911%20Calls%20Hotspot");
    mGeoprocessingTask.loadAsync();

    mGeoprocessingTask.addDoneLoadingListener(new Runnable() {
      @Override public void run() {
        analyzeButton.setVisibility(View.VISIBLE);
        analyzeButton.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            analyzeHotspots("1998-01-01", "1998-01-31");
          }
        });
      }
    });

    // custom dialog
    final Dialog dialog = new Dialog(MainActivity.this);
    dialog.setContentView(R.layout.custom_alert_dialog);
    dialog.setTitle("Select Date Range for Analysis");

    fromDateText = (EditText) dialog.findViewById(R.id.fromDateText);
    toDateText = (EditText) dialog.findViewById(R.id.toDateText);

    final Calendar calendar = Calendar.getInstance();

    final DatePickerDialog.OnDateSetListener fromDate = new DatePickerDialog.OnDateSetListener() {
      @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        StringBuilder date = new StringBuilder().append(year).append("-").append(month + 1).append("-")
            .append(dayOfMonth);
        fromDateText.setText(date);

      }
    };

    final DatePickerDialog.OnDateSetListener toDate = new DatePickerDialog.OnDateSetListener() {
      @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        StringBuilder date = new StringBuilder().append(year).append("-").append(month + 1).append("-")
            .append(dayOfMonth);
        toDateText.setText(date);

      }
    };

    fromDateText.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        showCalendar(InputCalendar.From);
      }
    });

    toDateText.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        new DatePickerDialog(MainActivity.this, toDate, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show();
      }
    });

    Button doneButton = (Button) dialog.findViewById(R.id.doneButton);
    // if button is clicked, close the custom dialog
    doneButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
      }
    });

    dialog.show();
  }

  private void showCalendar(InputCalendar inputCalendar) {
    Calendar calendar = Calendar.getInstance();
    DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, fromDate,
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH));
    datePickerDialog.getDatePicker().setMinDate(mMinDate.getTime());
    datePickerDialog.getDatePicker().setMaxDate(mMaxDate.getTime());
    datePickerDialog.show();
  }

  private void analyzeHotspots(final String from, final String to) {

    // cancel previous job request
    if (mGeoprocessingJob != null) {
      mGeoprocessingJob.cancel();
    }

    // parameters
    final ListenableFuture<GeoprocessingParameters> paramsFuture = mGeoprocessingTask.createDefaultParametersAsync();
    paramsFuture.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          Log.i(TAG, "Parameters loaded.");
          GeoprocessingParameters geoprocessingParameters = paramsFuture.get();
          geoprocessingParameters.setProcessSpatialReference(mMapView.getSpatialReference());
          geoprocessingParameters.setOutputSpatialReference(mMapView.getSpatialReference());

          StringBuilder queryString = new StringBuilder("(\"DATE\" > date '")
              .append(from)
              .append(" 00:00:00' AND \"DATE\" < date '")
              .append(to)
              .append(" 00:00:00')");

          geoprocessingParameters.getInputs().put("Query", new GeoprocessingString(queryString.toString()));

          Log.i(TAG, "Query: " + queryString.toString());

          // create job
          mGeoprocessingJob = mGeoprocessingTask.createJob(geoprocessingParameters);

          // start job
          mGeoprocessingJob.start();

          mGeoprocessingJob.addJobDoneListener(new Runnable() {
            @Override public void run() {
              if (mGeoprocessingJob.getStatus() == Job.Status.SUCCEEDED) {
                Log.d(TAG, "Job succeeded.");
                // a map image layer is generated as a result. Remove any layer previously added to the map
                mMapView.getMap().getOperationalLayers().clear();

                GeoprocessingResult geoprocessingResult = mGeoprocessingJob.getResult();
                final ArcGISMapImageLayer hotspotMapImageLayer = geoprocessingResult.getMapImageLayer();

                // add the new layer to the map
                mMapView.getMap().getOperationalLayers().add(hotspotMapImageLayer);

                hotspotMapImageLayer.addDoneLoadingListener(new Runnable() {
                  @Override public void run() {
                    // set the map viewpoint to the MapImageLayer, once loaded
                    mMapView.setViewpointGeometryAsync(hotspotMapImageLayer.getFullExtent());
                  }
                });
              } else {
                Log.e(TAG, "Job did not succeed!");
              }
            }
          });
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
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

public enum InputCalendar {
  From, //
  To
}
