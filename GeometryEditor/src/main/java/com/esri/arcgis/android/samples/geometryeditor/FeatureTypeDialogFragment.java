package com.esri.arcgis.android.samples.geometryeditor;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.esri.arcgis.android.samples.geometryeditor.GeometryEditorActivity.FeatureTypeListAdapter;

/**
 * This class implements a DialogFragment that asks the user to select the type of feature to add.
 */
public class FeatureTypeDialogFragment extends DialogFragment {
  AdapterView.OnItemClickListener mListListener;

  FeatureTypeListAdapter mAdapter;

  // Mandatory empty constructor for fragment manager to recreate fragment after it's destroyed.
  public FeatureTypeDialogFragment() {
  }

  /**
   * Sets listener for click on a list item.
   * 
   * @param listener
   */
  public void setListListener(AdapterView.OnItemClickListener listener) {
    mListListener = listener;
  }

  /**
   * Sets list adapter.
   * 
   * @param adapter
   */
  public void setListAdapter(FeatureTypeListAdapter adapter) {
    mAdapter = adapter;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.selectfeaturetype, container, false);
    getDialog().setTitle(R.string.title_feature_type);
    ListView listView = (ListView) view.findViewById(R.id.listView1);
    if (mListListener != null) {
      listView.setOnItemClickListener(mListListener);
    }
    if (mAdapter != null) {
      listView.setAdapter(mAdapter);
    }
    return view;
  }
}
