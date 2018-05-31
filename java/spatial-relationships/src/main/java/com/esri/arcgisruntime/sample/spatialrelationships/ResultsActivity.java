package com.esri.arcgisruntime.sample.spatialrelationships;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);
    setContentView(R.layout.results_expandable_list);

    // get intent from main activity
    Intent intent = getIntent();

    LinkedHashMap<String,List<String>> child = new LinkedHashMap<>();
    ArrayList<String> header = new ArrayList<>();
    header.add("Point");
    header.add("Polyline");
    header.add("Polygon");
    ArrayList<String> relationships = new ArrayList<>();
    relationships.add("me");
    relationships.add("you");
    relationships.add("lol");
    child.put(header.get(0),relationships) ;
    child.put(header.get(1),relationships);
    child.put(header.get(2),relationships);

    ExpandableListView expandableListView = findViewById(R.id.expandableList);
    ResultsExpandableListAdapter expandableListAdapter = new ResultsExpandableListAdapter(this,header,child);
    expandableListView.setAdapter(expandableListAdapter);




  }
}
