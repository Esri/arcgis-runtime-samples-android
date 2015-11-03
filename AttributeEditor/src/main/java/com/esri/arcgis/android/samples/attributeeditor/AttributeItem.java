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

import android.view.View;

import com.esri.core.map.Field;

/**
 * POJO for storing the data associated with a row in the attributes list
 */
class AttributeItem {

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
