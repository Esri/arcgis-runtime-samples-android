package com.esri.arcgisruntime.sample.listrelatedfeatures;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.ArcGISFeatureTable;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.RelatedFeatureQueryResult;
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
    private ArcGISMap mArcGISMap;
    private final ArrayList<FeatureLayer> mOperationalLayers = new ArrayList<>();

    private BottomSheetBehavior mBottomSheetBehavior = null;
    private ArrayAdapter<String> mArrayAdapter;
    private final List<String> mRelatedValues = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The View with the BottomSheetBehavior
        mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        ListView mListView = (ListView) findViewById(R.id.related_list);
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mRelatedValues);
        mListView.setAdapter(mArrayAdapter);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        // create a mArcGISMap a webmap
        mArcGISMap = new ArcGISMap(getResources().getString(R.string.webmap_url));
        // set the mArcGISMap to be displayed in this view
        mMapView.setMap(mArcGISMap);
        mArcGISMap.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if(mArcGISMap.getLoadStatus() == LoadStatus.LOADED){
                    // create Features to use for listing related features
                    createFeatures(mArcGISMap);
                }
            }
        });

        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // clear ListAdapter of previous results
                mArrayAdapter.clear();
                // get the point that was clicked and convert it to a point in mArcGISMap coordinates
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
                        mArcGISMap.getSpatialReference());
                QueryParameters queryParams = new QueryParameters();
                queryParams.setGeometry(envelope);
                // get the FeatureLayer to query
                final FeatureLayer selectedLayer = mOperationalLayers.get(0);
                // get a list of related features to display
                queryRelatedFeatures(selectedLayer, queryParams);
                return super.onSingleTapConfirmed(e);
            }
        });
    }

    /**
     * Uses the selected FeatureLayer to get FeatureTable RelationshipInfos used to
     * QueryRelatedFeaturesAsync which returns a list of related features.
     *
     * @param featureLayer Layer selected from the Map
     * @param queryParameters Input parameters for query
     */
    private void queryRelatedFeatures(final FeatureLayer featureLayer, QueryParameters queryParameters){
        final ListenableFuture<FeatureQueryResult> future = featureLayer.selectFeaturesAsync(queryParameters, FeatureLayer.SelectionMode.NEW);
        // clear previously selected layers
        featureLayer.clearSelection();

        future.addDoneListener(new Runnable() {
            @Override
            public void run() {
                //call get on the future to get the result
                try {
                    if(future.get().iterator().hasNext()){
                        FeatureQueryResult result = future.get();
                        // iterate over features returned
                        for (Feature feature : result) {
                            ArcGISFeature arcGISFeature = (ArcGISFeature)feature;
                            featureLayer.setSelectionColor(Color.YELLOW);
                            featureLayer.setSelectionWidth(5);

                            ArcGISFeatureTable selectedTable = (ArcGISFeatureTable)feature.getFeatureTable();

                            final ListenableFuture<List<RelatedFeatureQueryResult>> relatedFeatureQueryResultFuture = selectedTable.queryRelatedFeaturesAsync(arcGISFeature);
                            relatedFeatureQueryResultFuture.addDoneListener(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        List<RelatedFeatureQueryResult> relatedFeatureQueryResultList = relatedFeatureQueryResultFuture.get();
                                        // iterate over returned RelatedFeatureQueryResults
                                        for(RelatedFeatureQueryResult relatedQueryResult : relatedFeatureQueryResultList){
                                            // iterate over Features returned
                                            for (Feature relatedFeature : relatedQueryResult) {
                                                // Get the Display field to use as filter on related attributes
                                                ArcGISFeature agsFeature = (ArcGISFeature) relatedFeature;
                                                String displayFieldName = agsFeature.getFeatureTable().getLayerInfo().getDisplayFieldName();
                                                String displayFieldValue = agsFeature.getAttributes().get(displayFieldName).toString();

                                                mRelatedValues.add(displayFieldValue);

                                                // notify ListAdapter content has changed
                                                mArrayAdapter.notifyDataSetChanged();
                                            }
                                        }

                                    } catch (Exception e) {
                                        Log.e(TAG, "Exception occurred: " + e.getMessage());
                                    }
                                }
                            });

                        }
                    } else {
                        // did not tap on a feature, display no results
                        mRelatedValues.add(getResources().getString(R.string.no_results));
                        // notify ListAdapter content has changed
                        mArrayAdapter.notifyDataSetChanged();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Exception occurred: " + e.getMessage());
                }
                // show the bottomsheet with results
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

        });
    }

    /**
     * Create Features from Layers in the Map
     *
     * @param map ArcGISMap to get Layers and Tables
     */
    private void createFeatures(ArcGISMap map){
        LayerList layers = map.getOperationalLayers();
        // add the National Parks Feature layer to LayerList
        for(Layer layer: layers){
            FeatureLayer fLayer = (FeatureLayer) layer;
            if(fLayer.getName().contains("Alaska National Parks")){
                mOperationalLayers.add(fLayer);
            }
        }

        // create ArcGISFeatureTable from non-spatial tables in Map
        List<ArcGISFeatureTable> tables = map.getTables();
        final ArcGISFeatureTable speciesFeatureTable = tables.get(0);
        speciesFeatureTable.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Found table: " + speciesFeatureTable.getTableName());
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
