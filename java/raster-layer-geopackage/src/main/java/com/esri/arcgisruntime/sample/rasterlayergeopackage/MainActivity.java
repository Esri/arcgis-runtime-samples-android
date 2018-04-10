package com.esri.arcgisruntime.sample.rasterlayergeopackage;

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

import com.esri.arcgisruntime.data.GeoPackage;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.Raster;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map with the BasemapType light gray canvas
    ArcGISMap map = new ArcGISMap(Basemap.Type.LIGHT_GRAY_CANVAS, 39.7294, -104.8319, 11);

    // set the map to be displayed in this view
    mMapView.setMap(map);

    requestReadPermission();
  }

  private void rasterLayerGeoPackage() {

    // open the GeoPackage
    GeoPackage geoPackage = new GeoPackage(
        Environment.getExternalStorageDirectory() + getString(R.string.geopackage_path));
    geoPackage.loadAsync();
    geoPackage.addDoneLoadingListener(() -> {
      if (geoPackage.getLoadStatus() == LoadStatus.LOADED) {
        if (!geoPackage.getGeoPackageRasters().isEmpty()) {
          // read raster images and get the first one
          Raster geoPackageRaster = geoPackage.getGeoPackageRasters().get(0);

          // create a layer to show the raster
          RasterLayer geoPackageRasterLayer = new RasterLayer(geoPackageRaster);

          // add the image as a raster layer to the map (with default symbology)
          mMapView.getMap().getOperationalLayers().add(geoPackageRasterLayer);
          
        } else {
          String emptyMessage = "No rasters found in this GeoPackage!";
          Toast.makeText(MainActivity.this, emptyMessage, Toast.LENGTH_LONG).show();
          Log.e(TAG, emptyMessage);
        }
      } else {
        String error = "GeoPackage failed to load!";
        Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Request read permission on the device.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this,
        reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      rasterLayerGeoPackage();
    } else {
      // request permission
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      rasterLayerGeoPackage();
    } else {
      // report to user that permission was denied
      Toast.makeText(MainActivity.this, getResources().getString(R.string.read_permission_denied),
          Toast.LENGTH_SHORT).show();
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
