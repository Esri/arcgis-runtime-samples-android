package com.esri.arcgisruntime.sample.spatialrelationships;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);
    setContentView(R.layout.results_expandable_list);

    // get intent from main activity
    Intent intent = getIntent();

    LinkedHashMap<String,List<String>> child = new LinkedHashMap<>();
    ArrayList<String> header = new ArrayList<>();
    header.add("Point");
    header.add("Polyline");
    header.add("Polygon");
    ArrayList<String> relationships = new ArrayList<>();
    relationships.add("me");
    relationships.add("you");
    relationships.add("lol");
    child.put(header.get(0),relationships) ;
    child.put(header.get(1),relationships);
    child.put(header.get(2),relationships);

    ExpandableListView expandableListView = findViewById(R.id.expandableList);
    ResultsExpandableListAdapter expandableListAdapter = new ResultsExpandableListAdapter(this,header,child);
    expandableListView.setAdapter(expandableListAdapter);
    GeometryType selectedGeometryType = (GeometryType) intent.getSerializableExtra("selectedGeometryType");

    if(selectedGeometryType != GeometryType.POINT){
//      getSpatialRelationships(selectedGeometryType,)
    }


  }
  /**
   * Gets a list of spatial relationships that the first geometry has to the second geometry.
   *
   * @param a first geometry
   * @param b second geometry
   * @return list of relationships a has to b
   */
  private List<QueryParameters.SpatialRelationship> getSpatialRelationships(Geometry a, Geometry b) {
    List<QueryParameters.SpatialRelationship> relationships = new ArrayList<>();
    if (GeometryEngine.crosses(a, b)) relationships.add(QueryParameters.SpatialRelationship.CROSSES);
    if (GeometryEngine.contains(a, b)) relationships.add(QueryParameters.SpatialRelationship.CONTAINS);
    if (GeometryEngine.disjoint(a, b)) relationships.add(QueryParameters.SpatialRelationship.DISJOINT);
    if (GeometryEngine.intersects(a, b)) relationships.add(QueryParameters.SpatialRelationship.INTERSECTS);
    if (GeometryEngine.overlaps(a, b)) relationships.add(QueryParameters.SpatialRelationship.OVERLAPS);
    if (GeometryEngine.touches(a, b)) relationships.add(QueryParameters.SpatialRelationship.TOUCHES);
    if (GeometryEngine.within(a, b)) relationships.add(QueryParameters.SpatialRelationship.WITHIN);
    return relationships;
  }
}
