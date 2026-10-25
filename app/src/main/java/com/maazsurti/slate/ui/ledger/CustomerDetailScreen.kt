package com.maazsurti.slate.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maazsurti.slate.core.domain.model.LedgerEntry
import com.maazsurti.slate.core.domain.model.LedgerKind
import com.maazsurti.slate.ui.common.Hairline
import com.maazsurti.slate.ui.common.Overline
import com.maazsurti.slate.ui.common.Panel
import com.maazsurti.slate.ui.common.PrimaryButton
import com.maazsurti.slate.ui.common.ScreenHeader
import com.maazsurti.slate.ui.common.dayLabel
import com.maazsurti.slate.ui.common.display
import com.maazsurti.slate.ui.common.toClock
import com.maazsurti.slate.ui.common.toLocalDate
import com.maazsurti.slate.ui.slateContainer
import com.maazsurti.slate.ui.theme.Green
import com.maazsurti.slate.ui.theme.GreenWash
import com.maazsurti.slate.ui.theme.RustWash
import com.maazsurti.slate.ui.theme.MoneyStyle
import com.maazsurti.slate.ui.theme.Rust
import com.maazsurti.slate.ui.vmFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(customerId: Long, onBack: () -> Unit) {
    val container = slateContainer()
    val viewModel: CustomerDetailViewModel = viewModel(
        key = "customer-$customerId",
        factory = vmFactory {
            CustomerDetailViewModel(customerId, container.customers, container.ledger, container.recordPayment)
        },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var paying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScreenHeader(
                overline = "Account",
                title = state.name.ifBlank { "Customer" },
                meta = state.phone.ifBlank { "No phone number on file" },
                onBack = onBack,
            )
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                item {
                    Panel(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Overline("Owes the shop")
                        Spacer(Modifier.height(4.dp))
                        Text(state.balance.display(), style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(
                            label = "Record payment",
                            onClick = { paying = true },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                item {
                    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 10.dp)) {
                        Overline("History")
                    }
                }
                if (state.entries.isEmpty()) {
                    item {
                        Text(
                            "No entries yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        )
                    }
                }
                items(state.entries, key = { it.id }) { entry ->
                    EntryRow(entry)
                    Hairline()
                }
            }
        }
    }

    if (paying) {
        ModalBottomSheet(
            onDismissRequest = { paying = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            PaymentSheet(
                balance = state.balance.display(),
                onSave = { amount ->
                    paying = false
                    viewModel.pay(amount)
                },
                onDismiss = { paying = false },
            )
        }
    }
}

@Composable
private fun EntryRow(entry: LedgerEntry) {
    val charge = entry.kind == LedgerKind.CHARGE
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(if (charge) RustWash else GreenWash, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (charge) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = if (charge) Rust else Green,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(if (charge) "Credit sale" else "Payment received", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                "${dayLabel(entry.at.toLocalDate())} · ${entry.at.toClock()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            (if (charge) "+" else "−") + entry.amount.display(),
            style = MoneyStyle,
            color = if (charge) Rust else Green,
        )
    }
}

@Composable
private fun PaymentSheet(balance: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 28.dp)) {
        Overline("Money in")
        Spacer(Modifier.height(6.dp))
        Text("Record a payment", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "Outstanding right now: $balance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            singleLine = true,
            textStyle = MoneyStyle,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            label = "Record payment",
            onClick = { onSave(amount) },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
