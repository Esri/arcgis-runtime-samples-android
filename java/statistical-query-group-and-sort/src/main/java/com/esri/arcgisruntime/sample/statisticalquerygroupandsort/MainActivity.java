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

package com.esri.arcgisruntime.sample.statisticalquerygroupandsort;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.data.StatisticDefinition;
import com.esri.arcgisruntime.data.StatisticRecord;
import com.esri.arcgisruntime.data.StatisticType;
import com.esri.arcgisruntime.data.StatisticsQueryParameters;
import com.esri.arcgisruntime.data.StatisticsQueryResult;
import com.google.gson.Gson;

/**
 * This class demonstrates querying statistics from a service feature table. To make the query relevant,
 * StatisticsQueryParameters are created which allow the user to specify:
 * - Which fields and statistical types to include in the result
 * - A list of fields by which the statistical query result should be grouped
 * - A list of grouped fields which should be ordered
 */
public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private ServiceFeatureTable mUsStatesFeatureTable;

  private RecyclerView mStatisticsDefinitionRecyclerView;
  private RecyclerView mGroupRecyclerView;
  private RecyclerView mOrderByRecyclerView;
  private RecyclerViewAdapter mStatisticsDefinitionAdapter;
  private RecyclerViewAdapter mGroupAdapter;
  private RecyclerViewAdapter mOrderByAdapter;
  private AlertDialog mQueryExecutingAlert;

  private List<StatisticDefinition> mStatisticDefinitionList;
  private List<String> mStatisticDefinitionsAsStringsList;
  private List<String> mOrderByList;
  private List<String> mFieldNameList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    inflateUiViews();

    // create US states feature table
    mUsStatesFeatureTable = new ServiceFeatureTable(getString(R.string.obesity_inactivity_diabetes_feature_service));

    // collection of (user-defined) statistics to use in the query
    mStatisticDefinitionList = new ArrayList<>();

    // load the table
    mUsStatesFeatureTable.loadAsync();
    mUsStatesFeatureTable.addDoneLoadingListener(() -> {

      // fill array with field names
      mFieldNameList = new ArrayList<>();
      for (int i = 0; i < mUsStatesFeatureTable.getFields().size(); i++) {
        String fieldName = mUsStatesFeatureTable.getFields().get(i).getName();
        mFieldNameList.add(fieldName);
      }

      createRecyclerViews();

      // fill the field spinner with field names
      Spinner fieldSpinner = findViewById(R.id.fieldSpinner);
      fieldSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mFieldNameList));

      // fill the type spinner with StatisticType values
      Spinner typeSpinner = findViewById(R.id.typeSpinner);
      typeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, StatisticType.values()));

      // add default search values
      addStatistic("Diabetes_Percent", StatisticType.AVERAGE);
      addStatistic("Diabetes_Percent", StatisticType.COUNT);
      addStatistic("Diabetes_Percent", StatisticType.STANDARD_DEVIATION);
      mGroupAdapter.getCheckedList()[3] = true;
      addFieldToOrderBy("State");

      // wire up buttons to work only after the US states feature table has loaded
      Button addButton = findViewById(R.id.addButton);
      addButton.setOnClickListener(v -> {
        // get field and stat type from the respective spinners
        String fieldName = fieldSpinner.getSelectedItem().toString();
        StatisticType statType = StatisticType.valueOf(typeSpinner.getSelectedItem().toString());
        addStatistic(fieldName, statType);
      });
      Button removeStatisticButton = findViewById(R.id.removeStatisticButton);
      removeStatisticButton.setOnClickListener(view -> removeStatistic());
      Button moveRightButton = findViewById(R.id.moveRightButton);
      moveRightButton.setOnClickListener(view -> {
        // get the selected field from the group recycler view
        String field = mGroupAdapter.getItem(mGroupAdapter.getSelectedPosition());
        addFieldToOrderBy(field);
      });
      Button moveLeftButton = findViewById(R.id.moveLeftButton);
      moveLeftButton.setOnClickListener(view -> removeFieldFromOrderBy());
      Button changeSortOrderButton = findViewById(R.id.changeSortOrderButton);
      changeSortOrderButton.setOnClickListener(view -> changeSortOrder());
      Button getStatisticsButton = findViewById(R.id.getStatisticsButton);
      getStatisticsButton.setOnClickListener(view -> {
        // verify that there is at least one statistic definition in the statistic definition list before query
        if (!mStatisticDefinitionList.isEmpty()) {
          executeStatisticsQuery();
        }
      });
    });

    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    View queryExecutingView = getLayoutInflater().inflate(R.layout.query_executing_dialog, null);
    dialogBuilder.setView(queryExecutingView);
    mQueryExecutingAlert = dialogBuilder.create();
    mQueryExecutingAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
  }

  /**
   * Creates a new StatisticsQueryParameters from a list of  StatisticDefinitions. Also adds a list of field names on
   * which the result should be grouped, as well as a list of field names for sort order.
   */
  private void executeStatisticsQuery() {

    // create the statistics query parameters, pass in the list of statistic definitions
    StatisticsQueryParameters statQueryParams = new StatisticsQueryParameters(mStatisticDefinitionList);

    // ignore counties with missing data
    statQueryParams.setWhereClause("\"State\" IS NOT NULL");

    // specify the fields to group by (if any)
    List<String> groupList = new ArrayList<>();
    for (int i = 0; i < mGroupAdapter.getCheckedList().length; i++) {
      if (mGroupAdapter.getCheckedList()[i]) {
        groupList.add(mFieldNameList.get(i));
      }
    }
    statQueryParams.getGroupByFieldNames().addAll(groupList);

    // specify the fields to order by (if any)
    for (String fieldAndSortOrder : mOrderByList) {
      // check that all orderBy fields are also checked for grouping
      if (groupList.contains(getFieldFrom(fieldAndSortOrder))) {
        // create a new OrderBy object to define the sort order for the selected field
        QueryParameters.OrderBy orderBy = new QueryParameters.OrderBy(getFieldFrom(fieldAndSortOrder),
            getSortOrderFrom(fieldAndSortOrder));
        statQueryParams.getOrderByFields().add(orderBy);
      } else {
        Toast.makeText(this,
            "Only checked fields in the 'Group Fields' list can be selected for ordering in the 'Order by Field' list.",
            Toast.LENGTH_LONG).show();
      }
    }

    // execute the statistical query with these parameters and await the results
    ListenableFuture<StatisticsQueryResult> statisticsQueryResultFuture = mUsStatesFeatureTable
        .queryStatisticsAsync(statQueryParams);

    // show a loading dialog on execution of query
    mQueryExecutingAlert.show();

    statisticsQueryResultFuture.addDoneListener(() -> {
      try {
        // get the StatisticsQueryResult
        StatisticsQueryResult statisticsQueryResult = statisticsQueryResultFuture.get();

        // create a LinkedHashMap (preserves ordering) and populate it with the statistics query result
        Map<String, List<String>> groupedStatistics = new LinkedHashMap<>();
        // get each statistic record
        for (Iterator<StatisticRecord> results = statisticsQueryResult.iterator(); results.hasNext(); ) {
          StatisticRecord statisticRecord = results.next();
          // if statistic record contains no grouping
          if (statisticRecord.getGroup().isEmpty()) {
            List<String> statsWithoutGroup = new ArrayList<>();
            for (Map.Entry<String, Object> stat : statisticRecord.getStatistics().entrySet()) {
              statsWithoutGroup.add(stat.getKey() + ": " + stat.getValue());
            }
            // add statistics to an expandable list view category called ungrouped statistics
            groupedStatistics.put("Ungrouped statistics", statsWithoutGroup);
          } else {
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
        }

        // write the linked hash map out to json
        Gson gson = new Gson();
        String groupedStatisticsJson = gson.toJson(groupedStatistics, LinkedHashMap.class);
        // pass the results to displayResults
        displayResults(groupedStatisticsJson);
      } catch (InterruptedException | ExecutionException e) {
        Log.e(TAG, "Invalid statistics definition: " + e.getMessage());
      }
    });
  }

  /**
   * Adds a statistic, consisting of a field and StatisticType to a list of StatisticDefinitions and to the recycler
   * view.
   */
  private void addStatistic(String fieldName, StatisticType statType) {

    // check if the list already contains this field/type pair, and adds it if it doesn't
    if (!mStatisticDefinitionsAsStringsList.contains(fieldName + " (" + statType + ')')) {
      mStatisticDefinitionsAsStringsList.add(fieldName + " (" + statType + ')');
      mStatisticsDefinitionAdapter.notifyItemInserted(mStatisticDefinitionsAsStringsList.size() - 1);
      StatisticDefinition statDefinition = new StatisticDefinition(fieldName, statType);
      mStatisticDefinitionList.add(statDefinition);
    } else {
      Toast.makeText(this, "The statistic definitions list already contains this field and type pair.",
          Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Removes a statistic from the StatisticsDefinition list and recycler view.
   */
  private void removeStatistic() {
    // check if the statistic definitions list has any statistic definitions
    if (!mStatisticDefinitionList.isEmpty()) {
      // remove statistic definition from statistics definition list and recycler view
      int position = mStatisticsDefinitionAdapter.getSelectedPosition();
      if (position < mStatisticDefinitionList.size()) {
        mStatisticDefinitionList.remove(position);
        mStatisticDefinitionsAsStringsList.remove(position);
        mStatisticsDefinitionAdapter.notifyItemRemoved(position);
      }
    }
  }

  /**
   * Adds a field to the order by list and recycler view, thus selecting the field for order by ASCENDING (by default).
   */
  private void addFieldToOrderBy(String field) {

    // check if field is checked
    if (getCheckedFields().contains(field)) {
      // check if the order by list already contains the field
      if (!mOrderByList.contains(field + " (" + QueryParameters.SortOrder.ASCENDING + ')') &&
          !mOrderByList.contains(field + " (" + QueryParameters.SortOrder.DESCENDING + ')')) {
        // add field to order list with a sort order of ASCENDING
        mOrderByList.add(field + " (" + QueryParameters.SortOrder.ASCENDING + ')');
        mOrderByAdapter.notifyItemInserted(mOrderByList.size() - 1);
      } else {
        Toast.makeText(this, "Statistics are already being ordered by " + field, Toast.LENGTH_LONG).show();
      }
    } else {
      Toast.makeText(this, "Only fields selected for grouping can also be ordered.", Toast.LENGTH_LONG)
          .show();
    }
  }

  /**
   * Removes a field from the order by list and recycler view.
   */
  private void removeFieldFromOrderBy() {
    // check that order by list has any order bys
    if (!mOrderByList.isEmpty()) {

      // remove field from recycler view
      int position = mOrderByAdapter.getSelectedPosition();
      mOrderByList.remove(position);
      mOrderByAdapter.notifyItemRemoved(position);
    }
  }

  /**
   * Toggles the order by list between ASCENDING and DESCENDING.
   */
  private void changeSortOrder() {
    int position = mOrderByAdapter.getSelectedPosition();

    // check if there are any fields in the order by recycler view
    if (position >= 0 && position < mOrderByList.size()) {
      // get the field and order and remove it from recycler view
      String fieldAndSortOrder = mOrderByAdapter.getItem(position);
      mOrderByList.remove(position);
      // get the field and sort order strings
      String field = getFieldFrom(fieldAndSortOrder);
      QueryParameters.SortOrder sortOrder = getSortOrderFrom(fieldAndSortOrder);
      // toggle between ASCENDING and DESCENDING
      if (sortOrder == QueryParameters.SortOrder.ASCENDING) {
        mOrderByList.add(position, field + " (" + QueryParameters.SortOrder.DESCENDING + ')');
      } else {
        mOrderByList.add(position, field + " (" + QueryParameters.SortOrder.ASCENDING + ')');
      }
      mOrderByAdapter.notifyItemChanged(position);
    } else {
      Toast.makeText(this, "Please select a field with sort order.", Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Creates a new activity to display results.
   *
   * @param groupedStatisticsJson Json of LinkedHashMap containing grouped result from statisticsQueryResult
   */
  private void displayResults(String groupedStatisticsJson) {
    Intent intent = new Intent(this, ResultsActivity.class);
    ResultsActivity.results = groupedStatisticsJson;
    startActivity(intent);
  }

  /**
   * Inflate all views in the user interface.
   */
  private void inflateUiViews() {
    mStatisticsDefinitionRecyclerView = findViewById(R.id.fieldTypeRecyclerView);
    mGroupRecyclerView = findViewById(R.id.groupFieldRecyclerView);
    mOrderByRecyclerView = findViewById(R.id.orderFieldRecyclerView);
  }

  /**
   * Create recycler views and their adapters.
   */
  private void createRecyclerViews() {
    // field type recycler view
    mStatisticDefinitionsAsStringsList = new ArrayList<>();
    mStatisticsDefinitionRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mStatisticsDefinitionAdapter = new RecyclerViewAdapter(this, mStatisticDefinitionsAsStringsList, false);
    mStatisticsDefinitionRecyclerView.setAdapter(mStatisticsDefinitionAdapter);

    // group field recycler view which just takes the full list of of field names
    mGroupRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mGroupAdapter = new RecyclerViewAdapter(this, mFieldNameList, true);
    mGroupRecyclerView.setAdapter(mGroupAdapter);

    // order by field recycler view
    mOrderByList = new ArrayList<>();
    mOrderByRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mOrderByAdapter = new RecyclerViewAdapter(this, mOrderByList, false);
    mOrderByRecyclerView.setAdapter(mOrderByAdapter);
  }

  /**
   * Helper method to get a list of the fields which are checked in the group fields recycler view.
   *
   * @return list of checked fields
   */
  private ArrayList<String> getCheckedFields() {

    ArrayList<String> checkedFields = new ArrayList<>();

    // add all checked fields in the group fields list to the query parameters
    for (int i = 0; i < mGroupAdapter.getCheckedList().length; i++) {
      if (mGroupAdapter.getCheckedList()[i]) {
        checkedFields.add(mFieldNameList.get(i));
      }
    }
    return checkedFields;
  }

  @Override protected void onPause() {
    super.onPause();
    mQueryExecutingAlert.dismiss();
  }

  /**
   * Helper method to get the sort order from a string containing a field and sort order.
   *
   * @param fieldAndOrder string from recycler view
   * @return SortOrder (either ASCENDING or DESCENDING)
   */
  private static QueryParameters.SortOrder getSortOrderFrom(String fieldAndOrder) {
    String orderString = fieldAndOrder.substring(fieldAndOrder.indexOf('(') + 1, fieldAndOrder.indexOf(')'));
    QueryParameters.SortOrder sortOrder;
    switch (orderString) {
      case "DESCENDING":
        sortOrder = QueryParameters.SortOrder.DESCENDING;
        break;
      case "ASCENDING":
        sortOrder = QueryParameters.SortOrder.ASCENDING;
        break;
      default:
        Log.e(TAG, "Invalid sort order: " + orderString);
        sortOrder = null;
        break;
    }
    return sortOrder;
  }

  /**
   * Helper method to get a field from a string.
   *
   * @param fieldAndOrder string from recycler view
   * @return field as a string
   */
  private static String getFieldFrom(String fieldAndOrder) {
    return fieldAndOrder.substring(0, fieldAndOrder.indexOf(' '));
  }

}
