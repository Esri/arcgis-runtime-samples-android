/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.analyzehotspots;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateRangeDialogFragment extends DialogFragment {

  private static final String TAG = DateRangeDialogFragment.class.getSimpleName();

  private SimpleDateFormat mSimpleDateFormatter;

  private Date mMinDate;

  private Date mMaxDate;

  private OnAnalyzeButtonClickListener onAnalyzeButtonClickListener;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // create a simple date formatter to parse strings to date
    mSimpleDateFormatter = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
    try {
      // set default date range for the data set
      mMinDate = mSimpleDateFormatter.parse(getString(R.string.min_date));
      mMaxDate = mSimpleDateFormatter.parse(getString(R.string.max_date));
    } catch (ParseException e) {
      Log.e(TAG, "Error in date format: " + e.getMessage());
    }
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnAnalyzeButtonClickListener) {
      this.onAnalyzeButtonClickListener = (OnAnalyzeButtonClickListener) context;
    } else {
      throw new ClassCastException(context.toString()
          + " must implement OnAnalyzeButtonClickListener");
    }
  }

  @NonNull @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.date_range_dialog, null);

    final EditText fromEditText = dialogView.findViewById(R.id.fromEditText);
    final EditText toEditText = dialogView.findViewById(R.id.toEditText);
    Button analyzeButton = dialogView.findViewById(R.id.analyzeButton);

    final AlertDialog dateRangeDialog = builder.setTitle(R.string.date_range_dialog_title)
        .setView(dialogView)
        .setCancelable(true)
        .create();

    fromEditText.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        showCalendar(InputCalendar.From, (EditText) v);
      }
    });

    toEditText.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        showCalendar(InputCalendar.To, (EditText) v);
      }
    });

    // if button is clicked, close the custom dialog
    analyzeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (onAnalyzeButtonClickListener != null) {
          onAnalyzeButtonClickListener
              .onAnalyzeButtonClick(fromEditText.getText().toString(), toEditText.getText().toString());
        }
        getDialog().dismiss();
      }
    });

    return dateRangeDialog;
  }

  /**
   * Shows a date picker dialog and writes the date chosen to the correct editable text.
   *
   * @param inputCalendar enum which specifies which editable text the chosen date should be written to
   * @param editText      the instance of EditText that will be updated when a date has been picked
   */
  private void showCalendar(final InputCalendar inputCalendar, final EditText editText) {
    // create a date set listener
    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
      @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // build the correct date format for the query
        StringBuilder date = new StringBuilder()
            .append(year)
            .append("-")
            .append(month + 1)
            .append("-")
            .append(dayOfMonth);
        // set the date to correct text view
        if (inputCalendar == InputCalendar.From) {
          try {
            // limit the min date to after from date
            mMinDate = mSimpleDateFormatter.parse(date.toString());
          } catch (ParseException e) {
            e.printStackTrace();
          }
        } else if (inputCalendar == InputCalendar.To) {
          try {
            // limit the maximum date to before the to date
            mMaxDate = mSimpleDateFormatter.parse(date.toString());
          } catch (ParseException e) {
            e.printStackTrace();
          }
        }
        editText.setText(date);
      }
    };

    // define the date picker dialog
    Calendar calendar = Calendar.getInstance();
    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), onDateSetListener,
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    datePickerDialog.getDatePicker().setMinDate(mMinDate.getTime());
    datePickerDialog.getDatePicker().setMaxDate(mMaxDate.getTime());
    if (inputCalendar == InputCalendar.From) {
      // start from calendar from min date
      datePickerDialog.updateDate(1998, 0, 1);
    }
    datePickerDialog.show();
  }

  public interface OnAnalyzeButtonClickListener {
    void onAnalyzeButtonClick(String fromDate, String toDate);
  }
}

// enum to flag whether the date picker calendar shown should be for the 'from' or 'to' date
enum InputCalendar {
  From,
  To
}
