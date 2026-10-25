package com.maazsurti.slate.ui.stock

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maazsurti.slate.core.domain.model.Category
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.ui.common.CategoryBadge
import com.maazsurti.slate.ui.common.EmptyNote
import com.maazsurti.slate.ui.common.GhostButton
import com.maazsurti.slate.ui.common.Hairline
import com.maazsurti.slate.ui.common.look
import com.maazsurti.slate.ui.common.Overline
import com.maazsurti.slate.ui.common.PrimaryButton
import com.maazsurti.slate.ui.common.ScreenHeader
import com.maazsurti.slate.ui.common.Tag
import com.maazsurti.slate.ui.common.Tone
import com.maazsurti.slate.ui.common.display
import com.maazsurti.slate.ui.slateContainer
import com.maazsurti.slate.ui.theme.Hairline as HairlineColor
import com.maazsurti.slate.ui.theme.Ink
import com.maazsurti.slate.ui.theme.MoneyStyle
import com.maazsurti.slate.ui.vmFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen() {
    val container = slateContainer()
    val viewModel: StockViewModel = viewModel(
        factory = vmFactory { StockViewModel(container.products, container.saveProduct) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScreenHeader(
                overline = "Inventory",
                title = "Stock",
                meta = "${state.products.size} products · ${state.lowStockCount} running low",
                trailing = { GhostButton("Add", onClick = viewModel::startAdd, icon = Icons.Filled.Add) },
            )
            SearchField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )
            if (state.visible.isEmpty()) {
                EmptyNote("Nothing on the shelf", "Add a product and it shows up here and on the sale screen.")
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(state.visible, key = { it.id }) { product ->
                        StockRow(product, onClick = { viewModel.startEdit(product) })
                        Hairline()
                    }
                }
            }
        }
    }

    if (state.editorOpen) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeEditor,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            ProductEditor(product = state.editing, onSave = viewModel::save, onDismiss = viewModel::closeEditor)
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HairlineColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text("Search stock", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(Ink),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StockRow(product: Product, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryBadge(product.category, size = 32.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(3.dp))
            Text(
                "${product.price.display()} · ${product.stock} in stock",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (product.isOutOfStock) {
            Tag("Out", Tone.Negative)
        } else if (product.isLowStock) {
            Tag("Low", Tone.Warning)
        }
    }
}

@Composable
private fun ProductEditor(
    product: Product?,
    onSave: (String, String, String, String, Category) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(product) { mutableStateOf(product?.name.orEmpty()) }
    var price by remember(product) { mutableStateOf(product?.price?.format().orEmpty()) }
    var stock by remember(product) { mutableStateOf(product?.stock?.toString().orEmpty()) }
    var threshold by remember(product) { mutableStateOf(product?.lowStockThreshold?.toString() ?: "5") }
    var category by remember(product) { mutableStateOf(product?.category ?: Category.Other) }

    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 28.dp)) {
        Overline(if (product == null) "New line" else "Edit line")
        Spacer(Modifier.height(6.dp))
        Text(
            if (product == null) "Add a product" else product.name,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                singleLine = true,
                textStyle = MoneyStyle,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("In stock") },
                singleLine = true,
                textStyle = MoneyStyle,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = threshold,
            onValueChange = { threshold = it },
            label = { Text("Warn me at") },
            singleLine = true,
            textStyle = MoneyStyle,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Overline("Shelf")
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Category.entries.forEach { shelf ->
                val chosen = shelf == category
                val look = shelf.look()
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (chosen) look.wash else androidx.compose.ui.graphics.Color.Transparent)
                        .border(1.dp, if (chosen) look.ink else HairlineColor, RoundedCornerShape(8.dp))
                        .clickable { category = shelf }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(look.icon, contentDescription = null, tint = look.ink, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        shelf.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (chosen) look.ink else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            label = "Save product",
            onClick = { onSave(name, price, stock, threshold, category) },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
