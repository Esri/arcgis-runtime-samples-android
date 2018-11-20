package com.esri.arcgisruntime.mapreferencescale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private SeekBar mReferenceScaleSeekBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    requestReadPermission();

    TextView currReferenceScaleTextView = findViewById(R.id.currReferenceScale);
    mReferenceScaleSeekBar = findViewById(R.id.reference_scale_seek_bar);
    mReferenceScaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        currReferenceScaleTextView.setText(String.valueOf(progress));
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
        mMapView.getMap().setReferenceScale(seekBar.getProgress());
        Toast.makeText(MainActivity.this, "Map's reference scale set to " + mMapView.getMap().getReferenceScale(),
            Toast.LENGTH_SHORT).show();
      }
    });

  }

  /**
   * TODO
   */
  private void mapReferenceScale() {
    // load Yenisey mobile map package
    MobileMapPackage mapPackage = new MobileMapPackage(
        Environment.getExternalStorageDirectory() + getString(R.string.yenisey_mmpk_path));
    mapPackage.loadAsync();
    mapPackage.addDoneLoadingListener(() -> {
      // get the first map from the map package
      ArcGISMap map = mapPackage.getMaps().get(0);
      if (mapPackage.getLoadStatus() == LoadStatus.LOADED) {
        // set the map package map to map view's map
        mMapView.setMap(map);

        // initialize the seek bar to the map's initial reference scale
        map.addDoneLoadingListener(() -> {
          int referenceScale = (int) map.getReferenceScale();
          mReferenceScaleSeekBar.setMax(referenceScale * 2);
          mReferenceScaleSeekBar.setProgress(referenceScale);
        });
      } else {
        String error = "Map package failed to load: " + mapPackage.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  private void setScaleSymbol(FeatureLayer featureLayer, boolean isScaleSymbols) {
    featureLayer.setScaleSymbols(isScaleSymbols);
    StringBuilder scalingMessage = new StringBuilder(featureLayer.getName());
    if (isScaleSymbols) {
      scalingMessage.append(" is being scaled.");
    } else {
      scalingMessage.append(" is not being scaled.");
    }
    Toast.makeText(this, scalingMessage, Toast.LENGTH_SHORT).show();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.reference_scale, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int i = item.getItemId();
    if (i == R.id.setScaleSymbolsCities) {
      setScaleSymbol((FeatureLayer) mMapView.getMap().getOperationalLayers().get(0), !item.isChecked());
      item.setChecked(!item.isChecked());
    } else if (i == R.id.setScaleSymbolsRiver) {
      setScaleSymbol((FeatureLayer) mMapView.getMap().getOperationalLayers().get(1), !item.isChecked());
      item.setChecked(!item.isChecked());
    } else {
      Log.e(TAG, "Menu option not implemented");
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Request read permission on the device for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      mapReferenceScale();
    } else {
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      mapReferenceScale();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.map_reference_read_permission_denied), Toast.LENGTH_SHORT).show();
    }
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
