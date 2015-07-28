package com.esri.arcgis.android.samples.offlineeditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISPopupInfo;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.core.geodatabase.GeodatabaseEditError;
import com.esri.core.geodatabase.GeodatabaseFeature;
import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.Envelope;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;
import com.esri.core.table.TableException;

public class PopupForEditFeatureLayer {

	private MapView map;
	private Activity mainActivity;

	private PopupContainer popupContainer;
	private PopupDialog popupDialog;
	private ProgressDialog progressDialog;
	private AtomicInteger count;
	private LinearLayout editorBarLocal;
	private GeodatabaseFeature selectedGdbFeature;
	private AlertDialog attachmentDialog;
	private int counter = 0;

	private final String TAG = "PopupForEditFeatureLayer";

	public PopupForEditFeatureLayer(MapView mapView, Activity mainActivity) {
		this.map = mapView;
		this.mainActivity = mainActivity;
	}

	public void addAttachment(Uri selectedImage) {
		Popup popup = popupContainer.getCurrentPopup();
		if (popup != null)
			popup.addAttachment(selectedImage);
	}

	public void showPopup(float x, float y, int tolerance) {
		if (!map.isLoaded())
			return;

		// Instantiate a PopupContainer
		popupContainer = new PopupContainer(map);
		int id = popupContainer.hashCode();
		popupDialog = null;
		// Display spinner.
		if (progressDialog == null || !progressDialog.isShowing())
			progressDialog = ProgressDialog.show(map.getContext(), "",
					"Querying...");

		// Loop through each layer in the webmap
		Envelope env = new Envelope(map.toMapPoint(x, y), tolerance
				* map.getResolution(), tolerance * map.getResolution());
		Layer[] layers = map.getLayers();
		count = new AtomicInteger();
		for (Layer layer : layers) {
			// If the layer has not been initialized or is invisible, do
			// nothing.
			if (!layer.isInitialized() || !layer.isVisible())
				continue;

			if (layer instanceof FeatureLayer) {
				// Query feature layer and display popups
				FeatureLayer localFeatureLayer = (FeatureLayer) layer;
				if (localFeatureLayer.getPopupInfos() != null) {
					// Query feature layer which is associated with a popup
					// definition.
					count.incrementAndGet();
					new RunQueryLocalFeatureLayerTask(x, y, tolerance, id)
							.execute(localFeatureLayer);
				}
			}
		}

	}

