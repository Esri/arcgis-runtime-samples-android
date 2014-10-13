package com.esri.arcgis.android.samples.wmslayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.MapView;
import com.esri.android.map.ogc.WMSLayer;


public class MainActivity extends Activity {

    MapView mMapView;
    WMSLayer wmsLayer;
    String wmsURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // after the content of this activity is set
        // the map can be accessed from the layout
        mMapView = (MapView)findViewById(R.id.map);

        wmsURL = "http://wms.openweathermap.org/service";
        wmsLayer = new WMSLayer(wmsURL);
        wmsLayer.setImageFormat("image/png");
        String[] visibleLayers = {"clouds", "precipitation"};
        wmsLayer.setVisibleLayer(visibleLayers);
        wmsLayer.setOpacity(0.5f);
        mMapView.addLayer(wmsLayer);


        // Set the Esri logo to be visible, and enable map to wrap around date line.
        mMapView.setEsriLogoVisible(true);
        mMapView.enableWrapAround(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
