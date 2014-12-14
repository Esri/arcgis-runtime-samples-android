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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;

public class ProjectFragment extends Fragment {
	final static String ARG_POSITION = "position";

	int mCurrentPosition = -1;

	MapView mapViewTop;

	MapView mapViewBottomRight;
	
	MapView mapViewBottomLeft;

	ArcGISDynamicMapServiceLayer dynamiclayertop = null;

	ArcGISDynamicMapServiceLayer dynamiclayerbottomleft = null;

	ArcGISDynamicMapServiceLayer dynamiclayerbottomright = null;

	boolean isStartPointSet1 = false;

	volatile int countTap = 0;

	GraphicsLayer firstGeomLayer = null;

	GraphicsLayer gLayerBottomLeft = null;

	GraphicsLayer gLayerBottomRight = null;

	GEOMETRY_TYPE firstGeoType = GEOMETRY_TYPE.polyline;

	Geometry firstGeometry = null;

	RadioGroup geomTypeRadioGroup = null;

	Button projectButton = null;

	Button resetButton = null;

	enum GEOMETRY_TYPE {
		point, polyline, polygon
	}

	boolean enableSketching = true;

	/**
	 * Create a new instance of ProjectFragment, initialized to show the text at
	 * 'index'.
	 */
	public static ProjectFragment newInstance(int index) {
		ProjectFragment f = new ProjectFragment();

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
		// return inflater.inflate(R.layout.buffer_view, container, false);

		return inflater.inflate(R.layout.project, container, false);
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

		mapViewTop = (MapView) getActivity().findViewById(R.id.maptop);
		mapViewBottomLeft = (MapView) getActivity().findViewById(
				R.id.mapbottomleft);
		mapViewBottomRight = (MapView) getActivity().findViewById(
				R.id.mapbottomright);

		float gridSize = 0.1f;
		float gridlineSize = 0.1f;

		mapViewTop.setMapBackground(R.color.AntiqueWhite, R.color.Azure,
				gridSize, gridlineSize);
		mapViewBottomLeft.setMapBackground(R.color.AntiqueWhite, R.color.Azure,
				gridSize, gridlineSize);
		mapViewBottomRight.setMapBackground(R.color.AntiqueWhite,
				R.color.Azure, gridSize, gridlineSize);

		Envelope e = new Envelope(-1.30654425036186E7, -9895780.16430666,
				1.30654425036186E7, 6699635.080552442);
		mapViewBottomLeft.setExtent(e);

		// Load a Dynamic map service with spatial reference 4326
		dynamiclayertop = new ArcGISDynamicMapServiceLayer(
				"http://mobilesampleserver.arcgisonline.com/ArcGIS/rest/services/UCDemo/World/MapServer");
		mapViewTop.addLayer(dynamiclayertop);

		// Load a Dynamic map service with spatial reference 54024
		dynamiclayerbottomleft = new ArcGISDynamicMapServiceLayer(
				"http://mobilesampleserver.arcgisonline.com/ArcGIS/rest/services/UCDemo/WorldAitoff/MapServer");
		mapViewBottomLeft.addLayer(dynamiclayerbottomleft);

		// Load a Dynamic map service with spatial reference 54021
		dynamiclayerbottomright = new ArcGISDynamicMapServiceLayer(
				"http://mobilesampleserver.arcgisonline.com/ArcGIS/rest/services/UCDemo/WorldPolyconic/MapServer");
		mapViewBottomRight.addLayer(dynamiclayerbottomright);

		firstGeomLayer = new GraphicsLayer();
		mapViewTop.addLayer(firstGeomLayer);

		gLayerBottomLeft = new GraphicsLayer();
		mapViewBottomLeft.addLayer(gLayerBottomLeft);

		gLayerBottomRight = new GraphicsLayer();
		mapViewBottomRight.addLayer(gLayerBottomRight);

		/**
		 * Single tap listener for MapView ***************
		 */
		mapViewTop.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;

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

		Toast.makeText(
				getActivity(),
				"Sketch a geometry on the upper map and tap the project button",
				Toast.LENGTH_LONG).show();

		// set the behavior of all buttons
		setButtons();

		mCurrentPosition = position;
	}

