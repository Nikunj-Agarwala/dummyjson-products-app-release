package com.nikunjagarwala.dummyshop.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nikunjagarwala.dummyshop.ui.components.CachedDataBanner
import com.nikunjagarwala.dummyshop.ui.components.EmptyRetryState
import com.nikunjagarwala.dummyshop.ui.components.PriceRow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(viewModel: ProductDetailViewModel, onBackClick: () -> Unit) {
    val product by viewModel.product.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val refreshFailed by viewModel.refreshFailed.collectAsStateWithLifecycle()
    val isWishlisted by viewModel.isWishlisted.collectAsStateWithLifecycle()
    val isInCart by viewModel.isInCart.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.title.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (refreshFailed && product != null) {
                CachedDataBanner(onRetry = viewModel::refresh)
            }

            when {
                product == null && isRefreshing -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                product == null -> {
                    EmptyRetryState(
                        message = "Couldn't load this product. Check your connection and try again.",
                        onRetry = viewModel::refresh
                    )
                }
                else -> {
                    val current = product!!
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        val images = current.images.ifEmpty { listOf(current.thumbnail) }
                        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp)) {
                            items(images) { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = current.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .aspectRatio(1f)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(current.title, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                current.brand ?: current.category.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    String.format(Locale.US, "%.1f", current.rating),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                Text(
                                    " · ${current.stock} in stock · ${current.availabilityStatus}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            PriceRow(current)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(onClick = viewModel::addToCart, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                                    Text(if (isInCart) "Add another" else "Add to cart", modifier = Modifier.padding(start = 8.dp))
                                }
                                OutlinedButton(onClick = viewModel::toggleWishlist, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = null
                                    )
                                    Text(if (isWishlisted) "Wishlisted" else "Wishlist", modifier = Modifier.padding(start = 8.dp))
                                }
                            }

                            Text(
                                "Description",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 20.dp)
                            )
                            Text(current.description, style = MaterialTheme.typography.bodyMedium)

                            Text(
                                "Shipping & returns",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 20.dp)
                            )
                            Text(current.shippingInformation, style = MaterialTheme.typography.bodyMedium)
                            Text(current.warrantyInformation, style = MaterialTheme.typography.bodyMedium)
                            Text(current.returnPolicy, style = MaterialTheme.typography.bodyMedium)

                            if (current.reviews.isNotEmpty()) {
                                Text(
                                    "Reviews",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 20.dp)
                                )
                                current.reviews.forEach { review ->
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                            Text("${review.rating} · ${review.reviewerName}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
