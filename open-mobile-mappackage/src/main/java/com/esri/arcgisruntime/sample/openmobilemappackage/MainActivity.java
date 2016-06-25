/* Copyright 2016 Esri
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

package com.esri.arcgisruntime.sample.openmobilemappackage;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.mobilemappackage.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private static File extStorDir;
    private static String extSDCardDirName;
    private static String filename;

    private static final String TAG = "MMPK";
    private static final String FILE_EXTENSION = ".mmpk";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get sdcard resource name
        extStorDir = Environment.getExternalStorageDirectory();
        extSDCardDirName = this.getResources().getString(R.string.config_data_sdcard_offline_dir);
        filename = this.getResources().getString(R.string.config_mmpk_name);

        String mmpkFile = createMobileMapPackageFilePath();

        // retrieve the MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        final MobileMapPackage mapPackage = new MobileMapPackage(mmpkFile);
        mapPackage.loadAsync();

        mapPackage.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if(mapPackage.getLoadStatus() == LoadStatus.LOADED && mapPackage.getMaps().size() > 0){
                    mMapView.setMap(mapPackage.getMaps().get(0));
                }else{
                    // Log the issue
                    Log.e(TAG, mapPackage.getLoadError().getMessage());
                }
            }
        });
    }

    /**
     * Create the mobile map package file location and name structure
     */
    private static String createMobileMapPackageFilePath(){
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName + File.separator + filename + FILE_EXTENSION;
    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }
}
