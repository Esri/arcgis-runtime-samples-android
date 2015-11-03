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

package com.esri.arcgis.android.samples.attributeeditor;

import com.esri.core.map.FeatureType;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FeatureLayerUtils {

	public enum FieldType {
		NUMBER, STRING, DECIMAL, DATE;

		public static FieldType determineFieldType(Field field) {

			if (field.getFieldType() == Field.esriFieldTypeString) {
				return FieldType.STRING;
			} else if (field.getFieldType() == Field.esriFieldTypeSmallInteger
					|| field.getFieldType() == Field.esriFieldTypeInteger) {
				return FieldType.NUMBER;
			} else if (field.getFieldType() == Field.esriFieldTypeSingle
					|| field.getFieldType() == Field.esriFieldTypeDouble) {
				return FieldType.DECIMAL;
			} else if (field.getFieldType() == Field.esriFieldTypeDate) {
				return FieldType.DATE;
			}
			return null;
		}
	}

	/**
	 * Helper method to determine if a field should be shown in the list for
	 * editing
	 */
	private static boolean isFieldValidForEditing(Field field) {

		int fieldType = field.getFieldType();

		return field.isEditable() && fieldType != Field.esriFieldTypeOID
				&& fieldType != Field.esriFieldTypeGeometry
				&& fieldType != Field.esriFieldTypeBlob
				&& fieldType != Field.esriFieldTypeRaster
				&& fieldType != Field.esriFieldTypeGUID
				&& fieldType != Field.esriFieldTypeXML;

	}

	/**
	 * Helper method to set attributes on a graphic. Only sets the attributes on
	 * the newGraphic variable if the value has changed. Returns true if the
	 * value has changed and has been set on the graphic.
	 * 
	 * @return boolean hasValueChanged
	 */
	public static boolean setAttribute(Map<String, Object> attrs,
			Graphic oldGraphic, Field field, String value, DateFormat formatter) {

		boolean hasValueChanged = false;

		// if its a string, and it has changed from the oldGraphic value
		if (FieldType.determineFieldType(field) == FieldType.STRING) {
			if (!value.equals(oldGraphic.getAttributeValue(field.getName()))) {
				// set the value as it is
				attrs.put(field.getName(), value);
				hasValueChanged = true;
			}
		} else if (FieldType.determineFieldType(field) == FieldType.NUMBER) {
			// if its an empty string, its a 0 number value (nulls not
			// supported), check this is a
			// change before making it a 0
			if (value.equals("")
					&& oldGraphic.getAttributeValue(field.getName()) != Integer
							.valueOf(0)) {

				// set a null value on the new graphic
				attrs.put(field.getName(), 0);
				hasValueChanged = true;
			} else {
				// parse as an int and check this is a change
				int intValue = Integer.parseInt(value);
				if (intValue != Integer.parseInt(oldGraphic.getAttributeValue(
						field.getName()).toString())) {
					attrs.put(field.getName(), intValue);
					hasValueChanged = true;
				}
			}
		} else if (FieldType.determineFieldType(field) == FieldType.DECIMAL) {
			// if its an empty string, its a 0 double value (nulls not
			// supported), check this is a
			// change before making it a 0
			if ((value.equals("") && oldGraphic.getAttributeValue(field
					.getName()) != Double.valueOf(0))) {

				// set a null value on the new graphic
				attrs.put(field.getName(), 0.0);
				hasValueChanged = true;

			} else {

				// parse as an double and check this is a change
				double dValue = Double.parseDouble(value);
				if (dValue != Double.parseDouble(oldGraphic.getAttributeValue(
						field.getName()).toString())) {
					attrs.put(field.getName(), dValue);
					hasValueChanged = true;
				}
			}
		} else if (FieldType.determineFieldType(field) == FieldType.DATE) {
			// if its a date, get the milliseconds value
			Calendar c = Calendar.getInstance();
			long dateInMillis;
			try {
				// parse to a double and check this is a change
				c.setTime(formatter.parse(value));
				dateInMillis = c.getTimeInMillis();

				if (dateInMillis != Long.parseLong(oldGraphic
						.getAttributeValue(field.getName()).toString())) {

					attrs.put(field.getName(), dateInMillis);
					hasValueChanged = true;
				}
			} catch (ParseException e) {
				// do nothing
			}
		}
		return hasValueChanged;
	}

	/**
	 * Helper method to find the Types actual value from its name
	 */
	public static String returnTypeIdFromTypeName(FeatureType[] types,
			String name) {

		for (FeatureType type : types) {
			if (type.getName().equals(name)) {
				return type.getId();
			}
		}
		return null;
	}

	/**
	 * Helper method to setup the editable field indexes and store them for
	 * later retrieval in getItem() method.
	 */
	public static int[] createArrayOfFieldIndexes(Field[] fields) {

		// process count of fields and which are available for editing
		ArrayList<Integer> list = new ArrayList<>();
		int fieldCount = 0;

		for (int i = 0; i < fields.length; i++) {

			if (isFieldValidForEditing(fields[i])) {
				list.add(i);
				fieldCount++;
			}
		}

		int[] editableFieldIndexes = new int[fieldCount];

		for (int x = 0; x < list.size(); x++) {
			editableFieldIndexes[x] = list.get(x);
		}
		return editableFieldIndexes;
	}

	/**
	 * Helper method to create a String array of Type values for populating a
	 * spinner
	 */
	public static String[] createTypeNameArray(FeatureType[] types) {
		String[] typeNames = new String[types.length];
		int i = 0;
		for (FeatureType type : types) {

			typeNames[i] = type.getName();
			i++;
		}
		return typeNames;
	}

	/**
	 * Helper method to create a HashMap of types using the Id (value) as the
	 * key
	 */
	public static HashMap<String, FeatureType> createTypeMapByValue(
			FeatureType[] types) {

		HashMap<String, FeatureType> typeMap = new HashMap<>();

		for (FeatureType type : types) {
			typeMap.put(type.getId(), type);
		}
		return typeMap;
	}
}
