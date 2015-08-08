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

package com.esri.arcgis.samples.offlineanalysis;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.android.map.event.OnZoomListener;
import com.esri.core.analysis.LineOfSight;
import com.esri.core.analysis.Viewshed;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.raster.FunctionRasterSource;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * The Offline Analysis sample shows how to do Line of Sight and Viewshed
 * analysis on raster DEM files on device. The sample shows how to extend the
 * MapOnTouchListener to customize the apps response to tapping on the map.
 *
 */
public class MainActivity extends Activity {

	static final String TAG = "OfflineAnalysis";

	// offline data
	private static File demoDataFile;
	private static String offlineDataSDCardDirName;
	private static String filename;

	// Map objects
	MapView mMapView;
	FileRasterSource mRasterSource;

	private RasterLayer mRasterLayer;
	private String mRaster;
	private GraphicsLayer mGraphicsLayer;
	private LineOfSight mLineOfSight;
	private Layer mLosLayer;
	private FunctionRasterSource functionRS;
	private RasterLayer viewShedLayer;
	private Viewshed mViewshed;

	// ActionBar menu items
	private MenuItem mLOS;
	private MenuItem mVSmenu;

	private double mObserverZOffset = Double.NaN;
	private double mTargetZOffset = Double.NaN;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// create the path to local raster file
		demoDataFile = Environment.getExternalStorageDirectory();
		offlineDataSDCardDirName = this.getResources().getString(
				R.string.raster_dir);
		filename = this.getResources().getString(R.string.raster_file);

		// create the raster path
		mRaster = demoDataFile + File.separator + offlineDataSDCardDirName
				+ File.separator + filename;

		// create the mapview
		mMapView = (MapView) findViewById(R.id.map);

		try {
			// create the raster source
			mRasterSource = new FileRasterSource(mRaster);
			// create the raster layer
			mRasterLayer = new RasterLayer(mRasterSource);
			// add the layer
			mMapView.addLayer(mRasterLayer);
            // zoom to raster source extent
            mMapView.setExtent(mRasterSource.getExtent());
		} catch (FileNotFoundException | RuntimeException e) {
			Log.e(TAG, e.getMessage());
		}

        // add graphics layer
		mGraphicsLayer = new GraphicsLayer();
		mMapView.addLayer(mGraphicsLayer);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// get the analysis menu items
		mLOS = menu.getItem(0);
		mVSmenu = menu.getItem(1);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// Handle menu item selection
		switch (id) {
		case R.id.menu_analysis_los:
			mLOS.setChecked(true);

			Toast toast = Toast.makeText(getApplicationContext(),
					"Line of Sight selected", Toast.LENGTH_LONG);
			toast.show();
			performLOS();
			return true;
		case R.id.menu_analysis_viewshed:
			mVSmenu.setChecked(true);

			Toast toast2 = Toast.makeText(getApplicationContext(),
					"Viewshed selected", Toast.LENGTH_LONG);
			toast2.show();
			calculateViewshed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Call MapView.pause to suspend map rendering while the activity is
		// paused, which can save battery usage.
		if (mMapView != null) {
			mMapView.pause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Call MapView.unpause to resume map rendering when the activity
		// returns to the foreground.
		if (mMapView != null) {
			mMapView.unpause();
		}
	}

	/*
	 * Remove any analysis layers on map
	 */
	private void clearFunctionLayers() {
		turnOffLayer(mLosLayer);
		turnOffLayer(viewShedLayer);
		// clear any graphics
		if (mGraphicsLayer != null) {
		  mGraphicsLayer.removeAll();
		}
	}

	/*
	 * Remove layer and recycle
	 * Dispose of analysis functions
	 */
	private void turnOffLayer(Layer layer) {
		if (layer != null && !layer.isRecycled()) {
			mMapView.removeLayer(layer);
			layer.recycle();
			
			
			if(mViewshed != null){
				mViewshed.dispose();
				mViewshed = null;
			}
			
			if(mLineOfSight != null){
				mLineOfSight.dispose();
				mLineOfSight = null;
			}
			
		}

	}

	/*
	 * Line of Sight Analysis
	 */
	private void performLOS() {
		// clear any analysis layers showing
		clearFunctionLayers();

		try {
			mLineOfSight = new LineOfSight(mRaster);

  	} catch (FileNotFoundException | RuntimeException e) {
  	  e.printStackTrace();
  	}

        if (mLineOfSight != null) {
			mLosLayer = mLineOfSight.getOutputLayer();
			mMapView.addLayer(mLosLayer);
			// set observer features
			mLineOfSight.setObserver(mMapView.getCenter());
			mLineOfSight.setObserverZOffset(mObserverZOffset);
			mLineOfSight.setTargetZOffset(mTargetZOffset);

			// Set gesture used to change the position of the observer and
			// target.
			// When the position of a target is changed, the task will be
			// executed and
			// the result will be rendered on the map view.
			mMapView.setOnTouchListener(new OnTouchListenerLOS(mMapView
					.getContext(), mMapView, mLineOfSight));
			// Reset the observer to center of map on map zoom
			mMapView.setOnZoomListener(new OnZoomListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void preAction(float pivotX, float pivotY, double factor) {
				}

				@Override
				public void postAction(float pivotX, float pivotY, double factor) {
					// set the observer to the center of the map
					mLineOfSight.setObserver(mMapView.getCenter());
				}
			});
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Raster File Not Found", Toast.LENGTH_LONG);
			toast.show();
		}

	}

