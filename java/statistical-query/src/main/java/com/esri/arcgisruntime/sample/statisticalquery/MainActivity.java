package com.esri.arcgisruntime.sample.statisticalquery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
  private RecyclerView mFieldTypeRecyclerView;
  private RecyclerView mGroupFieldRecyclerView;
  private RecyclerView mOrderByFieldRecyclerView;
  private RecyclerViewAdapter mFieldTypeAdapter;
  private RecyclerViewAdapter mGroupFieldAdapter;
  private RecyclerViewAdapter mOrderByFieldAdapter;

  private List<StatisticDefinition> mStatisticDefinitionList;
  private List<String> mFieldTypeList;
  private List<String> mGroupFieldList;
  private List<String> mOrderByFieldList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    inflateUiViews();

    createRecyclerViews();

    // create US states feature table
    mUsStatesFeatureTable = new ServiceFeatureTable(getString(R.string.us_states_census));

    // Collection of (user-defined) statistics to use in the query
    mStatisticDefinitionList = new ArrayList<>();

    // load the table
    mUsStatesFeatureTable.loadAsync();

    mUsStatesFeatureTable.addDoneLoadingListener(() -> {

      // fill array with field names
      List<String> fieldNameList = new ArrayList<>();
      for (int i = 0; i < mUsStatesFeatureTable.getFields().size(); i++) {
        String fieldName = mUsStatesFeatureTable.getFields().get(i).getName();
        fieldNameList.add(fieldName);
      }

      // fill the field spinner with field names
      mFieldSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fieldNameList));

      // fill the type spinner with StatisticType values
      mTypeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, StatisticType.values()));

      // fill group field recycler view with field names
      mGroupFieldList.addAll(fieldNameList);
      mGroupFieldAdapter.notifyDataSetChanged();

      mAddButton.setOnClickListener(v -> {
        String fieldName = mFieldSpinner.getSelectedItem().toString();
        StatisticType statType = StatisticType.valueOf(mTypeSpinner.getSelectedItem().toString());
        mFieldTypeList.add(fieldName + " (" + statType + ")");
        mFieldTypeAdapter.notifyItemInserted(mFieldTypeList.size() - 1);
        StatisticDefinition statDefinition = new StatisticDefinition(fieldName, statType);
        mStatisticDefinitionList.add(statDefinition);
      });

      mRemoveStatisticButton.setOnClickListener(v -> {
        int position = mFieldTypeAdapter.getSelectedPosition();

        mStatisticDefinitionList.remove(position);

        // remove field and type from field type recycler view
        mFieldTypeList.remove(position);
        mFieldTypeAdapter.notifyItemRemoved(position);
      });

      mMoveRightButton.setOnClickListener(view -> {
        int position = mGroupFieldAdapter.getSelectedPosition();

        // add field to order by field recycler view with a sort order of ASCENDING
        mOrderByFieldList.add(mGroupFieldAdapter.getItem(position) + " (" + QueryParameters.SortOrder.ASCENDING + ")");
        mOrderByFieldAdapter.notifyItemInserted(mOrderByFieldList.size() - 1);

        // remove the field from group field recycler view
        //mGroupFieldList.remove(position);
        //mGroupFieldAdapter.notifyItemRemoved(position);
      });

      mMoveLeftButton.setOnClickListener(view -> {
        int position = mOrderByFieldAdapter.getSelectedPosition();

        // strip out (ASCENDING) or (DESCENDING) from string
        String fieldAndOrder = mOrderByFieldAdapter.getItem(position);
        String field = getField(fieldAndOrder);

        // get field's original index to insert back into the correct index of the recycler view
        int fieldIndex = fieldNameList.indexOf(field);

        // add the field back in to groupFieldRecyclerView at correct index
        mGroupFieldList.add(fieldIndex, field);
        mGroupFieldAdapter.notifyItemInserted(fieldIndex);

        // remove field from orderByFieldRecyclerView
        mOrderByFieldList.remove(position);
        mOrderByFieldAdapter.notifyItemRemoved(position);
      });

      mChangeSortOrder.setOnClickListener(view -> {
        int position = mOrderByFieldAdapter.getSelectedPosition();
        if (position >= 0) {
          String fieldAndOrder = mOrderByFieldAdapter.getItem(position);
          mOrderByFieldList.remove(position);
          String field = getField(fieldAndOrder);
          QueryParameters.SortOrder sortOrder = getSortOrder(fieldAndOrder);
          if (sortOrder == QueryParameters.SortOrder.ASCENDING) {
            mOrderByFieldList.add(position, field + " (" + QueryParameters.SortOrder.DESCENDING + ")");
          } else {
            mOrderByFieldList.add(position, field + " (" + QueryParameters.SortOrder.ASCENDING + ")");
          }
          mOrderByFieldAdapter.notifyItemChanged(position);
        } else {
          Toast.makeText(MainActivity.this, "Please select a field with sort order.", Toast.LENGTH_LONG).show();
        }
      });

      mGetStatisticsButton.setOnClickListener(view -> {
        executeStatisticsQuery();
      });
    });
  }

  private void executeStatisticsQuery() {
    // verify that there is at least one statistic definition
    if (mStatisticDefinitionList.size() == 0) {
      Toast.makeText(MainActivity.this, "Please define at least one statistic for the query.", Toast.LENGTH_LONG)
          .show();
      return;
    }

    // create the statistics query parameters, pass in the list of statistic definitions
    StatisticsQueryParameters statQueryParams = new StatisticsQueryParameters(mStatisticDefinitionList);
    Log.d("statsDefField", mStatisticDefinitionList.get(0).getFieldName());
    Log.d("statsDefType", mStatisticDefinitionList.get(0).getStatisticType().toString());

    // Specify the group fields (if any)
    for (String groupField : mGroupFieldList) {
      statQueryParams.getGroupByFieldNames().add(groupField);
    }
    // Specify the fields to order by (if any)
    for (String fieldAndSortOrder : mOrderByFieldList) {

      // create a new OrderBy object to define the sort for the selected field
      QueryParameters.OrderBy orderBy = new QueryParameters.OrderBy(getField(fieldAndSortOrder),
          getSortOrder(fieldAndSortOrder));

      Log.d("queryOrderByField", orderBy.getFieldName());
      Log.d("queryOrderBySort", orderBy.getSortOrder().toString());

      statQueryParams.getOrderByFields().add(orderBy);

    }
    // execute the statistical query with these parameters and await the results
    ListenableFuture<StatisticsQueryResult> statisticsQueryResultFuture = mUsStatesFeatureTable.queryStatisticsAsync(statQueryParams);

    Log.d("groupBy", statQueryParams.getGroupByFieldNames().get(0));
    //      Log.d("orderBy", statQueryParams.getOrderByFields().get(0).getFieldName());
    Log.d("statsDefQuery", statQueryParams.getStatisticDefinitions().get(0).getFieldName());

    statisticsQueryResultFuture.addDoneListener(() -> {
      Log.d(TAG, "stats query result future returned");
      try {
        StatisticsQueryResult statisticsQueryResult = statisticsQueryResultFuture.get();

        for (Iterator<StatisticRecord> stats = statisticsQueryResult.iterator(); stats.hasNext();) {
          //for (Map.Entry<String, Object> entry : stats.next().getStatistics().entrySet()) {
          //  Log.d("statsEntry", entry.getKey() + " " + entry.getValue());
          //}
          for (Map.Entry<String, Object> entry : stats.next().getGroup().entrySet()) {
            Log.d("groupEntry", entry.getKey() + " " + entry.getValue());
          }
        }


      } catch (InterruptedException | ExecutionException e) {
        Log.e(TAG, "Invalid statistics definition: " + e.getMessage());
      }
      // format the output, and display results in the tree view

    });

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
    mFieldTypeRecyclerView = findViewById(R.id.fieldTypeRecyclerView);
    mGroupFieldRecyclerView = findViewById(R.id.groupFieldRecyclerView);
    mOrderByFieldRecyclerView = findViewById(R.id.orderFieldRecyclerView);
  }

  private void createRecyclerViews() {
    // field type recycler view
    mFieldTypeList = new ArrayList<>();
    mFieldTypeRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mFieldTypeAdapter = new RecyclerViewAdapter(this, mFieldTypeList);
    mFieldTypeRecyclerView.setAdapter(mFieldTypeAdapter);

    // group field recycler view
    mGroupFieldList = new ArrayList<>();
    mGroupFieldRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mGroupFieldAdapter = new RecyclerViewAdapter(this, mGroupFieldList);
    mGroupFieldRecyclerView.setAdapter(mGroupFieldAdapter);

    // order by field recycler view
    mOrderByFieldList = new ArrayList<>();
    mOrderByFieldRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mOrderByFieldAdapter = new RecyclerViewAdapter(this, mOrderByFieldList);
    mOrderByFieldRecyclerView.setAdapter(mOrderByFieldAdapter);
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
}
