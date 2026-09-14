package com.arrazyfathan.kbbi.feature.settings.presentation.ai

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.feature.home.domain.model.AiBackendProviderModel
import com.arrazyfathan.kbbi.feature.home.domain.model.AiCustomProviderModel
import com.arrazyfathan.kbbi.feature.home.domain.model.AiProviderMode
import org.koin.androidx.compose.koinViewModel

@Composable
fun AiSettingsRoute(
    onNavigateBack: () -> Unit,
    onHaptic: (KBBIHapticType) -> Unit,
) {
    val viewModel: AiSettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AiSettingsEvent.ShowMessage -> {
                    Toast.makeText(context, event.messageResId, Toast.LENGTH_SHORT).show()
                    onHaptic(if (event.isError) KBBIHapticType.Reject else KBBIHapticType.Confirm)
                }
            }
        }
    }
    AiSettingsScreen(state, onNavigateBack, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    state: AiSettingsState,
    onNavigateBack: () -> Unit,
    onAction: (AiSettingsAction) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AiSettingsTopAppBar(onNavigateBack) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .navigationBarsPadding()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProviderSection(state.providerMode, onAction)
            BackendProvidersSection(state, onAction)
            CustomProvidersSection(state, onAction)
            if (state.isEditorVisible) CustomProviderEditor(state, onAction)
            PrivacyNote()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiSettingsTopAppBar(onNavigateBack: () -> Unit) {
    MediumTopAppBar(
        modifier =
            Modifier.background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
                ),
            ),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        title = {
            Column {
                Text(
                    text = stringResource(R.string.ai_settings_title),
                    fontFamily = MetropolisFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.ai_settings_menu_subtitle),
                    fontFamily = InterFontFamily,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(16.dp))
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    )
}

@Composable
private fun ProviderSection(
    selectedMode: AiProviderMode,
    onAction: (AiSettingsAction) -> Unit,
) {
    SettingsCard {
        SectionHeader(
            title = stringResource(R.string.ai_settings_section_title),
            description = stringResource(R.string.ai_settings_description),
        )
        SettingsDivider()
        Column(Modifier.selectableGroup()) {
            ProviderRow(
                title = stringResource(R.string.ai_settings_backend_title),
                description = stringResource(R.string.ai_settings_backend_description),
                iconRes = R.drawable.ic_auto_awesome,
                selected = selectedMode == AiProviderMode.BACKEND,
                onClick = { onAction(AiSettingsAction.OnProviderModeSelected(AiProviderMode.BACKEND)) },
            )
            SettingsDivider()
            ProviderRow(
                title = stringResource(R.string.ai_settings_custom_title),
                description = stringResource(R.string.ai_settings_custom_description),
                iconRes = R.drawable.ic_code,
                selected = selectedMode == AiProviderMode.CUSTOM,
                onClick = { onAction(AiSettingsAction.OnProviderModeSelected(AiProviderMode.CUSTOM)) },
            )
        }
    }
}

@Composable
private fun ProviderRow(
    title: String,
    description: String,
    @DrawableRes iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextH1)
            Spacer(Modifier.height(2.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextP)
        }
        Spacer(Modifier.width(8.dp))
        RadioButton(selected = selected, onClick = null)
    }
}