	private class RunQueryLocalFeatureLayerTask extends
			AsyncTask<FeatureLayer, Void, Feature[]> {

		private int tolerance;
		private float x;
		private float y;
		private FeatureLayer localFeatureLayer;
		private int id;

		public RunQueryLocalFeatureLayerTask(float x, float y, int tolerance,
											 int id) {
			super();
			this.x = x;
			this.y = y;
			this.tolerance = tolerance;
			this.id = id;
		}

		@Override
		protected Feature[] doInBackground(FeatureLayer... params) {
			for (FeatureLayer featureLayer : params) {
				this.localFeatureLayer = featureLayer;
				// Retrieve graphic ids near the point.
				long[] ids = featureLayer.getFeatureIDs(x, y, tolerance);
				if (ids != null && ids.length > 0) {
					ArrayList<Feature> features = new ArrayList<Feature>();
					for (long id : ids) {
						// Obtain graphic based on the id.

						Feature f = featureLayer.getFeature(id);
						if (f == null)
							continue;
						features.add(f);
					}
					// Return an array of graphics near the point.
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

			Map<Integer, ArcGISPopupInfo> popupInfos = localFeatureLayer
					.getPopupInfos();
			if (popupInfos == null) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing()
						&& count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}

			for (Feature fr : features) {
				// Graphic gr = new Graphic(fr.getGeometry(), null,
				// fr.getAttributes());
				Popup popup = localFeatureLayer.createPopup(map, 0, fr);
				popupContainer.addPopup(popup);
				popup.setEditMode(true);

			}
			counter++;
			if (counter < 2) {
				createEditorBarLocal(localFeatureLayer, false);

			}
			createPopupViews(id);
		}

	}

	private void createEditorBarLocal(final FeatureLayer fl,
									  final boolean existing) {

		if (fl == null || !fl.isInitialized()
				|| !fl.getFeatureTable().isEditable())
			return;

		editorBarLocal = new LinearLayout(mainActivity);

		final Button deleteButton = new Button(mainActivity);
		deleteButton.setText("Delete");
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (popupContainer == null
						|| popupContainer.getPopupCount() <= 0)
					return;
				popupDialog.dismiss();

				GeodatabaseFeature fr = (GeodatabaseFeature) popupContainer.getCurrentPopup()
						.getFeature();

				try {
					long deleteId = fr.getId();
					fr.getTable().deleteFeature(deleteId);
					applyEditsToServer(fr);
				} catch (TableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		if (!existing && popupContainer.getCurrentPopup().isDeletable())
			editorBarLocal.addView(deleteButton);

		final Button attachmentButton = new Button(mainActivity);
		attachmentButton.setText("Attach");

		attachmentButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//setSelectedGdbFeature((GeodatabaseFeature) popupContainer.getCurrentPopup().getFeature());

				final CharSequence[] items = {" Image ", " Video "};

				// Creating and Building the Dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
				builder.setTitle("Select Attachment Type");
				builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						switch (item) {
							case 0:
								mainActivity.startActivityForResult(new Intent(
										Intent.ACTION_PICK,
										MediaStore.Images.Media.INTERNAL_CONTENT_URI), 3);
								attachmentDialog.dismiss();
								break;

							case 1:
								mainActivity.startActivityForResult(new Intent(
										Intent.ACTION_PICK,
										MediaStore.Video.Media.INTERNAL_CONTENT_URI), 3);
								attachmentDialog.dismiss();
								break;
						}

					}
				});
				attachmentDialog = builder.create();
				attachmentDialog.show();

			}


		});

		if (!existing
				&& ((GeodatabaseFeatureTable) ((GeodatabaseFeature) popupContainer
				.getCurrentPopup().getFeature()).getTable())
				.hasAttachments() && popupContainer.getCurrentPopup().isEditable())
			attachmentButton.setVisibility(View.VISIBLE);
		else
			attachmentButton.setVisibility(View.INVISIBLE);
		editorBarLocal.addView(attachmentButton);


		final Button saveButton = new Button(mainActivity);
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
				GeodatabaseFeature fr = (GeodatabaseFeature) popup.getFeature();

				Map<String, Object> attributes = fr.getAttributes();
				Map<String, Object> updatedAttrs = popup.getUpdatedAttributes();

				Iterator<Map.Entry<String, Object>> iter = attributes.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, Object> entry = iter.next();
					Field f = fr.getTable().getField(entry.getKey());
					if (!f.isEditable()) {
						if (entry.getKey().equalsIgnoreCase("CREATED_USER") || entry.getKey().equalsIgnoreCase("CREATED_DATE") || entry.getKey().equalsIgnoreCase("LAST_EDITED_USER") || entry.getKey().equalsIgnoreCase("LAST_EDITED_DATE")) {
							iter.remove();
						}
					}

				}


				for (Entry<String, Object> entry : updatedAttrs.entrySet()) {

					attributes.put(entry.getKey(), entry.getValue());
					Log.d(entry.getKey(), String.valueOf(entry.getValue()));

				}


				Graphic newgr = new Graphic(null, null, attributes);

				try {
					long updateId = fr.getId();
					Log.d("Graphics", newgr.getAttributes()
							.toString());
					Log.d("graphics", popup.getAddedAttachments().size() + "");
					if (popup.getAddedAttachments().size() > 0) {
						for (File attachment : popup.getAddedAttachments()) {
							String fileName = attachment.getName();
							Log.d("Name", fileName);
							String contentType = getContentType(attachment);

							try {
								showProgress(mainActivity, true);
								((GeodatabaseFeatureTable) fr.getTable()).addAttachment(fr.getId(), attachment, contentType, fileName, new CallbackListener<Long>() {

									@Override
									public void onError(final Throwable e) {
										showProgress(mainActivity, false);
										mainActivity.runOnUiThread(new Runnable() {

											@Override
											public void run() {
												Toast.makeText(mainActivity,
														"Failed to Add Attachment !" + e.getMessage(), Toast.LENGTH_LONG)
														.show();
											}
										});
										e.printStackTrace();
									}

									@Override
									public void onCallback(final Long attachmentId) {
										showProgress(mainActivity, false);
										mainActivity.runOnUiThread(new Runnable() {

											@Override
											public void run() {
												Toast.makeText(mainActivity,
														"Succesfully Added attachment id:" + attachmentId.toString(), Toast.LENGTH_LONG)
														.show();
											}
										});
									}
								});
							} catch (final FileNotFoundException e) {
								// TODO Auto-generated catch block
								showProgress(mainActivity, false);
								mainActivity.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										Toast.makeText(mainActivity,
												"Failed to Add Attachment !" + e.getMessage(), Toast.LENGTH_LONG)
												.show();
									}
								});
								e.printStackTrace();

							}

						}
					}
					if (popup.getDeletedAttachmentIDs().size() > 0) {
						for (Integer attachId : popup.getDeletedAttachmentIDs()) {
							showProgress(mainActivity, true);
							((GeodatabaseFeatureTable) fr.getTable()).deleteAttachment(fr.getId(), (long) attachId, new CallbackListener<Void>() {

								@Override
								public void onError(final Throwable e) {
									showProgress(mainActivity, false);
									mainActivity.runOnUiThread(new Runnable() {

										@Override
										public void run() {
											Toast.makeText(mainActivity,
													"Failed to Delete Attachment !" + e.getMessage(), Toast.LENGTH_LONG)
													.show();
										}
									});
									e.printStackTrace();
								}

								@Override
								public void onCallback(Void attachmentId) {
									showProgress(mainActivity, false);
									mainActivity.runOnUiThread(new Runnable() {

										@Override
										public void run() {
											Toast.makeText(mainActivity,
													"Succesfully Deleted attachment:", Toast.LENGTH_LONG)
													.show();
										}
									});
								}
							});
						}
					}
					fr.getTable().updateFeature(updateId, newgr);
					applyEditsToServer(fr);
					fr.getTable().addFeature(newgr);
					applyEditsToServer(fr);

				} catch (TableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		editorBarLocal.addView(saveButton);

		final Button editButton = new Button(map.getContext());
		editButton.setBackgroundResource(android.R.drawable.ic_menu_edit);
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
				if (((GeodatabaseFeatureTable) ((GeodatabaseFeature) popupContainer
						.getCurrentPopup().getFeature()).getTable())
						.hasAttachments())
					attachmentButton.setVisibility(View.VISIBLE);
			}
		});
		if (existing && popupContainer.getCurrentPopup().isEditable()) {
			editorBarLocal.addView(editButton);
		}

		popupContainer.getPopupContainerView().addView(editorBarLocal, 0);

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
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			LinearLayout layout = new LinearLayout(getContext());
			layout.addView(popupContainer.getPopupContainerView(),
					android.widget.LinearLayout.LayoutParams.FILL_PARENT,
					android.widget.LinearLayout.LayoutParams.FILL_PARENT);
			setContentView(layout, params);
		}



	}

