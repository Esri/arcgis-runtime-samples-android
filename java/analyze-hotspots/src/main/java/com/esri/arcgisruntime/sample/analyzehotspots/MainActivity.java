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

package com.esri.arcgisruntime.sample.analyzehotspots;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingJob;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingParameters;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingResult;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingString;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// enum to flag whether the date picker calendar shown should be for the 'from' or 'to' date
enum InputCalendar {
  From,
  To
}

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private GeoprocessingTask mGeoprocessingTask;
  private GeoprocessingJob mGeoprocessingJob;

  private EditText fromDateText;
  private EditText toDateText;

  private SimpleDateFormat mSimpleDateFormatter;
  private Date mMinDate;
  private Date mMaxDate;

  private boolean mIsCanceled;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // create a simple date formatter to parse strings to date
    mSimpleDateFormatter = new SimpleDateFormat(getString(R.string.date_format), Locale.US);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map with the Basemap Style topographic
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);

    // set the map to the map view
    mMapView.setMap(map);

    // center for initial viewpoint
    Point center = new Point(-13671170, 5693633, SpatialReference.create(3857));

    // set initial viewpoint
    mMapView.setViewpoint(new Viewpoint(center, 57779));

    // initialize geoprocessing task with the url of the service
    mGeoprocessingTask = new GeoprocessingTask(getString(R.string.hotspot_911_calls));
    mGeoprocessingTask.loadAsync();

    FloatingActionButton calendarFAB = findViewById(R.id.calendarButton);

    calendarFAB.setOnClickListener(v -> showDateRangeDialog());

    calendarFAB.performClick();
  }

  /**
   * Creates the date range dialog. Includes listeners to handle click events, which call showCalendar(...) or
   * analyzeHotspots(...).
   */
  private void showDateRangeDialog() {
    // create custom dialog
    final Dialog dialog = new Dialog(this);
    dialog.setContentView(R.layout.custom_alert_dialog);
    dialog.setCancelable(true);

    try {
      // set default date range for the data set
      mMinDate = mSimpleDateFormatter.parse(getString(R.string.min_date));
      mMaxDate = mSimpleDateFormatter.parse(getString(R.string.max_date));
    } catch (ParseException e) {
      Log.e(TAG, "Error in date format: " + e.getMessage());
    }

    fromDateText = dialog.findViewById(R.id.fromDateText);
    toDateText = dialog.findViewById(R.id.toDateText);

    fromDateText.setOnClickListener(v -> showCalendar(InputCalendar.From));
    toDateText.setOnClickListener(v -> showCalendar(InputCalendar.To));

    Button analyzeButton = dialog.findViewById(R.id.analyzeButton);
    // if button is clicked, close the custom dialog
    analyzeButton.setOnClickListener(v -> {
      analyzeHotspots(fromDateText.getText().toString(), toDateText.getText().toString());
      dialog.dismiss();
    });

    dialog.show();
  }

  /**
   * Shows a date picker dialog and writes the date chosen to the correct editable text.
   *
   * @param inputCalendar enum which specifies which editable text the chosen date should be written to
   */
  private void showCalendar(final InputCalendar inputCalendar) {
    // create a date set listener
    DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, month, dayOfMonth) -> {
      // build the correct date format for the query
      StringBuilder date = new StringBuilder()
          .append(year)
          .append("-")
          .append(month + 1)
          .append("-")
          .append(dayOfMonth);
      // set the date to correct text view
      if (inputCalendar == InputCalendar.From) {
        fromDateText.setText(date);
        try {
          // limit the min date to after from date
          mMinDate = mSimpleDateFormatter.parse(date.toString());
        } catch (ParseException e) {
          e.printStackTrace();
        }
      } else if (inputCalendar == InputCalendar.To) {
        toDateText.setText(date);
        try {
          // limit the maximum date to before the to date
          mMaxDate = mSimpleDateFormatter.parse(date.toString());
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }
    };

    // define the date picker dialog
    Calendar calendar = Calendar.getInstance();
    DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener,
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    datePickerDialog.getDatePicker().setMinDate(mMinDate.getTime());
    datePickerDialog.getDatePicker().setMaxDate(mMaxDate.getTime());
    if (inputCalendar == InputCalendar.From) {
      // start from calendar from min date
      datePickerDialog.updateDate(1998, 0, 1);
    }
    datePickerDialog.show();
  }

  /**
   * Runs the geoprocessing job, updating progress while loading. On job done, loads the resulting
   * ArcGISMapImageLayer to the map and resets the Viewpoint of the MapView.
   *
   * @param from string which holds a date
   * @param to   string which holds a date
   */
  private void analyzeHotspots(final String from, final String to) {
    // cancel previous job request
    if (mGeoprocessingJob != null) {
      mGeoprocessingJob.cancel();
    }

    // a map image layer is generated as a result. Remove any layer previously added to the map
    mMapView.getMap().getOperationalLayers().clear();

    // set canceled flag to false
    mIsCanceled = false;

    // parameters
    final ListenableFuture<GeoprocessingParameters> paramsFuture = mGeoprocessingTask.createDefaultParametersAsync();
    paramsFuture.addDoneListener(() -> {
      try {
        GeoprocessingParameters geoprocessingParameters = paramsFuture.get();
        geoprocessingParameters.setProcessSpatialReference(mMapView.getSpatialReference());
        geoprocessingParameters.setOutputSpatialReference(mMapView.getSpatialReference());

        StringBuilder queryString = new StringBuilder("(\"DATE\" > date '")
            .append(from)
            .append(" 00:00:00' AND \"DATE\" < date '")
            .append(to)
            .append(" 00:00:00')");

        geoprocessingParameters.getInputs().put("Query", new GeoprocessingString(queryString.toString()));

        Log.i(TAG, "Query: " + queryString);

        // create job
        mGeoprocessingJob = mGeoprocessingTask.createJob(geoprocessingParameters);

        // start job
        mGeoprocessingJob.start();

        // create a dialog to show progress of the geoprocessing job
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Running geoprocessing job");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
          dialog.dismiss();
          // set canceled flag to true
          mIsCanceled = true;
          mGeoprocessingJob.cancel();
        });
        progressDialog.show();

        // update progress
        mGeoprocessingJob.addProgressChangedListener(() -> progressDialog.setProgress(mGeoprocessingJob.getProgress()));

        mGeoprocessingJob.addJobDoneListener(() -> {
          progressDialog.dismiss();
          if (mGeoprocessingJob.getStatus() == Job.Status.SUCCEEDED) {
            Log.i(TAG, "Job succeeded.");

            GeoprocessingResult geoprocessingResult = mGeoprocessingJob.getResult();
            final ArcGISMapImageLayer hotspotMapImageLayer = geoprocessingResult.getMapImageLayer();

            // add the new layer to the map
            mMapView.getMap().getOperationalLayers().add(hotspotMapImageLayer);

            hotspotMapImageLayer.addDoneLoadingListener(() -> {
              if (hotspotMapImageLayer.getLoadStatus() == LoadStatus.LOADED) {
                // set the map viewpoint to the MapImageLayer, once loaded
                mMapView.setViewpointGeometryAsync(hotspotMapImageLayer.getFullExtent());
              } else {
                Toast.makeText(this, "Error loading hot spot image layer: " + hotspotMapImageLayer.getLoadError().getCause().getMessage(),
                    Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading hot spot image layer: " + hotspotMapImageLayer.getLoadError().getCause().getMessage());
              }
            });
          } else if (mIsCanceled) {
            Toast.makeText(this, "Job canceled.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Job cancelled.");
          } else {
            Log.e(TAG, "Job did not succeed!");
            Toast.makeText(this, "Job did not succeed!", Toast.LENGTH_LONG).show();
          }
        });
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
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
