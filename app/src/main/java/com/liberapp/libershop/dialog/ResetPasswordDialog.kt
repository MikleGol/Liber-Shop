package com.liberapp.libershop.dialog

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.liberapp.libershop.R
import com.liberapp.libershop.viewmodel.LoginViewModel

    lateinit var edEmail : EditText
    lateinit var dialog : BottomSheetDialog

fun Fragment.setupBottomSheetDialog(
    onSendClick: (String) -> Unit
){
    dialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
    val view = layoutInflater.inflate(R.layout.reset_password_dialog,null)
    dialog.setContentView(view)
    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    dialog.show()

    edEmail = view.findViewById<EditText>(R.id.edResetPassword)
    val buttonSend = view.findViewById<Button>(R.id.buttonSendResetPassword)
    val buttonCancel = view.findViewById<Button>(R.id.buttonCancelResetPassword)


    buttonSend.setOnClickListener {
        val email = edEmail.text.toString().trim()
        onSendClick(email)
    }

    buttonCancel.setOnClickListener {
        dialog.dismiss()
    }

    dialog.setOnDismissListener {
        val decorView = requireActivity().window.decorView
        decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
    }

}

 fun errorMessage(message : String) {
     edEmail.apply {
         requestFocus()
         error = message
     }
 }

     fun dismissDialog(){
         dialog.dismiss()
     }
