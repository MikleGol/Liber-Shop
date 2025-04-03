package com.liberapp.libershop.fragments.shopping

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liberapp.libershop.R
import com.liberapp.libershop.activities.ShoppingActivity
import com.liberapp.libershop.adapters.CartProductAdapter
import com.liberapp.libershop.databinding.FragmentCartBinding
import com.liberapp.libershop.firebase.FirebaseCommon
import com.liberapp.libershop.util.Resource
import com.liberapp.libershop.util.VerticalItemDecoration
import com.liberapp.libershop.viewmodel.CartViewModel
import kotlinx.coroutines.flow.collectLatest

class CartFragment : Fragment(R.layout.fragment_cart) {
    private lateinit var binding: FragmentCartBinding
    private val cartAdapter by lazy { CartProductAdapter() }
    private val viewModel by activityViewModels<CartViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCartRv()

        var totalPrice = 0f
        lifecycleScope.launchWhenStarted {
            viewModel.productsPrice.collectLatest { price ->
                price?.let {
                    totalPrice = it
                    binding.tvTotalPrice.text = "$ $price"
                }
            }
        }

        cartAdapter.onProductClick = {
            val b = Bundle().apply {
                putParcelable("product", it.product)
            }
            findNavController().navigate(R.id.action_cartFragment_to_productDetailsFragment, b)
        }

        cartAdapter.onPlusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.INCREASE)
        }

        cartAdapter.onMinusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.DECREASE)
        }

        binding.buttonCheckout.setOnClickListener {
            val action = CartFragmentDirections.actionCartFragmentToBillingFragment(totalPrice,cartAdapter.differ.currentList.toTypedArray(), true)
            findNavController().navigate(action)
        }

        binding.imageCloseCart.setOnClickListener {
            findNavController().navigateUp()
        }



        lifecycleScope.launchWhenStarted {
            viewModel.deleteDialog.collectLatest {
                val alertDialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogTheme).apply {
                    setTitle("Delete item from cart")
                    setMessage("Do you want to delete this item from your cart?")
                    setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                        hideUI()
                    }
                    setPositiveButton("Yes") { dialog, _ ->
                        viewModel.deleteCartProduct(it)
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

        lifecycleScope.launchWhenStarted {
            viewModel.cartProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarCart.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarCart.visibility = View.INVISIBLE
                        if (it.data!!.isEmpty()) {
                            showEmptyCart()
                            hideOtherViews()
                        } else {
                            hideEmptyCart()
                            showOtherViews()
                            cartAdapter.differ.submitList(it.data)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressbarCart.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun showOtherViews() {
        binding.apply {
            rvCart.visibility = View.VISIBLE
            totalBoxContainer.visibility = View.VISIBLE
            buttonCheckout.visibility = View.VISIBLE
        }
    }

    private fun hideOtherViews() {
        binding.apply {
            rvCart.visibility = View.GONE
            totalBoxContainer.visibility = View.GONE
            buttonCheckout.visibility = View.GONE
        }
    }

    private fun hideEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.GONE
        }
    }

    private fun showEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.VISIBLE
        }
    }

    private fun setupCartRv() {
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = cartAdapter
            addItemDecoration(VerticalItemDecoration())
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