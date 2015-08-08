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

import android.app.Fragment;

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