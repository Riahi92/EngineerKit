package com.example

import com.example.core.data.ToolCatalog
import com.example.core.model.CategoryId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `tool catalog contains all 12 mandatory disciplines`() {
        assertEquals(12, ToolCatalog.categories.size)
        CategoryId.entries.forEach { categoryId ->
            val category = ToolCatalog.getCategoryById(categoryId)
            assertNotNull("Category $categoryId must exist in catalog", category)
        }
    }

    @Test
    fun `tool catalog contains realistic technical formulas and inputs`() {
        assertTrue(ToolCatalog.tools.isNotEmpty())
        ToolCatalog.tools.forEach { tool ->
            assertTrue("Tool ${tool.id} must have formula preview", tool.formulaPreview.isNotBlank())
            assertTrue("Tool ${tool.id} must have planned inputs", tool.plannedInputs.isNotEmpty())
            assertTrue("Tool ${tool.id} must have planned outputs", tool.plannedOutputs.isNotEmpty())
        }
    }

    @Test
    fun `category tool retrieval returns correct discipline subsets`() {
        val mecaTools = ToolCatalog.getToolsByCategory(CategoryId.MECANIQUE)
        assertTrue(mecaTools.isNotEmpty())
        mecaTools.forEach {
            assertEquals(CategoryId.MECANIQUE, it.categoryId)
        }
    }
}
