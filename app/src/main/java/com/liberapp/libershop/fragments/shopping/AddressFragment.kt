package com.liberapp.libershop.fragments.shopping

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.internal.ViewUtils
import com.liberapp.libershop.databinding.FragmentAddressBinding
import com.liberapp.libershop.util.Resource
import com.liberapp.libershop.viewmodel.AddressViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.view.View.OnFocusChangeListener

@AndroidEntryPoint
class AddressFragment : Fragment() {
    private lateinit var binding: FragmentAddressBinding
    val viewModel by viewModels<AddressViewModel>()
    val args by navArgs<AddressFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.addNewAddress.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressbarAddress.visibility = View.INVISIBLE
                        viewLifecycleOwner.lifecycleScope.launch {
                            findNavController().navigateUp()
                        }
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.editAddress.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressbarAddress.visibility = View.INVISIBLE
                        viewLifecycleOwner.lifecycleScope.launch {
                            findNavController().navigateUp()
                        }
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.deleteAddress.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressbarAddress.visibility = View.INVISIBLE
                        viewLifecycleOwner.lifecycleScope.launch {
                            findNavController().navigateUp()
                        }
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddressBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val address = args.address
        if (address == null) {
            binding.buttonDelelte.visibility = View.GONE
        } else {
            binding.apply {
                edAddressTitle.setText(address.addressTitle)
                edFullName.setText(address.fullName)
                edStreet.setText(address.street)
                edPhone.setText(address.phone)
                edCity.setText(address.city)
                edState.setText(address.state)
            }
        }

        binding.apply {
            buttonSave.setOnClickListener {
                if (address == null) {
                    val addressTitle = edAddressTitle.text.toString()
                    val fullName = edFullName.text.toString()
                    val street = edStreet.text.toString()
                    val phone = edPhone.text.toString()
                    val city = edCity.text.toString()
                    val state = edState.text.toString()
                    val addressNew = com.liberapp.libershop.data.Address(
                        "",
                        addressTitle,
                        fullName,
                        street,
                        phone,
                        city,
                        state
                    )

                    viewModel.checkIfAddressExists(addressNew)
                } else{
                    val addressTitle = edAddressTitle.text.toString()
                    val fullName = edFullName.text.toString()
                    val street = edStreet.text.toString()
                    val phone = edPhone.text.toString()
                    val city = edCity.text.toString()
                    val state = edState.text.toString()
                    val updatedAddress = com.liberapp.libershop.data.Address(
                        address.addressId,
                        addressTitle,
                        fullName,
                        street,
                        phone,
                        city,
                        state
                    )
                    viewModel.editAddress(updatedAddress)
                }
            }

            buttonDelelte.setOnClickListener{
                if (address != null) {
                    viewModel.deleteAddress(address.addressId)
                }
            }

        }

        binding.imageAddressClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.edFullName.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }

        binding.edAddressTitle.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }

        binding.edStreet.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }

        binding.edCity.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }

        binding.edPhone.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }

        binding.edState.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }


    }



}