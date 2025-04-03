package com.liberapp.libershop.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.liberapp.libershop.R
import com.liberapp.libershop.data.Address
import com.liberapp.libershop.databinding.AddressRvItemBinding

class AddressAdapter : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(val binding: AddressRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(address: Address,isSelected: Boolean) {
            binding.apply {
                buttonAddress.text = address.addressTitle
                if (isSelected){
                    buttonAddress.setBackgroundResource(R.drawable.selected_button_background)
                    buttonAddress.setTextColor(ContextCompat.getColor(itemView.context, R.color.beige_dark))
                }else{
                    buttonAddress.setBackgroundResource(R.drawable.unselected_button_background)
                    buttonAddress.setTextColor(ContextCompat.getColor(itemView.context, R.color.light_beige))
                }
            }
        }


    }

    private val diffUtil = object : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem.addressTitle == newItem.addressTitle && oldItem.fullName == newItem.fullName
        }

        override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        return AddressViewHolder(
            AddressRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    var selectedAddress = -1
    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = differ.currentList[position]
        holder.bind(address, selectedAddress == position)

        holder.binding.buttonAddress.setOnClickListener {
            if (selectedAddress >= 0)
                notifyItemChanged(selectedAddress)
            selectedAddress = holder.adapterPosition
            notifyItemChanged(selectedAddress)
            onClick?.invoke(address)
        }
    }
    //
    init {
        differ.addListListener { _, _ ->
            notifyItemChanged(selectedAddress)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    var onClick: ((Address) -> Unit)? = null
}