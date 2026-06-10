package com.newroutes.app.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

// TODO: Tela de detalhes da rota
// Deve exibir: lista de waypoints com endereços, distância, tempo estimado,
// lista de pedágios com custos, botão de confirmação
@Composable
fun RouteScreen(
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RouteViewModel = hiltViewModel()
) {
    // TODO: Collect uiState e renderizar:
    // - Lista de waypoints (ordenados)
    // - Card com distância e tempo
    // - Lista de pedágios com custo total
    // - Botões: Voltar e Confirmar
}
