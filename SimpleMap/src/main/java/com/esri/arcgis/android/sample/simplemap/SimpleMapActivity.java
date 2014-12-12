package com.esri.arcgis.android.sample.simplemap;

import android.app.Fragment;
import com.esri.arcgis.android.sample.simplemap.SingleFragmentActivity;

// This class inherits SingleFragmentActivity to provide an activity that contains a single instance
// of the SimpleMapFragment.
// This class uses the Fragment API available in Android API level 11 and later; however for devices
// with older APIs you could adapt this class to use the Android Support Library.
public class SimpleMapActivity extends SingleFragmentActivity {

  @Override
  protected Fragment createFragment() {
    return new SimpleMapFragment();
  }

}