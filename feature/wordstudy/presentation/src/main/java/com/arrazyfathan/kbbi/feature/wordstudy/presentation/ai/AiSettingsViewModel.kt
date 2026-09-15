package com.arrazyfathan.kbbi.feature.wordstudy.presentation.ai

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomProviderModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.GetAiProvidersUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.RemoveAiConfigurationUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SaveAiConfigurationUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SelectAiProviderModeUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SelectBackendAiProviderUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SelectCustomAiProviderUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.TestAiConnectionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class AiSettingsState(
    val providerMode: AiProviderMode = AiProviderMode.BACKEND,
    val backendProviders: List<AiBackendProviderModel> = emptyList(),
    val selectedBackendProviderId: String? = null,
    val selectedBackendModel: String? = null,
    val customProviders: List<AiCustomProviderModel> = emptyList(),
    val selectedCustomProviderId: String? = null,
    val isLoadingBackendProviders: Boolean = false,
    val backendProvidersError: Boolean = false,
    val isEditorVisible: Boolean = false,
    val editingProviderId: String? = null,
    val providerName: String = "",
    val baseUrl: String = "",
    val models: List<String> = listOf(""),
    val selectedModelIndex: Int = 0,
    val apiKey: String = "",
    val hasSavedApiKey: Boolean = false,
    val isApiKeyVisible: Boolean = false,
    val isSaving: Boolean = false,
    val isTesting: Boolean = false,
    val removingProviderId: String? = null,
)

sealed interface AiSettingsAction {
    data class OnProviderModeSelected(
        val mode: AiProviderMode,
    ) : AiSettingsAction

    data object OnRefreshBackendProviders : AiSettingsAction

    data class OnBackendProviderSelected(
        val providerId: String,
    ) : AiSettingsAction

    data class OnBackendModelSelected(
        val model: String,
    ) : AiSettingsAction

    data class OnCustomProviderSelected(
        val providerId: String,
    ) : AiSettingsAction

    data class OnCustomModelSelected(
        val providerId: String,
        val model: String,
    ) : AiSettingsAction

    data object OnAddCustomProvider : AiSettingsAction

    data class OnEditCustomProvider(
        val providerId: String,
    ) : AiSettingsAction

    data object OnCancelEditor : AiSettingsAction

    data class OnProviderNameChanged(
        val value: String,
    ) : AiSettingsAction

    data class OnBaseUrlChanged(
        val value: String,
    ) : AiSettingsAction

    data class OnModelChanged(
        val index: Int,
        val value: String,
    ) : AiSettingsAction

    data object OnAddModel : AiSettingsAction

    data class OnRemoveModel(
        val index: Int,
    ) : AiSettingsAction

    data class OnEditorModelSelected(
        val index: Int,
    ) : AiSettingsAction

    data class OnApiKeyChanged(
        val value: String,
    ) : AiSettingsAction

    data object OnApiKeyVisibilityToggled : AiSettingsAction

    data object OnSave : AiSettingsAction

    data object OnTestConnection : AiSettingsAction

    data class OnRemoveProvider(
        val providerId: String,
    ) : AiSettingsAction
}

sealed interface AiSettingsEvent {
    data class ShowMessage(
        @param:androidx.annotation.StringRes val messageResId: Int,
        val isError: Boolean,
    ) : AiSettingsEvent
}

