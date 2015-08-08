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
import android.os.AsyncTask;
import android.view.ViewGroup;

import com.esri.android.map.GroupLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.popup.ArcGISAttributeView;
import com.esri.android.map.popup.ArcGISTitleView;
import com.esri.android.map.popup.ArcGISValueFormat;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupLayout;
import com.esri.android.map.popup.PopupLayoutInfo;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.map.popup.PopupInfo;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * This class will loop through and query each layer in the mapview. The query result will
 * be displayed in pop-ups. The layout and style of the pop-ups will be customized through
 * XML and sub-classes of some built-in classes of the pop-up API. 
 */
public class LayerQueryTask {
	private MapView mMapView;
	private PopupFragment mPopupFragment;
	private AtomicInteger mCount;
	private boolean mShowGeometryInfo;

	public LayerQueryTask(MapView map, PopupFragment popupFragment) {
		this.mMapView = map;
		this.mPopupFragment = popupFragment;
	}

	// Query each layer in the mapview in async tasks then display result in
	// customized pop-ups.
	public void queryLayers(float x, float y, int tolerance,
			boolean showGeometryInfo) {
		if ((mMapView == null) || (!mMapView.isLoaded())
				|| (mPopupFragment == null))
			return;

		this.mShowGeometryInfo = showGeometryInfo;

		// Loop through each layer in the mapview
		Envelope env = new Envelope(mMapView.toMapPoint(x, y), tolerance
				* mMapView.getResolution(), tolerance
				* mMapView.getResolution());
		Layer[] layers = mMapView.getLayers();
		mCount = new AtomicInteger();
		for (Layer layer : layers) {
			// If the layer has not been initialized or is invisible, do
			// nothing.
			if ((!layer.isInitialized()) || (!layer.isVisible()))
				continue;

			if (layer instanceof GroupLayer) {
				Layer[] sublayers = ((GroupLayer) layer).getLayers();
				if (sublayers != null) {
					for (Layer flayer : sublayers) {
						ArcGISFeatureLayer featureLayer = (ArcGISFeatureLayer) flayer;
						if (featureLayer.getPopupInfo() != null) {
							// Query feature layer which is associated with a
							// pop-up definition.
							mCount.incrementAndGet();
							new RunQueryFeatureLayerTask(x, y, tolerance)
									.execute(featureLayer);
						}
					}
				}
			} else if (layer instanceof ArcGISFeatureLayer) {
				// Query feature layer and display pop-ups
				ArcGISFeatureLayer featureLayer = (ArcGISFeatureLayer) layer;
				if (featureLayer.getPopupInfo() != null) {
					// Query feature layer which is associated with a popup
					// definition.
					mCount.incrementAndGet();
					new RunQueryFeatureLayerTask(x, y, tolerance)
							.execute(featureLayer);
				}
			} else if ((layer instanceof ArcGISDynamicMapServiceLayer)
					|| (layer instanceof ArcGISTiledMapServiceLayer)) {
				// Query dynamic map service layer and display pop-ups.
				ArcGISLayerInfo[] layerinfos;
				// Retrieve layer info for each sub-layer of the dynamic map
				// service layer.
				if (layer instanceof ArcGISDynamicMapServiceLayer)
					layerinfos = ((ArcGISDynamicMapServiceLayer) layer)
							.getAllLayers();
				else
					layerinfos = ((ArcGISTiledMapServiceLayer) layer)
							.getAllLayers();

				if (layerinfos == null)
					continue;

				// Loop through each sub-layer
				for (ArcGISLayerInfo layerInfo : layerinfos) {
					// Obtain PopupInfo for sub-layer.
					int subLayerId = layerInfo.getId();
					// Check if a sub-layer is visible by checking its
					// visibility and tracing back to its parent.
					// A sub-layer's visibility depends on the visibility of
					// itself and its parent. When a sub-layer
					// is visible but its parent is invisible, its visibility is
					// false. So need to track back to its parent.
					ArcGISLayerInfo info = layerInfo;
					// If a sub-layer is visible then get hold of its parent and
					// check the parent's visibility.
					// Continue till the current sub-layer is invisible or
					// reaches the root.
					while ((info != null) && (info.isVisible()))
						info = info.getParentLayer();
					// If the current sub-layer is invisible skip it.
					if ((info != null) && (!info.isVisible()))
						continue;

					// Check if the sub-layer is within the scale range
					double maxScale = layerInfo.getMaxScale();
					double minScale = layerInfo.getMinScale();

					if (((maxScale == 0) || (mMapView.getScale() > maxScale))
							&& ((minScale == 0) || (mMapView.getScale() < minScale))) {
						// Query sub-layer which is associated with a pop-up
						// definition and is visible and in scale range.
						String url = layer.getQueryUrl(subLayerId);
						if ((url == null) || (url.length() < 1))
							url = layer.getUrl() + "/" + subLayerId;
						mCount.incrementAndGet();
						new RunQueryDynamicLayerTask(env, layer,
								layerInfo.getId(), layer.getSpatialReference())
								.execute(url);
					}
				}
			}
		}

	}

