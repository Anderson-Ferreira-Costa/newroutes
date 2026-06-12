package com.newroutes.app.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.usecase.GetRoutesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SavedRoutesViewModel @Inject constructor(
    getRoutesUseCase: GetRoutesUseCase
) : ViewModel() {

    val routes: StateFlow<List<Route>> = getRoutesUseCase.invoke()
        .map { routes -> routes.sortedBy { it.id } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
