package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.casting.CastQualityProfile
import com.example.data.local.CastSessionRecord
import com.example.data.local.DiscoveredDevice
import com.example.data.local.PairingStatus
import org.junit.Assert.assertEquals
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
    assertEquals("OmniCast 4K", appName)
  }

  @Test
  fun `verify casting profiles and latency targets`() {
    val lowLatency = CastQualityProfile.LOW_LATENCY_1080P60
    assertEquals(60, lowLatency.targetFps)
    assertEquals(16, lowLatency.targetBitrateMbps)
    assertEquals(false, lowLatency.isDirect4K)

    val cinema4k = CastQualityProfile.DIRECT_4K_MEDIA
    assertEquals(true, cinema4k.isDirect4K)
  }

  @Test
  fun `verify pairing states and device model`() {
    val device = DiscoveredDevice(
      id = "test_samsung",
      name = "Samsung QLED 4K",
      brand = "Samsung",
      ipAddress = "192.168.1.100",
      port = 8002,
      protocol = "Tizen WS",
      supports4K = true,
      pairingStatus = PairingStatus.AUTHENTICATED,
      authToken = "token_123"
    )
    assertEquals("Samsung", device.brand)
    assertEquals(PairingStatus.AUTHENTICATED, device.pairingStatus)
  }
}
