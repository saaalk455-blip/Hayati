package com.hayati.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskTitleTest {

    @Test
    fun trims_surrounding_whitespace() {
        assertEquals("قراءة ورد", TaskTitle.sanitize("  قراءة ورد \n"))
    }

    @Test
    fun blank_input_is_rejected() {
        assertNull(TaskTitle.sanitize(""))
        assertNull(TaskTitle.sanitize("   \t\n"))
    }

    @Test
    fun long_input_is_cut_to_the_maximum_length() {
        val result = TaskTitle.sanitize("a".repeat(TaskTitle.MAX_LENGTH + 50))

        assertEquals(TaskTitle.MAX_LENGTH, result?.length)
    }

    @Test
    fun title_of_exactly_the_maximum_length_is_kept() {
        val title = "b".repeat(TaskTitle.MAX_LENGTH)

        assertEquals(title, TaskTitle.sanitize(title))
    }
}
