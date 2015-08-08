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

package com.arcgis.android.samples.dynamiclayer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.map.DrawingInfo;
import com.esri.core.map.DynamicLayerInfo;
import com.esri.core.renderer.AlgorithmicColorRamp;
import com.esri.core.renderer.ClassBreaksDefinition;
import com.esri.core.renderer.ClassBreaksDefinition.ClassificationMethod;
import com.esri.core.renderer.ClassBreaksRenderer;
import com.esri.core.renderer.ColorRamp;
import com.esri.core.renderer.NormalizationType;
import com.esri.core.renderer.RampDefinition;
import com.esri.core.tasks.ags.GenerateRendererTask;
import com.esri.core.tasks.ags.GenerateRendererTaskParameters;

import java.util.List;
import java.util.Map;

/**
 * This app uses the DynamicLayerMapService and the DynamicLayer to demonstrate
 * the Class break rendering. On clicking the field from the spinner it will
 * display the rendered image classifying the attribute that you have selected.
 * 
 * Please be aware that the Dynamic Layer service is not guaranteed to be
 * running.
 * 
 */
public class DynamicLayerRendererActivity extends Activity {

	MapView mMapView = null;
	ProgressDialog progress = null;
	ActionBar action = null;
	ClassBreaksRenderer render = null;
	ArcGISDynamicMapServiceLayer dynamicLayer = null;
	
	// query menu items
	MenuItem mPop2007 = null;
	MenuItem mPop2000 = null;
	MenuItem mPop00_Sqmi = null;
	MenuItem mPop07_Sqmi = null;
	MenuItem mWhite = null;
	MenuItem mBlack = null;
	MenuItem mHispanic = null;
	MenuItem mAmeri_Es = null;

	final int layerid = 2;

	private static final int TRANSPARENCY = 10;
	private String classificationField = "POP2000";

	// Dialog to check progress for the service task
	static ProgressDialog dialog = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mMapView = (MapView) findViewById(R.id.map);
		// set layer to display
		int[] layers = new int[1];
		layers[0] = 2;

		dynamicLayer = new ArcGISDynamicMapServiceLayer(this.getResources()
				.getString(R.string.server_url));
		mMapView.addLayer(dynamicLayer);

		 dynamicLayer.setOpacity((float) 0.5);

		action = getActionBar();

		// to check whether the dynamic map is loaded successfully or not
		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
			private static final long serialVersionUID = 1L;

