package com.lorenzofelletti.simplebleapp

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import androidx.test.rule.ServiceTestRule
import com.lorenzofelletti.simplebleapp.ble.gattserver.PeripheralAdvertiseService
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class PeripheralAdvertiseServiceTest {
    @get:Rule
    val serviceRule = ServiceTestRule()

    @Test
    @Throws(TimeoutException::class)
    fun testService() {
        val serviceIntent = Intent(
            ApplicationProvider.getApplicationContext(),
            PeripheralAdvertiseService::class.java
        )

        val binder = serviceRule.bindService(serviceIntent)

        val service = (binder as PeripheralAdvertiseService.PeripheralAdvertiseBinder).getService()

        assertTrue(service.isRunning)
    }

}