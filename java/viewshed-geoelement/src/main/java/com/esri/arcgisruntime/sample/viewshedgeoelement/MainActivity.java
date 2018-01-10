package com.esri.arcgisruntime.sample.viewshedgeoelement;

import java.io.File;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.geoanalysis.GeoElementViewshed;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.ModelSceneSymbol;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());

    // add the SceneView to the stack pane
    SceneView sceneView = findViewById(R.id.sceneView);
    sceneView.setScene(scene);

    // add base surface for elevation data
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(getString(R.string.elevation_service)));
    scene.setBaseSurface(surface);

    // add a scene layer
    ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(getString(R.string.buildings_layer));
    scene.getOperationalLayers().add(sceneLayer);

    // create a graphics overlay for the tank
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    graphicsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
    sceneView.getGraphicsOverlays().add(graphicsOverlay);

    // set up heading expression for tank
    SimpleRenderer renderer3D = new SimpleRenderer();
    Renderer.SceneProperties renderProperties = renderer3D.getSceneProperties();
    renderProperties.setHeadingExpression("[HEADING]");
    graphicsOverlay.setRenderer(renderer3D);

    // create a graphic of a tank
    String modelURI = new File("./samples-data/bradley_low_3ds/bradle.3ds").getAbsolutePath();
    ModelSceneSymbol tankSymbol = new ModelSceneSymbol(modelURI, 10.0);
    tankSymbol.setHeading(90);
    tankSymbol.setAnchorPosition(SceneSymbol.AnchorPosition.BOTTOM);
    tankSymbol.loadAsync();
    Graphic tankGraphic = new Graphic(new Point(-4.506390, 48.385624, SpatialReferences.getWgs84()), tankSymbol);
    tankGraphic.getAttributes().put("HEADING", 0.0);
    graphicsOverlay.getGraphics().add(tankGraphic);

    // create a viewshed to attach to the tank
    GeoElementViewshed geoElementViewshed = new GeoElementViewshed(tankGraphic, 90.0, 40.0, 0.1, 250.0, 0.0, 0.0);
    // offset viewshed observer location to top of tank
    geoElementViewshed.setOffsetZ(3.0);

    // create an analysis overlay to add the viewshed to the scene view
    AnalysisOverlay analysisOverlay = new AnalysisOverlay();
    analysisOverlay.getAnalyses().add(geoElementViewshed);
    sceneView.getAnalysisOverlays().add(analysisOverlay);
  }
}
