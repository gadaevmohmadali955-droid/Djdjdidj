package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.KeyGenerator
import com.example.data.NameValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BloxChat", appName)
  }

  @Test
  fun `validate valid real names`() {
    val res1 = NameValidator.validateRealName("мохьмад")
    assertTrue(res1.isValid)
    assertEquals("Мохьмад", res1.formattedName)

    val res2 = NameValidator.validateRealName("Александр")
    assertTrue(res2.isValid)
    assertEquals("Александр", res2.formattedName)

    val res3 = NameValidator.validateRealName("Мария")
    assertTrue(res3.isValid)
    assertEquals("Мария", res3.formattedName)
  }

  @Test
  fun `reject invalid names or gibberish`() {
    val resEmpty = NameValidator.validateRealName("   ")
    assertFalse(resEmpty.isValid)

    val resShort = NameValidator.validateRealName("а")
    assertFalse(resShort.isValid)

    val resNumbers = NameValidator.validateRealName("Иван123")
    assertFalse(resNumbers.isValid)

    val resGibberish = NameValidator.validateRealName("asdfgh")
    assertFalse(resGibberish.isValid)

    val resRepetition = NameValidator.validateRealName("ааааа")
    assertFalse(resRepetition.isValid)
  }

  @Test
  fun `generate and validate unique security key`() {
    val key = KeyGenerator.generateUniqueKey()
    assertTrue(KeyGenerator.isValidKeyFormat(key))
    assertEquals(19, key.length) // 4 + 1 + 4 + 1 + 4 + 1 + 4 = 19
  }
}

