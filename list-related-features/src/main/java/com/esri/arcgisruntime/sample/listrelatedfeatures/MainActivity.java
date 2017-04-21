package com.esri.arcgisruntime.sample.listrelatedfeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.esri.arcgisruntime.arcgisservices.RelationshipInfo;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.ArcGISFeatureTable;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.RelatedFeatureQueryResult;
import com.esri.arcgisruntime.data.RelatedQueryParameters;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "RelatedFeatures";

    private MapView mMapView;
    private ArcGISMap map;
    private final ArrayList<FeatureLayer> mOperationalLayers = new ArrayList<>();

    private BottomSheetBehavior mBottomSheetBehavior = null;
    private TableLayout dataTable;

    int dimen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The View with the BottomSheetBehavior

        mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        dimen = (int)getResources().getDimension(R.dimen.activity_horizontal_margin)/8;
        dataTable = (TableLayout)findViewById(R.id.data_table);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        // create a map a webmap
        map = new ArcGISMap(getResources().getString(R.string.webmap_url));
        // set the map to be displayed in this view
        mMapView.setMap(map);

        map.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if(map.getLoadStatus() == LoadStatus.LOADED){
                    createFeatures(map);
                }
            }
        });

        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView){

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // clear all rows
                dataTable.removeAllViews();
                // get the point that was clicked and convert it to a point in map coordinates
                Point clickPoint = mMapView.screenToLocation(new android.graphics.Point(
                        Math.round(e.getX()),
                        Math.round(e.getY())));
                int tolerance = 10;
                double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
                // create objects required to do a selection with a query
                Envelope envelope = new Envelope(
                        clickPoint.getX() - mapTolerance,
                        clickPoint.getY() - mapTolerance,
                        clickPoint.getX() + mapTolerance,
                        clickPoint.getY() + mapTolerance,
                        map.getSpatialReference());
                QueryParameters queryParams = new QueryParameters();
                queryParams.setGeometry(envelope);

                for(final FeatureLayer layer : mOperationalLayers){
                    final ListenableFuture<FeatureQueryResult> future = layer.selectFeaturesAsync(queryParams, FeatureLayer.SelectionMode.NEW);
                    layer.clearSelection();
                    future.addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            //call get on the future to get the result
                            try {
                                FeatureQueryResult result = future.get();

                                for (Feature feature : result) {
                                    ArcGISFeature arcGISFeature = (ArcGISFeature)feature;

                                    layer.setSelectionColor(Color.YELLOW);
                                    layer.setSelectionWidth(10);
                                    ArcGISFeatureTable selectedTable = (ArcGISFeatureTable)feature.getFeatureTable();
                                    List<RelationshipInfo> relationshipInfos = selectedTable.getLayerInfo().getRelationshipInfos();
                                    for(RelationshipInfo relationshipInfo : relationshipInfos){
                                        RelatedQueryParameters relatedQueryParameters = new RelatedQueryParameters(relationshipInfo);

                                        final ListenableFuture<List<RelatedFeatureQueryResult>> relatedFeatureQueryResultFuture = selectedTable.queryRelatedFeaturesAsync(arcGISFeature, relatedQueryParameters);
                                        relatedFeatureQueryResultFuture.addDoneListener(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    List<RelatedFeatureQueryResult> relatedFeatureQueryResultList = relatedFeatureQueryResultFuture.get();

                                                    for(RelatedFeatureQueryResult relatedQueryResult : relatedFeatureQueryResultList){

                                                        for (Feature relatedFeature : relatedQueryResult) {
                                                            // feature returned from selection query
                                                            Map<String, Object> attributes = relatedFeature.getAttributes();

                                                            int counter = 0;
                                                            for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                                                                // add new rows
                                                                TableRow row = new TableRow(MainActivity.this);
                                                                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                                                                row.setLayoutParams(layoutParams);
                                                                TextView dataText = new TextView(MainActivity.this);
                                                                dataText.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.cell_shape, null));
                                                                dataText.setPadding(dimen, dimen, dimen, dimen);
                                                                dataText.setText(attribute.getKey() + " | " + attribute.getValue().toString());
                                                                row.addView(dataText);
                                                                dataTable.addView(row, counter);
                                                                counter++;
                                                            }
                                                        }
                                                    }

                                                } catch (Exception e) {
                                                    Log.e(TAG, "Exception occurred: " + e.getMessage());
                                                }
                                            }
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Exception occurred: " + e.getMessage());
                            }
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }

                    });

                }
                return super.onSingleTapConfirmed(e);
            }
        });

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
//                Log.d(TAG, "BottomSheet State changed");
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
//                Log.d(TAG, "BottomSheet State dragged");
            }
        });

    }

    private void createFeatures(ArcGISMap map){

        LayerList layers = map.getOperationalLayers();

        for(Layer layer: layers){
            FeatureLayer fLayer = (FeatureLayer) layer;
            mOperationalLayers.add(fLayer);
        }

        List<ArcGISFeatureTable> tables = map.getTables();
        ArcGISFeatureTable speciesFeatureTable = tables.get(0);

        speciesFeatureTable.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
//                Log.d(TAG, "Table name " + speciesFeatureTable.getTableName());
            }
        });

        speciesFeatureTable.loadAsync();

    }

    @Override
    protected void onPause(){
        super.onPause();
        // pause MapView
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // resume MapView
        mMapView.resume();
    }
}
