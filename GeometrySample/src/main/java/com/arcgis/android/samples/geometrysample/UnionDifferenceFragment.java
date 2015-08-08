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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;

public class UnionDifferenceFragment extends Fragment {
	final static String ARG_POSITION = "position";

	int mCurrentPosition = -1;

	MapView mapView;

	ArcGISTiledMapServiceLayer tileLayer;

	GraphicsLayer firstGeomLayer = null;

	GraphicsLayer secondGeomLayer = null;

	GraphicsLayer resultGeomLayer = null;

	Button addsecGeometry;

	Button resetButton;

	RadioGroup operationTypeRadio = null;

	int geomNumWorkon = 1;

	Geometry firstGeometry = null;

	Geometry secondGeometry = null;

	boolean isStartPointSet1 = false;

	boolean isStartPointSet2 = false;

	int plusButtonCount = 0;

	Geometry geometryFromUnion;

	Geometry geometryFromDiff;

	SpatialReference spatialRef = SpatialReference.create(102100);

	RadioButton union;

	RadioButton difference;

	boolean countDoOperationUnion = true;

	boolean countDoOperationDifference = true;

	boolean isSketchingEnabled = true;

	enum OPERATION_TYPE {
		union, difference
	}

	enum GEOMETRY_TYPE {
		point, multi_points, polyline, polygon
	}

	OPERATION_TYPE operationType = OPERATION_TYPE.union;

	GEOMETRY_TYPE firstGeoType = GEOMETRY_TYPE.polygon;

	GEOMETRY_TYPE secondGeoType = GEOMETRY_TYPE.polygon;

	/**
	 * Create a new instance of UnionDifferenceFragment, initialized to show the
	 * text at 'index'.
	 */
	public static UnionDifferenceFragment newInstance(int index) {
		UnionDifferenceFragment f = new UnionDifferenceFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);
		f.setArguments(args);