	// Create a customized layout for a pop-up.
	private PopupLayoutInfo createPopupLayout(Context context) {

		PopupLayoutInfo layoutInfo = new PopupLayoutInfo();
		// inflate the layout from xml
		ViewGroup xmlLayout = (ViewGroup) ((Activity) context)
				.getLayoutInflater().inflate(R.layout.popup_layout, null);
		// set the xml layout on the pop-up layout
		layoutInfo.setLayout(xmlLayout);

		// Set the place holders for the four basic views in a pop-up.
		layoutInfo.setTitleViewPlaceHolder((ViewGroup) xmlLayout
				.findViewById(R.id.title_view_placeholder));
		layoutInfo.setAttributesViewPlaceHolder((ViewGroup) xmlLayout
				.findViewById(R.id.attribute_view_placeholder));
		layoutInfo.setMediaViewPlaceHolder((ViewGroup) xmlLayout
				.findViewById(R.id.media_view_placeholder));
		layoutInfo.setAttachmentsViewPlaceHolder((ViewGroup) xmlLayout
				.findViewById(R.id.attachment_view_placeholder));

		return layoutInfo;
	}

	// Customize the four basic views of a pop-up via xml and sub-classes.
	private void createViews(Popup popup) {
		if ((popup == null) || (popup.getLayout() == null))
			return;

		PopupLayout popupLayout = popup.getLayout();
		Context context = mMapView.getContext();

		// Create a view for the title
		popupLayout.setTitleView(new MyTitleView(context, popup, mMapView));

		// Create a view for attributes
		popupLayout.setAttributesView(new ArcGISAttributeView(context, popup));
		ArcGISAttributeView attributeView = (ArcGISAttributeView) popupLayout
				.getAttributesView();
		// Create an adapter for the attributes in read-only mode
		attributeView.setReadOnlyAdapter(new MyReadOnlyAttributesAdapter(
				context, popup));
		// Create an adapter for the attributes in edit mode
		attributeView
				.setEditAdapter(new MyEditAttributesAdapter(context, popup));

		// Change the view for the attachments
		if ((popup.getPopupInfo() != null)
				&& (popup.getPopupInfo().isShowAttachments())) {
			popupLayout
					.setAttachmentsView(new MyAttachmentsView(context, popup));
		}

		// Change the view for the media
		popupLayout.setMediaView(new MyMediaView(context, popup));
	}

	// Compose geometry info for a feature based on its geometry type.
	private String getGeometryInfo(Graphic graphic,
			ArcGISValueFormat valueFormat) {
		Geometry geometry = graphic.getGeometry();
		String geometryInfo = null;
		if (geometry == null) {
			geometryInfo = "Missing location";
		} else {
			String unit = mMapView.getSpatialReference().getUnit()
					.getDisplayName();
			switch (graphic.getGeometry().getType()) {
			case ENVELOPE:
			case POLYGON:
			case MULTIPOINT:
				geometryInfo = "Area: "
						+ valueFormat.formatValue(new Double(geometry
								.calculateArea2D())) + " Square " + unit + "s";
				break;
			case LINE:
			case POLYLINE:
				geometryInfo = "Distance: "
						+ valueFormat.formatValue(new Double(geometry
								.calculateLength2D())) + " " + unit + "s";
				break;
			case POINT:
			default:
				geometryInfo = "X: "
						+ valueFormat.formatValue(new Double(((Point) geometry)
								.getX()))
						+ ", Y:"
						+ valueFormat.formatValue(new Double(((Point) geometry)
								.getY()));
			}
		}

		return geometryInfo;
	}

