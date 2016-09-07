package com.esri.arcgisruntime.sample.featurelayergeodatabase;

import java.io.File;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create the path to local data
        File demoDataFile = Environment.getExternalStorageDirectory();
        String offlineDataSDCardDirName = this.getResources().getString(R.string.data_dir);
        String vtpkFilename = this.getResources().getString(R.string.vtpk_file);
        String geodbFilename = this.getResources().getString(R.string.geodb_file);

        // full path to data
        String mVtpk = demoDataFile + File.separator + offlineDataSDCardDirName + File.separator + vtpkFilename;
        String mGeoDb = demoDataFile + File.separator + offlineDataSDCardDirName + File.separator + geodbFilename;

        // create MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // create a new ArcGISVectorTiledLayer from local path
        ArcGISVectorTiledLayer vectorTiledLayer = new ArcGISVectorTiledLayer(mVtpk);
        // create a Basemap instance for use in creating an ArcGISMap instance
        Basemap basemap = new Basemap(vectorTiledLayer);
        ArcGISMap map = new ArcGISMap(basemap);
        // set the map to be displayed in this view
        mMapView.setMap(map);
        mMapView.setWrapAroundMode(WrapAroundMode
                .ENABLE_WHEN_SUPPORTED);
        // set the initial viewpoint
        mMapView.setViewpoint(new Viewpoint(33.902017, -118.218533, 10));
    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }
}
