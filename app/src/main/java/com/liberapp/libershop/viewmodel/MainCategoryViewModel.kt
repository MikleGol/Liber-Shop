package com.liberapp.libershop.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.liberapp.libershop.data.CartProduct
import com.liberapp.libershop.data.Product
import com.liberapp.libershop.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
    ) : ViewModel() {

    private val _specialProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val specialProducts: StateFlow<Resource<List<Product>>> = _specialProducts

    private val _bestDealsProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestDealsProducts: StateFlow<Resource<List<Product>>> = _bestDealsProducts

    private val _bestProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestProducts: StateFlow<Resource<List<Product>>> = _bestProducts

    private val _searchedProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val searchedProducts: StateFlow<Resource<List<Product>>> = _searchedProducts

    private val _favoritesProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val favoritesProducts: StateFlow<Resource<List<Product>>> = _favoritesProducts

    private val pagingInfo = PagingInfo()

    private var favoritesProductDocuments = emptyList<DocumentSnapshot>()


    init {
        fetchSpecialProducts()
        fetchBestDeals()
        fetchBestProducts()
        fetchSearchedProducts()
        fetchFavoritesProducts()
    }

    fun fetchSpecialProducts() {
        viewModelScope.launch {
            _specialProducts.emit(Resource.Loading())
        }
        firestore.collection("Products")
            .whereEqualTo("category", "Special Products").get().addOnSuccessListener { result ->
                val specialProductsList = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _specialProducts.emit(Resource.Success(specialProductsList))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _specialProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }


    fun fetchBestDeals() {
        viewModelScope.launch {
            _bestDealsProducts.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("category", "Best Deals").get()
            .addOnSuccessListener { result ->
                val bestDealsProducts = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _bestDealsProducts.emit(Resource.Success(bestDealsProducts))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _bestDealsProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun fetchBestProducts() {
        if (!pagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _bestProducts.emit(Resource.Loading())
                firestore.collection("Products").limit(pagingInfo.bestProductsPage * 10).get()
                    .addOnSuccessListener { result ->
                        val bestProducts = result.toObjects(Product::class.java)
                        pagingInfo.isPagingEnd = bestProducts == pagingInfo.oldBestProducts
                        pagingInfo.oldBestProducts = bestProducts
                        viewModelScope.launch {
                            _bestProducts.emit(Resource.Success(bestProducts))
                        }
                        pagingInfo.bestProductsPage++
                    }.addOnFailureListener {
                        viewModelScope.launch {
                            _bestProducts.emit(Resource.Error(it.message.toString()))
                        }
                    }
            }
        }
    }

    fun fetchSearchedProducts(){
        viewModelScope.launch {
            _searchedProducts.emit(Resource.Loading())
        }
        firestore.collection("Products").get()
            .addOnSuccessListener { result ->
                val searchedProducts = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _searchedProducts.emit(Resource.Success(searchedProducts))
                }
            }.addOnFailureListener{
                viewModelScope.launch {
                    _searchedProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun fetchFavoritesProducts() {
        viewModelScope.launch { _favoritesProducts.emit(Resource.Loading()) }
        val userId = auth.uid
        if (userId != null) {
            firestore.collection("user").document(userId).collection("favorites")
                .addSnapshotListener { value, error ->
                    if (error != null || value == null) {
                        Log.d("123123", "Fetch 1")
                        viewModelScope.launch { _favoritesProducts.emit(Resource.Error(error?.message.toString())) }
                    } else {
                        favoritesProductDocuments = value.documents
                        val favoritesProducts = value.toObjects(Product::class.java)
                        viewModelScope.launch {
                            _favoritesProducts.emit(Resource.Success(favoritesProducts))
                            Log.d("123123", favoritesProducts.toString())
                        }
                    }
                }
        } else {
            viewModelScope.launch { _favoritesProducts.emit(Resource.Error("User ID is null")) }
        }
    }

}

internal data class PagingInfo(
    var bestProductsPage: Long = 1,
    var oldBestProducts: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false
)