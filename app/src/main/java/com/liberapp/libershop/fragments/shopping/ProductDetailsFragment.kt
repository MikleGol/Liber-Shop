package com.liberapp.libershop.fragments.shopping

import android.graphics.Paint
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.liberapp.libershop.R
import com.liberapp.libershop.activities.ShoppingActivity
import com.liberapp.libershop.adapters.ColorsAdapter
import com.liberapp.libershop.adapters.SizesAdapter
import com.liberapp.libershop.adapters.ViewPager2Images
import com.liberapp.libershop.data.CartProduct
import com.liberapp.libershop.databinding.FragmentProductDetailsBinding
import com.liberapp.libershop.helper.getProductPrice
import com.liberapp.libershop.util.Resource
import com.liberapp.libershop.util.hideBottomNavigationView
import com.liberapp.libershop.viewmodel.DetailsViewModel
import com.liberapp.libershop.viewmodel.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {
    private val args by navArgs<ProductDetailsFragmentArgs>()
    private lateinit var binding: FragmentProductDetailsBinding
    private val viewPagerAdapter by lazy { ViewPager2Images() }
    private val sizesAdapter by lazy { SizesAdapter() }
    private val colorsAdapter by lazy { ColorsAdapter() }
    private var selectedColor: Int? = null
    private var selectedSize: String? = null
    private val viewModel by viewModels<DetailsViewModel>()
    private val viewModelMain by viewModels<MainCategoryViewModel>()
    private var favorite = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hideBottomNavigationView()
        binding = FragmentProductDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product

        setupSizesRv()
        setupColorsRv()
        setupViewpager()

        binding.imageClose.setOnClickListener {
            findNavController().navigateUp()
        }

        sizesAdapter.onItemClick = {
            selectedSize = it
        }

        colorsAdapter.onItemClick = {
            selectedColor = it
        }

        binding.buttonAddToCart.setOnClickListener {
            viewModel.addUpdateProductInCart(CartProduct(product, 1, selectedColor, selectedSize))
        }

        binding.imageFavorite.setOnClickListener {
            if(favorite){
                viewModel.addFavoriteProduct(product)
            } else{
                viewModel.addFavoriteProduct(product)
            }
        }



        lifecycleScope.launchWhenStarted {
            viewModel.addToCart.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.buttonAddToCart.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.buttonAddToCart.revertAnimation()
                        binding.buttonAddToCart.setBackgroundColor(resources.getColor(R.color.black))
                    }

                    is Resource.Error -> {
                        binding.buttonAddToCart.stopAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModelMain.favoritesProducts.collectLatest {
                when (it){
                    is Resource.Success -> {
                            if (it.data!!.isEmpty()) {
                                Log.d("123123","Empty Cart")
                            } else {
                                if(it.data.contains(product)){
                                    binding.imageFavorite.setImageResource(R.drawable.ic_favorite)
                                } else{
                                    binding.imageFavorite.setImageResource(R.drawable.ic_unfavorite)
                                }
                            }
                    }
                    else -> Unit
                }
            }
        }

        binding.apply {
            tvProductName.text = product.name
            product.offerPercentage?.let {
                val priceAfterOffer = product.offerPercentage.getProductPrice(product.price)
                tvNewProductPrice.text = "$ ${String.format("%.2f",priceAfterOffer)}"
                tvOldProductPrice.paintFlags = tvOldProductPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            tvOldProductPrice.text = "$ ${product.price}"
            tvProductDescription.text = product.description

            if (product.colors.isNullOrEmpty())
                tvProductColors.visibility = View.INVISIBLE
            if (product.sizes.isNullOrEmpty())
                tvProductSize.visibility = View.INVISIBLE
        }

        viewPagerAdapter.differ.submitList(product.images)
        product.colors?.let { colorsAdapter.differ.submitList(it) }
        product.sizes?.let { sizesAdapter.differ.submitList(it) }
    }

    private fun setupViewpager() {
        binding.apply {
            viewPagerProductImages.adapter = viewPagerAdapter
        }
    }

    private fun setupColorsRv() {
        binding.rvColors.apply {
            adapter = colorsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupSizesRv() {
        binding.rvSizes.apply {
            adapter = sizesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }
}