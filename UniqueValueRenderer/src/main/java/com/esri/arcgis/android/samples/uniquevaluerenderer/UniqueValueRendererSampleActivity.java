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

package com.esri.arcgis.android.samples.uniquevaluerenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.esri.android.action.IdentifyResultSpinner;
import com.esri.android.action.IdentifyResultSpinnerAdapter;
import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.UniqueValue;
import com.esri.core.renderer.UniqueValueRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.tasks.identify.IdentifyParameters;
import com.esri.core.tasks.identify.IdentifyResult;
import com.esri.core.tasks.identify.IdentifyTask;

/**
 * This sample allows the user to identify data based on the Unique Value
 * Rendering which defines the sub-regions of the map and on a single tap and
 * view the properties of the place such as belonging the State and also the
 * Area of the region in a Callout window which has a spinner in its layout.
 * Also the user can select any of the results displayed and view its details.
 * The details are the attribute values.
 * 
 * The output value shown in the spinner is the display field.
 * 
 */
public class UniqueValueRendererSampleActivity extends Activity {

	MapView mMapView = null;
	GraphicsLayer graphicsLayer = null;
	Graphic fillGraphic = null;

	boolean boolQuery = true;
	ProgressDialog progress;
	IdentifyParameters params = null;
	Callout callout = null;

	// UniqueValueRenderer used to assign unique values to feature
	UniqueValueRenderer uvrenderer = null;

	// Unique Values are objects containing the unique properties
	UniqueValue uv1, uv2, uv3, uv4, uv5, uv6, uv7, uv8, uv9;

	// Rendering Type which is used to fill the region
	SimpleFillSymbol defaultsymbol = null;

	// The set of unique attributes for rendering
	String[] uniqueAttribute1 = new String[1];
	String[] uniqueAttribute2 = new String[1];
	String[] uniqueAttribute3 = new String[1];
	String[] uniqueAttribute4 = new String[1];
	String[] uniqueAttribute5 = new String[1];
	String[] uniqueAttribute6 = new String[1];
	String[] uniqueAttribute7 = new String[1];
	String[] uniqueAttribute8 = new String[1];
	String[] uniqueAttribute9 = new String[1];

	// create UI objects
	static ProgressDialog dialog;

	/** Called when the activity is first created. */
	@SuppressWarnings("serial")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mMapView = (MapView) findViewById(R.id.map);

		// add demographic layer to the map
		final ArcGISFeatureLayer feature = new ArcGISFeatureLayer(this
				.getResources().getString(R.string.states_URL),
				ArcGISFeatureLayer.MODE.SNAPSHOT);

		uvrenderer = new UniqueValueRenderer();

		// setting the field for the unique values
		uvrenderer.setField1("SUB_REGION");

		uv1 = new UniqueValue();
		uv2 = new UniqueValue();
		uv3 = new UniqueValue();
		uv4 = new UniqueValue();
		uv5 = new UniqueValue();
		uv6 = new UniqueValue();
		uv7 = new UniqueValue();
		uv8 = new UniqueValue();
		uv9 = new UniqueValue();

		Resources res = getResources();

		// Defining the Unique attributes for classifier
		uniqueAttribute1[0] = res.getString(R.string.Mtn);
		uv1.setDescription(res.getString(R.string.Mtn));
		uv1.setValue(uniqueAttribute1);

		uniqueAttribute2[0] = res.getString(R.string.Pacific);
		uv2.setDescription(res.getString(R.string.Pacific));
		uv2.setValue(uniqueAttribute2);

		uniqueAttribute3[0] = res.getString(R.string.N_Eng);
		uv3.setDescription(res.getString(R.string.N_Eng));
		uv3.setValue(uniqueAttribute3);

		uniqueAttribute4[0] = res.getString(R.string.S_Atl);
		uv4.setDescription(res.getString(R.string.S_Atl));
		uv4.setValue(uniqueAttribute4);

		uniqueAttribute5[0] = res.getString(R.string.Mid_Atl);
		uv5.setDescription(res.getString(R.string.Mid_Atl));
		uv5.setValue(uniqueAttribute5);

		uniqueAttribute6[0] = res.getString(R.string.E_N_Cen);
		uv6.setDescription(res.getString(R.string.E_N_Cen));
		uv6.setValue(uniqueAttribute6);

		uniqueAttribute7[0] = res.getString(R.string.W_N_Cen);
		uv7.setDescription(res.getString(R.string.W_N_Cen));
		uv7.setValue(uniqueAttribute7);

