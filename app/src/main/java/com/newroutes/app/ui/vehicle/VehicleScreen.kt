package com.newroutes.app.ui.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.newroutes.app.domain.model.TollCategory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleScreen(
    modifier: Modifier = Modifier,
    viewModel: VehicleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            delay(1500)
            viewModel.resetForm()
        }
    }

    LaunchedEffect(uiState.vehicles) {
        if (uiState.vehicles.isEmpty() && uiState.name.isBlank()) {
            viewModel.resetForm()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Meus Veículos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(modifier = Modifier.size(48.dp))
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            if (uiState.vehicles.isNotEmpty()) {
                Text(
                    text = "Veículos cadastrados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.vehicles, key = { it.id }) { vehicle ->
                        VehicleListItem(
                            vehicle = vehicle,
                            onSetDefault = viewModel::setDefault,
                            onDismiss = {
                                viewModel.deleteVehicle(vehicle.id)
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Novo Veículo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChanged,
                        label = { Text("Nome do veículo") },
                        placeholder = { Text("Ex: Meu Carro") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Text(
                        text = "Categoria",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(TollCategory.entries) { category ->
                            CategoryChip(
                                category = category,
                                selected = uiState.category == category,
                                onClick = { viewModel.onCategoryChanged(category) }
                            )
                        }
                    }

                    val consumptionPlaceholder = when (uiState.category) {
                        TollCategory.TRUCK_2_AXLES, TollCategory.TRUCK_3_AXLES,
                        TollCategory.TRUCK_4_AXLES, TollCategory.TRUCK_5_AXLES,
                        TollCategory.TRUCK_6_AXLES -> "Ex: 4.0"
                        TollCategory.BUS -> "Ex: 6.0"
                        else -> "Ex: 12.0"
                    }

                    TextField(
                        value = uiState.fuelConsumption,
                        onValueChange = viewModel::onFuelConsumptionChanged,
                        label = { Text("Consumo (km/l)") },
                        placeholder = { Text(consumptionPlaceholder) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    TextField(
                        value = uiState.fuelPrice,
                        onValueChange = viewModel::onFuelPriceChanged,
                        label = { Text("Preço do combustível (R\$/l)") },
                        placeholder = { Text("Ex: 6.50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = if (uiState.vehicles.isEmpty()) true else uiState.isDefault,
                            onCheckedChange = {
                                if (uiState.vehicles.isNotEmpty()) {
                                    viewModel.onIsDefaultChanged(it)
                                }
                            },
                            enabled = uiState.vehicles.isNotEmpty()
                        )
                        Text(
                            text = if (uiState.vehicles.isEmpty()) "Padrão (primeiro veículo)" else "Definir como padrão",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    androidx.compose.material3.Button(
                        onClick = { viewModel.saveVehicle() },
                        enabled = !uiState.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = " Salvando...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = " Salvar veículo",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        )
    }
}

@Composable
private fun CategoryChip(
    category: TollCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val label = category.label

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { },
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun VehicleListItem(
    vehicle: com.newroutes.app.domain.model.Vehicle,
    onSetDefault: (java.util.UUID) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val dismissState = remember(density) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            density = density,
            confirmValueChange = { state ->
                if (state == SwipeToDismissBoxValue.EndToStart) {
                    onDismiss()
                    true
                } else {
                    false
                }
            },
            positionalThreshold = { distance -> distance * 0.5f }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFB71C1C))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        content = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconRes = when (vehicle.category) {
                        TollCategory.MOTORCYCLE -> Icons.Default.TwoWheeler
                        TollCategory.CAR -> Icons.Default.DirectionsCar
                        TollCategory.BUS -> Icons.Default.DirectionsBus
                        TollCategory.TRUCK_2_AXLES,
                        TollCategory.TRUCK_3_AXLES,
                        TollCategory.TRUCK_4_AXLES,
                        TollCategory.TRUCK_5_AXLES,
                        TollCategory.TRUCK_6_AXLES -> Icons.Default.LocalShipping
                        TollCategory.CAR_WITH_TRAILER -> Icons.Default.DirectionsCar
                    }

                    Icon(
                        imageVector = iconRes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = vehicle.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${vehicle.fuelConsumptionKmPerLiter} km/l · R$ ${"%.2f".format(vehicle.fuelPricePerLiter)}/l",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (vehicle.isDefault) {
                        androidx.compose.material3.FilterChip(
                            selected = true,
                            onClick = { },
                            label = {
                                Text(
                                    "Padrão",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    } else {
                        TextButton(
                            onClick = { onSetDefault(vehicle.id) }
                        ) {
                            Text(
                                text = "Padrão",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

private val TollCategory.label: String
    get() = when (this) {
        TollCategory.CAR -> "Carro"
        TollCategory.MOTORCYCLE -> "Moto"
        TollCategory.CAR_WITH_TRAILER -> "Carro c/ Reboque"
        TollCategory.TRUCK_2_AXLES -> "Caminhão 2 eixos"
        TollCategory.TRUCK_3_AXLES -> "Caminhão 3 eixos"
        TollCategory.TRUCK_4_AXLES -> "Caminhão 4 eixos"
        TollCategory.TRUCK_5_AXLES -> "Caminhão 5 eixos"
        TollCategory.TRUCK_6_AXLES -> "Caminhão 6 eixos"
        TollCategory.BUS -> "Ônibus"
    }
