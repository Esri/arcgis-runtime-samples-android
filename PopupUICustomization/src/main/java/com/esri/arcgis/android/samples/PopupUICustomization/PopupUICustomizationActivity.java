/* Copyright 2015 Esri
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

package com.esri.arcgis.android.samples.PopupUICustomization;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.android.toolkit.map.MapViewHelper;
import com.esri.android.toolkit.map.PopupCreateListener;
import com.esri.arcgis.android.samples.PopupUICustomization.PopupFragment.OnEditListener;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.Graphic;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*
 * This sample shows how to customize the UI of a pop-up. The layout and style of a pop-up can be 
 * changed through XML and extending some built-in classes of the pop-up API.
 * 
 * Single-tap will bring up pop-ups with customized UI. Long-press will display pop-ups of default UI for comparison. 
 * A helper class from Application Toolkit called MapViewHelper is used to create pop-ups with default UI. 
 */

public class PopupUICustomizationActivity extends FragmentActivity implements OnEditListener {
	
	private MapView mMapView;
	private MapViewHelper mMapViewHelper;
	private PopupFragment mPopupFragment;
	
	private final static String URL = "http://www.arcgis.com/home/item.html?id=c93081d0ca924317bebf2b362d3bc8de";
	
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Display a web map.
    mMapView = new MapView(this, URL, "", "");
    
    setContentView(mMapView );
    
    mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
     
      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(Object source, STATUS status) {
        if ((status == STATUS.INITIALIZED) && (source == mMapView)) {
          // Create a MapViewHelper object once the map view has been initialized.
          mMapViewHelper = new MapViewHelper(mMapView);
        }
      }
    });
    
    // Query all the layers within the mapview and customized the UI of the pop-ups
    // in async tasks.
    mMapView.setOnSingleTapListener(new OnSingleTapListener() {
      
      private static final long serialVersionUID = 1L;
      
      @Override
      public void onSingleTap(float x, float y) {
        if (!mMapView.isLoaded())
          return;
        
        mPopupFragment = new PopupFragment(mMapView);
        LayerQueryTask queryTask = new LayerQueryTask(mMapView, mPopupFragment);
        queryTask.queryLayers(x, y, 20, true);
      }
    });
    
    // Query all the layers and display pop-up with default UI using MapViewHelper
    // using a helper class of Application Toolkit.
    mMapView.setOnLongPressListener(new OnLongPressListener() {
      
      private static final long serialVersionUID = 1L;

      @Override
      public boolean onLongPress(float x, float y) {
        
        mPopupFragment = null;
        // The helper class from Application Toolkit will loop through and query each layer in the mapview.
        // A pop-up will be created for each feature in the query result and will be added to a PopupContainer.
        // The user-define PopupCreateListerner will be called when a pop-up is created.
        // User can put their logic in the PopupCreateListerner to display the pop-ups.
        mMapViewHelper.createPopup(x, y, new SimplePopupCreateListener());
        return true;
      }
    });
  }
  
  @Override
  public void onDelete(ArcGISFeatureLayer featureLayer, Popup popup) {
    // Commit deletion to server
    Graphic graphic = (Graphic)popup.getFeature();
    if ((graphic != null) && (featureLayer != null)) {
      featureLayer.applyEdits(null, new Graphic[]{graphic}, null, 
          new EditCallbackListener(this, featureLayer, popup, true, "Deleting feature"));
    }
    
    // Dismiss pop-up
    this.getSupportFragmentManager().popBackStack();
  }
  
  @Override
  public void onEdit(ArcGISFeatureLayer featureLayer, Popup popup) {
    // Set pop-up into editing mode
    popup.setEditMode(true);
    // refresh menu items
    this.invalidateOptionsMenu();
  }
  
  @Override
  public void onSave(ArcGISFeatureLayer featureLayer, Popup popup) {
    // Commit edits to server
    Graphic graphic = (Graphic)popup.getFeature();
    if ((graphic != null) && (featureLayer != null)) {
      Map<String, Object> attributes = graphic.getAttributes();
      Map<String, Object> updatedAttrs = popup.getUpdatedAttributes();
      for (Entry<String, Object> entry : updatedAttrs.entrySet()) {
        attributes.put(entry.getKey(), entry.getValue());
      }
      Graphic newGraphic = new Graphic(graphic.getGeometry(), null, attributes);
      featureLayer.applyEdits(null, null, new Graphic[]{newGraphic}, 
          new EditCallbackListener(this, featureLayer, popup, true, "Saving feature"));
    }
    
    // Dismiss pop-up
    this.getSupportFragmentManager().popBackStack();
  }
  
  // TODO: add your logic to handler click event of image buttons shown in a pop-up
  public void addFavorite(View view) {
    
  }
  
  public void findDirection(View view) {
    
  }
  
  public void share(View view) {
    
  }
  
	
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
	}

	 // Handle callback on committing edits to server
  private class EditCallbackListener implements CallbackListener<FeatureEditResult[][]> {
    private String mOperation = "Operation ";
    private ArcGISFeatureLayer mFeatureLayer = null;
    private boolean mExistingFeature = true;
    private Popup mPopup =null;
    private Context mContext;
    // constant defined for edit result index
    private final static int ADDITION_INDEX = 0;
    private final static int DELETION_INDEX = 1;
    private final static int UPDATE_INDEX = 2;
    
    public EditCallbackListener(Context context, ArcGISFeatureLayer featureLayer, Popup popup, 
        boolean existingFeature, String msg) {
      this.mOperation = msg;
      this.mFeatureLayer = featureLayer;
      this.mExistingFeature = existingFeature;
      this.mPopup = popup;
      this.mContext = context;
    }
    
    @Override
    public void onCallback(FeatureEditResult[][] editResults) {
      if ((mFeatureLayer == null) || (!mFeatureLayer.isInitialized()) 
          || (!mFeatureLayer.isEditable()))
        return;
      
      runOnUiThread(new Runnable() {
        
        @Override
        public void run() {
          Toast.makeText(mContext, mOperation + " succeeded!", Toast.LENGTH_SHORT).show();
        }
      });   
      
      // Similar to featureLayer.applyEdits editResults[0] holds result of feature addition, 
      // while editResults[1] for feature deletion and editResults[2] for feature update
      FeatureEditResult[] deletion = editResults[DELETION_INDEX];
      if ((deletion == null) || (deletion.length <= 0)) {
        // Save attachments to the server if newly added attachments exist.
        // Retrieve object id of the feature
        long oid; 
        FeatureEditResult featureEdit;
        if (mExistingFeature) {
          // The first item in the update edits
          featureEdit = editResults[UPDATE_INDEX][0];
        }  else {
          // The first item in the addition edits
          featureEdit = editResults[ADDITION_INDEX][0];
        }
        oid = featureEdit.getObjectId();
        // prepare oid as int for FeatureLayer
        int objectID = (int) oid;
        
        // Get newly added attachments
        List<File> attachments = mPopup.getAddedAttachments();
        if ((attachments != null) && (attachments.size() > 0)) {
          for (File attachment : attachments) {
            // Save newly added attachment based on the object id of the feature.
            mFeatureLayer.addAttachment(objectID, attachment, new CallbackListener<FeatureEditResult>() {
              @Override
              public void onError(Throwable e) {
                // Failed to save new attachments.
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Toast.makeText(mContext, "Adding attachment failed!", Toast.LENGTH_SHORT).show();
                  }
                });
              }
              
              @Override
              public void onCallback(FeatureEditResult arg0) {
                // New attachments have been saved.
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Toast.makeText(mContext, "Adding attachment succeeded!.", Toast.LENGTH_SHORT).show();
                  }
                });
              }
            });
          }
        }
        
        // Delete attachments if some attachments have been mark as delete.
        // Get ids of attachments which are marked as delete.
        List<Integer> attachmentIDs = mPopup.getDeletedAttachmentIDs();
        if ((attachmentIDs != null) && (attachmentIDs.size() > 0)) {
          int[] ids = new int[attachmentIDs.size()];
          for (int i = 0; i < attachmentIDs.size(); i++) {
            ids[i] = attachmentIDs.get(i);
          }
          // Delete attachments
          mFeatureLayer.deleteAttachments(objectID, ids, new CallbackListener<FeatureEditResult[]>() {
            @Override
            public void onError(Throwable e) {
              // Failed to delete attachments
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(mContext, "Deleting attachment failed!", Toast.LENGTH_SHORT).show();
                }
              });
            }
            
            @Override
            public void onCallback(FeatureEditResult[] objs) {
              // Attachments have been removed.
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(mContext, "Deleting attachment succeeded!", Toast.LENGTH_SHORT).show();
                }
              });
            }
          });
        }
      
      }
    }

    @Override
    public void onError(Throwable e) {
      runOnUiThread(new Runnable() {
        
        @Override
        public void run() {
          Toast.makeText(mContext, mOperation + " failed!", Toast.LENGTH_SHORT).show();
        }
      });   
    }
    
  }
  
	// Display pop-up fragment on a UI thread.
  private class SimplePopupCreateListener implements PopupCreateListener {
  
     @Override
     public void onResult(final PopupContainer container) {
       if ((container != null) && (container.getPopupCount() > 0)) {
          ((Activity) PopupUICustomizationActivity.this).runOnUiThread(new Runnable() {
  
             @Override
             public void run() {
               if (mPopupFragment == null ) {
                 mPopupFragment = new PopupFragment(mMapView, container);
                 mPopupFragment.show();
               }
             }
         });
       }
     }
     
   }

}