		uniqueAttribute8[0] = res.getString(R.string.E_S_Cen);
		uv8.setDescription(res.getString(R.string.E_S_Cen));
		uv8.setValue(uniqueAttribute8);

		uniqueAttribute9[0] = res.getString(R.string.W_S_Cen);
		uv9.setDescription(res.getString(R.string.W_S_Cen));
		uv9.setValue(uniqueAttribute9);

		// The symbol definition for each region
		final SimpleFillSymbol symbol1 = new SimpleFillSymbol(Color.BLUE);
		final SimpleFillSymbol symbol2 = new SimpleFillSymbol(Color.CYAN);
		final SimpleFillSymbol symbol3 = new SimpleFillSymbol(Color.GRAY);
		final SimpleFillSymbol symbol4 = new SimpleFillSymbol(Color.MAGENTA);
		final SimpleFillSymbol symbol5 = new SimpleFillSymbol(Color.GREEN);
		final SimpleFillSymbol symbol6 = new SimpleFillSymbol(Color.RED);
		final SimpleFillSymbol symbol7 = new SimpleFillSymbol(Color.YELLOW);
		final SimpleFillSymbol symbol8 = new SimpleFillSymbol(Color.WHITE);
		final SimpleFillSymbol symbol9 = new SimpleFillSymbol(Color.BLACK);

		// The default symbol
		defaultsymbol = new SimpleFillSymbol(Color.GREEN);

		// Setting the symbol to the unique values defined
		uv1.setSymbol(symbol1);
		uv2.setSymbol(symbol2);
		uv3.setSymbol(symbol3);
		uv4.setSymbol(symbol4);
		uv5.setSymbol(symbol5);
		uv6.setSymbol(symbol6);
		uv7.setSymbol(symbol7);
		uv8.setSymbol(symbol8);
		uv9.setSymbol(symbol9);

		// Add the unique values to the renderer
		uvrenderer.setDefaultSymbol(defaultsymbol);
		uvrenderer.addUniqueValue(uv1);
		uvrenderer.addUniqueValue(uv2);
		uvrenderer.addUniqueValue(uv3);
		uvrenderer.addUniqueValue(uv4);
		uvrenderer.addUniqueValue(uv5);
		uvrenderer.addUniqueValue(uv6);
		uvrenderer.addUniqueValue(uv7);
		uvrenderer.addUniqueValue(uv8);
		uvrenderer.addUniqueValue(uv9);

		// Add the renderer to the feature
		feature.setRenderer(uvrenderer);
		feature.setOpacity(0.5f);

		// Add feature to the mapview
		mMapView.addLayer(feature);

