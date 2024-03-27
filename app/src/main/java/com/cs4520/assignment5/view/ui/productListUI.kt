package com.cs4520.assignment5.view.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cs4520.assignment5.R
import com.cs4520.assignment5.model.database.Product
import com.cs4520.assignment5.modelview.ProductListViewModel
@Composable
fun ProductListScreen(modelViewModel: ProductListViewModel) {
    val productsState = modelViewModel.products.collectAsState()
    val loadingState = modelViewModel.loading.collectAsState()
    val errorState = modelViewModel.error.collectAsState()
    val pageState = modelViewModel.page.collectAsState()


    // Access the values
    val products = productsState.value
    val isLoading = loadingState.value
    val isError = errorState.value
    Column(modifier = Modifier.fillMaxSize()) {
        DropDown(modelViewModel)

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            if (isError) {
                // Show error message if there was an error loading products
                Text(
                    text = "Error loading products",
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            } else {
                if (products.isEmpty()) {
                    // Show message when there are no products available
                    Text(
                        text = "No products available",
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f) // Take remaining space after dropdown
                    ) {
                        items(products.size) { index ->
                            ProductItem(products[index])
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DropDown(modelViewModel: ProductListViewModel) {
    val pages = (1..10).toList() // Convert the range to a list
    var expanded by remember { mutableStateOf(false) } // Track the dropdown state
    var selectedPage by remember { mutableStateOf(1) } // Track the selected page

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        Text(
            color = Color.Black,
            text = "Selected Page " + selectedPage.toString(),
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.Center)
                .clickable { expanded = !expanded }
        )

        DropdownMenu(
            modifier = Modifier
                .widthIn(max = 200.dp),// Set a max width for the dropdown menu
            expanded = expanded,
            onDismissRequest = { expanded = false } // Dismiss dropdown if clicked outside
        ) {
            pages.forEach { page ->
                DropdownMenuItem(
                    onClick = {
                        // Set selected page in ViewModel or perform other actions
                        modelViewModel.setPage(page)
                        expanded = false // Close the dropdown after selection
                        selectedPage = page
                    },
                    modifier = Modifier.height(20.dp) // Set height of each item
                ) {
                    Text(text = "Page $page")
                }
            }
        }
    }
}


@Composable
fun ProductItem(product: Product) {
    val color = if (product.type == "Food") R.color.light_yellow else R.color.red
    val img = if (product.type == "Food") R.drawable.vegetable else R.drawable.tools
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = color))
            .padding(10.dp)
    ) {
        Image(
            painter = painterResource(id = img),
            contentDescription = null,
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
        )

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(text = product.name)
            if (product.expiryDate !== null){ Text(text = "Expiry: ${product.expiryDate}")}
            Text(text = "Price: ${product.price}")
        }
    }
}

