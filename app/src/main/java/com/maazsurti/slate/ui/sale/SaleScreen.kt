package com.maazsurti.slate.ui.sale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maazsurti.slate.core.domain.model.CartLine
import com.maazsurti.slate.core.domain.model.Category
import com.maazsurti.slate.core.domain.model.PaymentType
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.ui.common.Hairline
import com.maazsurti.slate.ui.common.MoneyText
import com.maazsurti.slate.ui.common.Overline
import com.maazsurti.slate.ui.common.PrimaryButton
import com.maazsurti.slate.ui.common.CategoryBadge
import com.maazsurti.slate.ui.common.look
import com.maazsurti.slate.ui.common.ScreenHeader
import com.maazsurti.slate.ui.common.Segmented
import com.maazsurti.slate.ui.common.Tag
import com.maazsurti.slate.ui.common.Tone
import com.maazsurti.slate.ui.common.display
import com.maazsurti.slate.ui.slateContainer
import com.maazsurti.slate.ui.theme.Hairline as HairlineColor
import com.maazsurti.slate.ui.theme.Ink
import com.maazsurti.slate.ui.theme.Clay
import com.maazsurti.slate.ui.theme.GreenWash
import com.maazsurti.slate.ui.theme.Green as GreenInk
import com.maazsurti.slate.ui.theme.MoneyStyle
import com.maazsurti.slate.ui.theme.Rust
import com.maazsurti.slate.ui.theme.RustWash
import com.maazsurti.slate.ui.theme.Paper
import com.maazsurti.slate.ui.vmFactory

@Composable
fun SaleScreen() {
    val container = slateContainer()
    val viewModel: SaleViewModel = viewModel(
        factory = vmFactory {
            SaleViewModel(
                completeSale = container.completeSale,
                products = container.products,
                customers = container.customers,
                sales = container.sales,
                ledger = container.ledger,
            )
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var pickingCustomer by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScreenHeader(
                overline = "Point of sale",
                title = "New sale",
                meta = "${state.cart.itemCount} in the basket · ${state.products.size} on the shelf",
                trailing = {
                    Tag(
                        java.time.LocalDate.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("EEE d MMM", java.util.Locale.ENGLISH)
                        ),
                        Tone.Neutral,
                    )
                },
            )
            TakingsStrip(
                today = state.todayTotal,
                bills = state.todayCount,
                owing = state.outstanding,
            )
            SearchField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )
            CategoryChips(
                shelves = state.shelves,
                selected = state.category,
                onSelect = viewModel::selectCategory,
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.visibleProducts, key = { it.id }) { product ->
                    ProductTile(
                        product = product,
                        inCart = state.cart.quantityOf(product.id),
                        onClick = { viewModel.add(product) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            AnimatedVisibility(
                visible = !state.cart.isEmpty,
                enter = slideInVertically(tween(220)) { it / 3 } + fadeIn(tween(180)),
                exit = slideOutVertically(tween(180)) { it / 3 } + fadeOut(tween(140)),
            ) {
                CartPanel(
                    lines = state.cart.lines,
                    total = state.cart.total,
                    payment = state.payment,
                    customerName = state.selectedCustomer?.name,
                    onIncrease = viewModel::increase,
                    onDecrease = viewModel::decrease,
                    onRemove = viewModel::remove,
                    onPayment = viewModel::selectPayment,
                    onPickCustomer = { pickingCustomer = true },
                    onComplete = viewModel::complete,
                )
            }
            if (state.cart.isEmpty) {
                Hairline()
                Text(
                    "Tap a product to start the sale",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                )
            }
        }
    }

    if (pickingCustomer) {
        CustomerPicker(
            customers = state.customers.map { it.id to it.name },
            selected = state.customerId,
            onSelect = {
                viewModel.selectCustomer(it)
                pickingCustomer = false
            },
            onDismiss = { pickingCustomer = false },
        )
    }
}

@Composable
private fun CategoryChips(shelves: List<Category>, selected: Category?, onSelect: (Category?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShelfChip("All", null, selected == null) { onSelect(null) }
        shelves.forEach { shelf ->
            ShelfChip(shelf.label, shelf.look().ink, selected == shelf) { onSelect(shelf) }
        }
    }
}

@Composable
private fun ShelfChip(label: String, dot: androidx.compose.ui.graphics.Color?, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Ink else androidx.compose.ui.graphics.Color.Transparent)
            .border(1.dp, if (selected) Ink else HairlineColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != null) {
            Box(modifier = Modifier.size(7.dp).background(dot, CircleShape))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Paper else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TakingsStrip(today: com.maazsurti.slate.core.domain.model.Money, bills: Int, owing: com.maazsurti.slate.core.domain.model.Money) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatChip("Today", today.display(), GreenWash, GreenInk, Modifier.weight(1.4f))
        StatChip("Bills", bills.toString(), MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface, Modifier.weight(0.8f))
        StatChip("Owing", owing.display(), RustWash, Rust, Modifier.weight(1.4f))
    }
}

