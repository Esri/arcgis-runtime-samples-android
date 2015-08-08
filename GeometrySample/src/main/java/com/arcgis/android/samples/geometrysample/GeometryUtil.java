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

package com.arcgis.android.samples.geometrysample;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;

public class GeometryUtil {

	/**
	 * highlight graphic objects in random colors
	 */
	public static void highlightGeometriesWithColor(Geometry[] results,
			GraphicsLayer graphicsLayer, int color) throws Exception {
		highlightGeometriesWithColor(results, graphicsLayer, color, 16);
	}

	public static void highlightGeometriesWithColor(Geometry[] results,
			GraphicsLayer graphicsLayer, int color, int size) throws Exception {

		if (results == null || results.length == 0)
			return;

		for (int i = 0; i < results.length; i++) {
			Geometry geom = results[i];
			String typeName = geom.getType().name();

			/*
			 * Create appropriate symbol, based on geometry type
			 */
			if (typeName.equalsIgnoreCase("point")) {

				addPointToGraphicsLayer((Point) geom, graphicsLayer, color,
						size, STYLE.CIRCLE);

			} else if (typeName.equalsIgnoreCase("polyline")) {
				addPolylineToGraphicsLayer((Polyline) geom, color, size / 4,
						graphicsLayer);

			} else if (typeName.equalsIgnoreCase("multipoint")) {
				addMultiPointToGraphicsLayer((MultiPoint) geom, STYLE.CIRCLE,
						size, color, graphicsLayer);
			}

			else if (typeName.equalsIgnoreCase("polygon")) {

				SimpleFillSymbol sfs = new SimpleFillSymbol(color);
				sfs.getOutline().setWidth(size / 4);
				// sfs.setAlpha(50);
				graphicsLayer.addGraphic(new Graphic(geom, sfs));
			}

			else if (typeName.equalsIgnoreCase("envelope")) {

				SimpleFillSymbol sfs = new SimpleFillSymbol(color);
				sfs.getOutline().setWidth(size / 4);
				graphicsLayer.addGraphic(new Graphic(geom, sfs));
			}
		}

	}// end of method

	public static void addPolylineToGraphicsLayer(Polyline pl, int color,
			int size, GraphicsLayer graphicsLayer) throws Exception {

		SimpleLineSymbol sls = new SimpleLineSymbol(color, size,
				com.esri.core.symbol.SimpleLineSymbol.STYLE.SOLID);
		Graphic g = new Graphic(pl, sls);
		graphicsLayer.addGraphic(g);
	}

	public static void addPolygonToGraphicsLayer(Polygon pg, int color,
			int size, int alpha, GraphicsLayer graphicsLayer) throws Exception {

		SimpleFillSymbol sfs = new SimpleFillSymbol(color);
		sfs.setAlpha(alpha);
		Graphic g = new Graphic(pg, sfs);
		graphicsLayer.addGraphic(g);
	}

	/**
	 * add points to graphic layer
	 */
	public static void addPointToGraphicsLayer(Point point,
			GraphicsLayer graphicsLayer, int color, int size, STYLE style)
			throws Exception {

		SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, size, style);
		Graphic graphic = new Graphic(point, sms);
		graphicsLayer.addGraphic(graphic);

	}// end of method

	/**
	 * add multipoint to a graphic layer
	 */
	public static void addMultiPointToGraphicsLayer(MultiPoint mPoint,
			STYLE style, int size, int color, GraphicsLayer graphicsLayer)
			throws Exception {
		int len = mPoint.getPointCount();
		Graphic[] graphics = new Graphic[len];

		for (int i = 0; i < len; i++) {
			Point geom = mPoint.getPoint(i);

			SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, size, style);
			graphics[i] = new Graphic(geom, sms);
			graphicsLayer.addGraphic(graphics[i]);

		}

	}// end of method

}
