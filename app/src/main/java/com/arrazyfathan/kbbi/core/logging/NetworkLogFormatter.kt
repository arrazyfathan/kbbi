package com.arrazyfathan.kbbi.core.logging

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val KTOR_BODY_START = "BODY START"
private const val KTOR_BODY_END = "BODY END"
private const val SECTION_WIDTH = 72
private const val REDACTED_VALUE = "<redacted>"
private const val SENSITIVE_KEY_PATTERN =
    "authorization|api[-_ ]?key|access[-_ ]?token|refresh[-_ ]?token|token|password"

private val sensitiveHeaderRegex =
    Regex(
        pattern = """(?im)^(.*\b(?:$SENSITIVE_KEY_PATTERN)\b\s*[:=]\s*).*$""",
    )

private val sensitiveJsonRegex =
    Regex(
        pattern = """"([^"]*(?:$SENSITIVE_KEY_PATTERN)[^"]*)"\s*:\s*"[^"]*"""",
        option = RegexOption.IGNORE_CASE,
    )

object NetworkLogFormatter {
    private val prettyJson =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun format(message: String): String {
        val sanitizedMessage = message.trim().redactSensitiveValues()
        val networkLog = NetworkLog.from(sanitizedMessage)

        return buildString {
            appendLine()
            appendLine(sectionTitle("NETWORK ${networkLog.type.label}"))
            appendLine("Time   : ${currentTimestamp()}")
            networkLog.method?.let { appendLine("Method : $it") }
            networkLog.url?.let { appendLine("Url    : $it") }
            networkLog.status?.let { appendLine("Status : $it") }
            networkLog.contentType?.let { appendLine("Type   : $it") }
            networkLog.bodySize?.let { appendLine("Body   : $it") }

            if (networkLog.headers.isNotEmpty()) {
                appendLine(sectionTitle("HEADERS"))
                appendLine(networkLog.headers)
            }

            if (networkLog.curl.isNotBlank()) {
                appendLine(sectionTitle("CURL"))
                appendLine(networkLog.curl)
            }

            if (networkLog.body.isNotBlank()) {
                appendLine(sectionTitle("BODY"))
                appendLine(networkLog.body)
            }

            if (networkLog.headers.isBlank() && networkLog.body.isBlank()) {
                appendLine(sectionTitle("RAW"))
                appendLine(networkLog.raw)
            }

            append(sectionTitle("END NETWORK ${networkLog.type.label}"))
        }
    }

    private fun sectionTitle(title: String): String {
        val normalizedTitle = " $title "
        val remainingWidth = (SECTION_WIDTH - normalizedTitle.length).coerceAtLeast(0)
        val left = remainingWidth / 2
        val right = remainingWidth - left
        return "=".repeat(left) + normalizedTitle + "=".repeat(right)
    }

    private fun currentTimestamp(): String =
        synchronized(timestampFormat) {
            timestampFormat.format(Date())
        }

    private fun String.toReadableBody(): String = toPrettyJsonOrNull() ?: this

    private fun String.toPrettyJsonOrNull(): String? {
        val trimmedMessage = trim()
        val isJsonObject = trimmedMessage.startsWith("{") && trimmedMessage.endsWith("}")
        val isJsonArray = trimmedMessage.startsWith("[") && trimmedMessage.endsWith("]")

        if (!isJsonObject && !isJsonArray) return null

        return runCatching {
            val jsonElement = prettyJson.parseToJsonElement(trimmedMessage)
            prettyJson.encodeToString(JsonElement.serializer(), jsonElement)
        }.getOrNull()
    }

    private fun String.redactSensitiveValues(): String =
        replace(sensitiveHeaderRegex) { matchResult ->
            matchResult.groupValues[1] + REDACTED_VALUE
        }.replace(sensitiveJsonRegex) { matchResult ->
            """"${matchResult.groupValues[1]}":"$REDACTED_VALUE""""
        }

    private data class NetworkLog(
        val type: NetworkLogType,
        val raw: String,
        val method: String?,
        val url: String?,
        val status: String?,
        val contentType: String?,
        val bodySize: String?,
        val headers: String,
        val body: String,
        val curl: String,
    ) {
        companion object {
            fun from(message: String): NetworkLog {
                val body = message.extractBody()
                val metadata = message.removeBodyBlock()
                val type = NetworkLogType.from(message)
                val method = metadata.extractMethod()
                val url = metadata.extractUrl()
                val headers = metadata.extractHeaders()
                val readableBody = body?.toReadableBody().orEmpty()

                return NetworkLog(
                    type = type,
                    raw = message,
                    method = method,
                    url = url,
                    status = metadata.extractStatus(),
                    contentType = metadata.extractContentType(),
                    bodySize = body?.length?.let { "$it characters" },
                    headers = headers,
                    body = readableBody,
                    curl =
                        CurlCommand.from(
                            type = type,
                            method = method,
                            url = url,
                            headers = metadata.extractHeaderFields(),
                            body = body,
                        ),
                )
            }
        }
    }

