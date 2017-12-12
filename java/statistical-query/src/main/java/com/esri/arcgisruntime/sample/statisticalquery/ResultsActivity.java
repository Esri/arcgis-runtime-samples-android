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

    // get intent from main activity
    Intent intent = getIntent();
    String statisticsQueryResultString = intent.getStringExtra("results");
    Gson gson = new Gson();
    // get statistics query result from intent as gson
    StatisticsQueryResult statisticsQueryResult = gson.fromJson(statisticsQueryResultString, StatisticsQueryResult.class);

    // create a linked hash map (which preserves ordering) for storage of results and
    // populate it with the statistics query result
    LinkedHashMap<String, List<String>> groupedStatistics = new LinkedHashMap<>();
    // get each statistic record
    for (Iterator<StatisticRecord> results = statisticsQueryResult.iterator(); results.hasNext(); ) {
      StatisticRecord statisticRecord = results.next();
      // get group for each statistic record
      for (Map.Entry<String, Object> group : statisticRecord.getGroup().entrySet()) {
        // add all stats for each group to a new list
        List<String> statsForGroup = new ArrayList<>();
        for (Map.Entry<String, Object> stat : statisticRecord.getStatistics().entrySet()) {
          statsForGroup.add(stat.getKey() + ": " + stat.getValue());
        }
        // add group and associated stats for that group to linked hash map
        groupedStatistics.put(group.getValue().toString(), statsForGroup);
      }
    }

    // create expandable list view
    ExpandableListView expandableListView = findViewById(R.id.expandableListView);
    ExpandableListAdapter expandableListAdapter = new ExpandableListViewAdapter(this, groupedStatistics);
    expandableListView.setAdapter(expandableListAdapter);
  }
}
