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

class ApplyTradeFragment : Fragment() {

    private val viewModel: ApplyTradeViewModel by viewModels()
    private val args: ApplyTradeFragmentArgs by navArgs()

    private var myItemsList: List<Item> = emptyList()
    private var selectedItem: Item? = null

    private lateinit var itemDropdown: AutoCompleteTextView
    private lateinit var quantityEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView

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

        // Change Title if Editing
        if (args.existingApplication != null) {
            toolbar.title = "Edit Application"
            submitButton.text = "Update Application"

            // Pre-fill Message and Quantity
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

        observeMyItems()
        observeApplyState()
    }

    private fun observeMyItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myItems.collectLatest { resource ->
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
                        // Don't overwrite quantity if we are editing and user hasn't changed item yet
                        // But usually, if they pick an item, we default to 1.
                        // To be smart: if selectedItem is same as existingApplication.itemId, use existing quantity.
                        if (selectedItem?.id == args.existingApplication?.offeredItemId) {
                            quantityEditText.setText(args.existingApplication!!.offeredItemQuantity.toString())
                        } else {
                            quantityEditText.setText("1")
                        }
                    }

                    // PRE-SELECTION LOGIC
                    if (args.existingApplication != null && selectedItem == null) {
                        // Find the item that was previously offered
                        val previousItemId = args.existingApplication!!.offeredItemId
                        val index = myItemsList.indexOfFirst { it.id == previousItemId }
                        if (index != -1) {
                            selectedItem = myItemsList[index]
                            itemDropdown.setText(itemNames[index], false) // false to disable filtering
                        }
                    }
                }
            }
        }
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