		return f;
	}

	public int getShownIndex() {
		return getArguments().getInt("index", 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// If activity recreated (such as from screen rotate), restore
		// the previous article selection set by onSaveInstanceState().
		// This is primarily necessary when in the two-pane layout.
		if (savedInstanceState != null) {
			mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
		}

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.uniondifference, container, false);

		// return inflater.inflate(R.layout.article_view, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// During startup, check if there are arguments passed to the fragment.
		// onStart is a good place to do this because the layout has already
		// been
		// applied to the fragment at this point so we can safely call the
		// method
		// below that sets the article text.
		Bundle args = getArguments();
		if (args != null) {
			// Set article based on argument passed in
			updateArticleView(args.getInt(ARG_POSITION));
		} else if (mCurrentPosition != -1) {
			// Set article based on saved instance state defined during
			// onCreateView
			updateArticleView(mCurrentPosition);
		}
	}

	public void updateArticleView(int position) {

		mapView = (MapView) getActivity().findViewById(R.id.map);

		mapView.enableWrapAround(true);

		// Add Tile layer to the MapView
		String tileURL = getActivity().getResources().getString(
				R.string.tileServiceURL);
		ArcGISTiledMapServiceLayer tilelayer = new ArcGISTiledMapServiceLayer(
				tileURL);
		mapView.addLayer(tilelayer);

		firstGeomLayer = new GraphicsLayer();
		mapView.addLayer(firstGeomLayer);

		secondGeomLayer = new GraphicsLayer();
		mapView.addLayer(secondGeomLayer);

		resultGeomLayer = new GraphicsLayer();
		mapView.addLayer(resultGeomLayer);

		/**
		 * Single tap listener for MapView ***************
		 */
		mapView.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;

			public void onSingleTap(float x, float y) {
				if (isSketchingEnabled) {
					try {
						singleTapAct(x, y);
					} catch (Exception ex) {
						Log.d("TAG", ex.getMessage()); 
					}
				}
			}
		});

		// Set the message in the toast
		Toast.makeText(getActivity(),
				"Draw two intersecting polygons by tapping on the map",
				Toast.LENGTH_LONG).show();

		// set the behavior of all buttons
		setButtons();

		mCurrentPosition = position;
	}

	/**
	 * SET BUTTONS **************************
	 */
	void setButtons() {

		operationTypeRadio = (RadioGroup) getActivity().findViewById(
				R.id.operationtype);
		union = (RadioButton) getActivity().findViewById(R.id.union);
		difference = (RadioButton) getActivity().findViewById(R.id.difference);
		operationTypeRadio
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {

						// Set operation type
						switch (checkedId) {

						case R.id.union:
							operationType = OPERATION_TYPE.union;
							break;

						case R.id.difference:
							operationType = OPERATION_TYPE.difference;
							break;

						}

						doOperation();

					}
				});

		resetButton = (Button) getActivity().findViewById(R.id.reset);
		resetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				firstGeometry = null;
				secondGeometry = null;

				firstGeomLayer.removeAll();
				secondGeomLayer.removeAll();

				resultGeomLayer.removeAll();
				addsecGeometry.setEnabled(true);

				union.setChecked(true);
				difference.setChecked(false);
				operationType = OPERATION_TYPE.union;

				countDoOperationDifference = true;
				countDoOperationUnion = true;

				geomNumWorkon = 1;
				plusButtonCount = 0;
				isSketchingEnabled = true;

			}
		});

		addsecGeometry = (Button) getActivity().findViewById(R.id.secgeometry);
		addsecGeometry.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// first geometry is done start second geometry
				// and after the second button press display result geometry
				plusButtonCount++;

				if (plusButtonCount < 3) {

					geomNumWorkon = 2;
					if (plusButtonCount == 2) {
						doOperation();
					}
				}

			}
		});

	}

	void doOperation() {

		if (plusButtonCount == 2) {
			addsecGeometry.setEnabled(false);
			isSketchingEnabled = false;
			plusButtonCount++;
		}

		if (firstGeometry != null && secondGeometry != null) {

			if (operationType == OPERATION_TYPE.union) {
				try {
					// perform union operation
					Geometry[] geoms = new Geometry[2];
					geoms[0] = firstGeometry;
					geoms[1] = secondGeometry;

					if (countDoOperationUnion == true) {
						geometryFromUnion = GeometryEngine.union(geoms,
								spatialRef);
						countDoOperationUnion = false;
					}
					displayResultGeometry(geometryFromUnion);
				} catch (Exception ex) {
					Log.e("union", "" + ex);
				}
			} else if (operationType == OPERATION_TYPE.difference) {
				// Execute difference operation
				try {

					if (countDoOperationDifference == true) {
						geometryFromDiff = GeometryEngine.difference(
								firstGeometry, secondGeometry, spatialRef);
						countDoOperationDifference = false;
					}
					displayResultGeometry(geometryFromDiff);

				} catch (Exception ex) {
					Log.e("difference", "" + ex);
				}
			}
		}

	}

	void displayResultGeometry(Geometry geometry) throws Exception {
		if (geometry == null)
			return;
		Geometry[] geometries = new Geometry[1];
		geometries[0] = geometry;
		int red = Color.RED;
		resultGeomLayer.removeAll();
		GeometryUtil.highlightGeometriesWithColor(geometries, resultGeomLayer,
				red);

	}

	/**
	 * action to take on single taping.
	 */
	volatile int countTap = 0;

	// private String TAG = UnionDifferenceFragment.class.getSimpleName();

	void singleTapAct(float x, float y) throws Exception {
		countTap++;
		Point point = mapView.toMapPoint(x, y);
		Log.d("sigle tap on screen:", "[" + x + "," + y + "]");
		Log.d("sigle tap on map:", "[" + point.getX() + "," + point.getY()
				+ "]");
		if (geomNumWorkon == 1) {
			if (firstGeometry == null) {
				if (firstGeoType == GEOMETRY_TYPE.point) {
					firstGeometry = point;

				} else if (firstGeoType == GEOMETRY_TYPE.multi_points) {
					firstGeometry = new MultiPoint();
					((MultiPoint) firstGeometry).add(point);
				} else if (firstGeoType == GEOMETRY_TYPE.polygon) {
					firstGeometry = new Polygon();
					((MultiPath) firstGeometry).startPath(point);
					isStartPointSet1 = true;
					Log.d("geometry step " + countTap, GeometryEngine
							.geometryToJson(mapView.getSpatialReference(),
									firstGeometry));

				} else if (firstGeoType == GEOMETRY_TYPE.polyline) {
					isStartPointSet1 = true;
					firstGeometry = new Polyline();
					((MultiPath) firstGeometry).startPath(point);
				}

			}

		} else if (geomNumWorkon == 2) {
			if (secondGeometry == null) {
				if (secondGeoType == GEOMETRY_TYPE.point) {
					secondGeometry = point;

				} else if (secondGeoType == GEOMETRY_TYPE.multi_points) {
					secondGeometry = new MultiPoint();
					((MultiPoint) secondGeometry).add(point);
				} else if (secondGeoType == GEOMETRY_TYPE.polygon) {
					secondGeometry = new Polygon();
					((MultiPath) secondGeometry).startPath(point);
					isStartPointSet2 = true;

				} else if (secondGeoType == GEOMETRY_TYPE.polyline) {
					isStartPointSet2 = true;
					secondGeometry = new Polyline();
					((MultiPath) secondGeometry).startPath(point);
				}

			}

		}

		if (geomNumWorkon == 1) {
			if (firstGeoType == null)
				return;
			int blue = Color.BLUE;
			int color1 = Color.argb(100, Color.red(blue), Color.green(blue),
					Color.blue(blue));
			drawGeomOnGraphicLyr(firstGeometry, firstGeomLayer, point,
					firstGeoType, color1, isStartPointSet1);
			Log.d("geometry step " + countTap, GeometryEngine.geometryToJson(
					mapView.getSpatialReference(), firstGeometry));

		} else if (geomNumWorkon == 2) {
			if (secondGeoType == null)
				return;
			int green = Color.GREEN;
			int color2 = Color.argb(100, Color.red(green), Color.green(green),
					Color.blue(green));
			drawGeomOnGraphicLyr(secondGeometry, secondGeomLayer, point,
					secondGeoType, color2, isStartPointSet2);
		}

	}

	void drawGeomOnGraphicLyr(Geometry geometryToDraw, GraphicsLayer glayer,
			Point point, GEOMETRY_TYPE geoTypeToDraw, int color,
			boolean startPointSet) {

		if (geoTypeToDraw == GEOMETRY_TYPE.point) {
			geometryToDraw = point;

		} else if (geoTypeToDraw == GEOMETRY_TYPE.multi_points) {
			((MultiPoint) geometryToDraw).add(point);
		} else {

			if (startPointSet) {

				if (geoTypeToDraw == GEOMETRY_TYPE.polygon) {
					((Polygon) geometryToDraw).lineTo(point);
				} else if (geoTypeToDraw == GEOMETRY_TYPE.polyline) {
					((Polyline) geometryToDraw).lineTo(point);
				}

			}
		}

		Geometry[] geoms = new Geometry[1];
		geoms[0] = geometryToDraw;

		try {
			glayer.removeAll();
			GeometryUtil.highlightGeometriesWithColor(geoms, glayer, color);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the
		// fragment
		outState.putInt(ARG_POSITION, mCurrentPosition);
	}
}
