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

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Mil2525cMessageParser {

	// no namespace
	private static final String ns = null;

	public List<GeoMessage> parse(InputStream in)
			throws XmlPullParserException, IOException {
		try {
			// instantiate a parser, use an InputStream as input
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			// start the parsing process
			parser.nextTag();
			// invoke readFeed() method to extract and process data
			return readGeoMessages(parser);
		} finally {
			in.close();
		}
	}

	/**
	 * 
	 * @param parser
	 * @return List containing the entries extracted from feed
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private List<GeoMessage> readGeoMessages(XmlPullParser parser)
			throws XmlPullParserException, IOException {

		List<GeoMessage> geoMessages = new ArrayList<GeoMessage>();

		parser.require(XmlPullParser.START_TAG, ns, "geomessages");
		while (parser.next() != XmlPullParser.END_TAG) {
			String pName = parser.getName();
			// Start looking for the geomessage tag
			if (pName != null && pName.equals("geomessage")) {
				geoMessages.add(readGeoMessage(parser));
			} else {
				Log.d("DEBUG", "TAG NAME = " + pName);
				continue;
			}

		}
		return geoMessages;
	}

	// Parses the contents of an geomessage.
	private GeoMessage readGeoMessage(XmlPullParser parser)
			throws XmlPullParserException, IOException {

		parser.require(XmlPullParser.START_TAG, ns, "geomessage");

		String name = null;
		String type = null;
		String action = null;
		String id = null;
		String controlpoints = null;
		String wkid = null;
		String sic = null;
		String uniquedesignation = null;

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String pName = parser.getName();
			if (pName.equals("_name")) {
				name = readName(parser);
			} else if (pName.equals("_type")) {
				type = readType(parser);
			} else if (pName.equals("_action")) {
				action = readAction(parser);
			} else if (pName.equals("_id")) {
				id = readID(parser);
			} else if (pName.equals("_control_points")) {
				controlpoints = readControlPoints(parser);
			} else if (pName.equals("_wkid")) {
				wkid = readWkid(parser);
			} else if (pName.equals("sic")) {
				sic = readSic(parser);
			} else if (pName.equals("uniquedesignation")) {
				uniquedesignation = readUniqueDesignation(parser);
			}

		}
		return new GeoMessage(name, type, action, id, controlpoints, wkid, sic,
				uniquedesignation);
	}

	private String readUniqueDesignation(XmlPullParser parser)
			throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "uniquedesignation");
		String uniquedesignation = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "uniquedesignation");

		return uniquedesignation;
	}

	private String readSic(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "sic");
		String sic = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "sic");

		return sic;
	}

	private String readWkid(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "_wkid");
		String wkid = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "_wkid");

		return wkid;
	}

	private String readControlPoints(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "_control_points");
		String controlpoints = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "_control_points");

		return controlpoints;
	}

	private String readID(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "_id");
		String id = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "_id");

		return id;
	}

	private String readAction(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "_action");
		String action = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "_action");

		return action;
	}

	private String readType(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "_type");
		String type = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "_type");

		return type;
	}

	private String readName(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "_name");
		String name = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "_name");

		return name;
	}

	// extract text values.
	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	public class GeoMessage {

		public final String name;
		public final String type;
		public final String action;
		public final String id;
		public final String controlpoints;
		public final String wkid;
		public final String sic;
		public final String uniquedesignation;

		private GeoMessage(String name, String type, String action, String id,
				String controlpoints, String wkid, String sic,
				String uniquedesignation) {
			this.name = name;
			this.type = type;
			this.action = action;
			this.id = id;
			this.controlpoints = controlpoints;
			this.wkid = wkid;
			this.sic = sic;
			this.uniquedesignation = uniquedesignation;
		}
	}

}
