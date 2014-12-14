/* Copyright 2013 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the use restrictions 
 * http://help.arcgis.com/en/sdk/10.0/usageRestrictions.htm.
 */

package com.arcgis.android.samples.geometrysample;

import java.text.DecimalFormat;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.AreaUnit;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;

public class MeasureFragment extends Fragment {
	final static String ARG_POSITION = "position";

	int mCurrentPosition = -1;

	MapView mapView;

	ArcGISTiledMapServiceLayer tileLayer = null;

	public static final LinearUnit LINEARUNIT_METER = (LinearUnit) Unit
			.create(LinearUnit.Code.METER);

	public static final AreaUnit AREAUNIT_SQUARE_METER = (AreaUnit) Unit
			.create(AreaUnit.Code.SQUARE_METER);

	GraphicsLayer firstGeomLayer = null;

	boolean enableSketching = true;

	Geometry firstGeometry = null;

	TextView resultText = null;

	GEOMETRY_TYPE firstGeoType = GEOMETRY_TYPE.polyline;

	RadioGroup geomTypeRadioGroup = null;

	Button resetButton = null;

	boolean isStartPointSet1 = false;

	volatile int countTap = 0;

	double measure = 0;

	double value = 0;

	DecimalFormat twoDForm;

	int resId = 0;

	int current_distance_unit = LinearUnit.Code.METER;

	int current_area_unit = AreaUnit.Code.SQUARE_METER;

	enum GEOMETRY_TYPE {
		point, polyline, polygon
	}

	protected int[] distance_units = new int[] { LinearUnit.Code.METER,
			LinearUnit.Code.MILE_US, LinearUnit.Code.YARD,
			LinearUnit.Code.FOOT, LinearUnit.Code.KILOMETER };

	protected int[] area_units = new int[] { AreaUnit.Code.SQUARE_METER,
			AreaUnit.Code.ACRE, AreaUnit.Code.SQUARE_MILE_US,
			AreaUnit.Code.SQUARE_YARD, AreaUnit.Code.SQUARE_KILOMETER };

	/**
	 * Create a new instance of MeasureFragment, initialized to show the text at
	 * 'index'.
	 */
	public static MeasureFragment newInstance(int index) {
		MeasureFragment f = new MeasureFragment();

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
		return inflater.inflate(R.layout.measure, container, false);

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

	@SuppressWarnings("serial")
	public void updateArticleView(int position) {

		mapView = (MapView) getActivity().findViewById(R.id.map);

		// Add Tile layer to the MapView
		String tileURL = getActivity().getResources().getString(
				R.string.tileServiceURL);
		ArcGISTiledMapServiceLayer tilelayer = new ArcGISTiledMapServiceLayer(
				tileURL);
		mapView.addLayer(tilelayer);

		// Set the extent of the map
		Envelope env = new Envelope(-8139237.214629, 5016257.541842,
				-8090341.387563, 5077377.325675);
		mapView.setExtent(env);

		// Add the graphics layer to the map
		firstGeomLayer = new GraphicsLayer();
		mapView.addLayer(firstGeomLayer);

		twoDForm = new DecimalFormat("#.##");

		/**
		 * Single tap listener for MapView ***************
		 */
		mapView.setOnSingleTapListener(new OnSingleTapListener() {
			@Override
			public void onSingleTap(float x, float y) {
				if (enableSketching) {
					try {
						singleTapAct(x, y);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		Toast.makeText(getActivity(),
				"Sketch on the map to measure distance or area",
				Toast.LENGTH_LONG).show();

		// set the behavior of all buttons

		setButtons();

		changeSpinnerUnits();

		mCurrentPosition = position;
	}

	/**
	 * SET BUTTONS **************************
	 */
	void setButtons() {

		resultText = (TextView) getActivity().findViewById(R.id.result);

		geomTypeRadioGroup = (RadioGroup) getActivity().findViewById(
				R.id.geometrytype);

		geomTypeRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {

						switch (checkedId) {
						case R.id.line:
							firstGeoType = GEOMETRY_TYPE.polyline;
							changeSpinnerUnits();
							doReset();
							break;
						case R.id.polygon:
							firstGeoType = GEOMETRY_TYPE.polygon;
							changeSpinnerUnits();
							doReset();
							break;
						}

					}
				});

		resetButton = (Button) getActivity().findViewById(R.id.reset);
		resetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doReset();
			}
		});

	}

	@SuppressWarnings("boxing")
	void doReset() {

		firstGeometry = null;

		firstGeomLayer.removeAll();

		enableSketching = true;

		measure = 0;

		value = 0;

		resultText.setText(Double.toString(value));

	}

	/**
	 * Changes the unit types for spinner values on selecting distance vs area
	 * 
	 * @param measuretype
	 */
	void changeSpinnerUnits() {

		if (firstGeoType == GEOMETRY_TYPE.polyline) {
			resId = R.array.DistanceUnits;
		} else {
			resId = R.array.AreaUnits;
		}

		// Create a spinner with the drop down values specified in
		// values->queryparameters.xml
		Spinner measureUnits = (Spinner) getActivity().findViewById(
				R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), resId, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		measureUnits.setAdapter(adapter);

		measureUnits.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {

				switch (pos) {

				// distance - Miles, area - Acres
				case 0:
					if (firstGeoType == GEOMETRY_TYPE.polyline) {
						current_distance_unit = distance_units[0];
						doConvert(current_distance_unit, GEOMETRY_TYPE.polyline);
					} else {
						current_area_unit = area_units[0];
						doConvert(current_area_unit, GEOMETRY_TYPE.polygon);
					}

					break;

				// distance - Yards, area - Square Miles
				case 1:
					if (firstGeoType == GEOMETRY_TYPE.polyline) {
						current_distance_unit = distance_units[1];
						doConvert(current_distance_unit, GEOMETRY_TYPE.polyline);
					} else {
						current_area_unit = area_units[1];
						doConvert(current_area_unit, GEOMETRY_TYPE.polygon);
					}

					break;

				// distance - Feet, area - Square Yards
				case 2:
					if (firstGeoType == GEOMETRY_TYPE.polyline) {
						current_distance_unit = distance_units[2];
						doConvert(current_distance_unit, GEOMETRY_TYPE.polyline);
					} else {
						current_area_unit = area_units[2];
						doConvert(current_area_unit, GEOMETRY_TYPE.polygon);
					}

					break;

				// distance - Kilometers, area - Square Kilometers
				case 3:
					if (firstGeoType == GEOMETRY_TYPE.polyline) {
						current_distance_unit = distance_units[3];
						doConvert(current_distance_unit, GEOMETRY_TYPE.polyline);
					} else {
						current_area_unit = area_units[3];
						doConvert(current_area_unit, GEOMETRY_TYPE.polygon);
					}

					break;

				// distance - Meters, area - Square Meters
				case 4:
					if (firstGeoType == GEOMETRY_TYPE.polyline) {
						current_distance_unit = distance_units[4];
						doConvert(current_distance_unit, GEOMETRY_TYPE.polyline);
					} else {
						current_area_unit = area_units[4];
						doConvert(current_area_unit, GEOMETRY_TYPE.polygon);
					}

					break;

				default:
					break;
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Do nothing

			}
		});

	}

