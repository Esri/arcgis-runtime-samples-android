package com.esri.arcgis.android.samples.geometryeditor;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * This class implements a DialogFragment that reports that edits applied to a feature layer failed.
 */
public class EditFailedDialogFragment extends DialogFragment {
  String mMessage;

  // Mandatory empty constructor for fragment manager to recreate fragment after it's destroyed.
  public EditFailedDialogFragment() {
  }

  /**
   * Sets message to display to the user.
   * 
   * @param message
   */
  public void setMessage(String message) {
    mMessage = message;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.edit_failed, container, false);
    getDialog().setTitle(R.string.title_edit_failed);
    Button button = (Button) view.findViewById(R.id.ok_key);
    TextView textView = (TextView) view.findViewById(R.id.edit_failed_msg);
    textView.setText(mMessage);
    button.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        dismiss();
      }

    });
    return view;
  }
}
