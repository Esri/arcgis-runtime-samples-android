package com.esri.arcgis.android.sample.simplemap;

import android.os.Bundle;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Activity;

// A generic activity that can be inherited to provide an activity that loads a single fragment.
// This class uses the Fragment API available in Android API level 11 and later; however for devices
// with older APIs you could adapt this class to use the Android Support Library.
public abstract class SingleFragmentActivity extends Activity {
  
  // Returns an instance of the fragment to be hosted in the activity.
  protected abstract Fragment createFragment();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_fragment);
    
    // If the single contained fragment does not already exist then create it.
    FragmentManager manager = getFragmentManager();
    Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
    if (fragment == null) {
      fragment = createFragment();
      manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
    }
  }
  
}
