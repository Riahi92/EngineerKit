package com.example.core.navigation

object EngineerKitDestinations {
    const val HOME_ROUTE = "home"
    const val TOOLS_ROUTE = "tools"
    const val SEARCH_ROUTE = "search"
    const val FAVORITES_ROUTE = "favorites"
    const val PROFILE_ROUTE = "profile"
    const val CATEGORY_DETAIL_ROUTE = "category_detail/{categoryId}"
    const val CALCULATOR_ROUTE = "calculator/{calculatorId}"

    fun categoryDetailRoute(categoryId: String): String = "category_detail/$categoryId"
    fun calculatorRoute(calculatorId: String): String = "calculator/$calculatorId"
}