	@SuppressWarnings("boxing")
	void doConvert(int toUnit, GEOMETRY_TYPE GeoType) {

		// All the measurement is done in meters and sq meters. No need to
		// convert units
		if (toUnit == LinearUnit.Code.METER
				|| toUnit == AreaUnit.Code.SQUARE_METER) {
			// only two digits after the decimal
			value = Double.valueOf(twoDForm.format(measure));
			resultText.setText(Double.toString(value));
			return;
		}

		// Calculate the value of measure in other units
		if (GeoType == GEOMETRY_TYPE.polyline) {
			value = Unit.convertUnits(measure, LINEARUNIT_METER,
					Unit.create(toUnit));
			value = Double.valueOf(twoDForm.format(value));
		} else if (GeoType == GEOMETRY_TYPE.polygon) {
			value = Unit.convertUnits(measure, AREAUNIT_SQUARE_METER,
					Unit.create(toUnit));
			value = Double.valueOf(twoDForm.format(value));
		}

		// Display result in textview
		resultText.setText(Double.toString(value));
	}

	/**
	 * action to take on single taping.
	 * 
	 * @param x
	 * @param y
	 * @throws Exception
	 */

	void singleTapAct(float x, float y) throws Exception {
		countTap++;
		Point point = mapView.toMapPoint(x, y);
		Log.d("single tap on screen:", "[" + x + "," + y + "]");
		Log.d("single tap on map:", "[" + point.getX() + "," + point.getY()
				+ "]");

		if (firstGeometry == null) {
			if (firstGeoType == GEOMETRY_TYPE.point) {
				firstGeometry = point;

			} else if (firstGeoType == GEOMETRY_TYPE.polygon) {
				firstGeometry = new Polygon();
				((MultiPath) firstGeometry).startPath(point);
				isStartPointSet1 = true;
				Log.d("geometry step " + countTap,
						GeometryEngine.geometryToJson(
								mapView.getSpatialReference(), firstGeometry));

			} else if (firstGeoType == GEOMETRY_TYPE.polyline) {
				isStartPointSet1 = true;
				firstGeometry = new Polyline();
				((MultiPath) firstGeometry).startPath(point);
			}

		}

		if (firstGeoType == null)
			return;
		int color1 = Color.BLUE;
		drawGeomOnGraphicLyr(firstGeometry, firstGeomLayer, point,
				firstGeoType, color1, isStartPointSet1);
		Log.d("geometry step " + countTap, GeometryEngine.geometryToJson(
				mapView.getSpatialReference(), firstGeometry));

	}

	void drawGeomOnGraphicLyr(Geometry geometryToDraw, GraphicsLayer glayer,
			Point point, GEOMETRY_TYPE geoTypeToDraw, int color,
			boolean startPointSet) {

		if (geoTypeToDraw == GEOMETRY_TYPE.point) {
			geometryToDraw = point;

		} else {

			if (startPointSet) {

				if (geoTypeToDraw == GEOMETRY_TYPE.polygon) {
					((Polygon) geometryToDraw).lineTo(point);
					// Simplify the geometry and project to spatial ref with
					// WKID for World Cylindrical Equal Area 54034
					Geometry geometry = GeometryEngine.simplify(geometryToDraw,
							mapView.getSpatialReference());
					Geometry g2 = GeometryEngine.project(geometry,
							mapView.getSpatialReference(),
							SpatialReference.create(54034));
					// Get the area for the polygon
					measure = Math.abs(g2.calculateArea2D());
					if (measure != 0.0)
						doConvert(current_area_unit, firstGeoType);

				} else if (geoTypeToDraw == GEOMETRY_TYPE.polyline) {
					((Polyline) geometryToDraw).lineTo(point);
					// Get the geodesic length for the polyline
					measure = GeometryEngine.geodesicLength(geometryToDraw,
							mapView.getSpatialReference(), LINEARUNIT_METER);
					if (measure != 0.0)
						doConvert(current_distance_unit, firstGeoType);
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
