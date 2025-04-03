package com.liberapp.libershop.fragments.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.internal.ViewUtils
import com.liberapp.libershop.R
import com.liberapp.libershop.activities.LoginRegisterActivity
import com.liberapp.libershop.data.User
import com.liberapp.libershop.databinding.FragmentUserAccountBinding
import com.liberapp.libershop.dialog.setupBottomSheetDialog
import com.liberapp.libershop.fragments.shopping.ProfileFragmentDirections
import com.liberapp.libershop.util.Resource
import com.liberapp.libershop.viewmodel.LoginViewModel
import com.liberapp.libershop.viewmodel.ProfileViewModel
import com.liberapp.libershop.viewmodel.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import android.view.View.OnFocusChangeListener

@AndroidEntryPoint
class UserAccountFragment : Fragment() {
    private lateinit var binding: FragmentUserAccountBinding
    private val viewModel by viewModels<UserAccountViewModel>()
    private val viewModelProfile by viewModels<ProfileViewModel>()
    private val viewModelLogin by viewModels<LoginViewModel>()
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                imageUri = it.data?.data
                Glide.with(this).load(imageUri).into(binding.imageUser)
            }
    }

    private fun moveToIntroduction() {
        viewModelProfile.logout()
        val intent = Intent(requireActivity(), LoginRegisterActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserAccountBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showUserLoading()
                    }
                    is Resource.Success -> {
                        hideUserLoading()
                        showUserInformation(it.data!!)
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.updateInfo.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.buttonSave.startAnimation()
                    }
                    is Resource.Success -> {
                        binding.buttonSave.revertAnimation()
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        binding.buttonSave.revertAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.deleteUser.collectLatest {
                when (it){
                    is Resource.Loading ->{
                        showUserLoading()
                    }
                    is Resource.Success ->{
                        hideUserLoading()
                        moveToIntroduction()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        binding.btnDeleteAccount.setOnClickListener{
            lifecycleScope.launchWhenStarted {
                viewModel.deleteUser.collectLatest {
                    val alertDialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogTheme).apply {
                        setTitle("Delete user account")
                        setMessage("Do you want to delete this account?")
                        setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                            hideUI()
                        }
                        setPositiveButton("Yes") { dialog, _ ->
                            viewModel.deleteUserInformation()
                            dialog.dismiss()
                            hideUI()
                        }
                        setOnDismissListener {
                            hideUI()
                        }
                        setBackground(ContextCompat.getDrawable(context, R.drawable.ed_background))
                    }
                    alertDialog.create()
                    alertDialog.show()
                }
            }
        }

        binding.tvUpdatePassword.setOnClickListener {
            setupBottomSheetDialog { email ->
                viewModelLogin.resetPassword(email)
            }
        }

        binding.buttonSave.setOnClickListener {
            binding.apply {
                val firstName = edFirstName.text.toString().trim()
                val lastName = edLastName.text.toString().trim()
                val email = edEmail.text.toString().trim()
                val user = User(firstName, lastName, email)
                viewModel.updateUser(user, imageUri)
            }
        }

        binding.imageEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imageActivityResultLauncher.launch(intent)
        }

        binding.imageUser.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imageActivityResultLauncher.launch(intent)
        }

        binding.imageCloseUserAccount.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.edFirstName.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }

        binding.edLastName.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }

        binding.edEmail.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }



    }

    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(this@UserAccountFragment).load(data.imagePath).error(ColorDrawable(Color.BLACK)).into(imageUser)
            edFirstName.setText(data.firstName)
            edLastName.setText(data.lastName)
            edEmail.setText(data.email)
        }
    }

    private fun hideUserLoading() {
        binding.apply {
            progressbarAccount.visibility = View.GONE
            imageUser.visibility = View.VISIBLE
            imageEdit.visibility = View.VISIBLE
            edFirstName.visibility = View.VISIBLE
            edLastName.visibility = View.VISIBLE
            edEmail.visibility = View.VISIBLE
            tvUpdatePassword.visibility = View.VISIBLE
            buttonSave.visibility = View.VISIBLE
        }
    }

    private fun showUserLoading() {
        binding.apply {
            progressbarAccount.visibility = View.VISIBLE
            imageUser.visibility = View.INVISIBLE
            imageEdit.visibility = View.INVISIBLE
            edFirstName.visibility = View.INVISIBLE
            edLastName.visibility = View.INVISIBLE
            edEmail.visibility = View.INVISIBLE
            tvUpdatePassword.visibility = View.INVISIBLE
            buttonSave.visibility = View.INVISIBLE
        }
    }

    private fun hideUI() {
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