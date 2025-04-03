package com.liberapp.libershop.fragments.loginRegister

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import com.liberapp.libershop.R
import com.liberapp.libershop.activities.ShoppingActivity
import com.liberapp.libershop.databinding.FragmentLoginBinding
import com.liberapp.libershop.databinding.ResetPasswordDialogBinding
import com.liberapp.libershop.dialog.dismissDialog
import com.liberapp.libershop.dialog.errorMessage
import com.liberapp.libershop.dialog.setupBottomSheetDialog
import com.liberapp.libershop.util.RegisterValidation
import com.liberapp.libershop.util.ResetPasswordFieldsState
import com.liberapp.libershop.util.ResetPasswordValidation
import com.liberapp.libershop.util.Resource
import com.liberapp.libershop.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var bindingReset: ResetPasswordDialogBinding
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)
        bindingReset = ResetPasswordDialogBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDontHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }


        binding.apply {
            buttonLoginLogin.setOnClickListener {
                val email = edEmailLogin.text.toString().trim()
                val password = edPasswordLogin.text.toString()
                viewModel.login(email, password)
            }
        }

        binding.tvForgotPasswordLogin.setOnClickListener {
            setupBottomSheetDialog { email ->
                viewModel.resetPassword(email)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.resetPassword.collect{
                when(it){
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        Snackbar.make(requireView(), "Reset link was sent to your email", Snackbar.LENGTH_LONG).show()
                    }

                    is Resource.Error -> {
                        Snackbar.make(requireView(), "Error: ${it.message}", Snackbar.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.login.collect{
                when(it){
                    is Resource.Loading -> {
                        binding.buttonLoginLogin.startAnimation()
                    }
                    is Resource.Success -> {
                        binding.buttonLoginLogin.revertAnimation()
                        Intent(requireActivity(), ShoppingActivity::class.java).also { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        binding.buttonLoginLogin.revertAnimation()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.validation.collect { validation ->
                if (validation.email is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.edEmailLogin.apply {
                            requestFocus()
                            error = validation.email.message
                        }
                    }
                }

                if (validation.password is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.edPasswordLogin.apply {
                            requestFocus()
                            error = validation.password.message
                        }
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.validationReset.collect { validationReset ->
                withContext(Dispatchers.Main) {
                    if (validationReset.emailReset is ResetPasswordValidation.Failed) {
                        errorMessage(validationReset.emailReset.message)
                    }

                    if (validationReset.emailReset is ResetPasswordValidation.Success){
                        dismissDialog()
                    }
                }
            }
        }

        binding.edEmailLogin.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    hideKeyboard(v)
                }
            }

        binding.edPasswordLogin.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    hideKeyboard(v)
                }
            }
    }






}