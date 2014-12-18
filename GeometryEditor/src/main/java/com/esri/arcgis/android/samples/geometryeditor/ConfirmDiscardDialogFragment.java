package com.esri.arcgis.android.samples.geometryeditor;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * This class implements a DialogFragment that asks the user to confirm that the feature being added is to be discarded.
 */
public class ConfirmDiscardDialogFragment extends DialogFragment {
  View.OnClickListener mYesListener;

  // Mandatory empty constructor for fragment manager to recreate fragment after it's destroyed.
  public ConfirmDiscardDialogFragment() {
  }

  /**
   * Sets listener for click on yes button.
   * 
   * @param listener
   */
  public void setYesListener(View.OnClickListener listener) {
    mYesListener = listener;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.confirm_discard, container, false);
    getDialog().setTitle(R.string.title_confirm_discard);
    Button button = (Button) view.findViewById(R.id.no_key);
    button.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        dismiss();
      }

    });
    button = (Button) view.findViewById(R.id.yes_key);
    if (mYesListener != null) {
      button.setOnClickListener(mYesListener);
    }
    return view;
  }
}
