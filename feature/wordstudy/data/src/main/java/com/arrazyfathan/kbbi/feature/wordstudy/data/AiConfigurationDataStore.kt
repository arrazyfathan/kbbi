package com.arrazyfathan.kbbi.feature.wordstudy.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiConfigurationModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomProviderModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.coroutines.cancellation.CancellationException

private val Context.aiConfigurationDataStore by preferencesDataStore(name = "ai_configuration")

class AiConfigurationDataStore(
    private val context: Context,
    private val json: Json,
) : AiConfigurationRepository {
    private val mutationMutex = Mutex()
    private val secretCipher = AiSecretCipher()

    override val configuration: Flow<AiConfigurationModel> =
        context.aiConfigurationDataStore.data.map { preferences -> preferences.toConfiguration(json) }

    override suspend fun saveCustomProvider(
        providerId: String?,
        name: String,
        baseUrl: String,
        models: List<String>,
        selectedModel: String,
        apiKey: String?,
    ): AppResult<String, DataError> =
        mutationMutex.withLock {
            try {
                val preferences = context.aiConfigurationDataStore.data.first()
                val providers = preferences.storedProviders(json).toMutableList()
                val resolvedId = providerId ?: UUID.randomUUID().toString()
                val existingIndex = providers.indexOfFirst { it.id == resolvedId }
                if (providerId != null && existingIndex < 0) return@withLock AppResult.Error(DataError.NotFound)

                val apiKeys =
                    readApiKeys(preferences)
                        .getOrElse {
                            return@withLock AppResult.Error(DataError.Unknown)
                        }.toMutableMap()
                if (apiKey != null) apiKeys[resolvedId] = apiKey
                if (apiKeys[resolvedId].isNullOrBlank()) return@withLock AppResult.Error(DataError.BadRequest)

                val stored =
                    StoredCustomProvider(
                        id = resolvedId,
                        name = name,
                        baseUrl = baseUrl,
                        models = models,
                        selectedModel = selectedModel,
                        hasApiKey = true,
                    )
                if (existingIndex >= 0) providers[existingIndex] = stored else providers += stored
                val encryptedKeys = secretCipher.encrypt(json.encodeToString(apiKeys))

                context.aiConfigurationDataStore.edit { mutablePreferences ->
                    mutablePreferences[CUSTOM_PROVIDERS_JSON] = json.encodeToString(providers)
                    mutablePreferences[ENCRYPTED_CUSTOM_API_KEYS] = encryptedKeys.ciphertext
                    mutablePreferences[ENCRYPTED_CUSTOM_API_KEYS_IV] = encryptedKeys.iv
                    if (mutablePreferences[SELECTED_CUSTOM_PROVIDER_ID] == null) {
                        mutablePreferences[SELECTED_CUSTOM_PROVIDER_ID] = resolvedId
                    }
                    mutablePreferences.clearLegacyCustomConfiguration()
                    mutablePreferences.incrementRevision()
                }
                AppResult.Success(resolvedId)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                AppResult.Error(DataError.Unknown)
            }
        }

    override suspend fun selectProviderMode(mode: AiProviderMode): AppResult<Unit, DataError> =
        mutationMutex.withLock {
            try {
                if (mode == AiProviderMode.CUSTOM && readCredentials() !is AppResult.Success) {
                    return@withLock AppResult.Error(DataError.BadRequest)
                }
                context.aiConfigurationDataStore.edit { preferences ->
                    preferences[PROVIDER_MODE] = mode.name
                    preferences.incrementRevision()
                }
                AppResult.Success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                AppResult.Error(DataError.Unknown)
            }
        }

    override suspend fun selectBackendProvider(
        providerId: String,
        model: String,
    ): AppResult<Unit, DataError> =
        mutationMutex.withLock {
            try {
                if (providerId.isBlank() || model.isBlank()) return@withLock AppResult.Error(DataError.BadRequest)
                context.aiConfigurationDataStore.edit { preferences ->
                    preferences[BACKEND_PROVIDER_ID] = providerId
                    preferences[BACKEND_MODEL] = model
                    preferences.incrementRevision()
                }
                AppResult.Success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                AppResult.Error(DataError.Unknown)
            }
        }

    override suspend fun selectCustomProvider(
        providerId: String,
        model: String,
    ): AppResult<Unit, DataError> =
        mutationMutex.withLock {
            try {
                val preferences = context.aiConfigurationDataStore.data.first()
                val providers = preferences.storedProviders(json).toMutableList()
                val index = providers.indexOfFirst { it.id == providerId }
                val provider = providers.getOrNull(index)
                if (provider == null || model !in provider.models ||
                    providerId !in readApiKeys(preferences).getOrThrow()
                ) {
                    return@withLock AppResult.Error(DataError.BadRequest)
                }
                providers[index] = provider.copy(selectedModel = model)
                context.aiConfigurationDataStore.edit { mutablePreferences ->
                    mutablePreferences[CUSTOM_PROVIDERS_JSON] = json.encodeToString(providers)
                    mutablePreferences[SELECTED_CUSTOM_PROVIDER_ID] = providerId
                    mutablePreferences.incrementRevision()
                }
                AppResult.Success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                AppResult.Error(DataError.Unknown)
            }
        }

    override suspend fun removeCustomProvider(providerId: String): AppResult<Unit, DataError> =
        mutationMutex.withLock {
            try {
                val preferences = context.aiConfigurationDataStore.data.first()
                val providers = preferences.storedProviders(json).filterNot { it.id == providerId }
                if (providers.size == preferences.storedProviders(json).size) {
                    return@withLock AppResult.Error(DataError.NotFound)
                }
                val apiKeys =
                    readApiKeys(
                        preferences,
                    ).getOrDefault(emptyMap()).toMutableMap().apply { remove(providerId) }
                val selectedId = preferences.selectedCustomProviderId()
                val nextSelectedId =
                    selectedId.takeIf { id -> providers.any { it.id == id } } ?: providers.firstOrNull()?.id
                val encryptedKeys =
                    apiKeys.takeIf(Map<String, String>::isNotEmpty)?.let {
                        secretCipher.encrypt(json.encodeToString(it))
                    }
                context.aiConfigurationDataStore.edit { mutablePreferences ->
                    mutablePreferences[CUSTOM_PROVIDERS_JSON] = json.encodeToString(providers)
                    if (nextSelectedId == null) {
                        mutablePreferences.remove(SELECTED_CUSTOM_PROVIDER_ID)
                        mutablePreferences[PROVIDER_MODE] = AiProviderMode.BACKEND.name
                    } else {
                        mutablePreferences[SELECTED_CUSTOM_PROVIDER_ID] = nextSelectedId
                    }
                    if (encryptedKeys == null) {
                        mutablePreferences.remove(ENCRYPTED_CUSTOM_API_KEYS)
                        mutablePreferences.remove(ENCRYPTED_CUSTOM_API_KEYS_IV)
                    } else {
                        mutablePreferences[ENCRYPTED_CUSTOM_API_KEYS] = encryptedKeys.ciphertext
                        mutablePreferences[ENCRYPTED_CUSTOM_API_KEYS_IV] = encryptedKeys.iv
                    }
                    mutablePreferences.clearLegacyCustomConfiguration()
                    mutablePreferences.incrementRevision()
                }
                if (apiKeys.isEmpty()) secretCipher.deleteKey()
                AppResult.Success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                AppResult.Error(DataError.Unknown)
            }
        }

    override suspend fun getCustomCredentials(providerId: String?): AppResult<AiCustomCredentialsModel, DataError> =
        mutationMutex.withLock { readCredentials(providerId, clearUnrecoverable = true) }

    private suspend fun readCredentials(
        requestedProviderId: String? = null,
        clearUnrecoverable: Boolean = false,
    ): AppResult<AiCustomCredentialsModel, DataError> {
        val preferences = context.aiConfigurationDataStore.data.first()
        val providerId =
            requestedProviderId ?: preferences.selectedCustomProviderId()
                ?: return AppResult.Error(DataError.BadRequest)
        val provider =
            preferences.storedProviders(json).firstOrNull { it.id == providerId }
                ?: return AppResult.Error(DataError.BadRequest)
        val apiKeys = readApiKeys(preferences).getOrNull()
        val apiKey = apiKeys?.get(providerId)
        if (apiKey.isNullOrBlank()) {
            if (clearUnrecoverable) clearBrokenSecrets()
            return AppResult.Error(DataError.BadRequest)
        }
        return AppResult.Success(
            AiCustomCredentialsModel(
                id = provider.id,
                name = provider.name,
                baseUrl = provider.baseUrl,
                model = provider.selectedModel,
                apiKey = apiKey,
            ),
        )
    }

    private fun readApiKeys(preferences: Preferences): Result<Map<String, String>> =
        runCatching {
            val encrypted = preferences[ENCRYPTED_CUSTOM_API_KEYS]
            val iv = preferences[ENCRYPTED_CUSTOM_API_KEYS_IV]
            if (!encrypted.isNullOrBlank() && !iv.isNullOrBlank()) {
                json.decodeFromString<Map<String, String>>(secretCipher.decrypt(encrypted, iv))
            } else {
                val legacyEncrypted = preferences[LEGACY_ENCRYPTED_API_KEY]
                val legacyIv = preferences[LEGACY_ENCRYPTED_API_KEY_IV]
                if (legacyEncrypted.isNullOrBlank() || legacyIv.isNullOrBlank()) {
                    emptyMap()
                } else {
                    mapOf(LEGACY_PROVIDER_ID to secretCipher.decrypt(legacyEncrypted, legacyIv))
                }
            }
        }

    private suspend fun clearBrokenSecrets() {
        context.aiConfigurationDataStore.edit { preferences ->
            preferences[PROVIDER_MODE] = AiProviderMode.BACKEND.name
            preferences.remove(ENCRYPTED_CUSTOM_API_KEYS)
            preferences.remove(ENCRYPTED_CUSTOM_API_KEYS_IV)
            preferences.remove(LEGACY_ENCRYPTED_API_KEY)
            preferences.remove(LEGACY_ENCRYPTED_API_KEY_IV)
            val providers = preferences.storedProviders(json).map { it.copy(hasApiKey = false) }
            preferences[CUSTOM_PROVIDERS_JSON] = json.encodeToString(providers)
            preferences.incrementRevision()
        }
        runCatching { secretCipher.deleteKey() }
    }
}

