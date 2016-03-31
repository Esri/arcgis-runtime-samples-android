package com.esri.arcgisruntime.sample.featurelayerupdateattributes;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DamageTypesListActivity extends ListActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    setContentView(R.layout.damage_types);

    setListAdapter(new ArrayAdapter<>(this, R.layout.damage_types, getResources().getStringArray(R.array.damage_types)));

    ListView listView = getListView();
    listView.setTextFilterEnabled(true);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view,
          int position, long id) {
        // When clicked, show a toast with the TextView text
        Toast.makeText(getApplicationContext(),
            ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
      }
    });

  }
}
