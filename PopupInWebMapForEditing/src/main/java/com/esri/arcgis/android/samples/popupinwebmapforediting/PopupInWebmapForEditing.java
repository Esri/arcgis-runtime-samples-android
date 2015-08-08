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

package com.esri.arcgis.android.samples.popupinwebmapforediting;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.FeatureType;
import com.esri.core.map.Graphic;
import com.esri.core.map.popup.PopupInfo;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public class PopupInWebmapForEditing extends Activity {
	private MapView map;
	private PopupContainer popupContainer;
	private PopupDialog popupDialog;
	private ProgressDialog progressDialog;
	private AtomicInteger count;
	private LinearLayout editorBar;

	/** Called when the activity is first created. */
	@Override
  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load a webmap.
		map = new MapView(
				this,
				"http://www.arcgis.com/home/item.html?id=81d2dcf906cf4df4889ec36c8dc0c1f9",
				"", "");
		setContentView(map);

		// Tap on the map and show popups for selected features.
		map.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;

			@Override
      public void onSingleTap(float x, float y) {
				if (map.isLoaded()) {
					// Instantiate a PopupContainer
					popupContainer = new PopupContainer(map);
					int id = popupContainer.hashCode();
					popupDialog = null;
					// Display spinner.
					if (progressDialog == null || !progressDialog.isShowing())
						progressDialog = ProgressDialog.show(map.getContext(),
								"", "Querying...");

					// Loop through each layer in the webmap
					int tolerance = 20;
					Envelope env = new Envelope(map.toMapPoint(x, y), 20 * map
							.getResolution(), 20 * map.getResolution());
					Layer[] layers = map.getLayers();
					count = new AtomicInteger();
					for (Layer layer : layers) {
						// If the layer has not been initialized or is
						// invisible, do nothing.
						if (!layer.isInitialized() || !layer.isVisible())
							continue;

						if (layer instanceof ArcGISFeatureLayer) {
							// Query feature layer and display popups
							ArcGISFeatureLayer featureLayer = (ArcGISFeatureLayer) layer;
							if (featureLayer.getPopupInfo() != null) {
								// Query feature layer which is associated with
								// a popup definition.
								count.incrementAndGet();
								new RunQueryFeatureLayerTask(x, y, tolerance,
										id).execute(featureLayer);
							}
						} else if (layer instanceof ArcGISDynamicMapServiceLayer) {
							// Query dynamic map service layer and display
							// popups.
							ArcGISDynamicMapServiceLayer dynamicLayer = (ArcGISDynamicMapServiceLayer) layer;
							// Retrieve layer info for each sub-layer of the
							// dynamic map service layer.
							ArcGISLayerInfo[] layerinfos = dynamicLayer
									.getAllLayers();
							if (layerinfos == null)
								continue;

							// Loop through each sub-layer
							for (ArcGISLayerInfo layerInfo : layerinfos) {
								// Obtain PopupInfo for sub-layer.
								PopupInfo popupInfo = dynamicLayer
										.getPopupInfo(layerInfo.getId());
								// Skip sub-layer which is without a popup
								// definition.
								if (popupInfo == null) {
									continue;
								}
								// Check if a sub-layer is visible.
								ArcGISLayerInfo info = layerInfo;
								while (info != null && info.isVisible()) {
									info = info.getParentLayer();
								}
								// Skip invisible sub-layer
								if (info != null && !info.isVisible()) {
									continue;
								}

								// Check if the sub-layer is within the scale
								// range
								double maxScale = (layerInfo.getMaxScale() != 0) ? layerInfo
										.getMaxScale() : popupInfo
										.getMaxScale();
								double minScale = (layerInfo.getMinScale() != 0) ? layerInfo
										.getMinScale() : popupInfo
										.getMinScale();

								if ((maxScale == 0 || map.getScale() > maxScale)
										&& (minScale == 0 || map.getScale() < minScale)) {
									// Query sub-layer which is associated with
									// a popup definition and is visible and in
									// scale range.
									count.incrementAndGet();
									new RunQueryDynamicLayerTask(env, layer,
											layerInfo.getId(), dynamicLayer
													.getSpatialReference(), id)
											.execute(dynamicLayer.getUrl()
													+ "/" + layerInfo.getId());
								}
							}
						}
					}
				}
			}
		});

		map.setOnLongPressListener(new OnLongPressListener() {
			private static final long serialVersionUID = 1L;
			private ArcGISFeatureLayer featureLayer = null;

			@Override
      public boolean onLongPress(float x, float y) {
				if (map.isLoaded()) {
					if (progressDialog != null && progressDialog.isShowing()
							&& count.intValue() == 0)
						progressDialog.dismiss();

					// Get the point featurelayer
					Layer[] layers = map.getLayers();
					for (Layer layer : layers) {
						if (layer instanceof ArcGISFeatureLayer) {
							ArcGISFeatureLayer fl = (ArcGISFeatureLayer) layer;
							if (fl.getGeometryType() == Geometry.Type.POINT) {
								featureLayer = fl;
								break;
							}
						}
					}

					if (featureLayer == null)
						return false;
					PopupInfo popupInfo = featureLayer.getPopupInfo();
					if (popupInfo == null)
						return false;

					// Create a new feature
					Point point = map.toMapPoint(x, y);
					Feature feature;
					FeatureType[] types = featureLayer.getTypes();
					if (types == null || types.length < 1) {
						FeatureTemplate[] templates = featureLayer
								.getTemplates();
						if (templates == null || templates.length < 1) {
							feature = new Graphic(point, null);
						} else {
							feature = featureLayer.createFeatureWithTemplate(
									templates[0], point);
						}
					} else {
						feature = featureLayer.createFeatureWithType(
								featureLayer.getTypes()[0], point);
					}

					// Instantiate a PopupContainer
					popupContainer = new PopupContainer(map);
					// Add Popup
					Popup popup = featureLayer.createPopup(map, 0, feature);
					popup.setEditMode(true);
					popupContainer.addPopup(popup);
					createEditorBar(featureLayer, false);

					// Create a dialog for the popups and display it.
					popupDialog = new PopupDialog(map.getContext(),
							popupContainer);
					popupDialog.show();
				}
				return true;
			}

		});
	}

	private class EditCallbackListener implements
			CallbackListener<FeatureEditResult[][]> {
		private String operation = "Operation ";
		private ArcGISFeatureLayer featureLayer = null;
		private boolean existingFeature = true;

		public EditCallbackListener(String msg,
				ArcGISFeatureLayer featureLayer, boolean existingFeature) {
			this.operation = msg;
			this.featureLayer = featureLayer;
			this.existingFeature = existingFeature;
		}

		@Override
		public void onCallback(FeatureEditResult[][] objs) {
			if (featureLayer == null || !featureLayer.isInitialized()
					|| !featureLayer.isEditable())
				return;

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(PopupInWebmapForEditing.this,
							operation + " succeeded!", Toast.LENGTH_SHORT)
							.show();
				}
			});

			if (objs[1] == null || objs[1].length <= 0) {
				// Save attachments to the server if newly added attachments
				// exist.
				// Retrieve object id of the feature
				long oid;
				if (existingFeature) {
					oid = objs[2][0].getObjectId();
				} else {
					oid = objs[0][0].getObjectId();
				}
				// prepare oid as int for FeatureLayer
				int objectID = (int) oid;
				// Get newly added attachments
				List<File> attachments = popupContainer.getCurrentPopup()
						.getAddedAttachments();
				if (attachments != null && attachments.size() > 0) {
					for (File attachment : attachments) {
						// Save newly added attachment based on the object id of
						// the feature.
						featureLayer.addAttachment(objectID, attachment,
								new CallbackListener<FeatureEditResult>() {
									@Override
                  public void onError(Throwable e) {
										// Failed to save new attachments.
										runOnUiThread(new Runnable() {
											@Override
                      public void run() {
												Toast.makeText(
														PopupInWebmapForEditing.this,
														"Adding attachment failed!",
														Toast.LENGTH_SHORT)
														.show();
											}
										});
									}

									@Override
                  public void onCallback(
											FeatureEditResult arg0) {
										// New attachments have been saved.
										runOnUiThread(new Runnable() {
											@Override
                      public void run() {
												Toast.makeText(
														PopupInWebmapForEditing.this,
														"Adding attachment succeeded!.",
														Toast.LENGTH_SHORT)
														.show();
											}
										});
									}
								});
					}
				}

				// Delete attachments if some attachments have been mark as
				// delete.
				// Get ids of attachments which are marked as delete.
				List<Integer> attachmentIDs = popupContainer.getCurrentPopup()
						.getDeletedAttachmentIDs();
				if (attachmentIDs != null && attachmentIDs.size() > 0) {
					int[] ids = new int[attachmentIDs.size()];
					for (int i = 0; i < attachmentIDs.size(); i++) {
						ids[i] = attachmentIDs.get(i);
					}
					// Delete attachments
					featureLayer.deleteAttachments(objectID, ids,
							new CallbackListener<FeatureEditResult[]>() {
								@Override
                public void onError(Throwable e) {
									// Failed to delete attachments
									runOnUiThread(new Runnable() {
										@Override
                    public void run() {
											Toast.makeText(
													PopupInWebmapForEditing.this,
													"Deleting attachment failed!",
													Toast.LENGTH_SHORT).show();
										}
									});
								}

								@Override
                public void onCallback(FeatureEditResult[] featureEditResults) {
									// Attachments have been removed.
									runOnUiThread(new Runnable() {
										@Override
                    public void run() {
											Toast.makeText(
													PopupInWebmapForEditing.this,
													"Deleting attachment succeeded!",
													Toast.LENGTH_SHORT).show();
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
					Toast.makeText(PopupInWebmapForEditing.this,
							operation + " failed!", Toast.LENGTH_SHORT).show();
				}
			});
		}

	}

	private void createEditorBar(final ArcGISFeatureLayer fl,
			final boolean existing) {
		if (fl == null || !fl.isInitialized() || !fl.isEditable())
			return;

		editorBar = new LinearLayout(this);

		Button cancelButton = new Button(this);
		cancelButton.setText("Cancel");
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (popupDialog != null)
					popupDialog.dismiss();
			}
		});
		editorBar.addView(cancelButton);

		final Button deleteButton = new Button(this);
		deleteButton.setText("Delete");
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (popupContainer == null
						|| popupContainer.getPopupCount() <= 0)
					return;
				popupDialog.dismiss();

				Feature fr = popupContainer.getCurrentPopup().getFeature();
				Graphic gr = new Graphic(fr.getGeometry(), fr.getSymbol(), fr.getAttributes());
				fl.applyEdits(null, new Graphic[] { gr }, null,
						new EditCallbackListener("Deleting feature", fl,
								existing));

			}
		});
		if (existing)
			editorBar.addView(deleteButton);

		final Button attachmentButton = new Button(this);
		attachmentButton.setText("Add Attachment");
		attachmentButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(Intent.ACTION_PICK,
						MediaStore.Images.Media.INTERNAL_CONTENT_URI), 1);
			}
		});
		if (!existing && fl.hasAttachments())
			attachmentButton.setVisibility(View.VISIBLE);
		else
			attachmentButton.setVisibility(View.INVISIBLE);
		editorBar.addView(attachmentButton);

		final Button saveButton = new Button(this);
		saveButton.setText("Save");
		if (existing)
			saveButton.setVisibility(View.INVISIBLE);
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (popupContainer == null
						|| popupContainer.getPopupCount() <= 0)
					return;
				popupDialog.dismiss();

				Popup popup = popupContainer.getCurrentPopup();
				Feature fr = popup.getFeature();
				Map<String, Object> attributes = fr.getAttributes();
				Map<String, Object> updatedAttrs = popup.getUpdatedAttributes();
				for (Entry<String, Object> entry : updatedAttrs.entrySet()) {
					attributes.put(entry.getKey(), entry.getValue());
				}
				Graphic newgr = new Graphic(fr.getGeometry(), null, attributes);
				if (existing)
					fl.applyEdits(null, null, new Graphic[] { newgr },
							new EditCallbackListener("Saving edits", fl,
									existing));
				else
					fl.applyEdits(new Graphic[] { newgr }, null, null,
							new EditCallbackListener("Creating new feature",
									fl, existing));
			}
		});
		editorBar.addView(saveButton);

		final Button editButton = new Button(map.getContext());
		editButton.setText("Edit");
		editButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (popupContainer == null
						|| popupContainer.getPopupCount() <= 0)
					return;

				popupContainer.getCurrentPopup().setEditMode(true);
				saveButton.setVisibility(View.VISIBLE);
				deleteButton.setVisibility(View.INVISIBLE);
				editButton.setVisibility(View.INVISIBLE);
				if (fl.hasAttachments())
					attachmentButton.setVisibility(View.VISIBLE);
			}
		});
		if (existing) {
			editorBar.addView(editButton);
		}

		popupContainer.getPopupContainerView().addView(editorBar, 0);

	}

	private void createPopupViews(final int id) {
		if (id != popupContainer.hashCode()) {
			if (progressDialog != null && progressDialog.isShowing()
					&& count.intValue() == 0)
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
	private class RunQueryFeatureLayerTask extends
			AsyncTask<ArcGISFeatureLayer, Void, Feature[]> {

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
			for (ArcGISFeatureLayer fLayer : params) {
				this.featureLayer = fLayer;
				// Retrieve feature ids near the point.
				int[] ids = fLayer.getGraphicIDs(x, y, tolerance);
				if (ids != null && ids.length > 0) {
					ArrayList<Feature> features = new ArrayList<Feature>();
					for (int graphicId : ids) {
						// Obtain feature based on the id.
						Feature f = fLayer.getGraphic(graphicId);
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
			// Validate parameter.
			if (features == null || features.length == 0) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing()
						&& count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}
			// Check if the requested PopupContainer id is the same as the
			// current PopupContainer.
			// Otherwise, abandon the obsoleted query result.
			if (id != popupContainer.hashCode()) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing()
						&& count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}

			PopupInfo popupInfo = featureLayer.getPopupInfo();
			if (popupInfo == null) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing()
						&& count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}

			for (Feature fr : features) {
				Popup popup = featureLayer.createPopup(map, 0, fr);
				popupContainer.addPopup(popup);
			}
			createEditorBar(featureLayer, true);
			createPopupViews(id);
		}

	}

	// Query dynamic map service layer by QueryTask
	private class RunQueryDynamicLayerTask extends
			AsyncTask<String, Void, FeatureSet> {
		private Envelope env;
		private SpatialReference sr;
		private int id;
		private Layer layer;
		private int subLayerId;

		public RunQueryDynamicLayerTask(Envelope env, Layer layer,
				int subLayerId, SpatialReference sr, int id) {
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
			// Validate parameter.
			count.decrementAndGet();
			if (result == null) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing()
						&& count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}
			Feature[] features = result.getGraphics();
			if (features == null || features.length == 0) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing()
						&& count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}
			// Check if the requested PopupContainer id is the same as the
			// current PopupContainer.
			// Otherwise, abandon the obsoleted query result.
			if (id != popupContainer.hashCode()) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing()
						&& count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}

			for (Feature fr : features) {
				Popup popup = layer.createPopup(map, subLayerId, fr);
				popupContainer.addPopup(popup);
			}
			createPopupViews(id);

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK && data != null
				&& popupContainer != null) {
			// Add the selected media as attachment.
			Uri selectedImage = data.getData();
			popupContainer.getCurrentPopup().addAttachment(selectedImage);
		}
	}

	// A customize full screen dialog.
	private class PopupDialog extends Dialog {
		private PopupContainer pContainer;

		public PopupDialog(Context context, PopupContainer popupContainer) {
			super(context, android.R.style.Theme);
			this.pContainer = popupContainer;
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			LinearLayout layout = new LinearLayout(getContext());
			layout.addView(pContainer.getPopupContainerView(),
					LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			setContentView(layout, params);
		}

	}

}