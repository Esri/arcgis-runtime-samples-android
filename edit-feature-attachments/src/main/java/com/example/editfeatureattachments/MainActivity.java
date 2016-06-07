package com.example.editfeatureattachments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.arcgis.ArcGISFeature;
import com.esri.arcgisruntime.datasource.arcgis.Attachment;
import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private ArcGISMap mMap;
    private Callout mCallout;
    private FeatureLayer mFeatureLayer;
    private ArcGISFeature mSelectedArcGISFeature;
    private android.graphics.Point mClickPoint;
    private ServiceFeatureTable mServiceFeatureTable;
    private List<Attachment> attachments;
    private Snackbar mSnackbarSuccess;
    private Snackbar mSnackbarFailure;
    private String mSelectedArcGISFeatureAttributeValue;
    private boolean mFeatureUpdated;
    private View mCoordinatorLayout;
    private String mAttributeID;
    ProgressDialog progressDialog;
    private static String TAG = "EditFeatureAttachment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCoordinatorLayout = findViewById(R.id.snackbarPosition);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // create a map with the streets basemap
        mMap = new ArcGISMap(Basemap.createStreets());

        //set an initial viewpoint
        mMap.setInitialViewpoint(new Viewpoint(new Point(544871.19, 6806138.66, SpatialReferences
                .getWebMercator()), 2E6));

        // set the map to be displayed in the mapview
        mMapView.setMap(mMap);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Fetching # of attachments");
        progressDialog.setMessage("Please wait!!");
        // get callout, set content and show
        mCallout = mMapView.getCallout();
        // create feature layer with its service feature table
        // create the service feature table
        mServiceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));
        mServiceFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_CACHE);
        // create the feature layer using the service feature table
        mFeatureLayer = new FeatureLayer(mServiceFeatureTable);

        // set the color that is applied to a selected feature.
        mFeatureLayer.setSelectionColor(Color.rgb(0, 255, 255)); //cyan, fully opaque
        // set the width of selection color
        mFeatureLayer.setSelectionWidth(3);

        // add the layer to the map
        mMap.getOperationalLayers().add(mFeatureLayer);

        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                // get the point that was clicked and convert it to a point in map coordinates
                mClickPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());

                // clear any previous selection
                mFeatureLayer.clearSelection();
                mSelectedArcGISFeature = null;

                // identify the GeoElements in the given layer
                final ListenableFuture<IdentifyLayerResult> future = mMapView.identifyLayerAsync(mFeatureLayer, mClickPoint, 5, 1);

                // add done loading listener to fire when the selection returns
                future.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // call get on the future to get the result
                            IdentifyLayerResult result = future.get();

                            List<GeoElement> resultGeoElements = result.getIdentifiedElements();
                            if (resultGeoElements.size() > 0) {
                                if (resultGeoElements.get(0) instanceof ArcGISFeature) {
                                    progressDialog.show();

                                    mSelectedArcGISFeature = (ArcGISFeature) resultGeoElements.get(0);
                                    // highlight the selected feature
                                    mFeatureLayer.selectFeature(mSelectedArcGISFeature);

                                    mAttributeID = mSelectedArcGISFeature.getAttributes().get("objectid").toString();

                                    // get the number of attachments
                                    final ListenableFuture<List<Attachment>> attachmentResults = mSelectedArcGISFeature.fetchAttachmentsAsync();

                                    attachmentResults.addDoneListener(new Runnable() {
                                        @Override
                                        public void run() {

                                            try {
                                                attachments = attachmentResults.get();
                                                Log.d("number of attachments :", attachments.size() + "");
                                                // show callout with the value for the attribute "typdamage" of the selected feature
                                                mSelectedArcGISFeatureAttributeValue = (String) mSelectedArcGISFeature.getAttributes().get("typdamage");
                                                if (progressDialog.isShowing()) {
                                                    progressDialog.dismiss();
                                                }
                                                showCallout(mSelectedArcGISFeatureAttributeValue, attachments.size());
                                                Toast.makeText(getApplicationContext(), "Tap on the info button to edit attachment", Toast.LENGTH_SHORT).show();

                                            } catch (Exception e) {
                                                Log.e(TAG, e.getMessage());
                                            }
                                        }
                                    });
                                }
                            } else {
                                // none of the features on the map were selected
                                mCallout.dismiss();
                            }
                        } catch (Exception e) {
                            Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e.getMessage());
                        }
                    }
                });
                return super.onSingleTapConfirmed(e);
            }
        });
    }

    /**
     * Displays Callout
     * @param title the text to show in the Callout
     */
    private void showCallout(String title, int noOfAttachments){

        // create a text view for the callout
        RelativeLayout calloutLayout = new RelativeLayout(getApplicationContext());

        TextView calloutContent = new TextView(getApplicationContext());
        calloutContent.setId(R.id.calloutTextView);
        calloutContent.setTextColor(Color.BLACK);
        calloutContent.setTextSize(18);
        calloutContent.setText(title);

        RelativeLayout.LayoutParams relativeParamsBelow = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParamsBelow.addRule(RelativeLayout.BELOW, calloutContent.getId());

        // create attachment text view for the callout
        TextView calloutAttachment = new TextView(getApplicationContext());
        calloutAttachment.setId(R.id.attchTV);
        calloutAttachment.setTextColor(Color.BLACK);
        calloutAttachment.setTextSize(13);
        calloutContent.setPadding(0,20,20,0);
        calloutAttachment.setLayoutParams(relativeParamsBelow);
        String attachmentText = "Number of attachments :: "+noOfAttachments;
        calloutAttachment.setText(attachmentText);

        RelativeLayout.LayoutParams relativeParamsRightOf = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParamsRightOf.addRule(RelativeLayout.RIGHT_OF, calloutAttachment.getId());

        // create image view for the callout
        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_info_outline_black_18dp));
        imageView.setLayoutParams(relativeParamsRightOf);
        imageView.setOnClickListener(new ImageViewOnclickListener());




        calloutLayout.addView(calloutContent);
        calloutLayout.addView(imageView);
        calloutLayout.addView(calloutAttachment);

        mCallout.setLocation(mMapView.screenToLocation(mClickPoint));
        mCallout.setContent(calloutLayout);
        mCallout.show();
    }

    /**
     * Defines the listener for the ImageView clicks
     */
    private class ImageViewOnclickListener implements View.OnClickListener {

        @Override public void onClick(View v) {
            Log.e("imageview", "tap");
            Intent myIntent = new Intent(MainActivity.this, EditAttachmentActivity.class);
            myIntent.putExtra(getApplication().getString(R.string.attribute),mAttributeID);
            myIntent.putExtra(getApplication().getString(R.string.noOfAttachments),attachments.size());
            startActivity(myIntent);

        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");

        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart()");
        restartCallout();
    }

    private void restartCallout() {
        progressDialog.setTitle("Refreshing");
        progressDialog.setMessage("Please wait!");
        progressDialog.show();
        // get the number of attachments
        final ListenableFuture<List<Attachment>> attachmentResults = mSelectedArcGISFeature.fetchAttachmentsAsync();

        attachmentResults.addDoneListener(new Runnable() {
            @Override
            public void run() {

                try {
                    attachments = attachmentResults.get();
                    Log.d("number of attachments :", attachments.size() + "");
                    // show callout with the value for the attribute "typdamage" of the selected feature
                    mSelectedArcGISFeatureAttributeValue = (String) mSelectedArcGISFeature.getAttributes().get("typdamage");
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    showCallout(mSelectedArcGISFeatureAttributeValue, attachments.size());

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }



}
