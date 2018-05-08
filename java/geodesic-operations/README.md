<h1>Geodesic Operations</h1>

<p>Demonstrates how to use the GeometryEngine to calculate a geodesic path between two points and measure its distance.</p>

<p><img src="GeodesicOperations.png"/></p>

<h2>How to use the sample</h2>

<p>Click on the map to select a path destination. The geodesic path between the two points will update.</p>

<h2>How it works</h2>

<p>To create a geodesic path between two points:</p>

<ol>
    <li>Create a <code>Polyline</code> using two points.</li>
    <li>Pass this polyline to: <code>GeometryEngine.densifyGeodetic(polyline, segmentLength, unitOfMeasurement, GeodeticCurveType
    .GEODESIC)</code>. This will create a new polyline with segments of length <code>segmentLength</code> and 
    <code>LinearUnit</code> set to <code>unitOfMeasurement</code>. The curve will be geodesic.</li>
    <li>You can set this geometry to a <code>Graphic</code> to display the curve in a <code>GraphicsOverlay</code>.</li>
    <li>To get the distance, use <code>GeometryEngine.lengthGeodetic(pathGeometry, unitOfMeasurement, GeodeticCurveType.GEODESIC)</code></li> 
</ol>

<h2>Features</h2>

<ul>
    <li>ArcGISMap</li>
    <li>GeodeticCurveType</li>
    <li>Geometry</li>
    <li>GeometryEngine</li>
    <li>Graphic</li>
    <li>GraphicsOverlay</li>
    <li>LinearUnit</li>
    <li>LinearUnitId</li>
    <li>MapView</li>
    <li>Point</li>
    <li>PointCollection</li>
    <li>Polyline</li>
    <li>SimpleLineSymbol</li>
    <li>SimpleMarkerSymbol</li>
</ul>
