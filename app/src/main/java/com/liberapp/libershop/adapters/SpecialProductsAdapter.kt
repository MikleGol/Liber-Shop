package com.liberapp.libershop.adapters

import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.liberapp.libershop.R
import com.liberapp.libershop.data.Product
import com.liberapp.libershop.databinding.SpecialRvItemBinding
import com.liberapp.libershop.helper.getProductPrice

class SpecialProductsAdapter :
    RecyclerView.Adapter<SpecialProductsAdapter.SpecialProductsViewHolder>() {

    inner class SpecialProductsViewHolder(private val binding: SpecialRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(imageSpecialRvItem)
                val priceAfterOffer = product.offerPercentage.getProductPrice(product.price)
                tvSpecialProductName.text = product.name
                tvSpecialPrdouctPrice.text = "$ ${String.format("%.2f", priceAfterOffer)}"

                imageSpecialRvItem.clipToOutline = true
                imageSpecialRvItem.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, 15F)
                    }
                }

                btnAddToCart.setOnClickListener {
                    onClick?.invoke(product)
                }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialProductsViewHolder {
        return SpecialProductsViewHolder(
            SpecialRvItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: SpecialProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Product) -> Unit)? = null

}