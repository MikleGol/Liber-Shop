package com.liberapp.libershop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.liberapp.libershop.util.RegisterFieldsState
import com.liberapp.libershop.util.RegisterValidation
import com.liberapp.libershop.util.ResetPasswordFieldsState
import com.liberapp.libershop.util.ResetPasswordValidation
import com.liberapp.libershop.util.Resource
import com.liberapp.libershop.util.validateEmail
import com.liberapp.libershop.util.validatePassword
import com.liberapp.libershop.util.validateResetPasswordEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
): ViewModel() {

    private val _login = MutableSharedFlow<Resource<FirebaseUser>>()
    val login = _login.asSharedFlow()

    private val _resetPassword = MutableSharedFlow<Resource<String>>()
    val resetPassword = _resetPassword.asSharedFlow()

    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()

    private val _validationReset = Channel<ResetPasswordFieldsState>()
    val validationReset = _validationReset.receiveAsFlow()

    fun login(email: String, password: String){
        if(checkValidation(email, password)) {
            viewModelScope.launch {
                _login.emit(Resource.Loading())
            }
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        it.user?.let {
                            _login.emit(Resource.Success(it))
                        }
                    }
                }.addOnFailureListener {
                    viewModelScope.launch {
                        _login.emit(Resource.Error(it.message.toString()))
                    }
                }
        } else{
            val loginFieldsState = RegisterFieldsState(validateEmail(email), validatePassword(password))

            viewModelScope.launch {
                _validation.send(loginFieldsState)
            }
        }
    }


    fun resetPassword(email: String){
        if(checkValidation(email)) {
            viewModelScope.launch {
                _resetPassword.emit(Resource.Loading())
            }

            firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        val resetPasswordFieldsState = ResetPasswordFieldsState(validateResetPasswordEmail(email))
                        _validationReset.send(resetPasswordFieldsState)
                        _resetPassword.emit(Resource.Success(email))
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _resetPassword.emit(Resource.Error(it.message.toString()))
                    }
                }
        } else{
            val resetPasswordFieldsState = ResetPasswordFieldsState(validateResetPasswordEmail(email))
            viewModelScope.launch {
                _validationReset.send(resetPasswordFieldsState)
            }
        }
    }

    private fun checkValidation(email: String, password: String) : Boolean{
        val emailValidation = validateEmail(email)
        val passwordValidation = validatePassword(password)
        val shouldRegister = emailValidation is RegisterValidation.Success && passwordValidation is RegisterValidation.Success

        return shouldRegister
    }

    private fun checkValidation(email: String) : Boolean{
        val emailValidation = validateEmail(email)
        val shouldRegister = emailValidation is RegisterValidation.Success

        return shouldRegister
    }
}