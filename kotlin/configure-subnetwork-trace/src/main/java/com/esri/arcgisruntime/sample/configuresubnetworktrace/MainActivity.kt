package com.esri.arcgisruntime.sample.configuresubnetworktrace

import android.os.Bundle
import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.util.Log
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

  private val TAG: String = MainActivity::class.java.simpleName

  private val utilityNetwork by lazy {
    UtilityNetwork(getString(R.string.naperville_electric_url))
  }

  private var initialExpression: UtilityTraceConditionalExpression? = null
  private var sourceTier: UtilityTier? = null
  private var sources: List<UtilityNetworkAttribute>? = null
  private var operators: Array<UtilityAttributeComparisonOperator>? = null
  private var startingLocation: UtilityElement? = null
  private var values: List<CodedValue>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    exampleTextView.movementMethod = ScrollingMovementMethod()

    // create a utility network and wait for it to finish to load
    utilityNetwork.loadAsync()
    utilityNetwork.addDoneLoadingListener {
      if (utilityNetwork.loadStatus == LoadStatus.LOADED) {
        // create a list of utility network attributes whose system is not defined
        sources = utilityNetwork.definition.networkAttributes.filter { !it.isSystemDefined }
          .also { sources ->

            sourceSpinner.apply {
              // assign an adapter to the spinner with source names
              adapter = ArrayAdapter<String>(
                applicationContext,
                android.R.layout.simple_spinner_item,
                sources.map { it.name })

              // add an on item selected listener which calls on comparison source changed
              onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                  parent: AdapterView<*>?,
                  view: View?,
                  position: Int,
                  id: Long
                ) {
                  (sources[sourceSpinner.selectedItemPosition])
                  onComparisonSourceChanged(sources[sourceSpinner.selectedItemPosition])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
              }
            }
          }

        // create a list of utility attribute comparison operators
        operators = UtilityAttributeComparisonOperator.values().also { operators ->
          // assign operator spinner an adapter of operator names
          operatorSpinner.adapter = ArrayAdapter<String>(
            applicationContext,
            android.R.layout.simple_spinner_item,
            operators.map { it.name })
        }

        // create a default starting location
        val networkSource =
          utilityNetwork.definition.getNetworkSource("Electric Distribution Device")
        val assetGroup = networkSource.getAssetGroup("Circuit Breaker")
        val assetType = assetGroup.getAssetType("Three Phase")
        val globalId = java.util.UUID.fromString("1CAF7740-0BF4-4113-8DB2-654E18800028")

        // utility element to start the trace from
        startingLocation = utilityNetwork.createElement(assetType, globalId).apply {
          terminal = assetType.terminalConfiguration.terminals.first { it.name == "Load" }
        }

        // get a default trace configuration from a tier to update the UI
        val domainNetwork = utilityNetwork.definition.getDomainNetwork("ElectricDistribution")
        sourceTier = domainNetwork.getTier("Medium Voltage Radial")?.apply {
          (traceConfiguration.traversability.barriers as? UtilityTraceConditionalExpression)?.let {
            expressionTextView.text = expressionToString(it)
            initialExpression = it
          }
          // set the traversability scope
          traceConfiguration.traversability.scope = UtilityTraversabilityScope.JUNCTIONS
        }
      } else {
        ("Utility network failed to load!").also {
          Toast.makeText(this, it, Toast.LENGTH_LONG).show()
          Log.e(TAG, it)
        }
      }
    }
  }

  /**
   * When a comparison source attribute is chosen check if it's a coded value domain and, if it is,
   * present a spinner of coded value domains. If not, show the correct UI view for the utility
   * network attribute data type.
   *
   * @param attribute being compared
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
        // add the coded values from the coded value domain to the values spinner
        codedValueDomain.codedValues.map { it.name }
      )
      // if the domain is not a coded value domain
    } ?: when (attribute.dataType) {
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
      else -> {
        ("Unexpected utility network attribute data type.").also {
          Toast.makeText(this, it, Toast.LENGTH_LONG).show()
          Log.e(TAG, it)
        }
      }
    }
  }

  /**
   * Show the given UI view and hide the others which share the same space.
   *
   * @param id of the view to make visible
   */
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

  /**
   * Add a new barrier condition to the trace options.
   *
   * @param view of the add button
   */
  fun addCondition(view: View) {
    // if source tier doesn't contain a trace configuration, create one
    val traceConfiguration = sourceTier?.traceConfiguration ?: UtilityTraceConfiguration().apply {
      // if the trace configuration doesn't contain traversability, create one
      traversability ?: UtilityTraversability()
    }

    // get the currently selected attribute
    val attribute = sources?.get(sourceSpinner.selectedItemPosition)
    attribute?.let {
      // get the currently selected attribute operator
      val attributeOperator = operators?.get(operatorSpinner.selectedItemPosition)
      attributeOperator?.let {
        // if the other value is a coded value domain
        val otherValue = if (attribute.domain is CodedValueDomain) {
          values?.get(valuesSpinner.selectedItemPosition)?.code?.let {
            convertToDataType(it, attribute.dataType)
          }
        } else {
          convertToDataType(valuesEditText.text, attribute.dataType)
        }
        try {
          // NOTE: You may also create a UtilityNetworkAttributeComparison with another
          // NetworkAttribute
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
        } catch (e: Exception) {
          val error =
            "Error creating UtilityNetworkAttributeComparison! Did you forget to input a numeric value? ${e.message}"
          Log.e(TAG, error)
          Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
          return
        }
      }
    }
  }

  /**
   * Run the network trace with the parameters and display the result in an alert dialog.
   *
   * @param view of the trace button
   */
  fun trace(view: View) {
    // don't attempt a trace on an unloaded utility network
    if (utilityNetwork.loadStatus != LoadStatus.LOADED) {
      return
    }
    try {
      val parameters =
        UtilityTraceParameters(UtilityTraceType.SUBNETWORK, listOf(startingLocation)).apply {
          sourceTier?.traceConfiguration?.let {
            traceConfiguration = it
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
          (e.cause?.message + "\nFor a working barrier condition, try \"Transformer Load\" Equal \"15\".").also {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            Log.e(TAG, it)
          }
        }
      }
    } catch (e: Exception) {
      ("Error during trace operation: " + e.message).also {
        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        Log.e(TAG, it)
      }
    }
  }

  /**
   * Convert the given UtilityTraceConditionalExpression into a string.
   *
   * @param expression to convert to a string
   */
  private fun expressionToString(expression: UtilityTraceConditionalExpression): String? {
    when (expression) {
      // when the expression is a category comparison expression
      is UtilityCategoryComparison -> {
        return expression.category.name + " " + expression.comparisonOperator
      }
      // when the expression is an attribute comparison expression
      is UtilityNetworkAttributeComparison -> {
        // the name and comparison operator of the expression
        val networkAttributeNameAndOperator =
          expression.networkAttribute.name + " " + expression.comparisonOperator + " "
        // check whether the network attribute has a coded value domain
        (expression.networkAttribute.domain as? CodedValueDomain)?.let { codedValueDomain ->
          // if there's a coded value domain name
          val codedValueDomainName = codedValueDomain.codedValues.first {
            convertToDataType(it.code, expression.networkAttribute.dataType) ==
                convertToDataType(expression.value, expression.networkAttribute.dataType)
          }.name
          return networkAttributeNameAndOperator + codedValueDomainName
        }
        // if there's no coded value domain name
          ?: return networkAttributeNameAndOperator + (expression.otherNetworkAttribute?.name
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

  /**
   * Reset the current barrier condition to the initial expression
   * "Operational Device Status EQUAL Open".
   *
   * @param view of the rest button
   */
  fun reset(view: View) {
    initialExpression?.let {
      val traceConfiguration = sourceTier?.traceConfiguration
      traceConfiguration?.traversability?.barriers = it
      expressionTextView.text = expressionToString(it)
    }
  }

  /**
   * Convert the given value into the correct Kotlin data type by using the attribute's data type.
   *
   * @param otherValue which will be converted
   * @param dataType to be converted to
   */
  private fun convertToDataType(otherValue: Any, dataType: UtilityNetworkAttribute.DataType): Any {
    return try {
      when (dataType) {
        UtilityNetworkAttribute.DataType.BOOLEAN -> otherValue.toString().toBoolean()
        UtilityNetworkAttribute.DataType.DOUBLE -> otherValue.toString().toDouble()
        UtilityNetworkAttribute.DataType.FLOAT -> otherValue.toString().toFloat()
        UtilityNetworkAttribute.DataType.INTEGER -> otherValue.toString().toInt()
      }
    } catch (e: Exception) {
      ("Error converting data type: " + e.message).also {
        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        Log.e(TAG, it)
      }
    }
  }
}