@Composable
private fun BackendProvidersSection(
    state: AiSettingsState,
    onAction: (AiSettingsAction) -> Unit,
) {
    SettingsCard {
        SectionHeader(
            title = stringResource(R.string.ai_settings_backend_providers_title),
            description = stringResource(R.string.ai_settings_backend_providers_description),
        )
        SettingsDivider()
        when {
            state.isLoadingBackendProviders -> {
                LoadingRow(stringResource(R.string.ai_settings_loading_providers))
            }

            state.backendProvidersError -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.ai_settings_provider_load_failed),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextP,
                    )
                    TextButton(onClick = { onAction(AiSettingsAction.OnRefreshBackendProviders) }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }

            state.backendProviders.isEmpty() -> {
                Text(
                    text = stringResource(R.string.ai_settings_no_backend_providers),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextP,
                    modifier = Modifier.padding(18.dp),
                )
            }

            else -> {
                Column(Modifier.selectableGroup()) {
                    state.backendProviders.forEachIndexed { index, provider ->
                        BackendProviderRow(
                            provider = provider,
                            selectedProviderId = state.selectedBackendProviderId,
                            selectedModel = state.selectedBackendModel,
                            onAction = onAction,
                        )
                        if (index != state.backendProviders.lastIndex) SettingsDivider()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BackendProviderRow(
    provider: AiBackendProviderModel,
    selectedProviderId: String?,
    selectedModel: String?,
    onAction: (AiSettingsAction) -> Unit,
) {
    val selected = provider.id == selectedProviderId
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = { onAction(AiSettingsAction.OnBackendProviderSelected(provider.id)) },
                    role = Role.RadioButton,
                ).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(provider.id, style = MaterialTheme.typography.titleSmall, color = TextH1)
                Text(
                    text = stringResource(R.string.ai_settings_default_model, provider.defaultModel),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextP,
                )
            }
            RadioButton(selected = selected, onClick = null)
        }
        if (selected) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                provider.models.forEach { model ->
                    FilterChip(
                        selected = model == selectedModel,
                        onClick = { onAction(AiSettingsAction.OnBackendModelSelected(model)) },
                        label = { Text(model) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomProvidersSection(
    state: AiSettingsState,
    onAction: (AiSettingsAction) -> Unit,
) {
    SettingsCard {
        SectionHeader(
            title = stringResource(R.string.ai_settings_custom_providers_title),
            description = stringResource(R.string.ai_settings_custom_providers_description),
        )
        SettingsDivider()
        if (state.customProviders.isEmpty()) {
            Text(
                text = stringResource(R.string.ai_settings_no_custom_providers),
                style = MaterialTheme.typography.bodySmall,
                color = TextP,
                modifier = Modifier.padding(18.dp),
            )
        } else {
            Column(Modifier.selectableGroup()) {
                state.customProviders.forEachIndexed { index, provider ->
                    CustomProviderRow(provider, state, onAction)
                    if (index != state.customProviders.lastIndex) SettingsDivider()
                }
            }
        }
        OutlinedButton(
            onClick = { onAction(AiSettingsAction.OnAddCustomProvider) },
            shape = RoundedCornerShape(100.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Text(stringResource(R.string.ai_settings_add_provider))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomProviderRow(
    provider: AiCustomProviderModel,
    state: AiSettingsState,
    onAction: (AiSettingsAction) -> Unit,
) {
    val selected = provider.id == state.selectedCustomProviderId
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = { onAction(AiSettingsAction.OnCustomProviderSelected(provider.id)) },
                    role = Role.RadioButton,
                ).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(provider.name, style = MaterialTheme.typography.titleSmall, color = TextH1)
                Text(provider.baseUrl, style = MaterialTheme.typography.bodySmall, color = TextP)
            }
            IconButton(onClick = { onAction(AiSettingsAction.OnEditCustomProvider(provider.id)) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_document),
                    contentDescription = stringResource(R.string.ai_settings_edit_provider),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(
                onClick = { onAction(AiSettingsAction.OnRemoveProvider(provider.id)) },
                enabled = state.removingProviderId == null,
            ) {
                if (state.removingProviderId == provider.id) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.ai_settings_remove_provider),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            provider.models.forEach { model ->
                FilterChip(
                    selected = selected && model == provider.selectedModel,
                    onClick = { onAction(AiSettingsAction.OnCustomModelSelected(provider.id, model)) },
                    label = { Text(model) },
                )
            }
        }
    }
}

@Composable
private fun CustomProviderEditor(
    state: AiSettingsState,
    onAction: (AiSettingsAction) -> Unit,
) {
    val actionsEnabled = !state.isSaving && !state.isTesting && state.removingProviderId == null
    SettingsCard {
        SectionHeader(
            title =
                stringResource(
                    if (state.editingProviderId == null) {
                        R.string.ai_settings_add_provider
                    } else {
                        R.string.ai_settings_edit_provider
                    },
                ),
            description = stringResource(R.string.ai_settings_provider_name_description),
        )
        SettingsDivider()
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.providerName,
                onValueChange = { onAction(AiSettingsAction.OnProviderNameChanged(it)) },
                label = { Text(stringResource(R.string.ai_settings_provider_name)) },
                supportingText = { Text(stringResource(R.string.ai_settings_provider_name_hint)) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = { onAction(AiSettingsAction.OnBaseUrlChanged(it)) },
                label = { Text(stringResource(R.string.ai_settings_base_url)) },
                supportingText = { Text(stringResource(R.string.ai_settings_base_url_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.ai_settings_models),
                style = MaterialTheme.typography.titleSmall,
                color = TextH1,
            )
            state.models.forEachIndexed { index, model ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = index == state.selectedModelIndex,
                        onClick = { onAction(AiSettingsAction.OnEditorModelSelected(index)) },
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { onAction(AiSettingsAction.OnModelChanged(index, it)) },
                        label = { Text(stringResource(R.string.ai_settings_model_number, index + 1)) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.models.size > 1) {
                        IconButton(onClick = { onAction(AiSettingsAction.OnRemoveModel(index)) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = stringResource(R.string.ai_settings_remove_model),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
            TextButton(onClick = { onAction(AiSettingsAction.OnAddModel) }) {
                Text(stringResource(R.string.ai_settings_add_model))
            }
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = { onAction(AiSettingsAction.OnApiKeyChanged(it)) },
                label = {
                    Text(
                        stringResource(
                            if (state.hasSavedApiKey) {
                                R.string.ai_settings_replace_key
                            } else {
                                R.string.ai_settings_api_key
                            },
                        ),
                    )
                },
                placeholder = {
                    if (state.hasSavedApiKey && state.apiKey.isEmpty()) Text("••••••••••••")
                },
                visualTransformation =
                    if (state.isApiKeyVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { onAction(AiSettingsAction.OnApiKeyVisibilityToggled) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_eye),
                            contentDescription = stringResource(R.string.ai_settings_toggle_key_visibility),
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onAction(AiSettingsAction.OnSave) },
                    enabled = actionsEnabled,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextH1),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ActionContent(
                        loading = state.isSaving,
                        label = stringResource(R.string.ai_settings_save),
                        progressColor = Color.White,
                    )
                }
                OutlinedButton(
                    onClick = { onAction(AiSettingsAction.OnTestConnection) },
                    enabled = actionsEnabled,
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ActionContent(
                        loading = state.isTesting,
                        label = stringResource(R.string.ai_settings_test),
                        progressColor = MaterialTheme.colorScheme.primary,
                    )
                }
                TextButton(
                    onClick = { onAction(AiSettingsAction.OnCancelEditor) },
                    enabled = actionsEnabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
private fun LoadingRow(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextP)
    }
}

@Composable
private fun ActionContent(
    loading: Boolean,
    label: String,
    progressColor: Color,
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = progressColor,
            strokeWidth = 2.dp,
        )
    } else {
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PrivacyNote() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_privacy),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.ai_settings_privacy_note),
            style = MaterialTheme.typography.bodySmall,
            color = TextP,
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextH1)
        Text(text = description, style = MaterialTheme.typography.bodySmall, color = TextP)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp),
        color = MaterialTheme.colorScheme.background,
    )
}

@Preview(showBackground = true)
@Composable
private fun AiSettingsPreview() {
    KBBITheme {
        AiSettingsScreen(
            state =
                AiSettingsState(
                    providerMode = AiProviderMode.CUSTOM,
                    backendProviders =
                        listOf(
                            com.arrazyfathan.kbbi.feature.home.domain.model.AiBackendProviderModel(
                                id = "openai",
                                defaultModel = "gpt-5-mini",
                                models = listOf("gpt-5-mini", "gpt-5"),
                            ),
                        ),
                    selectedBackendProviderId = "openai",
                    selectedBackendModel = "gpt-5-mini",
                    customProviders =
                        listOf(
                            com.arrazyfathan.kbbi.feature.home.domain.model.AiCustomProviderModel(
                                id = "one",
                                name = "OpenRouter",
                                baseUrl = "https://openrouter.ai/api/v1",
                                models = listOf("model-fast", "model-smart"),
                                selectedModel = "model-fast",
                                hasApiKey = true,
                            ),
                        ),
                    selectedCustomProviderId = "one",
                ),
            onNavigateBack = {},
            onAction = {},
        )
    }
}
