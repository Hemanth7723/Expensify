package com.example

import com.example.data.model.Expense
import com.example.data.model.withEditableFields
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun editing_preserves_non_editable_fields_and_updates_supported_fields() {
    val original = Expense(
      id = 7,
      title = "Lunch",
      amount = 10.0,
      category = "Food",
      timestamp = 100L,
      note = "Team meal"
    )

    val edited = original.withEditableFields(25.5, "Shopping", 200L)

    assertEquals(7, edited.id)
    assertEquals("Lunch", edited.title)
    assertEquals("Team meal", edited.note)
    assertEquals(25.5, edited.amount, 0.0)
    assertEquals("Shopping", edited.category)
    assertEquals(200L, edited.timestamp)
  }
}
