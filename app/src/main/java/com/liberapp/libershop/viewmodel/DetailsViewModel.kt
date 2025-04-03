package com.liberapp.libershop.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.liberapp.libershop.data.CartProduct
import com.liberapp.libershop.data.Product
import com.liberapp.libershop.firebase.FirebaseCommon
import com.liberapp.libershop.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
) : ViewModel() {

    private val _addToCart = MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
    val addToCart = _addToCart.asStateFlow()

    private val _addToFavorites = MutableStateFlow<Resource<Product>>(Resource.Unspecified())
    val addToFavorites = _addToFavorites.asStateFlow()


    fun addUpdateProductInCart(cartProduct: CartProduct) {
        viewModelScope.launch { _addToCart.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("cart")
            .whereEqualTo("product.id", cartProduct.product.id).get()
            .addOnSuccessListener {
                it.documents.let {
                    if (it.isEmpty()) { //Add new product
                        addNewProduct(cartProduct)
                    } else {
                        val product = it.first().toObject(CartProduct::class.java)
                        if(product!!.product == cartProduct.product && product.selectedColor == cartProduct.selectedColor && product.selectedSize== cartProduct.selectedSize){ //Increase the quantity (fixed quantity increasement issue)
                            val documentId = it.first().id
                            increaseQuantity(documentId, cartProduct)
                        } else { //Add new product
                            addNewProduct(cartProduct)
                        }
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch { _addToCart.emit(Resource.Error(it.message.toString())) }
            }
    }

    private fun addNewProduct(cartProduct: CartProduct) {
        firebaseCommon.addProductToCart(cartProduct) { addedProduct, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(addedProduct!!))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    private fun increaseQuantity(documentId: String, cartProduct: CartProduct) {
        firebaseCommon.increaseQuantity(documentId) { _, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(cartProduct))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))
            }
        }
    }

     fun addFavoriteProduct(product: Product) {
        viewModelScope.launch { _addToFavorites.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("favorites")
            .whereEqualTo("id", product.id).get()
            .addOnSuccessListener {
                it.documents.let {
                    if (it.isEmpty()) {
                        firebaseCommon.addProductToFavorites(product) { addedProduct, e ->
                            viewModelScope.launch {
                                if (e == null)
                                    _addToFavorites.emit(Resource.Success(addedProduct!!))
                                else
                                    _addToFavorites.emit(Resource.Error(e.message.toString()))
                            }
                        }
                    } else{
                        firebaseCommon.deleteProductFromFavorites(product){deletedProduct, e ->
                            viewModelScope.launch {
                                if(e == null)
                                    _addToFavorites.emit(Resource.Success(deletedProduct!!))
                                else
                                    _addToFavorites.emit(Resource.Error(e.message.toString()))
                            }
                        }
                    }
                }
            }
    }

}