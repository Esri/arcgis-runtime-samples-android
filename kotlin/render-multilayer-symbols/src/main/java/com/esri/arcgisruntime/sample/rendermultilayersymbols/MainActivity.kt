/* Copyright 2022 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.rendermultilayersymbols

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.rendermultilayersymbols.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.*
import java.net.URL


class MainActivity : AppCompatActivity() {

    private var graphicsOverlay: GraphicsOverlay? = null
    private val TAG = MainActivity::class.java.simpleName

    // define offset used to keep a consistent distance between symbols in the same column
    private val offset = 20.0

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required
        // to access basemaps and other location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_LIGHT_GRAY)


        // create a graphics overlay
        graphicsOverlay = GraphicsOverlay()

        mapView.apply {
            // set the map to be displayed in the layout's map view
            mapView.map = map
            // add the graphic overlay to the map view
            graphicsOverlays.add(graphicsOverlay)
        }

        // create labels to go above each category of graphic
        addTextGraphics()

        // create picture marker symbols, from URI or bitmap
        addImageGraphics()

        // add graphics with simple vector marker symbol elements (MultilayerPoint Simple Markers on app UI)
        val solidFillSymbolLayer = SolidFillSymbolLayer(Color.RED)
        val multilayerPolygonSymbol =
            MultilayerPolygonSymbol(listOf(solidFillSymbolLayer))
        val solidStrokeSymbolLayer =
            SolidStrokeSymbolLayer(1.0, Color.RED, listOf(DashGeometricEffect()))
        val multilayerPolylineSymbol =
            MultilayerPolylineSymbol(listOf(solidStrokeSymbolLayer))

        // define vector element for a diamond, triangle and cross
        val diamondGeometry =
            Geometry.fromJson("{\"rings\":[[[0.0,2.5],[2.5,0.0],[0.0,-2.5],[-2.5,0.0],[0.0,2.5]]]}")
        val triangleGeometry =
            Geometry.fromJson("{\"rings\":[[[0.0,5.0],[5,-5.0],[-5,-5.0],[0.0,5.0]]]}")
        val crossGeometry =
            Geometry.fromJson("{\"paths\":[[[-1,1],[0,0],[1,-1]],[[1,1],[0,0],[-1,-1]]]}")

        // add red diamond graphic
        addGraphicsWithVectorMarkerSymbolElements(
            multilayerPolygonSymbol,
            diamondGeometry,
            0.0
        )
        // add red triangle graphic
        addGraphicsWithVectorMarkerSymbolElements(
            multilayerPolygonSymbol,
            triangleGeometry,
            offset
        )
        // add red cross graphic
        addGraphicsWithVectorMarkerSymbolElements(
            multilayerPolylineSymbol,
            crossGeometry,
            2 * offset
        )

        // create line marker symbols
        // similar to SimpleLineSymbolStyle.SHORTDASHDOTDOT
        addLineGraphicsWithMarkerSymbols(
            listOf(4.0, 6.0, 0.5, 6.0, 0.5, 6.0),
            0.0
        )
        // similar to SimpleLineSymbolStyle.SHORTDASH
        addLineGraphicsWithMarkerSymbols(
            listOf(4.0, 6.0),
            offset
        )
        // similar to SimpleLineSymbolStyle.DASHDOTDOT
        addLineGraphicsWithMarkerSymbols(
            listOf(7.0, 9.0, 0.5, 9.0),
            2 * offset
        )

        // create polygon marker symbols
        // cross-hatched diagonal lines
        addPolygonGraphicsWithMarkerSymbols(
            listOf(-45.0, 45.0),
            0.0
        )
        // hatched diagonal lines
        addPolygonGraphicsWithMarkerSymbols(
            listOf(-45.0),
            offset
        )
        // hatched vertical lines
        addPolygonGraphicsWithMarkerSymbols(
            listOf(90.0),
            2 * offset
        )

        // define vector element for a hexagon which will be used as the basis of a complex point
        val complexPointGeometry =
            Geometry.fromJson("{\"rings\":[[[-2.89,5.0],[2.89,5.0],[5.77,0.0],[2.89,-5.0],[-2.89,-5.0],[-5.77,0.0],[-2.89,5.0]]]}")

        // create the more complex multilayer graphics: a point, polygon, and polyline
        addComplexPoint(complexPointGeometry)
        addComplexPolygon()
        addComplexPolyline()
    }

    /**
     * Creates the label graphics to be displayed above each category of symbol,
     * and adds them to the graphics overlay.
     */
    private fun addTextGraphics() {
        graphicsOverlay?.graphics?.addAll(
            listOf(
                Graphic(
                    Point(-150.0, 50.0, SpatialReferences.getWgs84()),
                    getTextSymbol("MultilayerPoint\nSimple Markers")
                ),
                Graphic(
                    Point(-80.0, 50.0, SpatialReferences.getWgs84()),
                    getTextSymbol("MultilayerPoint\nPicture Markers")
                ),
                Graphic(
                    Point(0.0, 50.0, SpatialReferences.getWgs84()),
                    getTextSymbol("Multilayer\nPolyline")
                ),
                Graphic(
                    Point(65.0, 50.0, SpatialReferences.getWgs84()),
                    getTextSymbol("Multilayer\nPolygon")
                ),
                Graphic(
                    Point(130.0, 50.0, SpatialReferences.getWgs84()),
                    getTextSymbol("Multilayer\nComplex Symbols")
                )
            )
        )
    }

    /**
     * @return the TextSymbol with the [text] to be displayed on the map.
     */
    private fun getTextSymbol(text: String): TextSymbol {
        val textSymbol = TextSymbol(
            10F,
            text,
            Color.BLACK,
            TextSymbol.HorizontalAlignment.CENTER,
            TextSymbol.VerticalAlignment.MIDDLE
        )
        // give the text symbol a white background
        textSymbol.backgroundColor = Color.WHITE
        return textSymbol
    }

    /**
     * Create picture marker symbols from URI or bitmap.
     */
    private fun addImageGraphics() {
        // URI of image to display
        val bluePinImageURI =
            "http://sampleserver6.arcgisonline.com/arcgis/rest/services/Recreation/FeatureServer/0/images/e82f744ebb069bb35b234b3fea46deae"
        // load the PictureMarkerSymbolLayer using the image URI
        val pictureMarkerFromUri =
            PictureMarkerSymbolLayer(bluePinImageURI)
        pictureMarkerFromUri.addDoneLoadingListener {
            if (pictureMarkerFromUri.loadStatus == LoadStatus.LOADED) {
                // add loaded layer to the map
                addGraphicFromPictureMarkerSymbolLayer(pictureMarkerFromUri, 0.0)
            } else {
                showError("Picture marker symbol layer failed to load from URI: ${pictureMarkerFromUri.loadError.message}")
            }
        }
        pictureMarkerFromUri.loadAsync()
    }

    /**
     * Loads a picture marker symbol layer and after it has loaded, creates a new multilayer point symbol from it.
     * A graphic is created from the multilayer point symbol and added to the graphics overlay.
     *
     * The [pictureMarkerSymbolLayer] to be loaded.
     * The [offset] value used to keep a consistent distance between symbols in the same column.
     *
     */
    private fun addGraphicFromPictureMarkerSymbolLayer(
        pictureMarkerSymbolLayer: PictureMarkerSymbolLayer,
        offset: Double
    ) {
        // wait for the picture marker symbol layer to load and check it has loaded
        pictureMarkerSymbolLayer.addDoneLoadingListener {
            if (pictureMarkerSymbolLayer.loadStatus === LoadStatus.LOADED) {
                // set the size of the layer and create a new multilayer point symbol from it
                pictureMarkerSymbolLayer.size = 40.0
                val multilayerPointSymbol =
                    MultilayerPointSymbol(listOf(pictureMarkerSymbolLayer))
                // create location for the symbol
                val point = Point(-80.0, 20.0 - offset, SpatialReferences.getWgs84())

                // create graphic with the location and symbol and add it to the graphics overlay
                val graphic = Graphic(point, multilayerPointSymbol)
                graphicsOverlay?.graphics?.add(graphic)
            } else if (pictureMarkerSymbolLayer.loadStatus === LoadStatus.FAILED_TO_LOAD) {
                showError("Picture marker symbol layer failed to load: ${pictureMarkerSymbolLayer.loadError.message}")
            }
        }
        // load the picture marker symbol layer
        pictureMarkerSymbolLayer.loadAsync()
    }

    /**
     * Adds new graphics constructed from multilayer point symbols.
     *
     * The [multilayerSymbol] to construct the vector marker symbol element with.
     * The input [geometry] for the vector marker symbol element.
     * [offset] the value used to keep a consistent distance between symbols in the same column.
     */
    private fun addGraphicsWithVectorMarkerSymbolElements(
        multilayerSymbol: MultilayerSymbol,
        geometry: Geometry,
        offset: Double
    ) {
        // define a vector element and create a new multilayer point symbol from it
        val vectorMarkerSymbolElement = VectorMarkerSymbolElement(geometry, multilayerSymbol)
        val vectorMarkerSymbolLayer = VectorMarkerSymbolLayer(listOf(vectorMarkerSymbolElement))
        val multilayerPointSymbol = MultilayerPointSymbol(listOf(vectorMarkerSymbolLayer))

        // create point graphic using the symbol and add it to the graphics overlay
        val graphic =
            Graphic(Point(-150.0, 20 - offset, SpatialReferences.getWgs84()), multilayerPointSymbol)
        graphicsOverlay?.graphics?.add(graphic)
    }

    /**
     * Adds new graphics constructed from multilayer polyline symbols.
     *
     * The pattern of [dashSpacing] dots/dashes used by the line and
     * [offset] the value used to keep a consistent distance between symbols in the same column.
     */
    private fun addLineGraphicsWithMarkerSymbols(dashSpacing: List<Double>, offset: Double) {
        // create a dash effect from the provided values
        val dashGeometricEffect = DashGeometricEffect(dashSpacing)
        // create stroke used by line symbols
        val solidStrokeSymbolLayer = SolidStrokeSymbolLayer(
            3.0,
            Color.RED,
            listOf(dashGeometricEffect)
        ).apply {
            capStyle = StrokeSymbolLayer.CapStyle.ROUND
        }
        // create a polyline for the multilayer polyline symbol
        val polylineBuilder = PolylineBuilder(SpatialReferences.getWgs84()).apply {
            addPoint(Point(-30.0, 20 - offset))
            addPoint(Point(30.0, 20 - offset))
        }
        // create a multilayer polyline symbol from the solidStrokeSymbolLayer
        val multilayerPolylineSymbol = MultilayerPolylineSymbol(listOf(solidStrokeSymbolLayer))
        // create a polyline graphic with geometry using the symbol created above, and add it to the graphics overlay
        graphicsOverlay?.graphics?.add(
            Graphic(
                polylineBuilder.toGeometry(),
                multilayerPolylineSymbol
            )
        )
    }

    /**
     * Adds new graphics constructed from multilayer polygon symbols.
     *
     * A list containing the [angles] at which to draw fill lines within the polygon and
     * [offset] the value used to keep a consistent distance between symbols in the same column.
     */
    private fun addPolygonGraphicsWithMarkerSymbols(angles: List<Double>, offset: Double) {
        val polygonBuilder = PolygonBuilder(SpatialReferences.getWgs84()).apply {
            addPoint(Point(60.0, 25 - offset))
            addPoint(Point(70.0, 25 - offset))
            addPoint(Point(70.0, 20 - offset))
            addPoint(Point(60.0, 20 - offset))
        }

        // create a stroke symbol layer to be used by patterns
        val strokeForHatches =
            SolidStrokeSymbolLayer(2.0, Color.RED, listOf(DashGeometricEffect()))

        // create a stroke symbol layer to be used as an outline for aforementioned patterns
        val strokeForOutline =
            SolidStrokeSymbolLayer(1.0, Color.BLACK, listOf(DashGeometricEffect()))

        // create an array to hold all necessary symbol layers - at least one for patterns and one for an outline at the end
        val symbolLayerArray = arrayOfNulls<SymbolLayer>(angles.size + 1)

        // for each angle, create a symbol layer using the pattern stroke, with hatched lines at the given angle
        for (i in angles.indices) {
            val hatchFillSymbolLayer = HatchFillSymbolLayer(
                MultilayerPolylineSymbol(listOf(strokeForHatches)),
                angles[i]
            )
            // define separation distance for lines and add them to the symbol layer array
            hatchFillSymbolLayer.separation = 9.0
            symbolLayerArray[i] = hatchFillSymbolLayer
        }

        // assign the outline layer to the last element of the symbol layer array
        symbolLayerArray[symbolLayerArray.size - 1] = strokeForOutline
        // create a multilayer polygon symbol from the symbol layer array
        val multilayerPolygonSymbol = MultilayerPolygonSymbol(listOf(*symbolLayerArray))
        // create a polygon graphic with geometry using the symbol created above, and add it to the graphics overlay
        val graphic = Graphic(polygonBuilder.toGeometry(), multilayerPolygonSymbol)
        graphicsOverlay?.graphics?.add(graphic)
    }

    /**
     * Creates a complex point from multiple symbol layers and a provided geometry.
     * @param complexPointGeometry a base geometry upon which other symbol layers are drawn.
     */
    private fun addComplexPoint(complexPointGeometry: Geometry) {
        // create marker layers for complex point
        val orangeSquareVectorMarkerLayer: VectorMarkerSymbolLayer =
            getLayerForComplexPoint(Color.CYAN, Color.BLUE, 11.0)
        val blackSquareVectorMarkerLayer: VectorMarkerSymbolLayer =
            getLayerForComplexPoint(Color.BLACK, Color.CYAN, 6.0)
        val purpleSquareVectorMarkerLayer: VectorMarkerSymbolLayer =
            getLayerForComplexPoint(Color.TRANSPARENT, Color.MAGENTA, 14.0)

        // set anchors for marker layers
        orangeSquareVectorMarkerLayer.anchor =
            SymbolAnchor(-4.0, -6.0, SymbolAnchor.PlacementMode.ABSOLUTE)
        blackSquareVectorMarkerLayer.anchor =
            SymbolAnchor(2.0, 1.0, SymbolAnchor.PlacementMode.ABSOLUTE)
        purpleSquareVectorMarkerLayer.anchor =
            SymbolAnchor(4.0, 2.0, SymbolAnchor.PlacementMode.ABSOLUTE)

        // create a yellow hexagon with a black outline
        val yellowFillLayer = SolidFillSymbolLayer(Color.YELLOW)
        val blackOutline =
            SolidStrokeSymbolLayer(2.0, Color.BLACK, listOf(DashGeometricEffect()))
        val hexagonVectorElement = VectorMarkerSymbolElement(
            complexPointGeometry,
            MultilayerPolylineSymbol(listOf(yellowFillLayer, blackOutline))
        )
        val hexagonVectorMarkerLayer = VectorMarkerSymbolLayer(listOf(hexagonVectorElement)).apply {
            size = 35.0
        }

        // create the multilayer point symbol
        val multilayerPointSymbol = MultilayerPointSymbol(
            listOf(
                hexagonVectorMarkerLayer,
                orangeSquareVectorMarkerLayer,
                blackSquareVectorMarkerLayer,
                purpleSquareVectorMarkerLayer
            )
        )

        // create the multilayer point graphic using the symbols created above
        val complexPointGraphic =
            Graphic(Point(130.0, 20.0, SpatialReferences.getWgs84()), multilayerPointSymbol)
        graphicsOverlay?.graphics?.add(complexPointGraphic)
    }

    /**
     * Creates a symbol layer for use in the composition of a complex point.
     * [fillColor] of the symbol
     * [outlineColor] of the symbol
     * [size] of the symbol
     * @return the vector marker symbol layer.
     */
    private fun getLayerForComplexPoint(
        fillColor: Int,
        outlineColor: Int,
        size: Double
    ): VectorMarkerSymbolLayer {
        // create the fill layer and outline
        val fillLayer = SolidFillSymbolLayer(fillColor)
        val outline = SolidStrokeSymbolLayer(
            2.0,
            outlineColor,
            listOf(DashGeometricEffect())
        )
        // create a geometry from an envelope
        val geometry = Envelope(
            Point(-0.5, -0.5, SpatialReferences.getWgs84()),
            Point(0.5, 0.5, SpatialReferences.getWgs84())
        )
        //create a symbol element using the geometry, fill layer, and outline
        val vectorMarkerSymbolElement = VectorMarkerSymbolElement(
            geometry,
            MultilayerPolygonSymbol(listOf(fillLayer, outline))
        )
        // create a symbol layer containing just the above symbol element, set its size, and return it
        val vectorMarkerSymbolLayer =
            VectorMarkerSymbolLayer(listOf(vectorMarkerSymbolElement)).apply {
                this.size = size
            }
        return vectorMarkerSymbolLayer
    }

    /**
     * Adds a complex polygon generated with multiple symbol layers.
     */
    private fun addComplexPolygon() {
        // create the multilayer polygon symbol
        val multilayerPolygonSymbol = MultilayerPolygonSymbol(getLayersForComplexPolys(true))
        // create the polygon
        val polygonBuilder = PolygonBuilder(SpatialReferences.getWgs84()).apply {
            addPoint(Point(120.0, 0.0))
            addPoint(Point(140.0, 0.0))
            addPoint(Point(140.0, -10.0))
            addPoint(Point(120.0, -10.0))
        }
        // create a multilayer polygon graphic with geometry using the symbols
        // created above and add it to the graphics overlay
        graphicsOverlay?.graphics?.add(
            Graphic(
                polygonBuilder.toGeometry(),
                multilayerPolygonSymbol
            )
        )
    }

    /**
     * Adds a complex polyline generated with multiple symbol layers.
     */
    private fun addComplexPolyline() {
        // create the multilayer polyline symbol
        val multilayerPolylineSymbol = MultilayerPolylineSymbol(getLayersForComplexPolys(false))
        val polylineBuilder = PolylineBuilder(SpatialReferences.getWgs84()).apply {
            addPoint(Point(120.0, -25.0))
            addPoint(Point(140.0, -25.0))
        }
        // create the multilayer polyline graphic with geometry using the symbols created above and add it to the graphics overlay
        graphicsOverlay?.graphics?.add(
            Graphic(
                polylineBuilder.toGeometry(),
                multilayerPolylineSymbol
            )
        )
    }

    /**
     * Generates and returns the symbol layers used by the addComplexPolygon and addComplexPolyline methods.
     * [includeRedFill] indicates whether to include the red fill needed by the complex polygon.
     * @return a list of symbol layers including the necessary effects.
     */
    private fun getLayersForComplexPolys(includeRedFill: Boolean): List<SymbolLayer> {
        // create a black dash effect
        val blackDashes = SolidStrokeSymbolLayer(
            1.0,
            Color.BLACK,
            listOf(DashGeometricEffect(listOf(5.0, 3.0)))
        ).apply {
            capStyle = StrokeSymbolLayer.CapStyle.SQUARE
        }

        // create a black outline
        val blackOutline =
            SolidStrokeSymbolLayer(
                7.0,
                Color.BLACK,
                listOf(DashGeometricEffect())
            ).apply {
                capStyle = StrokeSymbolLayer.CapStyle.ROUND
            }

        // create a yellow stroke inside
        val yellowStroke = SolidStrokeSymbolLayer(
            5.0,
            Color.YELLOW,
            listOf(DashGeometricEffect())
        ).apply {
            capStyle = StrokeSymbolLayer.CapStyle.ROUND
        }

        return if (includeRedFill) {
            // create a red filling for the polygon
            val redFillLayer = SolidFillSymbolLayer(Color.RED)
            listOf(redFillLayer, blackOutline, yellowStroke, blackDashes)
        } else {
            listOf(blackOutline, yellowStroke, blackDashes)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, message)
    }

    override fun onPause() {
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }
}
