/* Copyright 2020 Esri
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
package com.esri.arcgisruntime.sample.addgraphicsrenderer

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.CubicBezierSegment
import com.esri.arcgisruntime.geometry.EllipticArcSegment
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.Part
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.PolygonBuilder
import com.esri.arcgisruntime.geometry.PolylineBuilder
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.addgraphicsrenderer.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer

class MainActivity : AppCompatActivity() {

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with a topographic basemap
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        // set the map to be displayed in this view
        mapView.map = map
        mapView.setViewpoint(Viewpoint(15.169193, 16.333479, 100000000.0))

        // add graphics overlays
        mapView.graphicsOverlays.addAll(
          arrayOf(
            renderedPointGraphicsOverlay(),
            renderedLineGraphicsOverlay(),
            renderedPolygonGraphicsOverlay(),
          renderedCurvedPolygonGraphicsOverlay())
        )
    }

    /**
     * Create a point, its graphic, a graphics overlay for it, and add it to the map view.
     * */
    private fun renderedPointGraphicsOverlay(): GraphicsOverlay {
        // create point
        val pointGeometry = Point(40e5, 40e5, SpatialReferences.getWebMercator())
        // create graphic for point
        val pointGraphic = Graphic(pointGeometry)
        // red diamond point symbol
        val pointSymbol =
            SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.RED, 10f)
        // create simple renderer
        val pointRenderer = SimpleRenderer(pointSymbol)

        // create a new graphics overlay with these settings and add it to the map view
        return GraphicsOverlay().apply {
            // add graphic to overlay
            graphics.add(pointGraphic)
            // set the renderer on the graphics overlay to the new renderer
            renderer = pointRenderer
        }
    }

    /**
     * Create a polyline, its graphic, a graphics overlay for it, and add it to the map view.
     * */
    private fun renderedLineGraphicsOverlay(): GraphicsOverlay {
        // create line
        val lineGeometry = PolylineBuilder(SpatialReferences.getWebMercator()).apply {
            addPoint(-10e5, 40e5)
            addPoint(20e5, 50e5)
        }
        // create graphic for polyline
        val lineGraphic = Graphic(lineGeometry.toGeometry())
        // solid blue line symbol
        val lineSymbol =
            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f)
        // create simple renderer
        val lineRenderer = SimpleRenderer(lineSymbol)

        // create graphic overlay for polyline and add it to the map view
        return GraphicsOverlay().apply {
            // add graphic to overlay
            graphics.add(lineGraphic)
            // set the renderer on the graphics overlay to the new renderer
            renderer = lineRenderer
        }
    }

    /**
     * Create a polygon, its graphic, a graphics overlay for it, and add it to the map view.
     * */
    private fun renderedPolygonGraphicsOverlay(): GraphicsOverlay {
        // create polygon
        val polygonGeometry = PolygonBuilder(SpatialReferences.getWebMercator()).apply {
            addPoint(-20e5, 20e5)
            addPoint(20e5, 20e5)
            addPoint(20e5, -20e5)
            addPoint(-20e5, -20e5)
        }
        // create graphic for polygon
        val polygonGraphic = Graphic(polygonGeometry.toGeometry())
        // solid yellow polygon symbol
        val polygonSymbol =
            SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.YELLOW, null)
        // create simple renderer
        val polygonRenderer = SimpleRenderer(polygonSymbol)

        // create graphic overlay for polygon and add it to the map view
        return GraphicsOverlay().apply {
            // add graphic to overlay
            graphics.add(polygonGraphic)
            // set the renderer on the graphics overlay to the new renderer
            renderer = polygonRenderer
        }
    }

    /**
     * Create a polygon, its graphic, a graphics overlay for it, and add it to the map view.
     * */
    private fun renderedCurvedPolygonGraphicsOverlay(): GraphicsOverlay {
        // create a point for the center of the geometry
        val originPoint = Point(40e5, 5e5, SpatialReferences.getWebMercator())
        // create polygon
        val curvedPolygonGeometry = makeHeartGeometry(originPoint, 10e5)
        // create graphic for polygon
        val polygonGraphic = Graphic(curvedPolygonGeometry)
        // create a simple fill symbol with outline
        val curvedLineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 1f)
        val curvedFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.RED, curvedLineSymbol)
        // create simple renderer
        val polygonRenderer = SimpleRenderer(curvedFillSymbol)

        // create graphic overlay for polygon and add it to the map view
        return GraphicsOverlay().apply {
            // add graphic to overlay
            graphics.add(polygonGraphic)
            // set the renderer on the graphics overlay to the new renderer
            renderer = polygonRenderer
        }
    }

    /**
     * Create a heart-shape geometry with Bezier and elliptic arc segments from a given [center]
     * point and [sideLength].
     */
    private fun makeHeartGeometry(center: Point, sideLength: Double): Geometry {
        val spatialReference = center.spatialReference
        // the x and y coordinates to simplify the calculation
        val minX = center.x - 0.5 * sideLength
        val minY = center.y - 0.5 * sideLength
        // the radius of the arcs
        val arcRadius = sideLength * 0.25

        // bottom left curve
        val leftCurveStart = Point(center.x, minY, spatialReference)
        val leftCurveEnd = Point(minX, minY + 0.75 * sideLength, spatialReference)
        val leftControlPoint1 = Point(center.x, minY + 0.25 * sideLength, spatialReference)
        val leftControlPoint2 = Point(minX, center.y, spatialReference)
        val leftCurve = CubicBezierSegment(
            leftCurveStart,
            leftControlPoint1,
            leftControlPoint2,
            leftCurveEnd,
            spatialReference
        )

        // top left arc
        val leftArcCenter =
            Point(minX + 0.25 * sideLength, minY + 0.75 * sideLength, spatialReference)
        val leftArc = EllipticArcSegment.createCircularEllipticArc(
            leftArcCenter,
            arcRadius,
            Math.PI,
            -Math.PI,
            spatialReference
        )

        // top right arc
        val rightArcCenter =
            Point(minX + 0.75 * sideLength, minY + 0.75 * sideLength, spatialReference)
        val rightArc = EllipticArcSegment.createCircularEllipticArc(
            rightArcCenter,
            arcRadius,
            Math.PI,
            -Math.PI,
            spatialReference
        )

        // bottom right curve
        val rightCurveStart = Point(minX + sideLength, minY + 0.75 * sideLength, spatialReference)
        val rightCurveEnd = leftCurveStart
        val rightControlPoint1 = Point(minX + sideLength, center.y, spatialReference)
        val rightControlPoint2 = leftControlPoint1
        val rightCurve = CubicBezierSegment(
            rightCurveStart,
            rightControlPoint1,
            rightControlPoint2,
            rightCurveEnd,
            spatialReference
        )

        val heart = Part(spatialReference).apply {
            add(leftCurve)
            add(leftArc)
            add(rightArc)
            add(rightCurve)
        }
        return Polygon(heart, spatialReference)
    }override fun onPause() {
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