		// set Identify Parameters
		params = new IdentifyParameters();
		params.setTolerance(20);
		params.setDPI(98);
		params.setLayers(new int[] { 4 });
		params.setLayerMode(IdentifyParameters.ALL_LAYERS);
		
		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
			public void onStatusChanged(final Object source, final STATUS status) {

				if (STATUS.LAYER_LOADED == status) {
					if (source instanceof ArcGISFeatureLayer) {
						// ArcGISFeatureLayer loaded successfully
					}
				}
			}
		});

		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			public void onSingleTap(float x, float y) {

				if (!mMapView.isLoaded()) {
					return;
				}

				// Add to Identify Parameters based on tapped location
				Point identifyPoint = mMapView.toMapPoint(x, y);
				params.setGeometry(identifyPoint);
				params.setSpatialReference(mMapView.getSpatialReference());
				params.setMapHeight(mMapView.getHeight());
				params.setMapWidth(mMapView.getWidth());
				params.setReturnGeometry(false);

				// add the area of extent to identify parameters
				Envelope env = new Envelope();
				mMapView.getExtent().queryEnvelope(env);
				params.setMapExtent(env);

				// execute the identify task off UI thread
				MyIdentifyTask mTask = new MyIdentifyTask(identifyPoint);
				mTask.execute(params);

			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * The identity content
	 * 
	 * @param results
	 * @return
	 */
	private ViewGroup createIdentifyContent(final List<IdentifyResult> results) {

		// create a new LinearLayout in application context
		LinearLayout layout = new LinearLayout(this);

		// view height and widthwrap content
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		// default orientation
		layout.setOrientation(LinearLayout.HORIZONTAL);

		// Spinner to hold the results of an identify operation
		IdentifyResultSpinner spinner = new IdentifyResultSpinner(this, results);

		// make view clickable
		spinner.setClickable(false);
		spinner.canScrollHorizontally(BIND_ADJUST_WITH_ACTIVITY);

		// MyIdentifyAdapter creates a bridge between spinner and it's data
		MyIdentifyAdapter adapter = new MyIdentifyAdapter(this, results);
		spinner.setAdapter(adapter);
		spinner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		layout.addView(spinner);

		return layout;
	}

	/**
	 * This class allows the user to customize the string shown in the callout.
	 * By default its the display field name.
	 * 
	 * A spinner adapter defines two different views; one that shows the data in
	 * the spinner itself and one that shows the data in the drop down list when
	 * spinner is pressed.
	 * 
	 */
	public class MyIdentifyAdapter extends IdentifyResultSpinnerAdapter {
		String m_show = null;
		List<IdentifyResult> resultList;
		int currentDataViewed = -1;
		Context m_context;

		public MyIdentifyAdapter(Context context, List<IdentifyResult> results) {
			super(context, results);
			this.resultList = results;
			this.m_context = context;
		}

		// Get a TextView that displays identify results in the callout.
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String LSP = System.getProperty("line.separator");
			StringBuilder outputVal = new StringBuilder();

			// Resource Object to access the Resource fields
			Resources res = getResources();

			// Get Name attribute from identify results
			IdentifyResult curResult = this.resultList.get(position);

			if (curResult.getAttributes().containsKey(
					res.getString(R.string.NAME))) {
				outputVal.append("Place: "
						+ curResult.getAttributes()
								.get(res.getString(R.string.NAME)).toString());
				outputVal.append(LSP);
			}

			if (curResult.getAttributes().containsKey(
					res.getString(R.string.TOTPOP_CY))) {
				outputVal.append("Population: "
						+ curResult.getAttributes()
								.get(res.getString(R.string.TOTPOP_CY))
								.toString());
				outputVal.append(LSP);

			}

			if (curResult.getAttributes().containsKey(
					res.getString(R.string.LANDAREA))) {
				outputVal.append("Area: "
						+ curResult.getAttributes()
								.get(res.getString(R.string.LANDAREA))
								.toString());
				outputVal.append(LSP);

			}

			// Create a TextView to write identify results
			TextView txtView;
			txtView = new TextView(this.m_context);
			txtView.setText(outputVal);
			txtView.setTextColor(Color.BLACK);
			txtView.setLayoutParams(new ListView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			txtView.setGravity(Gravity.CENTER_VERTICAL);

			return txtView;
		}
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

	/**
	 * This class is mainly responsible to carry out the query execute tasks. It
	 * executes in the order of OnPreExecute, DoInBackground and OnPostExecute.
	 * 
	 */
	private class MyIdentifyTask extends
			AsyncTask<IdentifyParameters, Void, IdentifyResult[]> {

		IdentifyTask task = new IdentifyTask(
				UniqueValueRendererSampleActivity.this.getResources()
						.getString(R.string.result_URL));

		IdentifyResult[] M_Result;

		Point mAnchor;

		MyIdentifyTask(Point anchorPoint) {
			mAnchor = anchorPoint;
		}

		@Override
		protected void onPreExecute() {
			// create dialog while working off UI thread
			dialog = ProgressDialog
					.show(UniqueValueRendererSampleActivity.this,
							"Identifying the Region",
							"Please wait for the results ...");

		}

		protected IdentifyResult[] doInBackground(IdentifyParameters... params) {
			// check that you have the identify parameters
			if (params != null && params.length > 0) {
				IdentifyParameters mParams = params[0];

				try {
					// Run IdentifyTask with Identify Parameters
					M_Result = task.execute(mParams);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return M_Result;
		}

		@Override
		protected void onPostExecute(IdentifyResult[] results) {

			// dismiss dialog
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			ArrayList<IdentifyResult> resultList = new ArrayList<IdentifyResult>();

			IdentifyResult result_1;

			for (int index = 0; index < results.length; index++) {

				result_1 = results[index];
				String displayFieldName = result_1.getDisplayFieldName();
				Map<String, Object> attr = result_1.getAttributes();
				for (String key : attr.keySet()) {
					if (key.equalsIgnoreCase(displayFieldName)) {
						resultList.add(result_1);
					}
				}
			}
			// Create callout from MapView
			Callout mapCallout = mMapView.getCallout();
			mapCallout.setCoordinates(mAnchor);
			// populate callout with results from IdentifyTask
			mapCallout.setContent(createIdentifyContent(resultList));
			// show callout
			mapCallout.show();
			
		}
	}
}