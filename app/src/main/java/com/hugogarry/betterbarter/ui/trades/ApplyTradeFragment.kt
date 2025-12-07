package com.hugogarry.betterbarter.ui.trades

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class ApplyTradeFragment : Fragment() {

    private val viewModel: ApplyTradeViewModel by viewModels()
    private val args: ApplyTradeFragmentArgs by navArgs()

    private var myItemsList: List<Item> = emptyList()
    private var selectedItem: Item? = null
    private var targetTrade: Trade? = null

    private lateinit var itemDropdown: AutoCompleteTextView
    private lateinit var quantityEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView

    // Value Indicator Views
    private lateinit var valueIndicator: View
    private lateinit var valueBarContainer: FrameLayout
    private lateinit var valueFeedbackText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_apply_trade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbarApplyTrade)
        NavigationUI.setupWithNavController(toolbar, findNavController())

        itemDropdown = view.findViewById(R.id.autoCompleteTextViewMyItem)
        quantityEditText = view.findViewById(R.id.editTextQuantity)
        messageEditText = view.findViewById(R.id.editTextMessage)
        submitButton = view.findViewById(R.id.buttonSubmitApplication)
        progressBar = view.findViewById(R.id.progressBarApply)
        errorTextView = view.findViewById(R.id.textViewError)

        // Indicator views
        valueIndicator = view.findViewById(R.id.viewValueIndicator)
        valueBarContainer = view.findViewById(R.id.frameLayoutValueBar)
        valueFeedbackText = view.findViewById(R.id.textViewValueFeedback)

        // Initialize Indicator to center (hidden initially until data loads)
        valueIndicator.visibility = View.INVISIBLE

        // Change Title if Editing
        if (args.existingApplication != null) {
            toolbar.title = "Edit Application"
            submitButton.text = "Update Application"

            quantityEditText.setText(args.existingApplication!!.offeredItemQuantity.toString())
            messageEditText.setText(args.existingApplication!!.message ?: "")
        }

        submitButton.setOnClickListener {
            viewModel.submitApplication(
                tradeId = args.tradeId,
                selectedItem = selectedItem,
                quantityText = quantityEditText.text.toString(),
                message = messageEditText.text.toString().trim()
            )
        }

        // Add Listener to Quantity for live updates
        quantityEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateValueIndicator()
            }
        })

        // Fetch target trade details for comparison logic
        viewModel.fetchTargetTrade(args.tradeId)

        observeMyItems()
        observeTargetTrade()
        observeApplyState()
    }

    private fun observeMyItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myItems.collectLatest { resource ->
                if (resource is Resource.Success) {
                    myItemsList = resource.data ?: emptyList()
                    val itemNames = myItemsList.map { "${it.name} (Stock: ${it.stock}, Val: €${it.estimatedValue})" }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        itemNames
                    )
                    itemDropdown.setAdapter(adapter)
                    itemDropdown.setOnItemClickListener { _, _, position, _ ->
                        selectedItem = myItemsList[position]

                        if (selectedItem?.id == args.existingApplication?.offeredItemId) {
                            quantityEditText.setText(args.existingApplication!!.offeredItemQuantity.toString())
                        } else {
                            quantityEditText.setText("1")
                        }
                        updateValueIndicator()
                    }

                    if (args.existingApplication != null && selectedItem == null) {
                        val previousItemId = args.existingApplication!!.offeredItemId
                        val index = myItemsList.indexOfFirst { it.id == previousItemId }
                        if (index != -1) {
                            selectedItem = myItemsList[index]
                            itemDropdown.setText(itemNames[index], false)
                            updateValueIndicator()
                        }
                    }
                }
            }
        }
    }

    private fun observeTargetTrade() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.targetTrade.collectLatest { resource ->
                if (resource is Resource.Success) {
                    targetTrade = resource.data
                    updateValueIndicator()
                }
            }
        }
    }

    private fun updateValueIndicator() {
        val trade = targetTrade ?: return
        val offeredItem = trade.offeredItem ?: return
        val myItem = selectedItem ?: return

        val myQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0
        if (myQuantity <= 0) {
            valueIndicator.visibility = View.INVISIBLE
            valueFeedbackText.text = "Enter quantity to check value"
            return
        }

        // 1. Calculate Total Values
        val targetValue = offeredItem.estimatedValue * trade.offeredItemQuantity
        val myValue = myItem.estimatedValue * myQuantity

        if (targetValue <= 0) return // Avoid division by zero

        // 2. Calculate Difference Percentage
        // diff > 0 means offer is higher. diff < 0 means lower.
        // e.g. target 100, recipient's 110 -> 110/100 = 1.1 -> diff +0.1 (+10%)
        val diff = (myValue / targetValue) - 1.0

        // 3. Map Difference to Bar Position
        // The bar represents a range of roughly -50% (-0.5) to +50% (+0.5).
        // Center is 0.
        // If diff is -0.5, we want position 0.0 (Left edge)
        // If diff is 0.0, we want position 0.5 (Center)
        // If diff is +0.5, we want position 1.0 (Right edge)

        // Clamp diff to the visual range [-0.5, 0.5] for the UI
        val visualRange = 0.5
        val clampedDiff = diff.coerceIn(-visualRange, visualRange) // Coerce in makes sure it's within range

        // Normalize to 0..1 scale
        // positionFraction = (diff + 0.5) / 1.0 -> diff + 0.5
        val positionFraction = clampedDiff + 0.5

        valueBarContainer.post {
            val barWidth = valueBarContainer.width.toFloat()
            val indicatorWidth = valueIndicator.width.toFloat()

            // Calculate translation X
            val xPos = (barWidth * positionFraction) - (indicatorWidth / 2)

            valueIndicator.visibility = View.VISIBLE
            valueIndicator.translationX = xPos.toFloat()
        }

        // 4. Update Feedback Text
        val diffPercent = (diff * 100).toInt()
        val absPercent = abs(diffPercent)

        val zoneText = when {
            absPercent <= 10 -> "Perfect Match! (Green Zone)"
            absPercent <= 20 -> "Fair Trade (Orange Zone)"
            else -> "Value Mismatch (Red Zone)"
        }

        val direction = if (diff > 0) "+" else ""
        valueFeedbackText.text = "Difference: $direction$diffPercent% • $zoneText"
    }

    private fun observeApplyState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.applyState.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading
                errorTextView.isVisible = resource is Resource.Error
                submitButton.isEnabled = resource !is Resource.Loading

                if (resource is Resource.Success) {
                    Toast.makeText(context, "Application Sent!", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                } else if (resource is Resource.Error) {
                    errorTextView.text = resource.message
                }
            }
        }
    }
}