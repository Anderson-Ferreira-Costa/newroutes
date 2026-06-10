package com.newroutes.app.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

// TODO: Tela de resumo da rota
// Deve exibir: resumo completo da viagem, mapa miniatura, breakdown de custos,
// opções de salvar/compartilhar rota, botão de voltar
@Composable
fun SummaryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    // TODO: Collect uiState e renderizar:
    // - Card de resumo com distância, tempo e custo
    // - Lista de waypoints com nomes
    // - Lista de pedágios com custos individuais
    // - Mapa miniatura da rota
    // - Botões: Salvar rota e Voltar
}