class AiSettingsViewModel(
    private val configurationRepository: AiConfigurationRepository,
    private val getProviders: GetAiProvidersUseCase,
    private val saveConfiguration: SaveAiConfigurationUseCase,
    private val selectProviderMode: SelectAiProviderModeUseCase,
    private val selectBackendProvider: SelectBackendAiProviderUseCase,
    private val selectCustomProvider: SelectCustomAiProviderUseCase,
    private val removeConfiguration: RemoveAiConfigurationUseCase,
    private val testConnection: TestAiConnectionUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(AiSettingsState())
    val state = _state.asStateFlow()
    private val _events = Channel<AiSettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            configurationRepository.configuration.collect { configuration ->
                _state.update {
                    it.copy(
                        providerMode = configuration.providerMode,
                        selectedBackendProviderId = configuration.backendProviderId ?: it.selectedBackendProviderId,
                        selectedBackendModel = configuration.backendModel ?: it.selectedBackendModel,
                        customProviders = configuration.customProviders,
                        selectedCustomProviderId = configuration.selectedCustomProviderId,
                    )
                }
            }
        }
        loadBackendProviders()
    }

    fun onAction(action: AiSettingsAction) {
        when (action) {
            is AiSettingsAction.OnProviderModeSelected -> {
                selectMode(action.mode)
            }

            AiSettingsAction.OnRefreshBackendProviders -> {
                loadBackendProviders()
            }

            is AiSettingsAction.OnBackendProviderSelected -> {
                selectBackend(action.providerId, null)
            }

            is AiSettingsAction.OnBackendModelSelected -> {
                selectBackend(_state.value.selectedBackendProviderId, action.model)
            }

            is AiSettingsAction.OnCustomProviderSelected -> {
                selectCustom(action.providerId, null)
            }

            is AiSettingsAction.OnCustomModelSelected -> {
                selectCustom(action.providerId, action.model)
            }

            AiSettingsAction.OnAddCustomProvider -> {
                openEditor(null)
            }

            is AiSettingsAction.OnEditCustomProvider -> {
                openEditor(action.providerId)
            }

            AiSettingsAction.OnCancelEditor -> {
                closeEditor()
            }

            is AiSettingsAction.OnProviderNameChanged -> {
                _state.update { it.copy(providerName = action.value) }
            }

            is AiSettingsAction.OnBaseUrlChanged -> {
                _state.update { it.copy(baseUrl = action.value) }
            }

            is AiSettingsAction.OnModelChanged -> {
                updateModel(action.index, action.value)
            }

            AiSettingsAction.OnAddModel -> {
                _state.update { it.copy(models = it.models + "") }
            }

            is AiSettingsAction.OnRemoveModel -> {
                removeModel(action.index)
            }

            is AiSettingsAction.OnEditorModelSelected -> {
                _state.update { it.copy(selectedModelIndex = action.index) }
            }

            is AiSettingsAction.OnApiKeyChanged -> {
                _state.update { it.copy(apiKey = action.value) }
            }

            AiSettingsAction.OnApiKeyVisibilityToggled -> {
                _state.update { it.copy(isApiKeyVisible = !it.isApiKeyVisible) }
            }

            AiSettingsAction.OnSave -> {
                save()
            }

            AiSettingsAction.OnTestConnection -> {
                test()
            }

            is AiSettingsAction.OnRemoveProvider -> {
                remove(action.providerId)
            }
        }
    }

    private fun loadBackendProviders() {
        if (_state.value.isLoadingBackendProviders) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingBackendProviders = true, backendProvidersError = false) }
            when (val result = getProviders()) {
                is AppResult.Success -> {
                    val configuration = configurationRepository.configuration.first()
                    val providers = result.data.providers
                    val selectedProvider =
                        providers.firstOrNull { it.id == configuration.backendProviderId }
                            ?: providers.firstOrNull { it.id == result.data.defaultProvider }
                            ?: providers.firstOrNull()
                    val selectedModel =
                        configuration.backendModel?.takeIf { it in selectedProvider?.models.orEmpty() }
                            ?: selectedProvider?.defaultModel
                    _state.update {
                        it.copy(
                            backendProviders = providers,
                            selectedBackendProviderId = selectedProvider?.id,
                            selectedBackendModel = selectedModel,
                            isLoadingBackendProviders = false,
                        )
                    }
                }

                is AppResult.Error -> {
                    _state.update { it.copy(isLoadingBackendProviders = false, backendProvidersError = true) }
                }
            }
        }
    }

    private fun selectMode(mode: AiProviderMode) {
        viewModelScope.launch {
            when (selectProviderMode(mode)) {
                is AppResult.Success -> showMessage(R.string.ai_settings_mode_changed)
                is AppResult.Error -> showMessage(R.string.ai_settings_custom_incomplete, true)
            }
        }
    }

    private fun selectBackend(
        providerId: String?,
        requestedModel: String?,
    ) {
        val provider = _state.value.backendProviders.firstOrNull { it.id == providerId } ?: return
        val model = requestedModel?.takeIf { it in provider.models } ?: provider.defaultModel
        viewModelScope.launch {
            when (selectBackendProvider(provider.id, model)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(selectedBackendProviderId = provider.id, selectedBackendModel = model)
                    }
                }

                is AppResult.Error -> {
                    showMessage(R.string.ai_settings_selection_failed, true)
                }
            }
        }
    }

    private fun selectCustom(
        providerId: String,
        requestedModel: String?,
    ) {
        val provider = _state.value.customProviders.firstOrNull { it.id == providerId } ?: return
        val model = requestedModel?.takeIf { it in provider.models } ?: provider.selectedModel
        viewModelScope.launch {
            when (selectCustomProvider(provider.id, model)) {
                is AppResult.Success -> Unit
                is AppResult.Error -> showMessage(R.string.ai_settings_selection_failed, true)
            }
        }
    }

    private fun openEditor(providerId: String?) {
        val provider = _state.value.customProviders.firstOrNull { it.id == providerId }
        _state.update {
            it.copy(
                isEditorVisible = true,
                editingProviderId = provider?.id,
                providerName = provider?.name.orEmpty(),
                baseUrl = provider?.baseUrl.orEmpty(),
                models = provider?.models?.takeIf(List<String>::isNotEmpty) ?: listOf(""),
                selectedModelIndex = provider?.models?.indexOf(provider.selectedModel)?.coerceAtLeast(0) ?: 0,
                apiKey = "",
                hasSavedApiKey = provider?.hasApiKey == true,
                isApiKeyVisible = false,
            )
        }
    }

    private fun closeEditor() {
        _state.update {
            it.copy(
                isEditorVisible = false,
                editingProviderId = null,
                providerName = "",
                baseUrl = "",
                models = listOf(""),
                selectedModelIndex = 0,
                apiKey = "",
                hasSavedApiKey = false,
                isApiKeyVisible = false,
            )
        }
    }

    private fun updateModel(
        index: Int,
        value: String,
    ) {
        if (index !in _state.value.models.indices) return
        _state.update { current ->
            current.copy(
                models =
                    current.models.mapIndexed { modelIndex, model ->
                        if (modelIndex ==
                            index
                        ) {
                            value
                        } else {
                            model
                        }
                    },
            )
        }
    }

    private fun removeModel(index: Int) {
        val current = _state.value
        if (current.models.size <= 1 || index !in current.models.indices) return
        val models = current.models.filterIndexed { modelIndex, _ -> modelIndex != index }
        val selectedIndex =
            when {
                current.selectedModelIndex == index -> 0
                current.selectedModelIndex > index -> current.selectedModelIndex - 1
                else -> current.selectedModelIndex
            }
        _state.update { it.copy(models = models, selectedModelIndex = selectedIndex) }
    }

    private fun save() {
        val current = _state.value
        if (current.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val selectedModel = current.models.getOrNull(current.selectedModelIndex).orEmpty()
            when (
                saveConfiguration(
                    providerId = current.editingProviderId,
                    name = current.providerName,
                    baseUrl = current.baseUrl,
                    models = current.models,
                    selectedModel = selectedModel,
                    apiKey = current.apiKey.takeIf(String::isNotBlank),
                )
            ) {
                is AppResult.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    closeEditor()
                    showMessage(R.string.ai_settings_saved)
                }

                is AppResult.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    showMessage(R.string.ai_settings_invalid_configuration, true)
                }
            }
        }
    }

    private fun test() {
        val current = _state.value
        if (current.isTesting) return
        viewModelScope.launch {
            _state.update { it.copy(isTesting = true) }
            val selectedModel = current.models.getOrNull(current.selectedModelIndex).orEmpty()
            when (
                testConnection(
                    providerId = current.editingProviderId,
                    name = current.providerName,
                    baseUrl = current.baseUrl,
                    model = selectedModel,
                    apiKey = current.apiKey,
                )
            ) {
                is AppResult.Success -> showMessage(R.string.ai_settings_connection_success)
                is AppResult.Error -> showMessage(R.string.ai_settings_connection_failed, true)
            }
            _state.update { it.copy(isTesting = false) }
        }
    }

    private fun remove(providerId: String) {
        if (_state.value.removingProviderId != null) return
        viewModelScope.launch {
            _state.update { it.copy(removingProviderId = providerId) }
            when (removeConfiguration(providerId)) {
                is AppResult.Success -> {
                    if (_state.value.editingProviderId == providerId) closeEditor()
                    showMessage(R.string.ai_settings_removed)
                }

                is AppResult.Error -> {
                    showMessage(R.string.ai_settings_remove_failed, true)
                }
            }
            _state.update { it.copy(removingProviderId = null) }
        }
    }

    private suspend fun showMessage(
        messageResId: Int,
        isError: Boolean = false,
    ) {
        _events.send(AiSettingsEvent.ShowMessage(messageResId, isError))
    }
}
