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
import androidx.core.content.ContextCompat
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
                    val itemNames = myItemsList.map { "${it.name} (Val: €${it.estimatedValue})" }
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
        val diff = (myValue / targetValue) - 1.0

        // 3. Map Difference to Bar Position
        // Symmetrical scale: -50% to +50% mapped to 0.0 to 1.0
        val visualRange = 0.5
        val clampedDiff = diff.coerceIn(-visualRange, visualRange)

        // positionFraction = 0.0 (Left/Red), 0.5 (Center/Green), 1.0 (Right/Red)
        val positionFraction = clampedDiff + 0.5

        valueBarContainer.post {
            val barWidth = valueBarContainer.width.toFloat()
            val indicatorWidth = valueIndicator.width.toFloat()

            // Calculate translation X based on the center of the indicator
            val xPos = (barWidth * positionFraction) - (indicatorWidth / 2)

            valueIndicator.visibility = View.VISIBLE
            valueIndicator.translationX = xPos.toFloat()
        }

        // 4. Update Feedback Text & Colors matching the 7-segment XML layout
        val diffPercent = (diff * 100).toInt()
        val absPercent = abs(diffPercent)

        val (zoneText, colorRes) = when {
            absPercent <= 5 -> "Fair Match!" to android.R.color.holo_green_dark
            absPercent <= 15 -> "Minor Value Gap" to android.R.color.darker_gray
            absPercent <= 30 -> "Significant Gap" to android.R.color.holo_orange_dark
            else -> "High Value Mismatch" to android.R.color.holo_red_dark
        }

        val direction = if (diff > 0) "+" else ""
        valueFeedbackText.text = "Difference: $direction$diffPercent% • $zoneText"
        valueFeedbackText.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
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