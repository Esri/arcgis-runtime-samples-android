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

package com.arcgis.android.samples.ExportTileCacheTask;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.tilecache.ExportTileCacheParameters;
import com.esri.core.tasks.tilecache.ExportTileCacheParameters.ExportBy;
import com.esri.core.tasks.tilecache.ExportTileCacheStatus;
import com.esri.core.tasks.tilecache.ExportTileCacheTask;

public class ExportTileCacheTaskSampleActivity extends Activity {

	static final String TAG = "ExportTileCacheTaskSampleActivity";

	// Map elements
	MapView mMapView;
	ArcGISLocalTiledLayer localTiledLayer;

	// action bar menu items
	MenuItem selectLevels;
	MenuItem download;
	MenuItem switchMaps;

	boolean isLocalLayerVisible = false;

	// The generated tile cache will be a compact cache
	boolean createAsTilePackage = false;

	double[] levels;

	final CharSequence[] items = { "Level ID:0", "Level ID:1", "Level ID:2",
			"Level ID:3", "Level ID:4", "Level ID:5", "Level ID:6",
			"Level ID:7", "Level ID:8", "Level ID:9", };

	double[] mapResolution = { 156543.03392800014, 78271.51696399994,
			39135.75848200009, 19567.87924099992, 9783.93962049996,
			4891.96981024998, 2445.98490512499, 1222.992452562495,
			611.4962262813797, 305.74811314055756 };

	boolean[] itemsChecked = new boolean[items.length];
	ArrayList<Double> levelsArraylist = new ArrayList<Double>();
	// path to persist data to disk
	static String DEFAULT_BASEMAP_PATH;
	// tile package url
	String tileURL;

