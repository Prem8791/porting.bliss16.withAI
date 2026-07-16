package com.android.settings.prodx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProdXSettingsFailClosedTest {
    @Test
    fun absentAuditMediator_exposesNoHistoryAndAnExplicitUnavailableReason() {
        val controller = newController("ProdXAuditLogPreferenceController")
        val entries = controller.javaClass.getMethod("getAuditLogEntries").invoke(controller) as List<*>
        val reason = controller.javaClass.getMethod("getUnavailableReason").invoke(controller)
        assertTrue(entries.isEmpty())
        assertEquals("authority_history_mediator_unavailable", reason)
    }

    @Test
    fun absentGrantMediator_exposesNoAdministrativeGrants() {
        val controller = newController("ProdXGrantListPreferenceController")
        assertEquals(0, controller.javaClass.getMethod("getGrantCount").invoke(controller))
    }

    @Test
    fun unimplementedScreensaverIntegration_isInactive() {
        val controller = newController("ProdXScreensaverPreferenceController")
        assertFalse(controller.javaClass.getMethod("isScreensaverActive").invoke(controller) as Boolean)
    }

    private fun newController(simpleName: String): Any =
        Class.forName("com.android.settings.prodx.$simpleName")
            .getDeclaredConstructor()
            .newInstance()
}
