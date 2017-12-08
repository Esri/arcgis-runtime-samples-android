package com.esri.arcgisruntime.sample.statisticalquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.data.StatisticDefinition;
import com.esri.arcgisruntime.data.StatisticRecord;
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
  private RecyclerViewAdapterCheckBox mGroupAdapter;
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
    String fieldName = mFieldSpinner.getSelectedItem().toString();
    StatisticType statType = StatisticType.valueOf(mTypeSpinner.getSelectedItem().toString());
    if (mStatisticDefinitionsAsStringsList.contains(fieldName + " (" + statType + ")")) {
      mStatisticDefinitionsAsStringsList.add(fieldName + " (" + statType + ")");
      mStatisticsDefinitionAdapter.notifyItemInserted(mStatisticDefinitionsAsStringsList.size() - 1);
      StatisticDefinition statDefinition = new StatisticDefinition(fieldName, statType);
      mStatisticDefinitionList.add(statDefinition);
    } else {
      Toast.makeText(MainActivity.this, "The statistic definitions list already contains this pair of field and type.", Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Removes a statistic from the StatisticsDefinition list and recycler view.
   */
  private void removeStatistic() {
    // check statistic definitions list has any statistic definitions
    if (mStatisticDefinitionList.size() > 0) {
      int position = mStatisticsDefinitionAdapter.getSelectedPosition();

      // remove statistic definition from statistics definition list and recycler view
      mStatisticDefinitionList.remove(position);
      mStatisticDefinitionsAsStringsList.remove(position);
      mStatisticsDefinitionAdapter.notifyItemRemoved(position);
    }
  }

  /**
   * Adds a field to the order by list and recycler view therefore selecting the field for order for ASCENDING (by
   * default).
   */
  private void addFieldToOrderBy() {

    String field = mGroupAdapter.getItem(mGroupAdapter.getSelectedPosition());

    if (getCheckedFields().contains(field)) {

      if (!mOrderByList.contains(field + " (" + QueryParameters.SortOrder.ASCENDING + ")") &&
          !mOrderByList.contains(field + " (" + QueryParameters.SortOrder.DESCENDING + ")")) {

        // add field to order by recycler view with a sort order of ASCENDING
        mOrderByList.add(field + " (" + QueryParameters.SortOrder.ASCENDING + ")");
        mOrderByAdapter.notifyItemInserted(mOrderByList.size() - 1);

      } else {
        Toast.makeText(MainActivity.this, "Statistics are already being ordered by " + field, Toast.LENGTH_LONG).show();
      }
    } else {
      Toast.makeText(MainActivity.this, "Only fields selected for grouping can also be ordered.", Toast.LENGTH_LONG).show();
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

    if (position >= 0) {
      String fieldAndOrder = mOrderByAdapter.getItem(position);
      mOrderByList.remove(position);
      String field = getField(fieldAndOrder);
      QueryParameters.SortOrder sortOrder = getSortOrder(fieldAndOrder);
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
   *
   */
  private void executeStatisticsQuery() {
    // verify that there is at least one statistic definition
    if (mStatisticDefinitionList.size() == 0) {
      Toast.makeText(MainActivity.this, "Please define at least one statistic for the query.", Toast.LENGTH_LONG)
          .show();
      return;
    }

    // create the statistics query parameters, pass in the list of statistic definitions
    StatisticsQueryParameters statQueryParams = new StatisticsQueryParameters(mStatisticDefinitionList);

    // add all checked fields in the group fields list to the query parameters
    for (int i = 0; i < mGroupAdapter.getCheckedList().length; i++) {
      if (mGroupAdapter.getCheckedList()[i]) {
        statQueryParams.getGroupByFieldNames().add(mFieldNameList.get(i));
      }
    }

    // Specify the fields to order by (if any)
    for (String fieldAndSortOrder : mOrderByList) {

      // create a new OrderBy object to define the sort for the selected field
      QueryParameters.OrderBy orderBy = new QueryParameters.OrderBy(getField(fieldAndSortOrder), getSortOrder(fieldAndSortOrder));
      statQueryParams.getOrderByFields().add(orderBy);
    }

    // write the statistical query parameters to the log
    Log.i(TAG, "Statistical query parameters");
    for (StatisticDefinition statisticDefinition: statQueryParams.getStatisticDefinitions()) {
      Log.i(TAG, "Statistic definition: " + statisticDefinition.getFieldName() + ": " + statisticDefinition.getStatisticType());
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

        // create a hash map for storage of results and populate it with the statistics query result
        HashMap<String, List<String>> groupedStatistics = new HashMap<>();
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
        displayResults(groupedStatistics);
      } catch (InterruptedException | ExecutionException e) {
        Log.e(TAG, "Invalid statistics definition: " + e.getMessage());
      }
    });
  }

  /**
   * Creates a new activity to display results.
   *
   * @param groupedStatistics hash map which contains results by group
   */
  private void displayResults(HashMap<String, List<String>> groupedStatistics) {
    Intent intent = new Intent(this, ResultsActivity.class);
    intent.putExtra("results", groupedStatistics);
    startActivity(intent);
  }

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

  private void createRecyclerViews() {
    // field type recycler view
    mStatisticDefinitionsAsStringsList = new ArrayList<>();
    mStatisticsDefinitionRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mStatisticsDefinitionAdapter = new RecyclerViewAdapter(this, mStatisticDefinitionsAsStringsList);
    mStatisticsDefinitionRecyclerView.setAdapter(mStatisticsDefinitionAdapter);

    // group field recycler view which just takes the full list of of field names
    mGroupRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mGroupAdapter = new RecyclerViewAdapterCheckBox(this, mFieldNameList);
    mGroupRecyclerView.setAdapter(mGroupAdapter);

    // order by field recycler view
    mOrderByList = new ArrayList<>();
    mOrderByRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mOrderByAdapter = new RecyclerViewAdapter(this, mOrderByList);
    mOrderByRecyclerView.setAdapter(mOrderByAdapter);
  }

  /**
   * Helper method to get the sort order from a string.
   *
   * @param fieldAndOrder string from recycler view
   * @return SortOrder (either ASCENDING or DESCENDING)
   */
  private QueryParameters.SortOrder getSortOrder(String fieldAndOrder) {
    String orderString = fieldAndOrder.substring(fieldAndOrder.indexOf("(") + 1, fieldAndOrder.indexOf(")"));
    QueryParameters.SortOrder sortOrder;
    if (orderString.equals("DESCENDING")) {
      sortOrder = QueryParameters.SortOrder.DESCENDING;
    } else if (orderString.equals("ASCENDING")) {
      sortOrder = QueryParameters.SortOrder.ASCENDING;
    } else {
      Log.e(TAG, "Invalid sort order: " + orderString);
      sortOrder = null;
    }
    return sortOrder;
  }

  /**
   * Helper method to get a field from a string.
   *
   * @param fieldAndOrder string from recycler view
   * @return field as a string
   */
  private String getField(String fieldAndOrder) {
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