	private static String defaultPath = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);

		DEFAULT_BASEMAP_PATH = getResources().getString(R.string.offline_dir);
		defaultPath = Environment.getExternalStorageDirectory().getPath()
				+ DEFAULT_BASEMAP_PATH;

		// Initialize MapView and extent
		mMapView = (MapView) findViewById(R.id.mapView);

		// Add Tile layer to the MapView
		tileURL = getResources().getString(R.string.tileServiceURL);
		ArcGISTiledMapServiceLayer tilelayer = new ArcGISTiledMapServiceLayer(
				tileURL);
		mMapView.addLayer(tilelayer);

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		// menu items
		selectLevels = menu.getItem(0);
		selectLevels.setIcon(android.R.drawable.ic_menu_crop);
		download = menu.getItem(1);
		switchMaps = menu.getItem(2);

		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.select_levels:
			showDialog();
			return true;
		case R.id.download:
			downloadBasemap();
			return true;
		case R.id.switch_maps:
			if (isLocalLayerVisible) {
				switchToOnlineLayer();
			} else {
				switchToLocalLayer();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Launches the dialog to let the user select the Levels of Detail to be
	 * downloaded from the tile service. Called when selectLevels action bar
	 * icon is selected.
	 * 
	 **/
	public void showDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select the Levels of Detail");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// When ok button is pressed, we go through the array of
				// itemsChecked and add the selected
				// items to levelsArraylist
				for (int i = 0; i < items.length; i++) {
					if (itemsChecked[i]) {

						levelsArraylist.add((double) i);
						itemsChecked[i] = false;
					}
				}
			}
		});

		builder.setMultiChoiceItems(items, new boolean[] { false, false, false,
						false, false, false, false, false, false, false },
				new DialogInterface.OnMultiChoiceClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						itemsChecked[which] = isChecked;
					}
				});
		builder.show();
	}

	/**
	 * Download the basemap for the level of details selected by the user Called
	 * when download button is clicked.
	 * 
	 */
	@SuppressWarnings("boxing")
	public void downloadBasemap() {

		// Set the progressbar to VISIBLE
		setProgressBarIndeterminateVisibility(true);

		// Get the the extent covered by generated tile cache, here we are using
		// the area being displayed on screen
		Envelope extentForTPK = new Envelope();
		mMapView.getExtent().queryEnvelope(extentForTPK);

		// If the user does not select the Level of details
		// then give out the status message in a toast
		if (levelsArraylist.size() == 0) {
			Toast.makeText(this, "Please Select Levels of Detail",
					Toast.LENGTH_LONG).show();
			// Hide the progress bar
			setProgressBarIndeterminateVisibility(false);
			return;
		}

		levels = new double[levelsArraylist.size()];
		final String tileCachePath = defaultPath;

		// Specify all the Levels of details in an integer array
		for (int i = 0; i < levelsArraylist.size(); i++) {
			levels[i] = levelsArraylist.get(i);
		}

		// Create an instance of ExportTileCacheTask for the mapService that
		// supports the exportTiles() operation
		final ExportTileCacheTask exportTileCacheTask = new ExportTileCacheTask(
				tileURL, null);

		// Set up GenerateTileCacheParameters
		ExportTileCacheParameters params = new ExportTileCacheParameters(
				createAsTilePackage, levels, ExportBy.ID, extentForTPK,
				mMapView.getSpatialReference());

		// create tile cache
		createTileCache(params, exportTileCacheTask, tileCachePath);
	}

	/**
	 * Creates tile Cache locally by calling generateTileCache
	 * 
	 * @param params
	 * @param exportTileCacheTask
	 * @param tileCachePath
	 */
	private void createTileCache(ExportTileCacheParameters params,
			final ExportTileCacheTask exportTileCacheTask,
			final String tileCachePath) {

		// estimate tile cache size
		exportTileCacheTask.estimateTileCacheSize(params,
				new CallbackListener<Long>() {

					@Override
					public void onError(Throwable e) {
						Log.d("*** tilecachesize error: ", "" + e);
					}

					@Override
					public void onCallback(Long objs) {
						Log.d("*** tilecachesize: ", "" + objs);
						final long tilecachesize = objs / 1000;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(
										getApplicationContext(),
										"Approx. Tile Cache size to download : "
												+ tilecachesize + " KB",
										Toast.LENGTH_LONG).show();
							}
						});

					}
				});

		// create status listener for generateTileCache
		CallbackListener<ExportTileCacheStatus> statusListener = new CallbackListener<ExportTileCacheStatus>() {

			@Override
			public void onError(Throwable e) {
				Log.d("*** tileCacheStatus error: ", "" + e);
			}

			@Override
			public void onCallback(ExportTileCacheStatus objs) {
				Log.d("*** tileCacheStatus : ", objs.getStatus().toString());
				final String status = objs.getStatus().toString();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), status,
								Toast.LENGTH_SHORT).show();
					}
				});

			}
		};

		// Submit tile cache job and download
		exportTileCacheTask.generateTileCache(params, statusListener,
				new CallbackListener<String>() {
					private boolean errored = false;

					@Override
					public void onError(Throwable e) {
						errored = true;
						// print out the error message and disable the progress
						// bar
						Log.d("*** generateTileCache error: ", "" + e);
						final String error = e.toString();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setProgressBarIndeterminateVisibility(false);
								Toast.makeText(getApplicationContext(),
										"generateTileCache error: " + error,
										Toast.LENGTH_LONG).show();
							}
						});
					}

					@Override
					public void onCallback(String path) {
						if (!errored) {
							Log.d("the Download Path = ", "" + path);

							// switch to the successfully downloaded local layer
							localTiledLayer = new ArcGISLocalTiledLayer(path);
							mMapView.addLayer(localTiledLayer);
							// initially setting the visibility to false,
							// turning it back on in the switchToLocalLayer()
							// method
							mMapView.getLayers()[1].setVisible(false);

							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// Hide the progress bar
									setProgressBarIndeterminateVisibility(false);
									Toast.makeText(
											getApplicationContext(),
											"TileCache successfully downloaded, Switching to Local Tiled Layer",
											Toast.LENGTH_LONG).show();

									switchToLocalLayer();
								}
							});
						}
					}
				}, tileCachePath);

	}

	private void switchToLocalLayer() {
		// Set the resolution of the map to the first level selected by user
		mMapView.setResolution(mapResolution[(int) levels[0]]);
		// set local layer true
		isLocalLayerVisible = true;
		// just hide the map service layer rather than removing it
		mMapView.getLayers()[0].setVisible(false);
		mMapView.getLayers()[1].setVisible(true);

	}

	private void switchToOnlineLayer() {
		// set local layer false
		isLocalLayerVisible = false;
		// hide the local layer, make the online layer visible
		mMapView.getLayers()[1].setVisible(false);
		mMapView.getLayers()[0].setVisible(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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

}