package com.example

import com.example.core.model.CategoryId
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `engineering category count verification`() {
        assertEquals(12, CategoryId.entries.size)
    }
}
