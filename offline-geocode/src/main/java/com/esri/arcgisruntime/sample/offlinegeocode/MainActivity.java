package com.esri.arcgisruntime.sample.offlinegeocode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private ArcGISMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        // create a map with Topographic Basemap
        mMap = new ArcGISMap(Basemap.createTopographic());
        // set the map to be displayed in this view
        mMapView.setMap(mMap);
    }
}