private fun Preferences.toConfiguration(json: Json): AiConfigurationModel {
    val providers = storedProviders(json)
    return AiConfigurationModel(
        providerMode =
            runCatching { AiProviderMode.valueOf(this[PROVIDER_MODE].orEmpty()) }
                .getOrDefault(AiProviderMode.BACKEND),
        backendProviderId = this[BACKEND_PROVIDER_ID],
        backendModel = this[BACKEND_MODEL],
        customProviders = providers.map(StoredCustomProvider::toDomain),
        selectedCustomProviderId = selectedCustomProviderId(),
        revision = this[CONFIGURATION_REVISION] ?: 0L,
    )
}

private fun Preferences.storedProviders(json: Json): List<StoredCustomProvider> {
    this[CUSTOM_PROVIDERS_JSON]?.let { encoded ->
        return runCatching { json.decodeFromString<List<StoredCustomProvider>>(encoded) }.getOrDefault(emptyList())
    }
    val legacyBaseUrl = this[LEGACY_CUSTOM_BASE_URL].orEmpty()
    val legacyModel = this[LEGACY_CUSTOM_MODEL].orEmpty()
    if (legacyBaseUrl.isBlank() || legacyModel.isBlank()) return emptyList()
    return listOf(
        StoredCustomProvider(
            id = LEGACY_PROVIDER_ID,
            name =
                runCatching {
                    java.net
                        .URI(legacyBaseUrl)
                        .host
                        .orEmpty()
                }.getOrDefault(legacyBaseUrl),
            baseUrl = legacyBaseUrl,
            models = listOf(legacyModel),
            selectedModel = legacyModel,
            hasApiKey =
                !this[LEGACY_ENCRYPTED_API_KEY].isNullOrBlank() &&
                    !this[LEGACY_ENCRYPTED_API_KEY_IV].isNullOrBlank(),
        ),
    )
}

