package com.liberapp.libershop.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.liberapp.libershop.data.Product
import com.liberapp.libershop.databinding.BestDealsRvItemBinding
import com.liberapp.libershop.databinding.ProductRvItemBinding
import com.liberapp.libershop.helper.getProductPrice
import org.checkerframework.checker.units.qual.s

class SearchedProductsAdapter : RecyclerView.Adapter<SearchedProductsAdapter.SearchedProductsViewHolder>() {
    private var originalProducts: List<Product> = emptyList()

    inner class SearchedProductsViewHolder(private val binding: ProductRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                val priceAfterOffer = product.offerPercentage.getProductPrice(product.price)
                tvNewPrice.text = "$ ${String.format("%.2f", priceAfterOffer)}"
                tvPrice.paintFlags = tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                if (product.offerPercentage == null)
                    tvNewPrice.visibility = View.INVISIBLE

                Glide.with(itemView).load(product.images[0]).into(imgProduct)
                tvPrice.text = "$ ${product.price}"
                tvName.text = product.name
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchedProductsViewHolder {
        return SearchedProductsViewHolder(
            ProductRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: SearchedProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun filterByName(name: String) {
        // Використовуємо оригінальний список для відновлення оригінальних даних
        differ.submitList(originalProducts.filter { it.name.contains(name, ignoreCase = true) })
    }

    // Додаємо метод для встановлення оригінального списку продуктів
    fun setOriginalProducts(products: List<Product>) {
        originalProducts = products
        differ.submitList(products)
    }

    var onClick: ((Product) -> Unit)? = null
}