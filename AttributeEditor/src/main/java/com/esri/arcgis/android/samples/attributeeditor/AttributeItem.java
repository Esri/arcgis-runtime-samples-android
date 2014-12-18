/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the sample code usage restrictions document for further information.
 *
 */

package com.esri.arcgis.android.samples.attributeeditor;

import android.view.View;

import com.esri.core.map.Field;

/**
 * POJO for storing the data associated with a row in the attributes list
 */
public class AttributeItem {

	private Field field;

	private Object value;

	private View view;

	public View getView() {

		return view;
	}

	public void setView(View view) {

		this.view = view;
	}

	public Field getField() {

		return field;
	}

	public void setField(Field field) {

		this.field = field;
	}

	public Object getValue() {

		return value;
	}

	public void setValue(Object value) {

		this.value = value;
	}

}
