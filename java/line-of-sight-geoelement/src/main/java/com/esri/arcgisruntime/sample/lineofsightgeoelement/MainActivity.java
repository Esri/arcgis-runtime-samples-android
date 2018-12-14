package com.esri.arcgisruntime.sample.lineofsightgeoelement;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import com.esri.arcgisruntime.geoanalysis.GeoElementLineOfSight;
import com.esri.arcgisruntime.geoanalysis.LineOfSight;
import com.esri.arcgisruntime.geometry.AngularUnit;
import com.esri.arcgisruntime.geometry.AngularUnitId;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointBuilder;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.ModelSceneSymbol;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  private static final LinearUnit METERS = new LinearUnit(LinearUnitId.METERS);
  private static final AngularUnit DEGREES = new AngularUnit(AngularUnitId.DEGREES);

  private SceneView mSceneView;
  private List<Point> mWaypoints;
  private int mWaypointIndex = 0;
  //private Timeline animation;
  private Graphic mTaxiGraphic;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createTopographic());

    // add the SceneView to the stack pane
    mSceneView = findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    // add base surface for elevation data
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(getString(R.string.elevation_service_url)));
    scene.setBaseSurface(surface);

    // add buildings from New York City
    String buildingsURL = getString(R.string.new_york_buildings_service_url);
    ArcGISSceneLayer buildings = new ArcGISSceneLayer(buildingsURL);
    scene.getOperationalLayers().add(buildings);

    // create a graphics overlay for the graphics
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    graphicsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
    mSceneView.getGraphicsOverlays().add(graphicsOverlay);

    // set up a heading expression to handle graphic rotation
    SimpleRenderer renderer3D = new SimpleRenderer();
    Renderer.SceneProperties renderProperties = renderer3D.getSceneProperties();
    renderProperties.setHeadingExpression("[HEADING]");
    graphicsOverlay.setRenderer(renderer3D);

    // create a point graph near the Empire State Building to be the observer
    Point observationPoint = new Point(-73.9853, 40.7484, 200, SpatialReferences.getWgs84());
    Graphic observer = new Graphic(observationPoint,
        new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 5));
    graphicsOverlay.getGraphics().add(observer);

    // create a slider to change the observer's Z value
    SeekBar heightSeekBar = findViewById(R.id.heightSeekBar);
    TextView currHeightTextView = findViewById(R.id.currHeightTextView);
    heightSeekBar.setMax(150);
    heightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        // offset progress bar to go from 150 - 300
        int height = progress + 150;
        currHeightTextView.setText(String.valueOf(height));
        PointBuilder pointBuilder = new PointBuilder((Point) observer.getGeometry());
        pointBuilder.setZ(height);
        observer.setGeometry(pointBuilder.toGeometry());
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });


    // create waypoints around a block for the taxi to drive to
    mWaypoints = Arrays.asList(
        new Point(-73.984513, 40.748469, SpatialReferences.getWgs84()),
        new Point(-73.985068, 40.747786, SpatialReferences.getWgs84()),
        new Point(-73.983452, 40.747091, SpatialReferences.getWgs84()),
        new Point(-73.982961, 40.747762, SpatialReferences.getWgs84())
    );

    // create a graphic of a taxi to be the target
    String modelURI = new File("./samples-data/dolmus_3ds/dolmus.3ds").getAbsolutePath();
    ModelSceneSymbol taxiSymbol = new ModelSceneSymbol(modelURI, 1.0);
    taxiSymbol.setAnchorPosition(SceneSymbol.AnchorPosition.BOTTOM);
    taxiSymbol.loadAsync();
    mTaxiGraphic = new Graphic(mWaypoints.get(0), taxiSymbol);
    mTaxiGraphic.getAttributes().put("HEADING", 0.0);
    graphicsOverlay.getGraphics().add(mTaxiGraphic);

    // create an analysis overlay to hold the line of sight
    AnalysisOverlay analysisOverlay = new AnalysisOverlay();
    mSceneView.getAnalysisOverlays().add(analysisOverlay);

    // create a line of sight between the two graphics and add it to the analysis overlay
    GeoElementLineOfSight lineOfSight = new GeoElementLineOfSight(observer, mTaxiGraphic);
    analysisOverlay.getAnalyses().add(lineOfSight);

    // select (highlight) the taxi when the line of sight target visibility changes to visible
    lineOfSight.addTargetVisibilityChangedListener(targetVisibilityChangedEvent ->
        mTaxiGraphic
            .setSelected(targetVisibilityChangedEvent.getTargetVisibility() == LineOfSight.TargetVisibility.VISIBLE)
    );

    // create a timeline to animate the taxi driving around the block
    //animation = new Timeline();
    //animation.setCycleCount(-1);
    //animation.getKeyFrames().add(new KeyFrame(Duration.millis(100), e -> animate()));
    //animation.play();

    // zoom to show the observer
    Camera camera = new Camera((Point) observer.getGeometry(), 700, -30, 45, 0);
    mSceneView.setViewpointCamera(camera);
  }

  /**
   * Moves the taxi toward the current waypoint a short distance.
   */
  private void animate() {
    Point waypoint = mWaypoints.get(mWaypointIndex);
    // get current location and distance from waypoint
    Point location = (Point) mTaxiGraphic.getGeometry();
    GeodeticDistanceResult distance = GeometryEngine.distanceGeodetic(location, waypoint, METERS, DEGREES,
        GeodeticCurveType.GEODESIC);

    // move toward waypoint a short distance
    location = GeometryEngine.moveGeodetic(location, 1.0, METERS, distance.getAzimuth1(), DEGREES,
        GeodeticCurveType.GEODESIC);
    mTaxiGraphic.setGeometry(location);

    // rotate to the waypoint
    mTaxiGraphic.getAttributes().put("HEADING", distance.getAzimuth1());

    // reached waypoint, move to next waypoint
    if (distance.getDistance() <= 2) {
      mWaypointIndex = (mWaypointIndex + 1) % mWaypoints.size();
    }
  }
}
