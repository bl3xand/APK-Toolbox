package io.github.bl3xand.apkcloner.install.methods

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateOwnershipEligibilityTest {
    @Test
    fun documentedTargetThresholdsArePerPlatformRelease() {
        assertEquals(29, StockInstaller.minimumTargetForNoUserAction(31))
        assertEquals(30, StockInstaller.minimumTargetForNoUserAction(33))
        assertEquals(31, StockInstaller.minimumTargetForNoUserAction(34))
        assertEquals(33, StockInstaller.minimumTargetForNoUserAction(35))
        assertEquals(34, StockInstaller.minimumTargetForNoUserAction(36))
        assertEquals(35, StockInstaller.minimumTargetForNoUserAction(37))
    }

    @Test
    fun ownershipIsOnlyRequestedForPermittedFirstCloneInstallOnApi34Plus() {
        fun eligible(api: Int, clone: Boolean, first: Boolean, permission: Boolean) =
            ApkInstaller.canRequestUpdateOwnership(api, clone, first, permission)
        assertTrue(eligible(34, true, true, true))
        assertFalse(eligible(33, true, true, true))
        assertFalse(eligible(34, false, true, true))
        assertFalse(eligible(34, true, false, true))
        assertFalse(eligible(34, true, true, false))
    }
}