private fun Preferences.selectedCustomProviderId(): String? =
    this[SELECTED_CUSTOM_PROVIDER_ID] ?: storedLegacySelectedProviderId()

private fun Preferences.storedLegacySelectedProviderId(): String? =
    LEGACY_PROVIDER_ID.takeIf { this[LEGACY_CUSTOM_BASE_URL]?.isNotBlank() == true }

private fun androidx.datastore.preferences.core.MutablePreferences.incrementRevision() {
    this[CONFIGURATION_REVISION] = (this[CONFIGURATION_REVISION] ?: 0L) + 1L
}

private fun androidx.datastore.preferences.core.MutablePreferences.clearLegacyCustomConfiguration() {
    remove(LEGACY_CUSTOM_BASE_URL)
    remove(LEGACY_CUSTOM_MODEL)
    remove(LEGACY_ENCRYPTED_API_KEY)
    remove(LEGACY_ENCRYPTED_API_KEY_IV)
}

@Serializable
private data class StoredCustomProvider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val models: List<String>,
    val selectedModel: String,
    val hasApiKey: Boolean,
) {
    fun toDomain() =
        AiCustomProviderModel(
            id = id,
            name = name,
            baseUrl = baseUrl,
            models = models,
            selectedModel = selectedModel,
            hasApiKey = hasApiKey,
        )
}