    private enum class NetworkLogType(
        val label: String,
    ) {
        Request("REQUEST"),
        Response("RESPONSE"),
        Other("LOG"),
        ;

        companion object {
            fun from(message: String): NetworkLogType =
                when {
                    message.lineSequence().any { it.startsWith("REQUEST:") } -> Request
                    message.lineSequence().any { it.startsWith("RESPONSE:") } -> Response
                    else -> Other
                }
        }
    }

    private fun String.extractMethod(): String? =
        lineSequence().firstNotNullOfOrNull { line ->
            methodRegex.find(line)?.groupValues?.getOrNull(1)
        }

    private fun String.extractUrl(): String? =
        lineSequence().firstNotNullOfOrNull { line ->
            requestUrlRegex.find(line)?.groupValues?.getOrNull(1) ?: fromUrlRegex.find(line)?.groupValues?.getOrNull(1)
        }

    private fun String.extractStatus(): String? =
        lineSequence().firstNotNullOfOrNull { line ->
            responseStatusRegex.find(line)?.groupValues?.getOrNull(1)
        }

    private fun String.extractContentType(): String? =
        lineSequence().firstNotNullOfOrNull { line ->
            contentTypeRegex.find(line)?.groupValues?.getOrNull(1)
        }

    private fun String.extractHeaders(): String =
        lineSequence()
            .filterNot { line ->
                line.startsWith("REQUEST:") || line.startsWith("RESPONSE:") || line.startsWith("METHOD:") ||
                    line.startsWith(
                        "FROM:",
                    )
            }.joinToString(separator = "\n")
            .trim()

    private fun String.extractHeaderFields(): List<HeaderField> =
        lineSequence()
            .mapNotNull { line -> headerFieldRegex.find(line)?.toHeaderField() }
            .filterNot { header -> header.name.equals("REQUEST", ignoreCase = true) }
            .filterNot { header -> header.name.equals("RESPONSE", ignoreCase = true) }
            .filterNot { header -> header.name.equals("METHOD", ignoreCase = true) }
            .filterNot { header -> header.name.equals("FROM", ignoreCase = true) }
            .distinctBy { header -> header.name.lowercase(Locale.US) }
            .toList()

    private fun String.extractBody(): String? {
        val bodyStartIndex = indexOf(KTOR_BODY_START)
        if (bodyStartIndex == -1) return toPrettyJsonOrNull()

        val bodyContentStart = indexOf('\n', startIndex = bodyStartIndex).takeIf { it != -1 }?.plus(1) ?: return null
        val bodyEndIndex = indexOf(KTOR_BODY_END, startIndex = bodyContentStart).takeIf { it != -1 } ?: return null

        return substring(bodyContentStart, bodyEndIndex).trim()
    }

    private fun String.removeBodyBlock(): String {
        val bodyStartIndex = indexOf(KTOR_BODY_START)
        if (bodyStartIndex == -1) return this

        val bodyEndIndex = indexOf(KTOR_BODY_END, startIndex = bodyStartIndex).takeIf { it != -1 } ?: return this
        val bodyBlockEnd = indexOf('\n', startIndex = bodyEndIndex).takeIf { it != -1 }?.plus(1) ?: length

        return removeRange(bodyStartIndex, bodyBlockEnd)
    }

    private val methodRegex = Regex("""METHOD:\s*(?:HttpMethod\(value=)?([A-Z]+)""")
    private val requestUrlRegex = Regex("""REQUEST:\s*(.+)""")
    private val fromUrlRegex = Regex("""FROM:\s*(.+)""")
    private val responseStatusRegex = Regex("""RESPONSE:\s*(.+)""")
    private val contentTypeRegex = Regex("""(?i)\bcontent[- ]?type\s*[:=]\s*(.+)""")
    private val headerFieldRegex = Regex("""^(?:->|<-)?\s*([^:]+):\s*(.+)$""")

    private fun MatchResult.toHeaderField(): HeaderField {
        val name = groupValues[1].removePrefix("BODY ").trim()
        val value = groupValues[2].trim()
        return HeaderField(name = name, value = value)
    }

    private data class HeaderField(
        val name: String,
        val value: String,
    )

    private object CurlCommand {
        fun from(
            type: NetworkLogType,
            method: String?,
            url: String?,
            headers: List<HeaderField>,
            body: String?,
        ): String {
            if (type != NetworkLogType.Request || url.isNullOrBlank()) return ""

            val requestMethod = method ?: "GET"
            return buildList {
                add("curl --location --request $requestMethod ${url.shellQuote()}")
                headers.forEach { header ->
                    add("  --header ${"${header.name}: ${header.value}".shellQuote()}")
                }
                if (!body.isNullOrBlank() && requestMethod.allowsRequestBody()) {
                    add("  --data-raw ${body.shellQuote()}")
                }
            }.joinToString(separator = " \\\n")
        }

        private fun String.allowsRequestBody(): Boolean =
            !equals("GET", ignoreCase = true) && !equals("HEAD", ignoreCase = true)

        private fun String.shellQuote(): String = "'${replace("'", "'\\''")}'"
    }
}
