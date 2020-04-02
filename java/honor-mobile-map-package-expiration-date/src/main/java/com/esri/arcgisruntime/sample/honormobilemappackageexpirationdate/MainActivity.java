/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.honormobilemappackageexpirationdate;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.mapping.ExpirationType;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private TextView mExpirationMessageTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // get a reference to the expiration text view
    mExpirationMessageTextView = findViewById(R.id.expirationMessageTextView);

    // create a mobile map package from a local mmpk
    MobileMapPackage mobileMapPackage = new MobileMapPackage(
        getExternalFilesDir(null) + getString(R.string.path_to_expired_mmpk));

    // wait for the map package to load
    mobileMapPackage.addDoneLoadingListener(() -> {
      // check if the map package has expiration information and if so, has it expired yet
      if (mobileMapPackage.getExpiration() != null && mobileMapPackage.getExpiration().isExpired()) {
        // define a format for the date
        SimpleDateFormat daysHoursFormat = new SimpleDateFormat("yyyy-MM-dd' at 'hh:mm:ss", Locale.US);
        // show the expiration text view
        mExpirationMessageTextView.setVisibility(View.VISIBLE);
        // set the expiration message and expiration date to the text view
        mExpirationMessageTextView.setText(getString(R.string.expiration_text,
            mobileMapPackage.getExpiration().getMessage(),
            daysHoursFormat.format(mobileMapPackage.getExpiration().getDateTime().getTime())));
        if (mobileMapPackage.getExpiration().getType() == ExpirationType.ALLOW_EXPIRED_ACCESS) {
          // add the map to the map view
          mMapView.setMap(mobileMapPackage.getMaps().get(0));
        } else if (mobileMapPackage.getExpiration().getType() == ExpirationType.PREVENT_EXPIRED_ACCESS) {
          Toast.makeText(this, "The author of this mobile map package has disallowed access after the expiration date.",
              Toast.LENGTH_LONG).show();
        }
      }
    });
    mobileMapPackage.loadAsync();
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
