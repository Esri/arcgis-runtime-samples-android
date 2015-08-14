package com.arcgis.android.samples.dynamicsublayerreorder;

import android.app.Activity;
import android.os.Bundle;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.map.DynamicLayerInfo;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
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
