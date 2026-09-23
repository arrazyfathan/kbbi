package com.arrazyfathan.kbbi.feature.figure.presentation.figure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.arrazyfathan.kbbi.feature.figure.domain.usecase.GetFiguresUseCase

class FigureViewModel(
    getFigures: GetFiguresUseCase,
) : ViewModel() {
    val figures = getFigures(includeDetails = true).cachedIn(viewModelScope)
}
