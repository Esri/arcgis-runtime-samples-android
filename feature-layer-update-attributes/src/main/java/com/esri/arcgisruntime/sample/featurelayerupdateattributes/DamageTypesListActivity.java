package com.esri.arcgisruntime.sample.featurelayerupdateattributes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DamageTypesListActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.damage_types_listview);
    final String[] damageTypes = getResources().getStringArray(R.array.damage_types);

    ListView listView = (ListView) findViewById(R.id.listview);

    listView.setAdapter(new ArrayAdapter<>(this, R.layout.damage_types, damageTypes));

    listView.setTextFilterEnabled(true);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view,
          int position, long id) {
        // When clicked, show a toast with the TextView text
        Toast.makeText(getApplicationContext(),
            ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent();
        myIntent.putExtra("typdamage", damageTypes[position]); //Optional parameters
        setResult(100, myIntent);
        finish();
      }
    });

  }

  @Override
  public void onBackPressed() {
  }
}
