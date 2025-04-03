package com.liberapp.libershop.util

sealed class ResetPasswordValidation(){
    object Success : ResetPasswordValidation()
    data class Failed(val message : String) : ResetPasswordValidation()

}

data class ResetPasswordFieldsState(
    val emailReset: ResetPasswordValidation
)