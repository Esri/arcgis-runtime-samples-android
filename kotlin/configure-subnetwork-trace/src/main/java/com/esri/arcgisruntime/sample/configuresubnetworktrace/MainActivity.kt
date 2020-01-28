package com.esri.arcgisruntime.sample.configuresubnetworktrace

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.CodedValue
import com.esri.arcgisruntime.data.CodedValueDomain
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

    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    exampleTextView.movementMethod = ScrollingMovementMethod()

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
          onComparisonSourceChanged(sources[sourceSpinner.selectedItemPosition])


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
      val assetGroup = networkSource.getAssetGroup("Circuit Breaker")
      val assetType = assetGroup.getAssetType("Three Phase")
      val globalId = java.util.UUID.fromString("1CAF7740-0BF4-4113-8DB2-654E18800028")
      // utility element to start the trace from
      startingLocation = utilityNetwork.createElement(assetType, globalId)
      startingLocation.terminal =
        startingLocation.assetType.terminalConfiguration.terminals.first { it.name == "Load" }

      // get a default trace configuration from a tier to update the UI
      val domainNetwork = utilityNetwork.definition.getDomainNetwork("ElectricDistribution")
      sourceTier = domainNetwork.getTier("Medium Voltage Radial")

      (sourceTier.traceConfiguration.traversability.barriers as? UtilityTraceConditionalExpression)?.let {
        expressionTextView.text = expressionToString(it)
        initialExpression = it
      }

      // set the traversability scope
      sourceTier.traceConfiguration.traversability.scope = UtilityTraversabilityScope.JUNCTIONS
    }
  }

  /**
   * When a comparison source is chosen which doesn
   */
  private fun onComparisonSourceChanged(attribute: UtilityNetworkAttribute) {
    // if the domain is a coded value domain
    (attribute.domain as? CodedValueDomain)?.let { codedValueDomain ->
      // update the list of coded values
      values = codedValueDomain.codedValues
      // show the values spinner
      setVisible(valuesBackgroundView.id)
      // update the values spinner adapter
      valuesSpinner.adapter = ArrayAdapter<String>(
        applicationContext,
        android.R.layout.simple_spinner_item,
        // add the the coded values from the coded value domain to the values spinner
        codedValueDomain.codedValues.map { it.name }
      )
      // if the domain is not a coded value domain
    } ?: kotlin.run {
      when (attribute.dataType) {
        UtilityNetworkAttribute.DataType.BOOLEAN -> {
          setVisible(valueBooleanButton.id)
        }
        UtilityNetworkAttribute.DataType.DOUBLE, UtilityNetworkAttribute.DataType.FLOAT -> {
          // show the edit text and only allow numbers (decimals allowed)
          valuesEditText.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
          setVisible(valuesEditText.id)
        }
        UtilityNetworkAttribute.DataType.INTEGER -> {
          // show the edit text only allowing for integer input
          valuesEditText.inputType = InputType.TYPE_CLASS_NUMBER
          setVisible(valuesEditText.id)
        }

      }
    }
  }

  private fun setVisible(id: Int) {
    when (id) {
      valuesBackgroundView.id -> {
        valuesBackgroundView.visibility = View.VISIBLE
        valueBooleanButton.visibility = View.INVISIBLE
        valuesEditText.visibility = View.INVISIBLE
      }
      valuesEditText.id -> {
        valuesEditText.visibility = View.VISIBLE
        valueBooleanButton.visibility = View.INVISIBLE
        valuesBackgroundView.visibility = View.INVISIBLE
      }
      valueBooleanButton.id -> {
        valueBooleanButton.visibility = View.VISIBLE
        valuesBackgroundView.visibility = View.INVISIBLE
        valuesEditText.visibility = View.INVISIBLE
      }
    }
  }

  fun addCondition(view: View) {
    // if source tier doesn't contain a trace configuration, create one
    val traceConfiguration = sourceTier.traceConfiguration ?: UtilityTraceConfiguration().apply {
      // if the trace configuration doesn't contain traversability, create one
      traversability ?: UtilityTraversability()
    }

    // NOTE: You may also create a UtilityCategoryComparison with UtilityNetworkDefinition.Categories and UtilityCategoryComparisonOperator
    (sources[sourceSpinner.selectedItemPosition] as? UtilityNetworkAttribute)?.let { attribute ->
      (operators[operatorSpinner.selectedItemPosition] as? UtilityAttributeComparisonOperator)?.let { attributeOperator ->
        // NOTE: You may also create a UtilityCategoryComparison with UtilityNetworkDefinition.Categories and UtilityCategoryComparisonOperator
        val otherValue =
          if (attribute.domain is CodedValueDomain) {
            convertToDataType(values[valuesSpinner.selectedItemPosition].code, attribute.dataType)
          } else {
            convertToDataType(valuesEditText.text, attribute.dataType)
          }

        // NOTE: You may also create a UtilityNetworkAttributeComparison with another NetworkAttribute
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
        expressionTextView.text = expressionToString(expression)
      }
    }
  }

  fun trace(view: View) {
    // don't attempt a trace on an unloaded utility network
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
        try {
          val results = traceFuture.get()
          (results.firstOrNull() as? UtilityElementTraceResult)?.let { elementResult ->
            // create an alert dialog
            AlertDialog.Builder(this).apply {
              // set the alert dialog title
              setTitle("Trace result")
              // show the element result count
              setMessage(elementResult.elements.count().toString() + " elements found.")
            }.show()
          }
        } catch (e: Exception) {
          Toast.makeText(
            this,
            e.cause?.message + "\nFor a working barrier condition, try \"Transformer Load\" Equal \"15\".",
            Toast.LENGTH_LONG
          ).show()
        }
      }
    } catch (exception: Exception) {
      Toast.makeText(this, "Error during trace operation: " + exception.message, Toast.LENGTH_LONG)
        .show()
    }
  }

  private fun expressionToString(expression: UtilityTraceConditionalExpression): String? {
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
            convertToDataType(it.code, expression.networkAttribute.dataType) == convertToDataType(
              expression.value,
              expression.networkAttribute.dataType
            )
          }.name
          return expression.networkAttribute.name + " " + expression.comparisonOperator + " " + codedValueDomainName
        }
          ?: return expression.networkAttribute.name + " " + expression.comparisonOperator + " " + (expression.otherNetworkAttribute?.name
            ?: expression.value)
      }
      // when the expression is an utility trace AND condition
      is UtilityTraceAndCondition -> {
        return expressionToString(expression.leftExpression) + " AND\n" + expressionToString(
          expression.rightExpression
        )
      }
      // when the expression is an utility trace OR condition
      is UtilityTraceOrCondition -> {
        return expressionToString(expression.leftExpression) + " OR\n" + expressionToString(
          expression.rightExpression
        )
      }
      else -> {
        return null
      }
    }
  }

  fun reset(view: View) {
    val traceConfiguration = sourceTier.traceConfiguration as UtilityTraceConfiguration
    traceConfiguration.traversability.barriers = initialExpression
    expressionTextView.text = expressionToString(initialExpression)
  }

  private fun convertToDataType(otherValue: Any, dataType: UtilityNetworkAttribute.DataType): Any {
    return try {
      when (dataType) {
        UtilityNetworkAttribute.DataType.BOOLEAN -> otherValue.toString().toBoolean()
        UtilityNetworkAttribute.DataType.DOUBLE -> otherValue.toString().toDouble()
        UtilityNetworkAttribute.DataType.FLOAT -> otherValue.toString().toFloat()
        UtilityNetworkAttribute.DataType.INTEGER -> otherValue.toString().toInt()
      }
    } catch (e: Exception) {
      Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
    }
  }
}
