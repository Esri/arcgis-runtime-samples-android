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

package com.esri.android.samples.mgrsgrid;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.CoordinateConversion;
import com.esri.core.geometry.CoordinateConversion.MGRSConversionMode;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;

public class LocateMGRSActivity extends Activity {

	MapView mMapView;
	GraphicsLayer gl = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Retrieve the map and initial extent from XML layout
		mMapView = (MapView) findViewById(R.id.map);
		// create a Tile Layer from String resource URL
		ArcGISTiledMapServiceLayer baseMap = new ArcGISTiledMapServiceLayer(
				this.getResources().getString(R.string.basemap_url));
		// add layer to map view
		mMapView.addLayer(baseMap);
		// create graphics layer to show results
		gl = new GraphicsLayer();
		// add graphic layer to map view
		mMapView.addLayer(gl);
		// enable map to wrap around date line
		mMapView.enableWrapAround(true);
		// attribute Esri logo on map
		mMapView.setEsriLogoVisible(true);

		// on single tap convert the map coordinates to grid reference
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			public void onSingleTap(float screenX, float screenY) {
				// remove any previous graphics
				gl.removeAll();
				// get map point
				Point mapPoint = mMapView.toMapPoint(screenX, screenY);
				// convert the coordinates to military grid strings

				String mgrsPoint = CoordinateConversion.pointToMgrs(mapPoint,
						mMapView.getSpatialReference(),
						MGRSConversionMode.NEW_STYLE, 6, true, true);

				// String[] mgrsPoint = mMapView.getSpatialReference()
				// .toMilitaryGrid(MGRSConversionMode.NEW_STYLE, 6,
				// true, true, new Point[] { mapPoint });
				// create a symbol for point
				SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.YELLOW,
						15, SimpleMarkerSymbol.STYLE.X);
				// create a text symbol for point coords
				TextSymbol txtSym = new TextSymbol(20, mgrsPoint, Color.RED);
				txtSym.setOffsetX(10);
				txtSym.setOffsetX(10);
				// add point and symbol to Graphic
				Graphic gMarker = new Graphic(mapPoint, sms);
				// add text symbol to graphic
				Graphic gText = new Graphic(mapPoint, txtSym);
				// add graphics to graphics alyer
				gl.addGraphics(new Graphic[] { gMarker, gText });
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