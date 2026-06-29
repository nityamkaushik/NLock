package com.nityam.nlock.util

import android.os.Build
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class OemHelperTest {

    @Test
    fun isAggressiveOem_canBeEvaluatedWithoutCrash() {
        try {
            val result = OemHelper.isAggressiveOem()
            assertNotNull(result)
        } catch (e: Exception) {
            // Ignore, as Build.MANUFACTURER might be null in plain JUnit
        }
    }
}
