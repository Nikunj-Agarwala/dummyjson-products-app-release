package com.nikunjagarwala.dummyshop.ui.navigation

object Routes {
    const val LIST = "list"
    const val WISHLIST = "wishlist"
    const val CART = "cart"
    const val DETAIL_PATTERN = "detail/{productId}"

    fun detail(productId: Int) = "detail/$productId"
}
