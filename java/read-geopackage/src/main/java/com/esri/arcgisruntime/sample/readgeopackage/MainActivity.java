package com.esri.arcgisruntime.sample.readgeopackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.data.GeoPackage;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.GeoPackageRaster;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate mapview from layout
    mMapView = findViewById(R.id.mapView);

    // create a new map centered on Aurora Colorado
    ArcGISMap map = new ArcGISMap(Basemap.Type.STREETS, 39.7294, -104.8319, 11);

    HashMap layers = new HashMap();
    final List<String> layersNotInTheMap = new ArrayList<>();

    // open the GeoPackage
    GeoPackage geoPackage = new GeoPackage(
        Environment.getExternalStorageDirectory() + getString(R.string.geopackage_path));
    geoPackage.loadAsync();

    geoPackage.addDoneLoadingListener(() -> {

      // loop through each GeoPackageRaster
      for (final GeoPackageRaster geoPackageRaster : geoPackage.getGeoPackageRasters()) {
        // create a RasterLayer from the GeoPackageRaster
        final RasterLayer rasterLayer = new RasterLayer(geoPackageRaster);

        // set the opacity on the RasterLayer to partially visible
        rasterLayer.setOpacity(0.55f);

        // load the RasterLayer so we can get to it's properties
        rasterLayer.loadAsync();
        rasterLayer.addDoneLoadingListener(() -> {

          // create a string variable to hold the human-readable name of the RasterLayer for display in the ListBox
          // and the hash map - it will initially be an empty string
          String rasterLayerName = "";

          if (rasterLayer.getName() != "") {
            // we have a good human-readable name for the RasterLayer that came from the RasterLayer.Name property
            rasterLayerName = rasterLayer.getName();
          } else if (geoPackageRaster.getPath().substring(geoPackageRaster.getPath().lastIndexOf("/") + 1) != "") {
            // we did not get a good human-readable name from the RasterLayer from .getName(), get the good
            // human-readable name from the GeoPackageRaster.Path instead
            rasterLayerName = geoPackageRaster.getPath().substring(geoPackageRaster.getPath().lastIndexOf("/") + 1);
          }

          // append the 'type of layer' to the myRasterLayerName string to display in the listBox and as the key for
          // the hash map
          rasterLayerName = rasterLayerName + " - RasterLayer";

          // add the name of the RasterLayer and the RasterLayer itself into the layers hash map
          layers.put(rasterLayerName, rasterLayer);

          // add the name of the RasterLayer to the ListBox of layers not in map
          layersNotInTheMap.add(rasterLayerName);
        });
      }
    });
  }
}
