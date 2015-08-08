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