/*	public void setSelectedGdbFeature(GeodatabaseFeature selectedGdbFeature) {
		this.selectedGdbFeature = selectedGdbFeature;
	}*/

	private String getContentType(File localFile) {
		// TODO Auto-generated method stub	
		String contentType = "";
		String extension = "";
		String fullFilePath = localFile.getAbsolutePath();
		if (fullFilePath.contains(".")) {
			extension = fullFilePath.substring(fullFilePath.lastIndexOf(".") + 1);
			if (extension != null && !"".equals(extension)) {
				String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
				if (mimeType != null) {
					contentType = mimeType;
				}
			}
		}
		return contentType;
	}

	private void applyEditsToServer(final GeodatabaseFeature fr) {

		if (fr.getTable() instanceof GeodatabaseFeatureServiceTable) {

			((GeodatabaseFeatureServiceTable) fr.getTable()).applyEdits(new CallbackListener<List<GeodatabaseEditError>>() {

				@Override
				public void onError(Throwable e) {
					// TODO Auto-generated method stub
					mainActivity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(map.getContext(), "Failed to Apply Edits to Server", Toast.LENGTH_SHORT).show();
							AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
							builder.setTitle("Failed to ApplyEdits");
							builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									applyEditsToServer(fr);
								}
							});

							builder.setNegativeButton("Refresh", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									((GeodatabaseFeatureServiceTable) fr.getTable()).refreshFeatures();
								}
							});

							AlertDialog applyEdits = builder.create();
							applyEdits.show();

						}
					});
				}

				@Override
				public void onCallback(List<GeodatabaseEditError> objs) {
					// TODO Auto-generated method stub

					if (objs != null) {
						mainActivity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(map.getContext(), "Succesfully Applied Edits to Server", Toast.LENGTH_SHORT).show();
								applyAttachmnetEditsToServer(fr);
							}
						});
					}

				}
			});

		}
	}

	private void applyAttachmnetEditsToServer(final GeodatabaseFeature fr) {
		if (((GeodatabaseFeatureTable) fr.getTable()).hasAttachments()) {
			((GeodatabaseFeatureServiceTable) fr.getTable()).applyAttachmentEdits(new CallbackListener<List<GeodatabaseEditError>>() {

				@Override
				public void onError(Throwable e) {
					// TODO Auto-generated method stub						
					mainActivity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(map.getContext(), "Failed to Apply Attachments to Server", Toast.LENGTH_SHORT).show();
							AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
							builder.setTitle("Failed to ApplyAttachmentEdits");
							builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									applyAttachmnetEditsToServer(fr);
								}
							});
							builder.setNegativeButton("Refresh", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									((GeodatabaseFeatureServiceTable) fr.getTable()).refreshFeatures();
								}
							});
							AlertDialog applyAttachmentEdits = builder.create();
							applyAttachmentEdits.show();
						}
					});
				}

				@Override
				public void onCallback(List<GeodatabaseEditError> objs) {
					// TODO Auto-generated method stub
					if (objs != null) {
						mainActivity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(map.getContext(), "Successfully Applied Attachment Edits to Server", Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			});
		}
	}

	static void showProgress(final Activity activity, final boolean b) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				activity.setProgressBarIndeterminateVisibility(b);

			}
		});
	}


}