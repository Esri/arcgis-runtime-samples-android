package com.esri.arcgisruntime.sample.statisticalquery;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.data.StatisticType;

public class MainActivity extends AppCompatActivity {

  private ServiceFeatureTable mStatesFeatureTable;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate UI elements
    Button addButton = findViewById(R.id.addButton);
    Button removeStatisticButton = findViewById(R.id.removeStatisticButton);
    Spinner fieldSpinner = findViewById(R.id.fieldSpinner);
    Spinner typeSpinner = findViewById(R.id.typeSpinner);

    // create US states feature table
    mStatesFeatureTable = new ServiceFeatureTable(getString(R.string.us_states_census));

    // load the table
    mStatesFeatureTable.loadAsync();

    List<String> fieldTypeRecyclerViewList = new ArrayList<>();
    RecyclerView fieldTypeRecyclerView = findViewById(R.id.fieldTypeRecyclerView);
    fieldTypeRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(this, fieldTypeRecyclerViewList);
    fieldTypeRecyclerView.setAdapter(recyclerViewAdapter);

    mStatesFeatureTable.addDoneLoadingListener(() -> {

      // fill array with field names
      List<String> fieldsArrayAsStrings = new ArrayList<>();
      for (Field field : mStatesFeatureTable.getFields()) {
        fieldsArrayAsStrings.add(field.getName());
      }

      // fill the field spinner with field names
      fieldSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fieldsArrayAsStrings));

      // fill the type spinner with StatisticType values
      typeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, StatisticType.values()));

      // on add button click
      addButton.setOnClickListener(v -> {
        fieldTypeRecyclerViewList.add(fieldSpinner.getSelectedItem().toString() + " (" + typeSpinner.getSelectedItem().toString() + ")");
        recyclerViewAdapter.notifyItemInserted(fieldTypeRecyclerViewList.size() - 1);
      });

      // on remove statistic button click
      removeStatisticButton.setOnClickListener(v -> {
        fieldTypeRecyclerViewList.remove(recyclerViewAdapter.getSelectedPosition());
        recyclerViewAdapter.notifyItemRemoved(recyclerViewAdapter.getSelectedPosition());
      });
    });
  }

}
