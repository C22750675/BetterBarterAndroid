package com.hugogarry.betterbarter.ui.trades

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
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
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateTradeFragment : Fragment() {

    private val viewModel: CreateTradeViewModel by viewModels()
    private val args: CreateTradeFragmentArgs by navArgs()

    private var myItemsList: List<Item> = emptyList()
    private var selectedItem: Item? = null

    // Track if we are in edit mode
    private val isEditMode get() = args.tradeId != null

    private lateinit var itemDropdown: AutoCompleteTextView
    private lateinit var quantityEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_trade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.toolbarCreateTrade)
        NavigationUI.setupWithNavController(toolbar, findNavController())

        itemDropdown = view.findViewById(R.id.autoCompleteTextViewMyItem)
        quantityEditText = view.findViewById(R.id.editTextQuantity)
        descriptionEditText = view.findViewById(R.id.editTextDescription)
        submitButton = view.findViewById(R.id.buttonSubmitTrade)
        progressBar = view.findViewById(R.id.progressBarCreateTrade)
        errorTextView = view.findViewById(R.id.textViewCreateTradeError)

        // UI Adjustments for Edit Mode
        if (isEditMode) {
            toolbar.title = "Edit Trade Proposal"
            submitButton.text = "Update Trade Proposal"
            viewModel.fetchTradeForEditing(args.tradeId!!)
        }

        submitButton.setOnClickListener {
            if (isEditMode) {
                viewModel.updateTrade(
                    tradeId = args.tradeId!!,
                    selectedItem = selectedItem,
                    quantityText = quantityEditText.text.toString(),
                    description = descriptionEditText.text.toString().trim()
                )
            } else {
                viewModel.createTrade(
                    selectedItem = selectedItem,
                    circleId = args.circleId,
                    quantityText = quantityEditText.text.toString(),
                    description = descriptionEditText.text.toString().trim()
                )
            }
        }

        observeMyItems()
        observeExistingTrade()
        observeActionState()
    }

    private fun observeMyItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myItems.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading

                if (resource is Resource.Success) {
                    myItemsList = resource.data ?: emptyList()
                    val itemNames = myItemsList.map { "${it.name} (Stock: ${it.stock})" }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        itemNames
                    )
                    itemDropdown.setAdapter(adapter)
                    itemDropdown.setOnItemClickListener { _, _, position, _ ->
                        selectedItem = myItemsList[position]
                        if (!isEditMode) quantityEditText.setText("1")
                    }

                    // If editing, re-trigger the pre-fill once items are loaded
                    viewModel.existingTrade.value.data?.let { preFillTradeData(it) }
                }
            }
        }
    }

    private fun observeExistingTrade() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.existingTrade.collectLatest { resource ->
                if (resource is Resource.Success) {
                    resource.data?.let { preFillTradeData(it) }
                }
            }
        }
    }

    private fun preFillTradeData(trade: com.hugogarry.betterbarter.data.model.Trade) {
        descriptionEditText.setText(trade.description)
        quantityEditText.setText(trade.offeredItemQuantity.toString())

        // Find and select the item in the list
        val itemIndex = myItemsList.indexOfFirst { it.id == trade.offeredItem?.id }
        if (itemIndex != -1) {
            selectedItem = myItemsList[itemIndex]
            val itemNames = myItemsList.map { "${it.name} (Stock: ${it.stock})" }
            itemDropdown.setText(itemNames[itemIndex], false)
        }
    }

    private fun observeActionState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.actionState.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading
                errorTextView.isVisible = resource is Resource.Error

                if (resource is Resource.Success) {
                    val msg = if (isEditMode) "Trade proposal updated!" else "Trade proposal created!"
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                } else if (resource is Resource.Error) {
                    errorTextView.text = resource.message
                }
            }
        }
    }
}