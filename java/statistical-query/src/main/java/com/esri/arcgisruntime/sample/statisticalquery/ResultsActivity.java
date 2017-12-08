package com.esri.arcgisruntime.sample.statisticalquery;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class ResultsActivity extends AppCompatActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.results_expandablelistview);

    // get the results hashmap from MainActivity
    Intent intent = getIntent();

    // convert to tree map to sort on keys (alphabetical by group name)
    TreeMap<String, List<String>> results = new TreeMap<>((HashMap<String, List<String>>) intent.getSerializableExtra("results"));

    // create expandable list view
    ExpandableListView expandableListView = findViewById(R.id.expandableListView);
    ExpandableListAdapter expandableListAdapter = new ExpandableListViewAdapter(this, results);
    expandableListView.setAdapter(expandableListAdapter);
  }
}
