package com.esri.arcgisruntime.displaykml;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.KmlLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.kml.KmlDataset;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get the reference to the map view
    mMapView = findViewById(R.id.mapView);

    // create a map with the dark gray canvas basemap
    ArcGISMap map = new ArcGISMap(Basemap.Type.DARK_GRAY_CANVAS_VECTOR, 39, -98, 4);

    // set the map to the map view
    mMapView.setMap(map);

    // Set the initial KML source
    changeSourceToURL();

  }

  private void display(KmlLayer kmlLayer) {

    // clear the existing layers from the map
    mMapView.getMap().getOperationalLayers().clear();

    // add the loaded KML layer to the map
    mMapView.getMap().getOperationalLayers().add(kmlLayer);
  }

  private void changeSourceToURL() {
    // create a kml dataset from a URL
    KmlDataset kmlDataset = new KmlDataset(getString(R.string.kml_url));

    // a KML layer created from a remote KML file
    KmlLayer kmlLayer = new KmlLayer(kmlDataset);
    display(kmlLayer);
    //Toast.makeText(this, "Displaying a KML layer from a URL", Toast.LENGTH_LONG).show();
  }

  private void changeSourceToLocalFile() {
    // a dataset created by referencing the name of a KML file stored locally on the device
    KmlDataset kmlDataset = new KmlDataset("US_State_Capitals");
    // a KML layer created from a local KML file
    KmlLayer kmlLayer = new KmlLayer(kmlDataset);
    display(kmlLayer);
    Toast.makeText(this, "Displaying a KML layer from a local file", Toast.LENGTH_LONG).show();
  }

  private void changeSourceToPortalItem() {
    // create a portal to ArcGIS Online
    Portal portal = new Portal(getString(R.string.arcgis_online_url));
    portal.loadAsync();
    portal.addDoneLoadingListener(() -> {
      if (portal.getLoadStatus() == LoadStatus.LOADED) {
        // create a portal item from a kml item id
        PortalItem portalItem = new PortalItem(portal, getString(R.string.kml_item_id));
        portalItem.addDoneLoadingListener(() -> {
          // a KML layer created from an ArcGIS Online portal item
          KmlLayer kmlLayer = new KmlLayer(portalItem);
          display(kmlLayer);
          Toast.makeText(this, "Displaying a KML layer from a portal item", Toast.LENGTH_LONG).show();
        });
      } else {
        String error = "Error loading portal: " + portal.getLoadError().getCause().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.kml_sources, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.kmlFromUrl) {
      changeSourceToURL();
    } else if (item.getItemId() == R.id.kmlfromPortal) {
      changeSourceToPortalItem();
    }
    return super.onOptionsItemSelected(item);
  }
}

