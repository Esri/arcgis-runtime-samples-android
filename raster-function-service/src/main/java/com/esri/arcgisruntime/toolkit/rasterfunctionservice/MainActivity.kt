package com.esri.arcgisruntime.toolkit.rasterfunctionservice

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.raster.ImageServiceRaster
import com.esri.arcgisruntime.raster.Raster
import com.esri.arcgisruntime.raster.RasterFunction
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create a Dark Gray Vectory BaseMap
        val map = ArcGISMap(Basemap.createDarkGrayCanvasVector())
        // set the map to be displayed in this view
        mapView.map = map
        // create image service raster as raster layer
        val imageServiceRaster = ImageServiceRaster(
                resources.getString(R.string.image_service_raster_url))
        val imageRasterLayer = RasterLayer(imageServiceRaster)
        map.operationalLayers.add(imageRasterLayer)
        // zoom to the extent of the raster service
        imageRasterLayer.addDoneLoadingListener {
            if(imageRasterLayer.loadStatus == LoadStatus.LOADED){
                // zoom to extent of raster
                mapView.setViewpointGeometryAsync(imageServiceRaster.serviceInfo.fullExtent)
                applyRasterFunction(imageServiceRaster)
            }
        }

    }

    /**
     *
     */
    fun applyRasterFunction(raster: Raster){
        val hillshadeSimplified = resources.getString(R.string.hillshade_simplified)
        val rasterFunction = RasterFunction.fromJson(hillshadeSimplified)
        val rasterFunctionArguments = rasterFunction.arguments
        rasterFunctionArguments.setRaster(resources.getString(R.string.app_name), raster)
        val raster = Raster(rasterFunction)
        val rasterLayer = RasterLayer(raster)
        mapView.map.operationalLayers.clear()
        mapView.map.operationalLayers.add(rasterLayer)
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }
}