	/**
	 * SET BUTTONS **************************
	 */
	void setButtons() {

		geomTypeRadioGroup = (RadioGroup) getActivity().findViewById(
				R.id.geometrytype);

		geomTypeRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {

						switch (checkedId) {
						case R.id.line:
							firstGeoType = GEOMETRY_TYPE.polyline;
							break;
						case R.id.polygon:
							firstGeoType = GEOMETRY_TYPE.polygon;
							break;
						}

					}
				});

		resetButton = (Button) getActivity().findViewById(R.id.reset);
		resetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				firstGeometry = null;

				firstGeomLayer.removeAll();

				gLayerBottomLeft.removeAll();
				gLayerBottomRight.removeAll();
				enableSketching = true;
			}
		});

		projectButton = (Button) getActivity().findViewById(R.id.project);
		projectButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// Execute project action
				enableSketching = false;
				doOperation();

			}
		});

	}

	void doOperation() {

		// get projection for the 54024 spatial ref map

		Geometry geometryBottomLeft = GeometryEngine.project(firstGeometry,
				mapViewTop.getSpatialReference(),
				dynamiclayerbottomleft.getDefaultSpatialReference());

		if (geometryBottomLeft.getType().name().equalsIgnoreCase("polygon")) {
			SimpleFillSymbol sfs = new SimpleFillSymbol(Color.GREEN);
			sfs.setOutline(new SimpleLineSymbol(Color.RED, 4,
					com.esri.core.symbol.SimpleLineSymbol.STYLE.SOLID));
			sfs.setAlpha(40);
			Graphic g = new Graphic(geometryBottomLeft, sfs);
			gLayerBottomLeft.addGraphic(g);
		} else if (geometryBottomLeft.getType().name()
				.equalsIgnoreCase("polyline")) {

			SimpleLineSymbol sls = new SimpleLineSymbol(Color.RED, 4,
					com.esri.core.symbol.SimpleLineSymbol.STYLE.SOLID);
			Graphic g = new Graphic(geometryBottomLeft, sls);
			gLayerBottomLeft.addGraphic(g);
		}

		Geometry geometryBottomRight = GeometryEngine.project(firstGeometry,
				mapViewTop.getSpatialReference(),
				dynamiclayerbottomright.getDefaultSpatialReference());

		if (geometryBottomRight.getType().name().equalsIgnoreCase("polygon")) {
			SimpleFillSymbol sfs = new SimpleFillSymbol(Color.GREEN);
			sfs.setOutline(new SimpleLineSymbol(Color.RED, 4,
					com.esri.core.symbol.SimpleLineSymbol.STYLE.SOLID));
			sfs.setAlpha(40);
			Graphic g = new Graphic(geometryBottomRight, sfs);
			gLayerBottomRight.addGraphic(g);
		} else if (geometryBottomRight.getType().name()
				.equalsIgnoreCase("polyline")) {

			SimpleLineSymbol sls = new SimpleLineSymbol(Color.RED, 4,
					com.esri.core.symbol.SimpleLineSymbol.STYLE.SOLID);
			Graphic g = new Graphic(geometryBottomRight, sls);
			gLayerBottomRight.addGraphic(g);
		}

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
		Point point = mapViewTop.toMapPoint(x, y);
		Log.d("sigle tap on screen:", "[" + x + "," + y + "]");
		Log.d("sigle tap on map:", "[" + point.getX() + "," + point.getY()
				+ "]");

		if (firstGeometry == null) {
			if (firstGeoType == GEOMETRY_TYPE.point) {
				firstGeometry = point;

			} else if (firstGeoType == GEOMETRY_TYPE.polygon) {
				firstGeometry = new Polygon();
				((MultiPath) firstGeometry).startPath(point);
				isStartPointSet1 = true;
				Log.d("geometry step " + countTap, GeometryEngine
						.geometryToJson(mapViewTop.getSpatialReference(),
								firstGeometry));

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
				mapViewTop.getSpatialReference(), firstGeometry));

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