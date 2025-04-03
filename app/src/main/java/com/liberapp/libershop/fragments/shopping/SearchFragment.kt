package com.liberapp.libershop.fragments.shopping

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.liberapp.libershop.R
import com.liberapp.libershop.adapters.BestDealsAdapter
import com.liberapp.libershop.adapters.FavoritesProductAdapter
import com.liberapp.libershop.adapters.SearchedProductsAdapter
import com.liberapp.libershop.adapters.SpecialProductsAdapter
import com.liberapp.libershop.databinding.FragmentMainCategoryBinding
import com.liberapp.libershop.databinding.FragmentSearchBinding
import com.liberapp.libershop.util.Resource
import com.liberapp.libershop.util.hideBottomNavigationView
import com.liberapp.libershop.viewmodel.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchedProductsAdapter: SearchedProductsAdapter
    private lateinit var favoritesProductsAdapter: FavoritesProductAdapter //dddd
    private val viewModel by viewModels<MainCategoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: SearchFragmentArgs by navArgs()
        val searchQuery = args.searchQuery

        if(!searchQuery.equals(" ")){
            hideBottomNavigationView()
            binding.tvSearch.text = "Search: " + "\"" + searchQuery + "\""
            binding.rvSearchedProducts.visibility = View.VISIBLE
            binding.rvFavoritesProducts.visibility = View.GONE
            setupSearchedProductsRv(searchQuery)

            searchedProductsAdapter.onClick = {
                val b = Bundle().apply { putParcelable("product",it) }
                findNavController().navigate(R.id.action_searchFragment_to_productDetailsFragment, b)
            }
        }else{
            binding.tvSearch.text = "Favorites"
            binding.rvFavoritesProducts.visibility = View.VISIBLE
            binding.rvSearchedProducts.visibility = View.GONE
            setupFavoritesRv()

            favoritesProductsAdapter.onClick = {
                val b = Bundle().apply { putParcelable("product",it) }
                findNavController().navigate(R.id.action_searchFragment_to_productDetailsFragment, b)
            }
        }




        lifecycleScope.launchWhenStarted {
            viewModel.searchedProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showLoading()
                        hideEmptyFavorites()
                    }

                    is Resource.Success -> {
                        val productsList = it.data
                        if (!searchQuery.equals(" ")) {
                            searchedProductsAdapter.setOriginalProducts(productsList!!)
                            searchedProductsAdapter.filterByName(searchQuery)
                            hideEmptyFavorites()
                        }
                        hideLoading()
                    }

                    is Resource.Error -> {
                        hideLoading()
                        Log.e(TAG, it.message.toString())
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        hideEmptyFavorites()
                    }

                    else -> Unit
                }
            }
        }

            lifecycleScope.launchWhenStarted {
                viewModel.favoritesProducts.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            showLoading()
                        }

                        is Resource.Success -> {
                            if (searchQuery.equals(" ")) {
                                if (it.data!!.isEmpty()) {
                                    showEmptyFavorites()
                                } else {
                                    favoritesProductsAdapter.differ.submitList(it.data)
                                    hideEmptyFavorites()
                                }
                            }
                            hideLoading()
                        }

                        is Resource.Error -> {
                            hideLoading()
                            Log.e("123123", it.message.toString())
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }


        binding.imageCloseSearch.setOnClickListener {
            findNavController().navigateUp()
        }


    }

    private fun setupFavoritesRv() {
        favoritesProductsAdapter = FavoritesProductAdapter()
        binding.rvFavoritesProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = favoritesProductsAdapter
        }
    }

    private fun setupSearchedProductsRv(searchedProduct : String) {
        searchedProductsAdapter = SearchedProductsAdapter()
        binding.rvSearchedProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = searchedProductsAdapter
        }
    }

    private fun hideLoading() {
        binding.searchedProductsProgressbar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.searchedProductsProgressbar.visibility = View.VISIBLE

    }

    private fun showEmptyFavorites() {
        binding.apply {
            layoutFavoritesEmpty.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyFavorites() {
        binding.apply {
            layoutFavoritesEmpty.visibility = View.GONE
        }
    }
}