package com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers

import com.esri.arcgisruntime.data.ArcGISFeature

data class GardenSection(
    val arcGISFeature: ArcGISFeature,
    val title: String,
    val description: String,
    val imageURI: String
)
