package com.liberapp.libershop.fragments.shopping

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import br.com.simplepass.loadingbutton.BuildConfig
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.liberapp.libershop.R
import com.liberapp.libershop.activities.LoginRegisterActivity
import com.liberapp.libershop.databinding.FragmentProfileBinding
import com.liberapp.libershop.util.Resource
import com.liberapp.libershop.util.showBottomNavigationView
import com.liberapp.libershop.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collectLatest
private val PREFS_NAME = "MyPrefs"
private val IS_NOTIFICATION_ENABLED_KEY = "IsNotificationEnabled"

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isNotificationEnabled()){
            binding.switchNotification.isChecked = true
        } else {
            binding.switchNotification.isChecked = false
        }

        binding.constraintProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_userAccountFragment)
        }

        binding.linearAllOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_allOrdersFragment)
        }

        binding.linearTrackOrder.setOnClickListener {
            Snackbar.make(requireView(), "No accepted orders to track", Snackbar.LENGTH_LONG).show()
        }

        binding.linearBilling.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToBillingFragment(
                0f,
                emptyArray(),
                false
            )
            findNavController().navigate(action)
        }

        binding.linearLogOut.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireActivity(), LoginRegisterActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showNotificationPermissionDialog()
            }
        }


        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarSettings.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarSettings.visibility = View.GONE
                        Glide.with(requireView()).load(it.data!!.imagePath).error(
                            ColorDrawable(
                            Color.BLACK)
                        ).into(binding.imageUser)
                        binding.tvUserName.text = "${it.data.firstName} ${it.data.lastName}"
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarSettings.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        showBottomNavigationView()
    }

    private fun showNotificationPermissionDialog() {
        val alertDialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogTheme).apply {
            setTitle("Notifications")
            setMessage("Allow this app to send you messages?")
            setNegativeButton("No") { dialog, _ ->
                saveNotificationState(false)
                Snackbar.make(requireView(), "Permission denied", Snackbar.LENGTH_LONG).show()
                binding.switchNotification.isChecked = false
                dialog.dismiss()
                hideUI()
            }
            setPositiveButton("Yes") { dialog, _ ->
                saveNotificationState(true)
                Snackbar.make(requireView(), "Permission granted", Snackbar.LENGTH_LONG).show()
                binding.switchNotification.isChecked = true
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


    private fun saveNotificationState(isEnabled: Boolean) {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putBoolean(IS_NOTIFICATION_ENABLED_KEY, isEnabled)
            apply()
        }
    }

    private fun isNotificationEnabled(): Boolean {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(IS_NOTIFICATION_ENABLED_KEY, false)
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


