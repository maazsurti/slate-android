package com.maazsurti.slate.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maazsurti.slate.core.domain.model.CustomerBalance
import com.maazsurti.slate.ui.common.EmptyNote
import com.maazsurti.slate.ui.common.accountWash
import com.maazsurti.slate.ui.common.Hairline
import com.maazsurti.slate.ui.common.MoneyText
import com.maazsurti.slate.ui.common.Overline
import com.maazsurti.slate.ui.common.Panel
import com.maazsurti.slate.ui.common.ScreenHeader
import com.maazsurti.slate.ui.common.Tag
import com.maazsurti.slate.ui.common.Tone
import com.maazsurti.slate.ui.common.display
import com.maazsurti.slate.ui.slateContainer
import com.maazsurti.slate.ui.theme.MoneyStyle
import com.maazsurti.slate.ui.vmFactory

@Composable
fun LedgerScreen(onOpenCustomer: (Long) -> Unit) {
    val container = slateContainer()
    val viewModel: LedgerViewModel = viewModel(factory = vmFactory { LedgerViewModel(container.ledger) })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScreenHeader(
                overline = "Accounts",
                title = "Credit ledger",
                meta = "${state.owingCount} customers owe money · ${state.balances.size} on the books",
            )
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                item {
                    Panel(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    ) {
                        Overline("Outstanding")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            state.totalOutstanding.display(),
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Written up on credit, not yet paid back",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (state.balances.isEmpty()) {
                    item { EmptyNote("No accounts yet", "Customers appear here once they buy on credit.") }
                }
                items(state.balances, key = { it.customerId }) { row ->
                    CustomerRow(row, onClick = { onOpenCustomer(row.customerId) })
                    Hairline()
                }
            }
        }
    }
}

@Composable
private fun CustomerRow(row: CustomerBalance, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (wash, ink) = accountWash(row.customerId)
        Box(
            modifier = Modifier.size(34.dp).background(wash, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                row.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = ink,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(row.name, style = MaterialTheme.typography.titleMedium)
            Text(
                row.phone.ifBlank { "No phone number" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            MoneyText(row.balance, style = MoneyStyle, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            if (row.owes) Tag("Owing", Tone.Negative) else Tag("Settled", Tone.Positive)
        }
    }
}
