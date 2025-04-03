package com.liberapp.libershop.util

import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.liberapp.libershop.activities.ShoppingActivity

fun Fragment.hideBottomNavigationView(){
    val bottomNavigationView =
        (activity as ShoppingActivity).findViewById<BottomNavigationView>(
            com.liberapp.libershop.R.id.bottomNavigation
        )
    bottomNavigationView.visibility = android.view.View.GONE
}

fun Fragment.showBottomNavigationView(){
    val bottomNavigationView =
        (activity as ShoppingActivity).findViewById<BottomNavigationView>(
            com.liberapp.libershop.R.id.bottomNavigation
        )
    bottomNavigationView.visibility = android.view.View.VISIBLE
}