package com.cs4520.assignment5.view.navigation

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.work.WorkManager
import com.cs4520.assignment5.modelview.ProductListViewModel
import com.cs4520.assignment5.view.ui.LoginScreen
import com.cs4520.assignment5.view.ui.ProductListScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = NavigationItem.Login.route,
    context: Context,
    application: Application,
    workManager: WorkManager)
{
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        val productListViewModel = ProductListViewModel(application, workManager)
        composable(NavigationItem.Login.route) {
            LoginScreen(context, navController)
        }
        composable(NavigationItem.ProductList.route) {
            ProductListScreen(productListViewModel)
        }
    }
}