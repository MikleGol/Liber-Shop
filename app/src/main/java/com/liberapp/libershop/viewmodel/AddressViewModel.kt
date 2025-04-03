package com.liberapp.libershop.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.liberapp.libershop.data.Address
import com.liberapp.libershop.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ViewModel() {

    private val _addNewAddress = MutableStateFlow<Resource<Address>>(Resource.Unspecified())
    val addNewAddress = _addNewAddress.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val _editAddress = MutableStateFlow<Resource<Address>>(Resource.Unspecified())
    val editAddress = _editAddress.asStateFlow()

    private val _deleteAddress = MutableStateFlow<Resource<Unit>>(Resource.Unspecified())
    val deleteAddress = _deleteAddress.asStateFlow()


    fun editAddress(updatedAddress: Address) {
        val validateInputs = validateInputs(updatedAddress)

        if (validateInputs) {
            viewModelScope.launch {
                _editAddress.emit(Resource.Loading())
                try {
                    firestore.collection("user").document(auth.uid!!).collection("address")
                        .document(updatedAddress.addressId)
                        .set(updatedAddress)
                        .await()

                    _editAddress.emit(Resource.Success(updatedAddress))
                } catch (e: Exception) {
                    _editAddress.emit(Resource.Error(e.message.toString()))
                }
            }
        } else {
            viewModelScope.launch {
                _error.emit("All fields are required")
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _deleteAddress.emit(Resource.Loading())
            try {
                firestore.collection("user").document(auth.uid!!).collection("address")
                    .document(addressId)
                    .delete()
                    .await()

                _deleteAddress.emit(Resource.Success(Unit))
            } catch (e: Exception) {
                _deleteAddress.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    fun checkIfAddressExists(address: Address) {
        val userAddressCollection = firestore.collection("user").document(auth.uid!!)
            .collection("address")

        viewModelScope.launch {
            try {
                val querySnapshot = userAddressCollection
                    .whereEqualTo("addressTitle", address.addressTitle)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    addAddress(address)
                } else {
                    _error.emit("Address already exists")
                }
            } catch (e: Exception) {
                _addNewAddress.emit(Resource.Error(e.message.toString()))
            }
        }
    }


    fun addAddress(address: Address) {
        val validateInputs = validateInputs(address)

        if (validateInputs) {
            val addressId = firestore.collection("user").document(auth.uid!!)
                .collection("address").document().id
            address.addressId = addressId

            viewModelScope.launch { _addNewAddress.emit(Resource.Loading()) }
            firestore.collection("user").document(auth.uid!!).collection("address").document(addressId)
                .set(address).addOnSuccessListener {
                    viewModelScope.launch { _addNewAddress.emit(Resource.Success(address)) }
                }.addOnFailureListener {
                    viewModelScope.launch { _addNewAddress.emit(Resource.Error(it.message.toString())) }
                }
        } else {
            viewModelScope.launch {
                _error.emit("All fields are required")
            }
        }
    }



    private fun validateInputs(address: Address): Boolean {
        return address.addressTitle.trim().isNotEmpty() &&
                address.city.trim().isNotEmpty() &&
                address.phone.trim().isNotEmpty() &&
                address.state.trim().isNotEmpty() &&
                address.fullName.trim().isNotEmpty() &&
                address.street.trim().isNotEmpty()
    }

}