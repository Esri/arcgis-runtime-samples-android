package com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers

import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment

data class GardenSection(
    val arcGISFeature: ArcGISFeature,
    val title: String,
    val description: String,
    val attachments: MutableList<Attachment>
)
