package com.liberapp.libershop.fragments.shopping

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.liberapp.libershop.R
import com.liberapp.libershop.activities.ShoppingActivity
import com.liberapp.libershop.adapters.HomeViewpagerAdapter
import com.liberapp.libershop.data.Product
import com.liberapp.libershop.databinding.FragmentHomeBinding
import com.liberapp.libershop.fragments.categories.AccessoryFragment
import com.liberapp.libershop.fragments.categories.ChairFragment
import com.liberapp.libershop.fragments.categories.CupboardFragment
import com.liberapp.libershop.fragments.categories.FurnitureFragment
import com.liberapp.libershop.fragments.categories.MainCategoryFragment
import com.liberapp.libershop.fragments.categories.TableFragment
import dagger.hilt.android.qualifiers.ApplicationContext
import android.view.View.OnFocusChangeListener
import com.google.android.material.internal.ViewUtils

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var searchEditText: EditText
    private lateinit var searchImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewpagerHome.isUserInputEnabled = false

        searchEditText = view.findViewById(R.id.ed_search)
        searchImageView = view.findViewById(R.id.img_search)

        val categoriesFragments = arrayListOf(
            MainCategoryFragment(),
            ChairFragment(),
            CupboardFragment(),
            TableFragment(),
            AccessoryFragment(),
            FurnitureFragment()
        )

        val viewPager2Adapter = HomeViewpagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
        binding.viewpagerHome.adapter = viewPager2Adapter
        TabLayoutMediator(binding.tabLayout, binding.viewpagerHome) { tab, position ->
            when (position) {
                0 -> tab.text = "Home"
                1 -> tab.text = "Chair"
                2 -> tab.text = "Cupboard"
                3 -> tab.text = "Table"
                4 -> tab.text = "Accessory"
                5 -> tab.text = "Furniture"
            }
        }.attach()

        searchImageView.setOnClickListener {
            navigateToSearchFragment()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                navigateToSearchFragment()
                true
            } else {
                false
            }
        }

        binding.edSearch.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    ViewUtils.hideKeyboard(v)
                }
            }
    }

    private fun navigateToSearchFragment() {
        val searchQuery = searchEditText.text.toString()
        val action = HomeFragmentDirections.actionHomeFragmentToSearchFragment(searchQuery)
        findNavController().navigate(action)
    }




}