	// Query dynamic map service layer by QueryTask
	private class RunQueryDynamicLayerTask extends
			AsyncTask<String, Void, FeatureResult> {

		private Envelope env;
		private SpatialReference sr;
		private Layer layer;
		private int subLayerId;

		public RunQueryDynamicLayerTask(Envelope env, Layer layer,
				int subLayerId, SpatialReference sr) {
			super();
			this.env = env;
			this.sr = sr;
			this.layer = layer;
			this.subLayerId = subLayerId;
		}

		@Override
		protected FeatureResult doInBackground(String... urls) {

			for (String url : urls) {
				PopupInfo popupInfo = layer.getPopupInfo(subLayerId);
				// Skip sub-layer which is without a pop-up definition.
				if (popupInfo == null)
					continue;

				// Retrieve graphics within the envelope.
				QueryParameters queryParams = new QueryParameters();
				queryParams.setInSpatialReference(sr);
				queryParams.setOutSpatialReference(sr);
				queryParams.setGeometry(env);
				queryParams.setMaxFeatures(10);
				queryParams.setOutFields(new String[] { "*" });

				try {
					QueryTask queryTask;
					queryTask = new QueryTask(url, layer.getCredentials());

					FeatureResult results = queryTask.execute(queryParams);
					return results;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(final FeatureResult result) {
			// Validate parameter.
			mCount.decrementAndGet();
			if (result == null)
				return;

			// iterate through results
			for (Object element : result) {
				// if object is a feature cast to feature
				if (element instanceof Feature) {
					Feature feature = (Feature) element;
					// convert feature to graphic
					Graphic graphic = new Graphic(feature.getGeometry(), null,
							feature.getAttributes());
					PopupLayoutInfo layout = createPopupLayout(mMapView
							.getContext());
					Popup popup = layer.createPopup(mMapView, subLayerId,
							graphic, layout, false);
					createViews(popup);

					if (mShowGeometryInfo) {
						ArcGISValueFormat valueFormat = new ArcGISValueFormat(
								popup);
						ArcGISTitleView titleView = (ArcGISTitleView) popup
								.getLayout().getTitleView();
						titleView.setGeometryInfo(getGeometryInfo(graphic,
								valueFormat));
					}

					mPopupFragment.addPopup(popup);
					mPopupFragment.show();
				}
			}

		}

	}

	// Query feature layer by hit-test
	private class RunQueryFeatureLayerTask extends
			AsyncTask<ArcGISFeatureLayer, Void, Graphic[]> {

		private int tolerance;
		private float x;
		private float y;
		private ArcGISFeatureLayer featureLayer;

		public RunQueryFeatureLayerTask(float x, float y, int tolerance) {
			super();
			this.x = x;
			this.y = y;
			this.tolerance = tolerance;
		}

		@Override
		protected Graphic[] doInBackground(ArcGISFeatureLayer... params) {
			for (ArcGISFeatureLayer agsFeatureLayer : params) {
				this.featureLayer = agsFeatureLayer;
				// Retrieve graphic ids near the point.
				int[] ids = agsFeatureLayer.getGraphicIDs(x, y, tolerance);
				if ((ids != null) && (ids.length > 0)) {
					ArrayList<Graphic> graphics = new ArrayList<Graphic>();
					for (int id : ids) {
						// Obtain graphic based on the id.
						Graphic g = agsFeatureLayer.getGraphic(id);
						if (g == null)
							continue;
						graphics.add(g);
					}
					// Return an array of graphics near the point.
					return graphics.toArray(new Graphic[0]);
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Graphic[] graphics) {
			mCount.decrementAndGet();
			// Validate parameter.
			if (graphics == null || graphics.length == 0)
				return;

			PopupInfo popupInfo = featureLayer.getPopupInfo();
			if (popupInfo == null)
				return;

			for (Graphic gr : graphics) {
				PopupLayoutInfo layout = createPopupLayout(mMapView
						.getContext());
				Popup popup = featureLayer.createPopup(mMapView, 0, gr, layout,
						false);
				createViews(popup);

				if (mShowGeometryInfo) {
					ArcGISValueFormat valueFormat = new ArcGISValueFormat(popup);
					ArcGISTitleView titleView = (ArcGISTitleView) popup
							.getLayout().getTitleView();
					titleView.setGeometryInfo(getGeometryInfo(gr, valueFormat));
				}
				mPopupFragment.addPopup(popup);
				mPopupFragment.show();
			}

		}

	}
}
