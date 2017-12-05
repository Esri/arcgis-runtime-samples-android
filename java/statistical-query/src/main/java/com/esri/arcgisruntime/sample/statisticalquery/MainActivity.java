package com.esri.arcgisruntime.sample.statisticalquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.data.StatisticDefinition;
import com.esri.arcgisruntime.data.StatisticType;
import com.esri.arcgisruntime.data.StatisticsQueryParameters;

public class MainActivity extends AppCompatActivity {

  private ServiceFeatureTable mStatesFeatureTable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate UI elements
    Button addButton = findViewById(R.id.addButton);
    Button removeStatisticButton = findViewById(R.id.removeStatisticButton);
    Button moveRightButton = findViewById(R.id.moveRightButton);
    Button moveLeftButton = findViewById(R.id.moveLeftButton);
    Button getStatisticsButton = findViewById(R.id.getStatisticsButton);
    Spinner fieldSpinner = findViewById(R.id.fieldSpinner);
    Spinner typeSpinner = findViewById(R.id.typeSpinner);
    RecyclerView fieldTypeRecyclerView = findViewById(R.id.fieldTypeRecyclerView);
    RecyclerView groupFieldRecyclerView = findViewById(R.id.groupFieldRecyclerView);
    RecyclerView orderByFieldRecyclerView = findViewById(R.id.orderFieldRecyclerView);

    // field type recycler view
    List<String> fieldTypeRecyclerViewList = new ArrayList<>();
    fieldTypeRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    RecyclerViewAdapter fieldTypeRecyclerViewAdapter = new RecyclerViewAdapter(this, fieldTypeRecyclerViewList);
    fieldTypeRecyclerView.setAdapter(fieldTypeRecyclerViewAdapter);

    // group field recycler view
    List<String> groupFieldRecyclerViewList = new ArrayList<>();
    groupFieldRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    RecyclerViewAdapter groupFieldRecyclerViewAdapter = new RecyclerViewAdapter(this, groupFieldRecyclerViewList);
    groupFieldRecyclerView.setAdapter(groupFieldRecyclerViewAdapter);

    // order by field recycler view
    List<String> orderByFieldRecyclerViewList = new ArrayList<>();
    orderByFieldRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    RecyclerViewAdapter orderByFieldRecyclerViewAdapter = new RecyclerViewAdapter(this, orderByFieldRecyclerViewList);
    orderByFieldRecyclerView.setAdapter(orderByFieldRecyclerViewAdapter);

    // create US states feature table
    mStatesFeatureTable = new ServiceFeatureTable(getString(R.string.us_states_census));

    // Collection of (user-defined) statistics to use in the query
    List<StatisticDefinition> statisticDefinitionsList = new ArrayList<>();

    // load the table
    mStatesFeatureTable.loadAsync();

    mStatesFeatureTable.addDoneLoadingListener(() -> {

      // fill array with field names
      List<String> fieldsArrayAsStrings = new ArrayList<>();
      Map<String, Integer> fieldMap = new HashMap<>();
      for (int i = 0; i < mStatesFeatureTable.getFields().size(); i++) {
        String fieldAsString = mStatesFeatureTable.getFields().get(i).getName();
        fieldsArrayAsStrings.add(fieldAsString);
        fieldMap.put(fieldAsString, i);
      }

      // fill the field spinner with field names
      fieldSpinner
          .setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fieldMap.keySet().toArray()));

      // fill the type spinner with StatisticType values
      typeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, StatisticType.values()));

      // fill group field recycler view with field names
      groupFieldRecyclerViewList.addAll(fieldsArrayAsStrings);
      groupFieldRecyclerViewAdapter.notifyDataSetChanged();

      // on add button click
      addButton.setOnClickListener(v -> {
        String fieldName = fieldSpinner.getSelectedItem().toString();
        StatisticType statType = (StatisticType) typeSpinner.getSelectedItem();
        fieldTypeRecyclerViewList.add(fieldName + " (" + statType + ")");
        fieldTypeRecyclerViewAdapter.notifyItemInserted(fieldTypeRecyclerViewList.size() - 1);
        StatisticDefinition statDefinition = new StatisticDefinition(fieldName, statType,
            fieldName + "_" + statType.toString());
        statisticDefinitionsList.add(statDefinition);
      });

      // on remove statistic button click
      removeStatisticButton.setOnClickListener(v -> {
        fieldTypeRecyclerViewList.remove(fieldTypeRecyclerViewAdapter.getSelectedPosition());
        fieldTypeRecyclerViewAdapter.notifyItemRemoved(fieldTypeRecyclerViewAdapter.getSelectedPosition());
      });

      moveRightButton.setOnClickListener(view -> {
        orderByFieldRecyclerViewList
            .add(groupFieldRecyclerViewAdapter.getItem(groupFieldRecyclerViewAdapter.getSelectedPosition()) + " (" +
                QueryParameters.SortOrder.ASCENDING + ")");
        orderByFieldRecyclerViewAdapter.notifyItemInserted(orderByFieldRecyclerViewList.size() - 1);
        groupFieldRecyclerViewList.remove(groupFieldRecyclerViewAdapter.getSelectedPosition());
        groupFieldRecyclerViewAdapter.notifyItemRemoved(groupFieldRecyclerViewAdapter.getSelectedPosition());
      });

      moveLeftButton.setOnClickListener(view -> {

        // strip out (ASCENDING) or (DESCENDING) from orderByFieldRecyclerView
        String stringWithOrdering = orderByFieldRecyclerViewAdapter
            .getItem(orderByFieldRecyclerViewAdapter.getSelectedPosition());
        String field = stringWithOrdering.substring(0, stringWithOrdering.indexOf(" "));

        // get the field's associated index to insert back into the correct location in groupFieldRecyclerView
        int fieldIndex = fieldMap.get(field);

        // add the field back in to groupFieldRecyclerView at correct index
        groupFieldRecyclerViewList.add(fieldIndex, field);
        groupFieldRecyclerViewAdapter.notifyItemInserted(fieldIndex);

        // remove field from orderByFieldRecyclerView
        orderByFieldRecyclerViewList.remove(orderByFieldRecyclerViewAdapter.getSelectedPosition());
        orderByFieldRecyclerViewAdapter.notifyItemRemoved(orderByFieldRecyclerViewAdapter.getSelectedPosition());
      });
    });

    getStatisticsButton.setOnClickListener(view -> {
      // verify that there is at least one statistic definition
      if (statisticDefinitionsList.size() > 0) {
        // create the statistics query parameters, pass in the list of statistic definitions
        StatisticsQueryParameters statQueryParams = new StatisticsQueryParameters(statisticDefinitionsList);

        // Specify the group fields (if any)
        for (String groupField : groupFieldRecyclerViewList) {
          statQueryParams.getGroupByFieldNames().add(groupField);
        }

        // Specify the fields to order by (if any)
        for (String fieldAndOrder : orderByFieldRecyclerViewList) {

          statQueryParams.getOrderByFields().add(QueryParameters.OrderBy);
        }
      } else {
        Toast.makeText(MainActivity.this, "Please define at least one statistic for the query.", Toast.LENGTH_LONG)
            .show();
      }
    });

  }

}
