/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.statisticalquery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.data.StatisticDefinition;
import com.esri.arcgisruntime.data.StatisticType;
import com.esri.arcgisruntime.data.StatisticsQueryParameters;
import com.esri.arcgisruntime.data.StatisticsQueryResult;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private ServiceFeatureTable mUsStatesFeatureTable;
  private Button mAddButton;
  private Button mRemoveStatisticButton;
  private Button mMoveRightButton;
  private Button mMoveLeftButton;
  private Button mChangeSortOrder;
  private Button mGetStatisticsButton;
  private Spinner mFieldSpinner;
  private Spinner mTypeSpinner;
  private RecyclerView mStatisticsDefinitionRecyclerView;
  private RecyclerView mGroupRecyclerView;
  private RecyclerView mOrderByRecyclerView;
  private RecyclerViewAdapter mStatisticsDefinitionAdapter;
  private RecyclerViewAdapter mGroupAdapter;
  private RecyclerViewAdapter mOrderByAdapter;

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
    mUsStatesFeatureTable = new ServiceFeatureTable(getString(R.string.us_states_census));

    // Collection of (user-defined) statistics to use in the query
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
      mFieldSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mFieldNameList));

      // fill the type spinner with StatisticType values
      mTypeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, StatisticType.values()));

      // wire up buttons to work only after the US states feature table has loaded
      mAddButton.setOnClickListener(v -> addStatistic());
      mRemoveStatisticButton.setOnClickListener(view -> removeStatistic());
      mMoveRightButton.setOnClickListener(view -> addFieldToOrderBy());
      mMoveLeftButton.setOnClickListener(view -> removeFieldFromOrderBy());
      mChangeSortOrder.setOnClickListener(view -> changeSortOrder());
      mGetStatisticsButton.setOnClickListener(view -> executeStatisticsQuery());
    });
  }

  /**
   * Adds a statistic, consisting of a field and StatisticType to a list of StatisticDefinitions and to the recycler
   * view.
   */
  private void addStatistic() {
    // get field and stat type from the respective spinners
    String fieldName = mFieldSpinner.getSelectedItem().toString();
    StatisticType statType = StatisticType.valueOf(mTypeSpinner.getSelectedItem().toString());

    // check if the list already contains this field/type pair, if not, then add it to both UI string list and stat
    // definition list
    if (!mStatisticDefinitionsAsStringsList.contains(fieldName + " (" + statType + ")")) {
      mStatisticDefinitionsAsStringsList.add(fieldName + " (" + statType + ")");
      mStatisticsDefinitionAdapter.notifyItemInserted(mStatisticDefinitionsAsStringsList.size() - 1);
      StatisticDefinition statDefinition = new StatisticDefinition(fieldName, statType);
      mStatisticDefinitionList.add(statDefinition);
    } else {
      Toast.makeText(MainActivity.this, "The statistic definitions list already contains this field and type pair.",
          Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Removes a statistic from the StatisticsDefinition list and recycler view.
   */
  private void removeStatistic() {
    // check if the statistic definitions list has any statistic definitions
    if (mStatisticDefinitionList.size() > 0) {
      int position = mStatisticsDefinitionAdapter.getSelectedPosition();

      // remove statistic definition from statistics definition list and recycler view
      mStatisticDefinitionList.remove(position);
      mStatisticDefinitionsAsStringsList.remove(position);
      mStatisticsDefinitionAdapter.notifyItemRemoved(position);
    }
  }

  /**
   * Adds a field to the order by list and recycler view, thus selecting the field for order by ASCENDING (by default).
   */
  private void addFieldToOrderBy() {

    // get the selected field from the group recycler view
    String field = mGroupAdapter.getItem(mGroupAdapter.getSelectedPosition());

    // check if field is checked
    if (getCheckedFields().contains(field)) {

      // check if the order by list already contains the field
      if (!mOrderByList.contains(field + " (" + QueryParameters.SortOrder.ASCENDING + ")") &&
          !mOrderByList.contains(field + " (" + QueryParameters.SortOrder.DESCENDING + ")")) {

        // add field to order list with a sort order of ASCENDING
        mOrderByList.add(field + " (" + QueryParameters.SortOrder.ASCENDING + ")");
        mOrderByAdapter.notifyItemInserted(mOrderByList.size() - 1);
      } else {
        Toast.makeText(MainActivity.this, "Statistics are already being ordered by " + field, Toast.LENGTH_LONG).show();
      }
    } else {
      Toast.makeText(MainActivity.this, "Only fields selected for grouping can also be ordered.", Toast.LENGTH_LONG)
          .show();
    }
  }

  /**
   * Removes a field from the order by list and recycler view.
   */
  private void removeFieldFromOrderBy() {
    // check that order by list has any order bys
    if (mOrderByList.size() > 0) {

      // get the selected order by's position in the recycler view
      int position = mOrderByAdapter.getSelectedPosition();

      // remove field from recycler view
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
    if (position >= 0) {

      // get the field and order and remove it from recycler view
      String fieldAndSortOrder = mOrderByAdapter.getItem(position);
      mOrderByList.remove(position);

      // get the field and sort order strings
      String field = getFieldFrom(fieldAndSortOrder);
      QueryParameters.SortOrder sortOrder = getSortOrderFrom(fieldAndSortOrder);

      // toggle between ASCENDING and DESCENDING
      if (sortOrder == QueryParameters.SortOrder.ASCENDING) {
        mOrderByList.add(position, field + " (" + QueryParameters.SortOrder.DESCENDING + ")");
      } else {
        mOrderByList.add(position, field + " (" + QueryParameters.SortOrder.ASCENDING + ")");
      }
      mOrderByAdapter.notifyItemChanged(position);
    } else {
      Toast.makeText(MainActivity.this, "Please select a field with sort order.", Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Creates a new StatisticsQueryParameters from the StatisticDefinitionList. Also adds a list of field names on which
   * the result should be grouped, as well as a list of field names for sort order.
   */
  private void executeStatisticsQuery() {
    // verify that there is at least one statistic definition in the statistic definition list
    if (mStatisticDefinitionList.size() == 0) {
      Toast.makeText(MainActivity.this, "Please define at least one statistic for the query.", Toast.LENGTH_LONG)
          .show();
      return;
    }

    // create the statistics query parameters, pass in the list of statistic definitions
    StatisticsQueryParameters statQueryParams = new StatisticsQueryParameters(mStatisticDefinitionList);

    // add all checked fields in the group fields list to the query parameters
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
        Toast.makeText(MainActivity.this,
            "Only checked fields in the 'Group Fields' list can be selected for ordering in the 'Order by Field' list.",
            Toast.LENGTH_LONG).show();
      }
    }

    // write the statistical query parameters to the log
    Log.i(TAG, "Statistical query parameters");
    for (StatisticDefinition statisticDefinition : statQueryParams.getStatisticDefinitions()) {
      Log.i(TAG, "Statistic definition: " + statisticDefinition.getFieldName() + ": " + statisticDefinition
          .getStatisticType());
    }
    for (String group : statQueryParams.getGroupByFieldNames()) {
      Log.i(TAG, "Group by: " + group);
    }
    for (QueryParameters.OrderBy orderBy : statQueryParams.getOrderByFields()) {
      Log.i(TAG, "Order " + orderBy.getFieldName() + " by " + orderBy.getSortOrder());
    }

    // execute the statistical query with these parameters and await the results
    ListenableFuture<StatisticsQueryResult> statisticsQueryResultFuture = mUsStatesFeatureTable
        .queryStatisticsAsync(statQueryParams);
    statisticsQueryResultFuture.addDoneListener(() -> {
      Log.d(TAG, "stats query result future returned");
      try {
        StatisticsQueryResult statisticsQueryResult = statisticsQueryResultFuture.get();
        // pass the results to displayResults
        displayResults(statisticsQueryResult);
      } catch (InterruptedException | ExecutionException e) {
        Log.e(TAG, "Invalid statistics definition: " + e.getMessage());
      }
    });
  }

  /**
   * Creates a new activity to display results and passes statisticsQueryResult to the new activity as JSON.
   *
   * @param statisticsQueryResult as generated in executeStatisticsQuery()
   */
  private void displayResults(StatisticsQueryResult statisticsQueryResult) {
    Intent intent = new Intent(this, ResultsActivity.class);
    Gson gson = new Gson();
    intent.putExtra("results", gson.toJson(statisticsQueryResult));
    startActivity(intent);
  }

  /**
   * Inflate all views in the user interface.
   */
  private void inflateUiViews() {
    mAddButton = findViewById(R.id.addButton);
    mRemoveStatisticButton = findViewById(R.id.removeStatisticButton);
    mMoveRightButton = findViewById(R.id.moveRightButton);
    mMoveLeftButton = findViewById(R.id.moveLeftButton);
    mChangeSortOrder = findViewById(R.id.changeSortOrderButton);
    mGetStatisticsButton = findViewById(R.id.getStatisticsButton);
    mFieldSpinner = findViewById(R.id.fieldSpinner);
    mTypeSpinner = findViewById(R.id.typeSpinner);
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
   * Helper method to get the sort order from a string containing a field and sort order.
   *
   * @param fieldAndOrder string from recycler view
   * @return SortOrder (either ASCENDING or DESCENDING)
   */
  private QueryParameters.SortOrder getSortOrderFrom(String fieldAndOrder) {
    String orderString = fieldAndOrder.substring(fieldAndOrder.indexOf("(") + 1, fieldAndOrder.indexOf(")"));
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
  private String getFieldFrom(String fieldAndOrder) {
    return fieldAndOrder.substring(0, fieldAndOrder.indexOf(" "));
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
      // if checked
      if (mGroupAdapter.getCheckedList()[i]) {
        checkedFields.add(mFieldNameList.get(i));
      }
    }
    return checkedFields;
  }
}
