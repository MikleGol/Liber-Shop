package com.liberapp.libershop.helper

fun Float?.getProductPrice(price: Float): Float{
    //this --> Percentage
    if (this == null)
        return price
    val remainingPricePercentage = 100f - this
    val priceAfterOffer = remainingPricePercentage * price / 100

    return priceAfterOffer
}