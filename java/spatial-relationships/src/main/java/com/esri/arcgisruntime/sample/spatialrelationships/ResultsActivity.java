/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.spatialrelationships;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ExpandableListView;

public class ResultsActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);
    setContentView(R.layout.results_expandable_list);

    // get intent from main activity
    Intent intent = getIntent();
    // get the HashMap created in MainActivity
    HashMap<String,ArrayList<String>> child = (HashMap<String,ArrayList<String>>) intent.getSerializableExtra("HashMap");
    ArrayList<String> header = new ArrayList<>();
    header.add("Point");
    header.add("Polyline");
    header.add("Polygon");

    // create an expandable list view and an adapter to display in new activity.
    ExpandableListView expandableListView = findViewById(R.id.expandableList);
    ExpandableListAdapter expandableListAdapter = new ExpandableListAdapter(this,header,child);
    expandableListView.setAdapter(expandableListAdapter);
  }
}
