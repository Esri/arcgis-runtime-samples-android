package com.esri.arcgisruntime.mapreferencescale;

import java.io.File;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a map and add it to the map view
    ArcGISMap map = new ArcGISMap();
    mMapView.setMap(map);

    // Load the refScale mobile map package
    String mmpkPath = new File(Util.getMmpkDirectory(), "refScale.mmpk").getAbsolutePath();
    mMapPackage = new MobileMapPackage(mmpkPath);
    mMapPackage.loadAsync();
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