	/*
	 * Viewshed Analysis
	 */
	private void calculateViewshed() {
		// clear any analysis layers showing
		clearFunctionLayers();
		// create a viewshed
		try {
			mViewshed = new Viewshed(mRaster);

    } catch (FileNotFoundException | RuntimeException e) {
      e.printStackTrace();
    }

        if (mViewshed != null) {
			functionRS = mViewshed.getOutputFunctionRasterSource();
			viewShedLayer = new RasterLayer(functionRS);
			mMapView.addLayer(viewShedLayer);

			mViewshed.setObserverZOffset(mObserverZOffset);
			// Set gesture used to change the position of the observer
			mMapView.setOnTouchListener(new OnTouchListenerViewshed(mMapView
					.getContext(), mMapView, mViewshed));
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Raster File Not Found", Toast.LENGTH_LONG);
			toast.show();
		}

	}

	/*
	 * Override com.esri.android.map.MapOnTouchListener to customize gesture
	 * used to change the position of the observer and target.
	 */
	private class OnTouchListenerLOS extends MapOnTouchListener {

		MapView mMap;
		LineOfSight mTask;

		public OnTouchListenerLOS(Context context, MapView map, LineOfSight task) {
			super(context, map);
			mMap = map;
			mTask = task;
		}

		@Override
		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
			try {
				Point p = mMap.toMapPoint(to.getX(), to.getY());
				mTask.setTarget(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		public boolean onSingleTap(MotionEvent tap) {
			try {
				Point p = mMap.toMapPoint(tap.getX(), tap.getY());
				mTask.setTarget(p);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return true;
		}

		/*
		 * Override method to change the observers position in calculating Line
		 * of Sight. 
		 * 
		 * @see
		 * com.esri.android.map.MapOnTouchListener#onLongPress(android.view.
		 * MotionEvent)
		 */
		@Override
		public void onLongPress(MotionEvent tap) {
			Point p = mMap.toMapPoint(tap.getX(), tap.getY());
			mTask.setObserver(p);

		}
	}

	/*
	 * Override com.esri.android.map.MapOnTouchListener to customize gesture
	 * used to change the position of the observer.
	 */
	private class OnTouchListenerViewshed extends MapOnTouchListener {

		private MapView mMap;
		private Viewshed mTask;

		public OnTouchListenerViewshed(Context context, MapView map,
				Viewshed task) {
			super(context, map);
			mMap = map;
			mTask = task;
		}

		@Override
		public boolean onSingleTap(MotionEvent tap) {
			Point mapPoint = mMap.toMapPoint(tap.getX(), tap.getY());
			// clear any graphics
	    mGraphicsLayer.removeAll();
			// create a graphic to represent observer position
			Graphic graphic = new Graphic(mapPoint, new SimpleMarkerSymbol(
					Color.YELLOW, 20, STYLE.CROSS));
			// add graphic to map
			mGraphicsLayer.addGraphic(graphic);
			// set observer on viewshed
			mTask.setObserver(mapPoint);

			return true;
		}

	}

}