@Composable
private fun StatChip(label: String, value: String, wash: androidx.compose.ui.graphics.Color, ink: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(wash, RoundedCornerShape(10.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = ink.copy(alpha = 0.75f))
        Spacer(Modifier.height(3.dp))
        Text(value, style = MoneyStyle.copy(fontSize = 14.sp), color = ink)
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
                Text("Search the shelf", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxWidth(),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Ink),
            )
        }
    }
}

@Composable
private fun ProductTile(product: Product, inCart: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val chosen = inCart > 0
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(122.dp)
            .clickable(enabled = !product.isOutOfStock, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (chosen) Paper else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (chosen) 1.5.dp else 1.dp, if (chosen) Ink else HairlineColor),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            CategoryBadge(product.category)
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (chosen) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier.size(19.dp).background(Ink, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$inCart", style = MaterialTheme.typography.labelSmall, color = Paper)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    product.price.display(),
                    style = MoneyStyle.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
                if (product.isOutOfStock) {
                    Tag("Out", Tone.Negative)
                } else {
                    Text(
                        "${product.stock} left",
                        style = MoneyStyle,
                        color = if (product.isLowStock) Clay else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CartPanel(
    lines: List<CartLine>,
    total: com.maazsurti.slate.core.domain.model.Money,
    payment: PaymentType,
    customerName: String?,
    onIncrease: (Long) -> Unit,
    onDecrease: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onPayment: (PaymentType) -> Unit,
    onPickCustomer: () -> Unit,
    onComplete: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            Hairline()
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Column(
                    modifier = Modifier.heightIn(max = 132.dp).verticalScroll(rememberScrollState()),
                ) {
                    lines.forEachIndexed { index, line ->
                        if (index > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 9.dp), thickness = 1.dp, color = HairlineColor)
                        }
                        CartRow(line, onIncrease, onDecrease, onRemove)
                    }
                }
                Hairline(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Overline("Total")
                        Spacer(Modifier.height(2.dp))
                        Text(total.display(), style = MaterialTheme.typography.displaySmall)
                    }
                    Segmented(
                        options = listOf("Cash" to (payment == PaymentType.CASH), "Credit" to (payment == PaymentType.CREDIT)),
                        onSelect = { if (it == 0) onPayment(PaymentType.CASH) else onPayment(PaymentType.CREDIT) },
                        modifier = Modifier.width(168.dp),
                    )
                }
                if (payment == PaymentType.CREDIT) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Overline("On credit to")
                        Spacer(Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (customerName == null) MaterialTheme.colorScheme.error else HairlineColor,
                                    RoundedCornerShape(8.dp),
                                )
                                .clickable(onClick = onPickCustomer)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = if (customerName == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                customerName ?: "Choose a customer",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (customerName == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
                PrimaryButton(
                    label = "Complete sale",
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun CartRow(line: CartLine, onIncrease: (Long) -> Unit, onDecrease: (Long) -> Unit, onRemove: (Long) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(line.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${line.unitPrice.display()} each",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        MoneyText(line.total)
        Spacer(Modifier.width(10.dp))
        StepperButton(Icons.Filled.Remove, "One less") { onDecrease(line.productId) }
        Spacer(Modifier.width(8.dp))
        Text("${line.quantity}", style = MoneyStyle, modifier = Modifier.width(16.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.width(8.dp))
        StepperButton(Icons.Filled.Add, "One more") { onIncrease(line.productId) }
        Spacer(Modifier.width(4.dp))
        StepperButton(Icons.Filled.Close, "Remove line", onClick = { onRemove(line.productId) })
    }
}

@Composable
private fun StepperButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .border(1.dp, HairlineColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(13.dp))
    }
}

@Composable
private fun CustomerPicker(
    customers: List<Pair<Long, String>>,
    selected: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Who is buying on credit?", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                customers.forEach { (id, name) ->
                    val chosen = selected == id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(id) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        if (chosen) Tag("Chosen", Tone.Positive)
                    }
                    Hairline()
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
