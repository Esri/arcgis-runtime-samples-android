package com.esri.arcgis.android.samples.measure;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.MapView;
import com.esri.android.toolkit.analysis.MeasuringTool;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Unit;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;


public class MainActivity extends ActionBarActivity {

    MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // after the content of this activity is set
        // the map can be accessed from the layout
        mMapView = (MapView)findViewById(R.id.map);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SimpleFillSymbol fillSymbol;

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_measure) {

            Unit[] linearUnits = new Unit[] {
                    Unit.create(LinearUnit.Code.CENTIMETER),
                    Unit.create(LinearUnit.Code.METER),
                    Unit.create(LinearUnit.Code.KILOMETER),
                    Unit.create(LinearUnit.Code.INCH),
                    Unit.create(LinearUnit.Code.FOOT),
                    Unit.create(LinearUnit.Code.YARD),
                    Unit.create(LinearUnit.Code.MILE_STATUTE)
            };

            SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(Color.BLUE, 10, SimpleMarkerSymbol.STYLE.DIAMOND);
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.YELLOW, 3);
            fillSymbol = new SimpleFillSymbol(Color.argb(100, 0, 225, 255));
            fillSymbol.setOutline(new SimpleLineSymbol(Color.TRANSPARENT, 0));

            // create the tool, required.
            MeasuringTool measuringTool = new MeasuringTool(mMapView);

            // customize the tool, optional.
            measuringTool.setLinearUnits(linearUnits);
            measuringTool.setMarkerSymbol(markerSymbol);
            measuringTool.setLineSymbol(lineSymbol);
            measuringTool.setFillSymbol(fillSymbol);

            // fire up the tool, required.
            startActionMode(measuringTool);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
