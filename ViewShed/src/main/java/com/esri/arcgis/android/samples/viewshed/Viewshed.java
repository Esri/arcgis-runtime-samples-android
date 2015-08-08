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

package com.esri.arcgis.android.samples.viewshed;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.tasks.ags.geoprocessing.GPFeatureRecordSetLayer;
import com.esri.core.tasks.ags.geoprocessing.GPLinearUnit;
import com.esri.core.tasks.ags.geoprocessing.GPParameter;
import com.esri.core.tasks.ags.geoprocessing.GPResultResource;
import com.esri.core.tasks.ags.geoprocessing.Geoprocessor;

/**
 * This sample application illustrates the usage of Geoprocessing. It allows the
 * user to select a point on the map and see all the areas that are visible
 * within the chosen distance.
 * 
 */

public class Viewshed extends Activity {

	protected static final int CLOSE_LOADING_WINDOW = 0;
	protected static final int CANCEL_LOADING_WINDOW = 1;
	MapView map = null;
	private ArrayList<GPParameter> params;
	Geoprocessor gp;
	GraphicsLayer gLayer;
	ProgressDialog dialog = null;
	Timer cancelViewShed = new Timer();
	Point mappoint;
	int gId=0;
	
	Handler uiHandler = new Handler(new Callback() {
		@Override
		public boolean handleMessage(final Message msg) {
			switch (msg.what) {
			case CLOSE_LOADING_WINDOW:
				if (dialog != null) {
					dialog.dismiss();
				}
				cancelViewShed.cancel();
				break;
			case CANCEL_LOADING_WINDOW:
				if (dialog != null) {
					dialog.dismiss();
				}
				Toast toast = Toast.makeText(Viewshed.this,
						"ViewShed too long, canceled", Toast.LENGTH_SHORT);
				toast.show();
				break;
			}
			return false;
		}

	});

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            Bundle
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		map = (MapView) findViewById(R.id.map);
		
		// Create a graphics layer for Viewshed drawing
		gLayer = new GraphicsLayer();
		map.addLayer(gLayer);
		// Map Buttons

		Toast.makeText(this, "Single tap on the map", Toast.LENGTH_SHORT)
				.show();


		map.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSingleTap(final float x, final float y) {
				if(mappoint!=null){
					gLayer.removeGraphic(gId);
				}
				mappoint = map.toMapPoint(x, y);
				Graphic g = new Graphic(mappoint, new SimpleMarkerSymbol(
						Color.RED, 10, STYLE.CIRCLE));
				gId=gLayer.addGraphic(g);
			}
		});


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_layout, menu);
		return super.onCreateOptionsMenu(menu);

	}
	/**
	 * Clear all graphics from the graphics layer. The method is called when
	 * the trash can button is clicked by the user.
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.go:
			if(gId==0){
				Toast.makeText(this, "Single Tap on the Map", Toast.LENGTH_SHORT).show();
				return false;
			}
			start(mappoint);
			return true;
		case R.id.delete:
			gLayer.removeAll();
			gId=0;
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	class ViewShedQuery extends
			AsyncTask<ArrayList<GPParameter>, Void, GPParameter[]> {

		GPParameter[] outParams = null;

		/**
		 * Method onPostExecute.
		 * 
		 * @param result
		 *            GPParameter[]
		 */
		@Override
		protected void onPostExecute(GPParameter[] result) {
			if (result == null)
				return;
			for (int i = 0; i < result.length; i++) {
				if (result[i] instanceof GPFeatureRecordSetLayer) {

					GPFeatureRecordSetLayer fsl = (GPFeatureRecordSetLayer) result[i];
					for (Graphic feature : fsl.getGraphics()) {
						Graphic g = new Graphic(feature.getGeometry(),
								new SimpleFillSymbol(Color.CYAN));
						gLayer.addGraphic(g);
					}
				}
			}
			uiHandler.sendEmptyMessage(CLOSE_LOADING_WINDOW);
		}

		/**
		 * Method doInBackground.
		 * 
		 * @param params1
		 *            ArrayList<GPParameter>[]
		 * @return GPParameter[]
		 */
		@Override
		protected GPParameter[] doInBackground(
				ArrayList<GPParameter>... params1) {

			gp = new Geoprocessor(
					"http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/Elevation/ESRI_Elevation_World/GPServer/Viewshed");
			/*
			 * API v1.1 requires SpatialReference parameter
			 */
			gp.setOutSR(map.getSpatialReference());
			try {
				GPResultResource rr = gp.execute(params1[0]);
				outParams = rr.getOutputParameters();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return outParams;
		}
	}

	/**
	 * 1) Create the GP object by passing it the url. 2) Create the input
	 * parameters and add to the GPParameter object. 3) Pass these params to the
	 * execute method.
	 * 
	 * @param mPoint
	 *            - point selected on the map by the user
	 */
	@SuppressWarnings("unchecked")
	public void start(Point mPoint) {
		// First input parameter
		GPFeatureRecordSetLayer gpf = new GPFeatureRecordSetLayer(
				"Input_Observation_Point");
		gpf.setSpatialReference(map.getSpatialReference());
		gpf.setGeometryType(Geometry.Type.POINT);
		// Add the point selected by the user
		Graphic f = new Graphic(mPoint, new SimpleMarkerSymbol(Color.RED, 25,
				STYLE.DIAMOND));
		gpf.addGraphic(f);

		// Second input parameter
		GPLinearUnit gpl = new GPLinearUnit("Viewshed_Distance");
		gpl.setUnits("esriMeters");
		gpl.setDistance(8046.72);

		// Add params
		params = new ArrayList<GPParameter>();
		params.add(gpf);
		params.add(gpl);

		try {
			dialog = ProgressDialog.show(Viewshed.this, "",
					"Calculating Viewshed ...", true, true);
			new ViewShedQuery().execute(params);
			cancelViewShed = new Timer();
			cancelViewShed.schedule(new TimerTask() {

				@Override
				public void run() {
					uiHandler.sendEmptyMessage(CANCEL_LOADING_WINDOW);
				}
			}, 60000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		map.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		map.unpause();
	}

}
