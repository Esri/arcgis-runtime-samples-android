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

package com.esri.arcgis.android.samples.popupinwebmapforviewing;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.popup.PopupInfo;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * NOTE: TO RUN THIS SAMPLE YOU NEED THE ANDROID SUPPORT LIBRARY! 
 * 
 * Developing with the ArcGIS for Android Popup API uses Android API's that are 
 * not available on all supported platforms. In order to use this sample you will
 * need to add this to your project. 
 * 
 * Right click the project and select Android Tools > Add Support Library... 
 * and follow the wizard.
 *
 */
public class PopupInWebmapForViewing extends Activity {
	private MapView map;
  private PopupContainer popupContainer;
  private PopupDialog popupDialog;
  private ProgressDialog progressDialog;
  private AtomicInteger count;
	
  /** Called when the activity is first created. */    
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Load a webmap.
    map = new MapView(this, "http://www.arcgis.com/home/item.html?id=81d2dcf906cf4df4889ec36c8dc0c1f9", "", "");
    setContentView(map);
    
    // Tap on the map and show popups for selected features.
    map.setOnSingleTapListener(new OnSingleTapListener() {
      private static final long serialVersionUID = 1L;

      public void onSingleTap(float x, float y) {    	
        if (map.isLoaded()) {
        	// Instantiate a PopupContainer
        	popupContainer = new PopupContainer(map);
        	int id = popupContainer.hashCode();
        	popupDialog = null;
        	// Display spinner.
        	if (progressDialog == null || !progressDialog.isShowing())
        		progressDialog = ProgressDialog.show(map.getContext(), "", "Querying...");

        	// Loop through each layer in the webmap
        	int tolerance = 20;
					Envelope env = new Envelope(map.toMapPoint(x, y), 20 * map.getResolution(), 20 * map.getResolution());
        	Layer[] layers = map.getLayers();
        	count = new AtomicInteger();
        	for (Layer layer : layers) {
        		// If the layer has not been initialized or is invisible, do nothing.
      			if (!layer.isInitialized() || !layer.isVisible())
      				continue;
      			
      			if (layer instanceof ArcGISFeatureLayer) { 
      				// Query feature layer and display popups
      				ArcGISFeatureLayer featureLayer = (ArcGISFeatureLayer) layer;          				
      				if (featureLayer.getPopupInfo() != null) {
      					// Query feature layer which is associated with a popup definition.
      					count.incrementAndGet();
      					new RunQueryFeatureLayerTask(x, y, tolerance, id).execute(featureLayer);
      				}
      			}
      			else if (layer instanceof ArcGISDynamicMapServiceLayer) { 
      				// Query dynamic map service layer and display popups.
      				ArcGISDynamicMapServiceLayer dynamicLayer = (ArcGISDynamicMapServiceLayer) layer;
      				// Retrieve layer info for each sub-layer of the dynamic map service layer.
      				ArcGISLayerInfo[] layerinfos = dynamicLayer.getAllLayers();
      				if (layerinfos == null)
      					continue;
      				
      				// Loop through each sub-layer
      				for (ArcGISLayerInfo layerInfo : layerinfos) {
      					// Obtain PopupInfo for sub-layer.
      					PopupInfo popupInfo = dynamicLayer.getPopupInfo(layerInfo.getId());
      					// Skip sub-layer which is without a popup definition.
      					if (popupInfo == null) {
      						continue;
      					}
      					// Check if a sub-layer is visible.
      					ArcGISLayerInfo info = layerInfo;
      					while ( info != null && info.isVisible() ) {
      						info = info.getParentLayer();
      					}
      					// Skip invisible sub-layer
      					if ( info != null && ! info.isVisible() ) {
      						continue;
      					};

      					// Check if the sub-layer is within the scale range
      					double maxScale = (layerInfo.getMaxScale() != 0) ? layerInfo.getMaxScale():popupInfo.getMaxScale();
      					double minScale = (layerInfo.getMinScale() != 0) ? layerInfo.getMinScale():popupInfo.getMinScale();

      					if ((maxScale == 0 || map.getScale() > maxScale) && (minScale == 0 || map.getScale() < minScale)) {
      						// Query sub-layer which is associated with a popup definition and is visible and in scale range.
      						count.incrementAndGet();
      						new RunQueryDynamicLayerTask(env, layer, layerInfo.getId(), dynamicLayer.getSpatialReference(), id).execute(dynamicLayer.getUrl() + "/" + layerInfo.getId());
      					}
      				}
      			}      			
        	}
        }
      }
    });
  }
  
  private void createPopupViews(Feature[] features, final int id) {
		if ( id != popupContainer.hashCode() ) {
			if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
				progressDialog.dismiss();

			return;
		}

		if (popupDialog == null) {
			if (progressDialog != null && progressDialog.isShowing()) 
				progressDialog.dismiss();
			
			// Create a dialog for the popups and display it.
			popupDialog = new PopupDialog(map.getContext(), popupContainer);
			popupDialog.show();
		}
  }
  
  // Query feature layer by hit test
  private class RunQueryFeatureLayerTask extends AsyncTask<ArcGISFeatureLayer, Void, Feature[]> {

		private int tolerance;
		private float x;
		private float y;
		private ArcGISFeatureLayer featureLayer;
		private int id;

		public RunQueryFeatureLayerTask(float x, float y, int tolerance, int id) {
			super();
			this.x = x;
			this.y = y;
			this.tolerance = tolerance;
			this.id = id;
		}

		@Override
		protected Feature[] doInBackground(ArcGISFeatureLayer... params) {
			for (ArcGISFeatureLayer featureLayer : params) {
				this.featureLayer = featureLayer;
				// Retrieve feature ids near the point.
				int[] ids = featureLayer.getGraphicIDs(x, y, tolerance);
				if (ids != null && ids.length > 0) {
					ArrayList<Feature> features = new ArrayList<Feature>();
					for (int id : ids) {
						// Obtain feature based on the id.
						Feature f = featureLayer.getGraphic(id);
						if (f == null)
							continue;
						features.add(f);
					}
					// Return an array of features near the point.
					return features.toArray(new Feature[0]);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Feature[] features) {
			count.decrementAndGet();
			if (features == null || features.length == 0) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
					progressDialog.dismiss();
				
				return;
			}
			// Check if the requested PopupContainer id is the same as the current PopupContainer.
			// Otherwise, abandon the obsoleted query result.
			if ( id != popupContainer.hashCode() ) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
					progressDialog.dismiss();
				
				return;
			}

			for (Feature fr : features) {
				Popup popup = featureLayer.createPopup(map, 0, fr);
				popupContainer.addPopup(popup);
			}
			createPopupViews(features, id);
		}

	}
  
  // Query dynamic map service layer by QueryTask
  private class RunQueryDynamicLayerTask extends AsyncTask<String, Void, FeatureSet> {
		private Envelope env;
		private SpatialReference sr;
		private int id;
		private Layer layer;
		private int subLayerId;

		public RunQueryDynamicLayerTask(Envelope env, Layer layer, int subLayerId, SpatialReference sr, int id) {
			super();
			this.env = env;
			this.sr = sr;
			this.id = id;
			this.layer = layer;
			this.subLayerId = subLayerId;
		}

		@Override
		protected FeatureSet doInBackground(String... urls) {
			for (String url : urls) {
				// Retrieve features within the envelope.
				Query query = new Query();
				query.setInSpatialReference(sr);
				query.setOutSpatialReference(sr);
				query.setGeometry(env);
				query.setMaxFeatures(10);
				query.setOutFields(new String[] { "*" });

				QueryTask queryTask = new QueryTask(url);
				try {
					FeatureSet results = queryTask.execute(query);
					return results;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(final FeatureSet result) {
			count.decrementAndGet();
			if (result == null) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
					progressDialog.dismiss();

				return;
			}
			
			Feature[] features = result.getGraphics();
			if (features == null || features.length == 0) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
					progressDialog.dismiss();

				return;
			}
			// Check if the requested PopupContainer id is the same as the current PopupContainer.
			// Otherwise, abandon the obsoleted query result.
			if (id != popupContainer.hashCode()) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
					progressDialog.dismiss();

				return;
			}
			PopupInfo popupInfo = layer.getPopupInfo(subLayerId);
			if (popupInfo == null) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0)  
					progressDialog.dismiss();

				return;
			}
			
			for (Feature fr : features) {
				Popup popup = layer.createPopup(map, subLayerId, fr);
				popupContainer.addPopup(popup);
			}
			createPopupViews(features, id);
			
		}
	}
  
  // A customize full screen dialog.
  private class PopupDialog extends Dialog {
	  private PopupContainer popupContainer;
	  
	  public PopupDialog(Context context, PopupContainer popupContainer) {
		  super(context, android.R.style.Theme);
		  this.popupContainer = popupContainer;
	  }
	  
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			LinearLayout layout = new LinearLayout(getContext());
			layout.addView(popupContainer.getPopupContainerView(), android.widget.LinearLayout.LayoutParams.FILL_PARENT, android.widget.LinearLayout.LayoutParams.FILL_PARENT);
			setContentView(layout, params);
		}
	  
  }
  
}