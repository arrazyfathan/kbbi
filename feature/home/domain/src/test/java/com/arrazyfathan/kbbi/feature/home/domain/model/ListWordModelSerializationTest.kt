package com.arrazyfathan.kbbi.feature.home.domain.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListWordModelSerializationTest {
    @Test
    fun `older detail route defaults to ordinary source`() {
        val model = Json.decodeFromString<ListWordModel>(
            """{"word":"ajar","listWords":[{"entry":"ajar","meanings":[]}],"visitorCount":1}""",
        )

        assertFalse(model.aiGenerated)
    }

    @Test
    fun `detail route retains AI source`() {
        val original = ListWordModel("pencilan", listOf(WordModel("pencilan", emptyList())), aiGenerated = true)

        val restored = Json.decodeFromString<ListWordModel>(Json.encodeToString(original))

        assertTrue(restored.aiGenerated)
    }
}
