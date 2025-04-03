package com.liberapp.libershop.fragments.loginRegister

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.internal.ViewUtils
import com.liberapp.libershop.R
import com.liberapp.libershop.data.User
import com.liberapp.libershop.databinding.FragmentRegisterBinding
import com.liberapp.libershop.util.RegisterValidation
import com.liberapp.libershop.util.Resource
import com.liberapp.libershop.util.validateEmail
import com.liberapp.libershop.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import android.view.View.OnFocusChangeListener


private val TAG : String = "RegisterFragment"

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding : FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDoYouHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.apply {
            buttonRegisterRegister.setOnClickListener {
                val user = User(
                    edFirstNameRegister.text.toString().trim(),
                    edLastNameRegister.text.toString().trim(),
                    edEmailRegister.text.toString().trim()
                )
                val password = edPasswordRegister.text.toString()
                viewModel.createAccountWithEmailAndPassword(user, password)
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.register.collect{
                when(it){
                    is Resource.Loading -> {
                        binding.buttonRegisterRegister.startAnimation()
                    }
                    is Resource.Success -> {
                        binding.buttonRegisterRegister.revertAnimation()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                    is Resource.Error -> {
                        Log.e(TAG, it.message.toString())
                        binding.buttonRegisterRegister.revertAnimation()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.validation.collect{ validation ->
                if(validation.email is RegisterValidation.Failed){
                    withContext(Dispatchers.Main){
                        binding.edEmailRegister.apply {
                            requestFocus()
                            error = validation.email.message
                        }
                    }
                }

                if(validation.password is RegisterValidation.Failed){
                    withContext(Dispatchers.Main){
                        binding.edPasswordRegister.apply {
                            requestFocus()
                            error = validation.password.message
                        }
                    }
                }
            }
        }

        binding.edPasswordRegister.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }

        binding.edEmailRegister.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }

        binding.edFirstNameRegister.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }

        binding.edLastNameRegister.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }
    }
}