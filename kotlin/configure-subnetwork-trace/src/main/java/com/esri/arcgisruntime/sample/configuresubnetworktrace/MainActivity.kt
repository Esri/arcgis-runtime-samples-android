package com.esri.arcgisruntime.sample.configuresubnetworktrace

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.data.CodedValue
import com.esri.arcgisruntime.data.CodedValueDomain
import com.esri.arcgisruntime.data.Domain
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.utilitynetworks.UtilityAttributeComparisonOperator
import com.esri.arcgisruntime.utilitynetworks.UtilityCategoryComparison
import com.esri.arcgisruntime.utilitynetworks.UtilityElement
import com.esri.arcgisruntime.utilitynetworks.UtilityElementTraceResult
import com.esri.arcgisruntime.utilitynetworks.UtilityNetwork
import com.esri.arcgisruntime.utilitynetworks.UtilityNetworkAttribute
import com.esri.arcgisruntime.utilitynetworks.UtilityNetworkAttributeComparison
import com.esri.arcgisruntime.utilitynetworks.UtilityTier
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceAndCondition
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceConditionalExpression
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceConfiguration
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceOrCondition
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceParameters
import com.esri.arcgisruntime.utilitynetworks.UtilityTraceType
import com.esri.arcgisruntime.utilitynetworks.UtilityTraversability
import com.esri.arcgisruntime.utilitynetworks.UtilityTraversabilityScope
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {

  private lateinit var initialExpression: UtilityTraceConditionalExpression

  private lateinit var sourceTier: UtilityTier

  private lateinit var sources: List<UtilityNetworkAttribute>

  private lateinit var operators: Array<UtilityAttributeComparisonOperator>

  private lateinit var startingLocation: UtilityElement
  private val utilityNetwork by lazy { UtilityNetwork("https://sampleserver7.arcgisonline.com/arcgis/rest/services/UtilityNetwork/NapervilleElectric/FeatureServer") }
  private lateinit var values: List<CodedValue>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)


    // create a utility network and wait for it to finish to load
    utilityNetwork.loadAsync()
    utilityNetwork.addDoneLoadingListener {


      // populate spinners for network attribute comparison
      sources =
        utilityNetwork.definition.networkAttributes.filter { !it.isSystemDefined }
      sourceSpinner.adapter = ArrayAdapter<String>(
        applicationContext,
        android.R.layout.simple_spinner_item,
        sources.map { it.name })
      sourceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          (sources[sourceSpinner.selectedItemPosition].domain)?.run {
            onComparisonSourceChanged(sources[sourceSpinner.selectedItemPosition].domain)
          } ?: run {
            valuesSpinner.visibility = android.view.View.GONE
          }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
      }

      operators = UtilityAttributeComparisonOperator.values()
      operatorSpinner.adapter = ArrayAdapter<String>(
        applicationContext,
        android.R.layout.simple_spinner_item,
        operators.map { it.name })

      // create a default starting location
      val networkSource = utilityNetwork.definition.getNetworkSource("Electric Distribution Device")
      val assetGroup = networkSource.getAssetGroup("Service Point")
      val assetType = assetGroup.getAssetType("Three Phase Low Voltage Meter")
      val globalId = java.util.UUID.fromString("3AEC2649-D867-4EA7-965F-DBFE1F64B090")
      startingLocation = utilityNetwork.createElement(assetType, globalId)

      // get a default trace configuration from a tier to update the UI
      val domainNetwork = utilityNetwork.definition.getDomainNetwork("ElectricDistribution")
      sourceTier = domainNetwork.getTier("Medium Voltage Radial")

      (sourceTier.traceConfiguration.traversability.barriers as? UtilityTraceConditionalExpression)?.let {
        expressionTextView.text = getExpression(it)
        initialExpression = it
      }

      // set the traversability scope
      sourceTier.traceConfiguration.traversability.scope = UtilityTraversabilityScope.JUNCTIONS
    }
  }

  /**
   * When a comparison source is chosen which doesn
   */
  private fun onComparisonSourceChanged(domain: Domain) {
    // if the domain is a coded value domain
    (domain as? CodedValueDomain)?.let { codedValueDomain ->
      // update the list of coded values
      values = codedValueDomain.codedValues
      // show the values spinner
      valuesSpinner.visibility = View.VISIBLE
      // update the values spinner adapter
      valuesSpinner.adapter = ArrayAdapter<String>(
        applicationContext,
        android.R.layout.simple_spinner_item,
        // add the the coded values from the coded value domain to the values spinner
        codedValueDomain.codedValues.map { it.name }
      )
      // if the domain is not a coded value domain
    } ?: kotlin.run {
      // hide the values spinner
      valuesSpinner.visibility = View.GONE
    }
  }

  fun addCondition(view: View) {
    // if source tier doesn't contain a trace configuration, create one
    val traceConfiguration = sourceTier.traceConfiguration ?: UtilityTraceConfiguration().apply {
      // if the trace configuration doesn't contain traversability, create one
      traversability ?: UtilityTraversability()
    }

    // NOTE: You may also create a UtilityCategoryComparison with UtilityNetworkDefinition.Categories and UtilityCategoryComparisonOperator.
    (sources[sourceSpinner.selectedItemPosition] as? UtilityNetworkAttribute)?.let { attribute ->
      (operators[operatorSpinner.selectedItemPosition] as? UtilityAttributeComparisonOperator)?.let { attributeOperator ->
        (values[valuesSpinner.selectedItemPosition] as? CodedValue)?.let { codedValue ->
          val otherValue = if (attribute.domain is CodedValueDomain) {
            convertToDataType(codedValue.code, attribute.dataType)
          } else {
            convertToDataType(attribute, attribute.dataType)
          }

          // NOTE: You may also create a UtilityNetworkAttributeComparison with another NetworkAttribute.
          var expression: UtilityTraceConditionalExpression = UtilityNetworkAttributeComparison(
            attribute,
            attributeOperator,
            otherValue
          )
          (traceConfiguration.traversability.barriers as? UtilityTraceConditionalExpression)?.let { otherExpression ->
            // NOTE: You may also combine expressions with UtilityTraceAndCondition
            expression = UtilityTraceOrCondition(otherExpression, expression)
          }
          traceConfiguration.traversability.barriers = expression
          expressionTextView.text = getExpression(expression)
        }
      }
    }
  }

  fun trace(view: View) {
    if (utilityNetwork.loadStatus != LoadStatus.LOADED) {
      return
    }
    try {
      val parameters =
        UtilityTraceParameters(UtilityTraceType.SUBNETWORK, listOf(startingLocation)).apply {
          if (sourceTier.traceConfiguration is UtilityTraceConfiguration) {
            traceConfiguration = sourceTier.traceConfiguration
          }
        }
      val traceFuture = utilityNetwork.traceAsync(parameters)
      traceFuture.addDoneListener {

        val results = traceFuture.get()
        (results.firstOrNull() as? UtilityElementTraceResult)?.let { elementResult ->
          alert(elementResult.elements.count().toString() + " elements found.", "Trace result").show()
        } ?: alert("For a working barrier condition, try \"Transformer Load\" Equal \"15\".").show()
      }
    } catch (exception: ArcGISRuntimeException) {
      toast("Error during trace operation: " + exception.message)
    }
  }

  private fun getExpression(expression: UtilityTraceConditionalExpression): String? {
    when (expression) {
      // when the expression is a category comparison expression
      is UtilityCategoryComparison -> {
        return expression.category.name + " " + expression.comparisonOperator
      }
      // when the expression is an attribute comparison expression
      is UtilityNetworkAttributeComparison -> {
        (expression.networkAttribute.domain as? CodedValueDomain)?.let { codedValueDomain ->
          // if there's a coded value domain name
          val codedValueDomainName = codedValueDomain.codedValues.first {
            convertToDataType(it.code, expression.networkAttribute.dataType) == convertToDataType(expression.value, expression.networkAttribute.dataType)
          }.name
          return expression.networkAttribute.name + " " + expression.comparisonOperator + " " + codedValueDomainName
        }
          ?: return expression.networkAttribute.name + " " + expression.comparisonOperator + " " + expression.otherNetworkAttribute.name
      }
      // when the expression is an utility trace AND condition
      is UtilityTraceAndCondition -> {
        return getExpression(expression.leftExpression) + " AND\n" + getExpression(expression.rightExpression)
      }
      // when the expression is an utility trace OR condition
      is UtilityTraceOrCondition -> {
        return getExpression(expression.leftExpression) + " OR\n" + getExpression(expression.rightExpression)
      }
      else -> {
        return null
      }
    }
  }

  fun reset(view: View) {
    val traceConfiguration = sourceTier.traceConfiguration as UtilityTraceConfiguration
    traceConfiguration.traversability.barriers = initialExpression
    expressionTextView.text = getExpression(initialExpression)
  }

  private fun convertToDataType(otherValue: Any, dataType: UtilityNetworkAttribute.DataType): Any {
    return when (dataType) {
      UtilityNetworkAttribute.DataType.BOOLEAN -> otherValue.toString().toBoolean()
      UtilityNetworkAttribute.DataType.DOUBLE -> otherValue.toString().toDouble()
      UtilityNetworkAttribute.DataType.FLOAT -> otherValue.toString().toFloat()
      UtilityNetworkAttribute.DataType.INTEGER -> otherValue.toString().toInt()
    }
  }
}
