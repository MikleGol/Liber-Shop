package com.liberapp.libershop.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.liberapp.libershop.data.CartProduct
import com.liberapp.libershop.data.Product

class FirebaseCommon(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val cartCollection by lazy { fetchCartCollection() }

    private val favoritesCollection by lazy { fetchFavoritesCollection() }

    private fun fetchCartCollection(): CollectionReference {
        val userId = auth.uid
        requireNotNull(userId) { "User ID must not be null" }
        return firestore.collection("user").document(userId).collection("cart")
    }

    private fun fetchFavoritesCollection(): CollectionReference {
        val userId = auth.uid
        requireNotNull(userId) { "User ID must not be null" }
        return firestore.collection("user").document(userId).collection("favorites")
    }


    fun addProductToCart(cartProduct: CartProduct, onResult: (CartProduct?, Exception?) -> Unit) {
        cartCollection.document().set(cartProduct)
            .addOnSuccessListener {
                onResult(cartProduct, null)
            }.addOnFailureListener {
                onResult(null, it)
            }
    }

    fun addProductToFavorites(product: Product, onResult: (Product?, Exception?) -> Unit) {
        favoritesCollection.document().set(product)
            .addOnSuccessListener {
                onResult(product, null)
            }.addOnFailureListener {
                onResult(null, it)
            }
    }

    fun deleteProductFromFavorites(product: Product, onResult: (Product?, Exception?) -> Unit) {
        favoritesCollection.whereEqualTo("id", product.id).get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    favoritesCollection.document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            onResult(product, null)
                            return@addOnSuccessListener
                        }
                        .addOnFailureListener { exception ->
                            onResult(null, exception)
                            return@addOnFailureListener
                        }
                }
            }
    }

    fun increaseQuantity(documentId: String, onResult: (String?, Exception?) -> Unit) {
        firestore.runTransaction { transition ->
            val documentRef = cartCollection.document(documentId)
            val document = transition.get(documentRef)
            val productObject = document.toObject(CartProduct::class.java)
            productObject?.let { cartProduct ->
                val newQuantity = cartProduct.quantity + 1
                val newProductObject = cartProduct.copy(quantity = newQuantity)
                transition.set(documentRef, newProductObject)
            }
        }.addOnSuccessListener {
            onResult(documentId, null)
        }.addOnFailureListener {
            onResult(null, it)
        }
    }

    fun decreaseQuantity(documentId: String, onResult: (String?, Exception?) -> Unit) {
        firestore.runTransaction { transition ->
            val documentRef = cartCollection.document(documentId)
            val document = transition.get(documentRef)
            val productObject = document.toObject(CartProduct::class.java)
            productObject?.let { cartProduct ->
                val newQuantity = cartProduct.quantity - 1
                val newProductObject = cartProduct.copy(quantity = newQuantity)
                transition.set(documentRef, newProductObject)
            }
        }.addOnSuccessListener {
            onResult(documentId, null)
        }.addOnFailureListener {
            onResult(null, it)
        }
    }

    enum class QuantityChanging {
        INCREASE,DECREASE
    }


}