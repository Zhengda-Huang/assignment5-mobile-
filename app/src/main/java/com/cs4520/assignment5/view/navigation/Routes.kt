package com.cs4520.assignment5.view.navigation

enum class Screen {
    LOGIN,
    PRODUCT_LIST
}
sealed class NavigationItem(val route: String) {
    object Login : NavigationItem(Screen.LOGIN.name)
    object ProductList : NavigationItem(Screen.PRODUCT_LIST.name)

}