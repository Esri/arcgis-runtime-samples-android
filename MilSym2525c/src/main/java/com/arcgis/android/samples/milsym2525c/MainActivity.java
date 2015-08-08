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

package com.arcgis.android.samples.milsym2525c;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.arcgis.android.samples.milsym2525c.Mil2525cMessageParser.GeoMessage;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.advanced.Message;
import com.esri.core.symbol.advanced.MessageGroupLayer;
import com.esri.core.symbol.advanced.MessageHelper;
import com.esri.core.symbol.advanced.MessageProcessor;
import com.esri.core.symbol.advanced.SymbolDictionary.DictionaryType;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends Activity {

	MapView mMapView;
	MessageProcessor mProcessor;

	private MessageGroupLayer messageGrLayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mMapView = (MapView) findViewById(R.id.map);

		try {
			messageGrLayer = new MessageGroupLayer(DictionaryType.MIL2525C);
			mMapView.addLayer(messageGrLayer);
			mProcessor = messageGrLayer.getMessageProcessor();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSingleTap(float x, float y) {
				InputStream is = getResources().openRawResource(R.raw.symbols);
				// instantiate the parser
				Mil2525cMessageParser mil2525cParser = new Mil2525cMessageParser();
				List<GeoMessage> geoMessages = null;
				try {
					geoMessages = mil2525cParser.parse(is);

				} catch (XmlPullParserException ex){
                    Log.e("ERROR", ex.getLocalizedMessage());
                } catch (IOException e) {
					e.printStackTrace();
				} finally { // make sure InputStream is closed after the app is
							// finished
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				for (GeoMessage geoMessage : geoMessages) {
					// Message creation
					Message message = new Message();

					message.setProperty("_name", geoMessage.name);
					message.setProperty("_Type", geoMessage.type);
					message.setProperty("_Action", geoMessage.action);
					message.setID(geoMessage.id);
					message.setProperty("_Control_Points",
							geoMessage.controlpoints);
					Log.i("Test", "control point:" + geoMessage.controlpoints);
					message.setProperty("_WKID", geoMessage.wkid);
					message.setProperty("sic", geoMessage.sic);
					Log.i("Test", "sic: " + geoMessage.sic);
					message.setProperty("UniqueDesignation",
							geoMessage.uniquedesignation);

					mProcessor.processMessage(message);

					Log.i("Test", String.valueOf(messageGrLayer.count()));
				}

			}
		});

		mMapView.setOnLongPressListener(new OnLongPressListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean onLongPress(float x, float y) {
				// get to the layer where the selected graphic is
				Layer[] layers = messageGrLayer.getLayers();
				for (Layer layer : layers) {
					if (layer instanceof GraphicsLayer) {
                        GraphicsLayer gLayer = (GraphicsLayer) layer;
						int[] graphics = gLayer.getGraphicIDs(x, y, 50);
						if (graphics != null && graphics.length > 0) {
							Log.d("Test", "Graphic is found");
							// Create graphic
							Graphic graphic = gLayer.getGraphic(graphics[0]);
							Geometry geom = graphic.getGeometry();
							Point targetControlPnt = null;
							if (geom instanceof Point) {
								Point pnt = (Point) geom;
								Point screenPnt = mMapView.toScreenPoint(pnt);
								screenPnt = new Point(screenPnt.getX() + 50,
										screenPnt.getY() - 50);
								targetControlPnt = mMapView
										.toMapPoint(screenPnt);
								Log.d("Test", "x: " + targetControlPnt.getX()
										+ "; y: " + targetControlPnt.getY());
							}

							Message message = mProcessor
									.createMessageFrom(graphic);
							String controlPoints = (String) message
									.getProperty(MessageHelper.MESSAGE_2525C_CONTROL_POINTS_PROPERTY_NAME);
							Log.i("Test", "control point:" + controlPoints);
							if (targetControlPnt != null) {
								message.setProperty(
										MessageHelper.MESSAGE_2525C_CONTROL_POINTS_PROPERTY_NAME,
										targetControlPnt.getX() + ","
												+ targetControlPnt.getY());
							}
							message.setProperty("_Action", "update");
							mProcessor.processMessage(message);

						}
					}
				}

				return true;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		return id == R.id.action_settings || super.onOptionsItemSelected(item);
	}

}