			public void onStatusChanged(final Object source, final STATUS status) {

				if (STATUS.LAYER_LOADED == status) {
					if (source instanceof ArcGISDynamicMapServiceLayer) {
						// ArcGISFeatureLayer loaded successfully
						System.out.println("Loaded Successfully " + source);

						// check whether the server offer the dynamic layer
						// service
						if (dynamicLayer.isDynamicLayersSupported() == true) {
							System.out
									.println("supports dynamic layer service");

						} else {
							System.out
									.println("doesnt support dynamic layer service");
						}
					}
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate the menu; this adds items to the action bar
		getMenuInflater().inflate(R.menu.action, menu);
		
		// get the query menu items
		mPop2007 = menu.getItem(0);
		mPop2000 = menu.getItem(1);
		mPop00_Sqmi = menu.getItem(2);
		mPop07_Sqmi = menu.getItem(3);
		mWhite = menu.getItem(4);
		mBlack = menu.getItem(5);
		mHispanic = menu.getItem(6);
		mAmeri_Es = menu.getItem(7);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {

		case R.id.pop2007:
			classificationField = "POP2007";
			// defining the class break renderer
			getClassBreaksRenderer(classificationField,
					ClassificationMethod.NaturalBreaks, 5,
					NormalizationType.None);
			mPop2007.setChecked(true);
			break;

		case R.id.pop2000:
			classificationField = "POP2000";
			// defining the class break renderer
			getClassBreaksRenderer(classificationField,
					ClassificationMethod.NaturalBreaks, 5,
					NormalizationType.None);
			mPop2000.setChecked(true);
			break;

		case R.id.pop07sqmi:
			classificationField = "POP07_SQMI";
			// defining the class break renderer
			getClassBreaksRenderer(classificationField,
					ClassificationMethod.NaturalBreaks, 5,
					NormalizationType.None);
			mPop07_Sqmi.setChecked(true);
			break;

		case R.id.pop00sqmi:
			classificationField = "POP00_SQMI";
			// defining the class break renderer
			getClassBreaksRenderer(classificationField,
					ClassificationMethod.NaturalBreaks, 5,
					NormalizationType.None);
			mPop00_Sqmi.setChecked(true);
			break;

		case R.id.white:
			classificationField = "WHITE";
			// defining the class break renderer
			getClassBreaksRenderer(classificationField,
					ClassificationMethod.NaturalBreaks, 5,
					NormalizationType.None);
			mWhite.setChecked(true);
			break;

		case R.id.black:
			classificationField = "BLACK";
			// defining the class break renderer
			getClassBreaksRenderer(classificationField,
					ClassificationMethod.NaturalBreaks, 5,
					NormalizationType.None);
			mBlack.setChecked(true);
			break;

		case R.id.hispanic:
			classificationField = "HISPANIC";
			// defining the class break renderer
			getClassBreaksRenderer(classificationField,
					ClassificationMethod.NaturalBreaks, 5,
					NormalizationType.None);
			mHispanic.setChecked(true);
			break;
			
		case R.id.ameries:
			classificationField = "AMERI_ES";
			// defining the class break renderer
			getClassBreaksRenderer(classificationField,
					ClassificationMethod.NaturalBreaks, 5,
					NormalizationType.None);
			mAmeri_Es.setChecked(true);
		}
		return true;
	}

	/**
	 * 
	 * @param classificationMethod
	 * @param numBreaks
	 * @param normalizationMethod
	 * @return classbreakrenderer
	 */
	private void getClassBreaksRenderer(String classField,
			ClassificationMethod classificationMethod, int numBreaks,
			NormalizationType normalizationMethod) {

		ClassBreaksDefinition classificationDef = null;

		classificationDef = ClassBreaksDefinition.createByClassificationMethod(
				classField, classificationMethod, numBreaks);
		// define start and end colors for ramp
		int[] colors = { Color.YELLOW, Color.RED };
		ColorRamp colorRamp = new AlgorithmicColorRamp(colors[0], colors[1],
				RampDefinition.Algorithm.HSV);
		classificationDef.setColorRamp(colorRamp);
		// execute the task
		RenderTask renderTask = new RenderTask();
		renderTask.execute(classificationDef);

	}

	/**
	 * Called when the activity is destroyed
	 */
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Called when the activity is Paused
	 */
	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}

	/**
	 * Called when the activity is Resumed
	 */
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
	}

	private class RenderTask extends
			AsyncTask<ClassBreaksDefinition, Void, ClassBreaksRenderer> {

		protected void onPreExecute() {
			dialog = ProgressDialog.show(DynamicLayerRendererActivity.this, "",
					"Generating Renderer");
		}

		@Override
		protected ClassBreaksRenderer doInBackground(
				ClassBreaksDefinition... params) {

			// generate a class breaks renderer using our class breaks
			// definition
			GenerateRendererTask generateRenderTask = new GenerateRendererTask(
					dynamicLayer.getUrl() + "/" + layerid);
			GenerateRendererTaskParameters taskParams = new GenerateRendererTaskParameters(
					params[0]);

			ClassBreaksRenderer resultRenderer = null;

			try {
				resultRenderer = (ClassBreaksRenderer) generateRenderTask
						.execute(taskParams);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return resultRenderer;

		}

		protected void onPostExecute(ClassBreaksRenderer renderer) {
			dialog.dismiss();

			// Get the layerInfo from the dynamic layer and set the drawing info
			List<DynamicLayerInfo> layerInfos = dynamicLayer.getDynamicLayerInfos();

			if (layerInfos != null) {
				// found dynamic layers
				Log.d("TAG", "dynamic layer infos found");
				for (DynamicLayerInfo dynamicLayerInfo : layerInfos) {
					if (dynamicLayerInfo.getId() == layerid) {
						// Create new drawing info from our renderer, with set
						// transparency
						Map<Integer, DrawingInfo> dInfo = dynamicLayer
								.getDrawingOptions();
						dInfo.put(Integer.valueOf(layerid), new DrawingInfo(
								renderer, TRANSPARENCY));
					}
				}
			}

			// Refresh the layer
			dynamicLayer.refresh();

		}

	}

}