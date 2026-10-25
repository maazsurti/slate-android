package com.maazsurti.slate.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maazsurti.slate.core.domain.model.Category
import com.maazsurti.slate.core.domain.model.Money
import com.maazsurti.slate.core.domain.model.Product
import com.maazsurti.slate.core.domain.repo.ProductRepository
import com.maazsurti.slate.core.domain.usecase.SaveProduct
import com.maazsurti.slate.core.domain.usecase.SaveProductResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StockUiState(
    val query: String = "",
    val products: List<Product> = emptyList(),
    val editing: Product? = null,
    val editorOpen: Boolean = false,
) {
    val visible: List<Product>
        get() = if (query.isBlank()) products else products.filter { it.name.contains(query, ignoreCase = true) }

    val lowStockCount: Int get() = products.count { it.isLowStock }
}

class StockViewModel(
    products: ProductRepository,
    private val saveProduct: SaveProduct,
) : ViewModel() {

    private data class Editor(val product: Product?, val open: Boolean)

    private val query = MutableStateFlow("")
    private val editor = MutableStateFlow(Editor(null, false))

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    val state: StateFlow<StockUiState> = combine(
        products.observeAll(),
        query,
        editor,
    ) { products, query, editor ->
        StockUiState(
            query = query,
            products = products,
            editing = editor.product,
            editorOpen = editor.open,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StockUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun startAdd() {
        editor.value = Editor(null, true)
    }

    fun startEdit(product: Product) {
        editor.value = Editor(product, true)
    }

    fun closeEditor() {
        editor.value = Editor(null, false)
    }

    fun save(name: String, priceText: String, stockText: String, thresholdText: String, category: Category) {
        val price = Money.parse(priceText)
        val stock = stockText.trim().toIntOrNull()
        val threshold = thresholdText.trim().toIntOrNull() ?: 5
        if (price == null || stock == null) {
            _messages.trySend("Price and stock need a number")
            return
        }
        val existing = editor.value.product
        viewModelScope.launch {
            val result = saveProduct(
                Product(
                    id = existing?.id ?: 0,
                    name = name,
                    price = price,
                    stock = stock,
                    lowStockThreshold = threshold,
                    category = category,
                )
            )
            _messages.send(when (result) {
                is SaveProductResult.Saved -> {
                    editor.value = Editor(null, false)
                    if (existing == null) "Added ${name.trim()}" else "Saved ${name.trim()}"
                }
                SaveProductResult.BlankName -> "Give the product a name"
                SaveProductResult.NegativePrice -> "Price cannot be negative"
                SaveProductResult.NegativeStock -> "Stock cannot be negative"
            })
        }
    }

    fun delete(product: Product) {
        viewModelScope.launch {
            editor.value = Editor(null, false)
            _messages.send("${product.name} removed")
        }
    }
}
