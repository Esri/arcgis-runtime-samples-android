package com.esri.arcgisruntime.sample.statisticalquery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.google.gson.Gson;

import com.esri.arcgisruntime.data.StatisticRecord;
import com.esri.arcgisruntime.data.StatisticsQueryResult;

public class ResultsActivity extends AppCompatActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.results_expandablelistview);

    // get the results hashmap from MainActivity
    Intent intent = getIntent();

    Gson gson = new Gson();
    String statisticsQueryResultString = intent.getStringExtra("results");
    StatisticsQueryResult statisticsQueryResult = gson.fromJson(statisticsQueryResultString, StatisticsQueryResult.class);

    // create a hash map for storage of results and populate it with the statistics query result
    LinkedHashMap<String, List<String>> groupedStatistics = new LinkedHashMap<>();
    for (Iterator<StatisticRecord> results = statisticsQueryResult.iterator(); results.hasNext(); ) {
      StatisticRecord statisticRecord = results.next();
      for (Map.Entry<String, Object> group : statisticRecord.getGroup().entrySet()) {
        List<String> statsForGroup = new ArrayList<>();
        for (Map.Entry<String, Object> stat : statisticRecord.getStatistics().entrySet()) {
          statsForGroup.add(stat.getKey() + ": " + stat.getValue());
        }
        groupedStatistics.put(group.getValue().toString(), statsForGroup);
      }
    }

    // create expandable list view
    ExpandableListView expandableListView = findViewById(R.id.expandableListView);
    ExpandableListAdapter expandableListAdapter = new ExpandableListViewAdapter(this, groupedStatistics);
    expandableListView.setAdapter(expandableListAdapter);
  }
}
