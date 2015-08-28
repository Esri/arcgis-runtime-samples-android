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

package com.arcgis.android.samples.dynamicsublayerreorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.map.DynamicLayerInfo;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    MapView mMapView;
    ArcGISTiledMapServiceLayer mBaseMap;
    DynamicListView mLayerList;
    public ArrayList<String> mSubLayersName;
    public static  ArcGISDynamicMapServiceLayer mDynamicLayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creating elements of app with findViewById
        mMapView = (MapView)findViewById(R.id.map);
        mLayerList = (DynamicListView)findViewById(R.id.listView);
        mSubLayersName = new ArrayList<>();

        //Creating ArcGISTiledMapServiceLayer and ArcGISDynamicMapServiceLayer
        mBaseMap = new ArcGISTiledMapServiceLayer(getString(R.string.basemap_url));
        mDynamicLayer = new ArcGISDynamicMapServiceLayer(getString(R.string.dynamiclayer_url));

        //Add DynamicLayer's sublayers to ArrayList
        mSubLayersName.add("Cities");
        mSubLayersName.add("Highways");
        mSubLayersName.add("States");
        mSubLayersName.add("Counties");

        //DynamicListView's adapter
        StableArrayAdapter mAdapter = new StableArrayAdapter(MainActivity.this,R.layout.listview_text,mSubLayersName);

        mLayerList.setAdapter(mAdapter);

        //Set ListItems for DynamicListView
        mLayerList.setListItems(mSubLayersName);


        //Adding layers to MapView
        mMapView.addLayer(mBaseMap);
        mMapView.addLayer(mDynamicLayer);

        //Extent to MapView
        Envelope mapExtent = new Envelope(-14029650.509177,3560436.632155,-12627306.217347,5430229.021262);
        mMapView.setExtent(mapExtent);

    }

    /*
    * Drag drop start with onLongClickListener
    * */
    public void dragDrop(int indexone, int indextwo) {

        //Getting DynamicLayerInfos from DynamicMapServiceLayer
        List<DynamicLayerInfo> mTemporaryDynamicInfo = mDynamicLayer.getDynamicLayerInfos();

        // Getting one temporary item from DynamicLayerInfo array.
        // This temporary item used for drap drop
        DynamicLayerInfo mTemporaryItem= mTemporaryDynamicInfo.get(indexone);
        mTemporaryDynamicInfo.remove(mTemporaryItem);
        mTemporaryDynamicInfo.add(indextwo, mTemporaryItem);
        mDynamicLayer.setDynamicLayerInfos(mTemporaryDynamicInfo);
        mDynamicLayer.refresh();

    }


}
