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

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.database.DataSetObserver;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.esri.arcgis.android.samples.attributeeditor.FeatureLayerUtils.FieldType;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.FeatureType;
import com.esri.core.map.Field;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Adapter class which contains the logic of how to use and process the
 * FeatureLayers Fields and Attributes into an List Layout
 */
class AttributeListAdapter extends BaseAdapter {

	FeatureSet featureSet;

	private final Field[] fields;
	private final FeatureType[] types;
	private final String typeIdFieldName;
	private final Context context;
	private final LayoutInflater lInflator;
	private final int[] editableFieldIndexes;
	private final String[] typeNames;
	private final HashMap<String, FeatureType> typeMap;
	private AttributeItem[] items;

	final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT,
			DateFormat.SHORT);

	/**
	 * Constructor
	 */
	public AttributeListAdapter(Context context, Field[] fields,
			FeatureType[] types, String typeIdFieldName) {

		this.context = context;
		this.lInflator = LayoutInflater.from(context);
		this.fields = fields;
		this.types = types;
		this.typeIdFieldName = typeIdFieldName;

		// this.fieldsTemplateMap = createInitialFieldTemplateMap(this.fields);
		// parseTypes();

		// Setup processed variables
		this.editableFieldIndexes = FeatureLayerUtils
				.createArrayOfFieldIndexes(this.fields);
		this.typeNames = FeatureLayerUtils.createTypeNameArray(this.types);
		this.typeMap = FeatureLayerUtils.createTypeMapByValue(this.types);

		// register dataset observer to track when the underlying data is
		// changed
		this.registerDataSetObserver(new DataSetObserver() {

			public void onChanged() {

				// clear the array of attribute items
				AttributeListAdapter.this.items = new AttributeItem[AttributeListAdapter.this.editableFieldIndexes.length];

			}
		});
	}

	/**
	 * Implemented method from BaseAdapter class
	 */
	public int getCount() {

		return this.editableFieldIndexes.length;

	}

	/**
	 * Implemented method from BaseAdapter class. This method returns the actual
	 * data associated with a row in the list. In this case we return the field
	 * along with the field value as a custom object. We subsequently add the
	 * View which displays the value to this object so we can retrieve it when
	 * applying edits.
	 */
	public Object getItem(int position) {

		// get field associated with the position from the editableFieldIndexes
		// array created at startup
		int fieldIndex = this.editableFieldIndexes[position];

		AttributeItem row;

		// check to see if we have already created an attribute item if not
		// create
		// one
		if (items[position] == null) {

			// create new Attribute item for persisting the data for subsequent
			// events
			row = new AttributeItem();
			row.setField(this.fields[fieldIndex]);
			Object value = this.featureSet.getGraphics()[0]
					.getAttributeValue(fields[fieldIndex].getName());
			row.setValue(value);
			items[position] = row;

		} else {

			// reuse existing item to ensure View instance is kept.
			row = items[position];

		}

		return row;

	}

	/**
	 * Implemented method from BaseAdapter class
	 */
	public long getItemId(int position) {

		return position;

	}

	/**
	 * Implemented method from BaseAdapter class. This is the main method for
	 * returning a View which corresponds to a row in the list. This calls the
	 * getItem() method to get the data. It is called multiple times by the
	 * ListView and may be improved on by saving the previous result.
	 */
	public View getView(int position, View convertView, ViewGroup parent) {

		View container = null;

		AttributeItem item = (AttributeItem) getItem(position);

		// check field type
		// TODO if you want to support domains, add checks here and use the
		// createSpinnerViewFromArray to create spinners
		if (item.getField().getName().equals(this.typeIdFieldName)) {
			// This is the featurelayers type field

			container = lInflator.inflate(R.layout.item_spinner, null);
			// get the types name for this feature from the available values
			String typeStringValue = this.typeMap.get(
					item.getValue().toString()).getName();
			Spinner spinner = createSpinnerViewFromArray(container,
					item.getField(), typeStringValue, this.typeNames);
			item.setView(spinner);

			// TODO set listener to change types associated domain fields if
			// required

		} else if (FieldType.determineFieldType(item.getField()) == FieldType.DATE) {
			// create date picker for date fields

			container = lInflator.inflate(R.layout.item_date, null);
			long date = Long.parseLong(item.getValue().toString());

			Button dateButton = createDateButtonFromLongValue(container,
					item.getField(), date);
			item.setView(dateButton);

		} else {
			// create number and text fields
			// View object for saving in the AttrbuteItem once it has been set
			// up, for
			// accessing later when we apply edits.
			View valueView = null;

			if (FieldType.determineFieldType(item.getField()) == FieldType.STRING) {

				// get the string specific layout
				container = lInflator.inflate(R.layout.item_text, null);
				valueView = createAttributeRow(container, item.getField(),
						item.getValue());

			} else if (FieldType.determineFieldType(item.getField()) == FieldType.NUMBER) {

				// get the number specific layout
				container = lInflator.inflate(R.layout.item_number, null);
				valueView = createAttributeRow(container, item.getField(),
						item.getValue());

			} else if (FieldType.determineFieldType(item.getField()) == FieldType.DECIMAL) {

				// get the decimal specific layout
				container = lInflator.inflate(R.layout.item_decimal, null);
				valueView = createAttributeRow(container, item.getField(),
						item.getValue());

			}

			// set the rows view onto the item so it can be received when
			// applying
			// edits
			item.setView(valueView);

		}

		return container;

	}

	/**
	 * Sets the FeatureSet, called by the activity when a new queryResult is
	 * returned
	 * 
	 * @param featureSet
	 */
	public void setFeatureSet(FeatureSet featureSet) {

		this.featureSet = featureSet;

	}

	/**
	 * Helper method to create a spinner for a field and insert it into the View
	 * container. This uses, the String[] to create the list, and selects the
	 * value that is passed in from the list (the features value). Can be used
	 * for domains as well as types.
	 */
	private Spinner createSpinnerViewFromArray(View container, Field field,
											   Object value, String[] values) {

		TextView fieldAlias = (TextView) container
				.findViewById(R.id.field_alias_txt);
		Spinner spinner = (Spinner) container
				.findViewById(R.id.field_value_spinner);
		fieldAlias.setText(field.getAlias());
		spinner.setPrompt(field.getAlias());

		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
				this.context, android.R.layout.simple_spinner_item, values);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);
		// set current selection based on the value passed in
		spinner.setSelection(spinnerAdapter.getPosition(value.toString()));

		return spinner;
	}

	/**
	 * Helper method to create a date button, with appropriate onClick and
	 * onDateSet listeners to handle dates as a long (milliseconds since 1970),
	 * it uses the locale and presents a button with the date and time in short
	 * format.
	 */
	private Button createDateButtonFromLongValue(View container, Field field, long date) {

		TextView fieldAlias = (TextView) container
				.findViewById(R.id.field_alias_txt);
		Button dateButton = (Button) container
				.findViewById(R.id.field_date_btn);
		fieldAlias.setText(field.getAlias());

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);
		dateButton.setText(formatter.format(c.getTime()));

		addListenersToDatebutton(dateButton);

		return dateButton;
	}

	/**
	 * Helper method to add the field alias and the fields value into columns of
	 * a view using standard id names. If the field has a length set, then this
	 * is used to constrain the EditText's allowable characters. No validation
	 * is applied here, it is assumed that the container has this set already
	 * (in XML).
	 */
	private View createAttributeRow(View container, Field field, Object value) {

		TextView fieldAlias = (TextView) container
				.findViewById(R.id.field_alias_txt);
		EditText fieldValue = (EditText) container
				.findViewById(R.id.field_value_txt);
		fieldAlias.setText(field.getAlias());

		// set the length of the text field and its value
		if (field.getLength() > 0) {
			InputFilter.LengthFilter filter = new InputFilter.LengthFilter(
					field.getLength());
			fieldValue.setFilters(new InputFilter[] { filter });
		}

		Log.d(AttributeEditorActivity.TAG, "value is null? =" + (value == null));
		Log.d(AttributeEditorActivity.TAG, "value=" + value);

		if (value != null) {
			fieldValue.setText(value.toString(), BufferType.EDITABLE);
		} else {
			fieldValue.setText("", BufferType.EDITABLE);
		}

		return fieldValue;
	}

	/**
	 * Helper method to create the date button and its associated events
	 */
	private void addListenersToDatebutton(Button dateButton) {

		// create new onDateSetLisetener with the button associated with it
		final ListOnDateSetListener listener = new ListOnDateSetListener(
				dateButton);

		// add a click listener to the button
		dateButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				// if its a date, get the milliseconds value
				Calendar c = Calendar.getInstance();
				formatter.setCalendar(c);

				try {

					// parse to a double
					Button button = (Button) v;
					c.setTime(formatter.parse(button.getText().toString()));

				} catch (ParseException e) {
					// do nothing as should parse
				}

				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);

				// show date picker with date set to the items value (hence
				// built
				// outside of onCreateDialog)
				// TODO implement time picker if required, this picker only
				// supports
				// date and therefore showing the dialog will cause a change in
				// the time
				// value for the field
				DatePickerDialog dialog = new DatePickerDialog(context,
						listener, year, month, day);
				dialog.show();
			}
		});
	}

	/**
	 * Inner class for handling date change events from the date picker dialog
	 */
	class ListOnDateSetListener implements OnDateSetListener {

		final Button button;

		public ListOnDateSetListener(Button button) {

			this.button = button;
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {

			Calendar c = Calendar.getInstance();
			c.set(year, month, day);

			// Update the button to show the chosen date
			button.setText(formatter.format(c.getTime()));

		}
	}
}
