package com.nikunjagarwala.dummyshop.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nikunjagarwala.dummyshop.ui.components.CachedDataBanner
import com.nikunjagarwala.dummyshop.ui.components.EmptyMessageState
import com.nikunjagarwala.dummyshop.ui.components.EmptyRetryState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(viewModel: CartViewModel, onProductClick: (Int) -> Unit) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val subtotal by viewModel.subtotal.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val hasError by viewModel.hasError.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            items.isEmpty() && isRefreshing -> Unit
            items.isEmpty() && hasError -> EmptyRetryState(
                message = "Couldn't load your cart. Check your connection and try again.",
                onRetry = viewModel::refresh
            )
            items.isEmpty() -> EmptyMessageState(message = "Your cart is empty.")
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (hasError) CachedDataBanner(onRetry = viewModel::refresh)
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.weight(1f)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items, key = { it.product.id }) { entry ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .clickable { onProductClick(entry.product.id) }
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = entry.product.thumbnail,
                                            contentDescription = entry.product.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        )
                                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                            Text(entry.product.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text(
                                                "$" + String.format(Locale.US, "%.2f", entry.product.discountedPrice),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                                IconButton(onClick = { viewModel.updateQuantity(entry.product.id, entry.quantity - 1) }) {
                                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity")
                                                }
                                                Text("${entry.quantity}")
                                                IconButton(onClick = { viewModel.updateQuantity(entry.product.id, entry.quantity + 1) }) {
                                                    Icon(Icons.Filled.Add, contentDescription = "Increase quantity")
                                                }
                                            }
                                        }
                                        IconButton(onClick = { viewModel.remove(entry.product.id) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Remove from cart")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                    Surface {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "$" + String.format(Locale.US, "%.2f", subtotal),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
