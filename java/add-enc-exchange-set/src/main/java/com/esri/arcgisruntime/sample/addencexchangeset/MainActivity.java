package com.esri.arcgisruntime.sample.addencexchangeset;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.hydrography.EncCell;
import com.esri.arcgisruntime.hydrography.EncDataset;
import com.esri.arcgisruntime.hydrography.EncExchangeSet;
import com.esri.arcgisruntime.layers.EncLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(Basemap.createOceans());
    // set the map to be displayed in this view
    mMapView.setMap(map);

    requestReadPermission();
  }

  private void addEncExchangeSet() {

    List<String> encPath = new ArrayList<>();
    encPath.add(Environment.getExternalStorageDirectory() + getString(R.string.enc_path));

    EncExchangeSet encExchangeSet = new EncExchangeSet(encPath);
    encExchangeSet.loadAsync();
    encExchangeSet.addDoneLoadingListener(() -> {
      if (encExchangeSet.getLoadStatus() == LoadStatus.LOADED) {
        // store a list of data set extent's - will be used to zoom the map view to the full extent of the Exchange Set
        List<Geometry> dataSetExtents = new ArrayList<>();
        // add each data set as a layer
        for (EncDataset encDataset : encExchangeSet.getDatasets()) {
          dataSetExtents.add(encDataset.getExtent());

          // create a layer from an enc cell from the data set
          EncCell encCell = new EncCell(encDataset);
          Log.d(TAG, "ENC cell path: " + encCell.getPath());
          encCell.loadAsync();
          encCell.addDoneLoadingListener(() -> {
            if (encCell.getLoadStatus() == LoadStatus.LOADED) {
              Log.d(TAG, "ENC cell loaded");
              EncLayer encLayer = new EncLayer(encCell);
              // add the layer to the map
              mMapView.getMap().getOperationalLayers().add(encLayer);
              // add the data set extent to a list
            } else {
              String error = "Error loading ENC cell: " + encCell.getLoadError().getMessage();
              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
              Log.e(TAG, error);
            }
          });
        }
        // use the geometry engine to compute the full extent of the ENC exchange set
        Envelope fullExtent = GeometryEngine.combineExtents(dataSetExtents);

        // set the view point
        mMapView.setViewpointAsync(new Viewpoint(fullExtent));
      } else {
        String error = "Error loading ENC exchange set: " + encExchangeSet.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Request read external storage permissions for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      // do something
      addEncExchangeSet();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      // do something
      addEncExchangeSet();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.enc_read_permission_denied), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
