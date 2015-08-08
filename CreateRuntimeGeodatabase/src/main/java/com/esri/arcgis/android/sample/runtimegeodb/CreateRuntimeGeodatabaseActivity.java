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

package com.esri.arcgis.android.sample.runtimegeodb;

import java.io.File;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.ags.FeatureServiceInfo;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusCallback;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusInfo;
import com.esri.core.tasks.geodatabase.GeodatabaseSyncTask;

public class CreateRuntimeGeodatabaseActivity extends Activity {

	static MapView mMapView;
	String fLayerUrl;
	String fServiceUrl;
	static ArcGISFeatureLayer wildfireFL;
	static GeodatabaseSyncTask gdbSyncTask;

	static ProgressDialog mProgressDialog;
	static TextView pathView;

	private static File demoDataFile;
	private static String offlineDataSDCardDirName;
	private static String filename;
	static String localGdbFilePath;
	
	protected static final String TAG = "CRGdb";
	protected static String OFFLINE_FILE_EXTENSION = ".geodatabase";
	
	private static Context mContext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// set app context so it can be obtained to update progress
		CreateRuntimeGeodatabaseActivity.setContext(this);

		// get sdcard resource names
		demoDataFile = Environment.getExternalStorageDirectory();
		offlineDataSDCardDirName = this.getResources().getString(
				R.string.config_data_sdcard_offline_dir);
		filename = this.getResources().getString(
				R.string.config_geodatabase_name);

		// Retrieve the map and map options from XML layout
		mMapView = (MapView) findViewById(R.id.map);
		// create service layer
		fServiceUrl = this.getResources()
				.getString(R.string.featureservice_url);
		
		mProgressDialog = new ProgressDialog(CreateRuntimeGeodatabaseActivity.this);
		mProgressDialog.setTitle("Create local runtime geodatabase");
		
		// attribute app and pan across dateline
		addAttributes();
	}

	private void addAttributes() {
		// attribute ESRI logo to map
		mMapView.setEsriLogoVisible(true);
		// enable map to wrap around date line
		mMapView.enableWrapAround(true);

	}
	
	// methods to ensure context is available when updating the progress dialog
	public static Context getContext(){
		return mContext;
	}
	
	public static void setContext(Context context){
		mContext = context;
	}

	/*
	 * Create the geodatabase file location and name structure
	 */
	static String createGeodatabaseFilePath() {
		return demoDataFile.getAbsolutePath() + File.separator + offlineDataSDCardDirName + File.separator + filename + OFFLINE_FILE_EXTENSION;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate action bar menu
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle menu item selection
		switch (item.getItemId()) {
		case R.id.action_download:
			downloadData(fServiceUrl);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	/**
	 * Create the GeodatabaseTask from the feature service URL w/o credentials.
	 */
	private void downloadData(String url) {
		Log.i(TAG, "Create GeoDatabase");
		// create a dialog to update user on progress
		mProgressDialog.show();
		// create the GeodatabaseTask

		gdbSyncTask = new GeodatabaseSyncTask(url, null);
		gdbSyncTask
				.fetchFeatureServiceInfo(new CallbackListener<FeatureServiceInfo>() {

					@Override
					public void onError(Throwable arg0) {
						Log.e(TAG, "Error fetching FeatureServiceInfo");
					}

					@Override
					public void onCallback(FeatureServiceInfo fsInfo) {
						if (fsInfo.isSyncEnabled()) {
							createGeodatabase(fsInfo);
						}
					}
				});

	}

	/**
	 * Set up parameters to pass the the {@link #submitTask()} method. A
	 * {@link CallbackListener} is used for the response.
	 */
	private static void createGeodatabase(FeatureServiceInfo featureServerInfo) {
		// set up the parameters to generate a geodatabase
		GenerateGeodatabaseParameters params = new GenerateGeodatabaseParameters(
				featureServerInfo, mMapView.getExtent(),
				mMapView.getSpatialReference());

		// a callback which fires when the task has completed or failed.
		CallbackListener<String> gdbResponseCallback = new CallbackListener<String>() {
			@Override
			public void onError(final Throwable e) {
				Log.e(TAG, "Error creating geodatabase");
				mProgressDialog.dismiss();
			}

			@Override
			public void onCallback(String path) {
				Log.i(TAG, "Geodatabase is: " + path);
				mProgressDialog.dismiss();
				// update map with local feature layer from geodatabase
				updateFeatureLayer(path);
				// log the path to the data on device
				Log.i(TAG, "path to geodatabase: " + path);
			}
		};

		// a callback which updates when the status of the task changes
		GeodatabaseStatusCallback statusCallback = new GeodatabaseStatusCallback() {
			@Override
			public void statusUpdated(final GeodatabaseStatusInfo status) {
				// get current status
				String progress = status.getStatus().toString();
				// get activity context
				Context context = CreateRuntimeGeodatabaseActivity.getContext();
				// create activity from context
				CreateRuntimeGeodatabaseActivity activity = (CreateRuntimeGeodatabaseActivity) context;
				// update progress bar on main thread
				showProgressBar(activity, progress);

			}
		};

		// create the fully qualified path for geodatabase file
		localGdbFilePath = createGeodatabaseFilePath();

		// get geodatabase based on params
		submitTask(params, localGdbFilePath, statusCallback,
				gdbResponseCallback);
	}

	/**
	 * Request database, poll server to get status, and download the file
	 */
	private static void submitTask(GenerateGeodatabaseParameters params,
			String file, GeodatabaseStatusCallback statusCallback,
			CallbackListener<String> gdbResponseCallback) {
		// submit task
		gdbSyncTask.generateGeodatabase(params, file, false, statusCallback,
				gdbResponseCallback);
	}

	/**
	 * Add feature layer from local geodatabase to map
	 * 
	 * @param featureLayerPath
	 */
	private static void updateFeatureLayer(String featureLayerPath) {
		// create a new geodatabase
		Geodatabase localGdb = null;
		try {
			localGdb = new Geodatabase(featureLayerPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Geodatabase contains GdbFeatureTables representing attribute data
		// and/or spatial data. If GdbFeatureTable has geometry add it to
		// the MapView as a Feature Layer
		if (localGdb != null) {
			for (GeodatabaseFeatureTable gdbFeatureTable : localGdb
					.getGeodatabaseTables()) {
				if (gdbFeatureTable.hasGeometry()){
					mMapView.addLayer(new FeatureLayer(gdbFeatureTable));

				}
			}
		}
	}
	
	private static void showProgressBar(final CreateRuntimeGeodatabaseActivity activity, final String message){
		activity.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				mProgressDialog.setMessage(message);
			}
			
		});
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