private data class EncryptedSecret(
    val ciphertext: String,
    val iv: String,
)

private class AiSecretCipher {
    private val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    fun encrypt(value: String): EncryptedSecret {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return EncryptedSecret(
            ciphertext = Base64.encodeToString(cipher.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP),
            iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP),
        )
    }

    fun decrypt(
        ciphertext: String,
        iv: String,
    ): String {
        val key = keyStore.getKey(KEY_ALIAS, null) as? SecretKey ?: error("Missing AI configuration key")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.decode(iv, Base64.NO_WRAP)),
        )
        return cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)).toString(Charsets.UTF_8)
    }

    fun deleteKey() {
        if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
    }

    private fun getOrCreateKey(): SecretKey {
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        generator.init(
            KeyGenParameterSpec
                .Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }
}

private val PROVIDER_MODE = stringPreferencesKey("provider_mode")
private val BACKEND_PROVIDER_ID = stringPreferencesKey("backend_provider_id")
private val BACKEND_MODEL = stringPreferencesKey("backend_model")
private val CUSTOM_PROVIDERS_JSON = stringPreferencesKey("custom_providers_json")
private val SELECTED_CUSTOM_PROVIDER_ID = stringPreferencesKey("selected_custom_provider_id")
private val ENCRYPTED_CUSTOM_API_KEYS = stringPreferencesKey("encrypted_custom_api_keys")
private val ENCRYPTED_CUSTOM_API_KEYS_IV = stringPreferencesKey("encrypted_custom_api_keys_iv")
private val CONFIGURATION_REVISION = longPreferencesKey("configuration_revision")
private val LEGACY_CUSTOM_BASE_URL = stringPreferencesKey("custom_base_url")
private val LEGACY_CUSTOM_MODEL = stringPreferencesKey("custom_model")
private val LEGACY_ENCRYPTED_API_KEY = stringPreferencesKey("encrypted_api_key")
private val LEGACY_ENCRYPTED_API_KEY_IV = stringPreferencesKey("encrypted_api_key_iv")
private const val LEGACY_PROVIDER_ID = "legacy"
private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val KEY_ALIAS = "kbbi_ai_custom_provider_key"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH_BITS = 128
