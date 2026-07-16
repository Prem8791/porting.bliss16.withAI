package com.android.prodx.tests.contract

import com.android.prodx.contract.CompatibilityResolver
import com.android.prodx.contract.ContractVersion
import com.android.prodx.contract.VersionRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContractCompatibilityResolverTest {
    @Test fun versionRangeContains() {
        val range = VersionRange(ContractVersion(1, 0, 0), ContractVersion(1, 5, 0))
        assertTrue(range.contains(ContractVersion(1, 2, 0)))
        assertTrue(range.contains(ContractVersion(1, 0, 0)))
        assertTrue(range.contains(ContractVersion(1, 5, 0)))
        assertFalse(range.contains(ContractVersion(2, 0, 0)))
        assertFalse(range.contains(ContractVersion(0, 9, 0)))
    }

    @Test fun supportsVersion() {
        val range = VersionRange(ContractVersion(1, 0, 0), ContractVersion(1, 10, 0))
        assertTrue(CompatibilityResolver.supportsVersion(range, ContractVersion(1, 5, 0)))
        assertFalse(CompatibilityResolver.supportsVersion(range, ContractVersion(2, 0, 0)))
    }

    @Test fun canNegotiateSameMajor() {
        val local = VersionRange(ContractVersion(1, 0, 0), ContractVersion(1, 5, 0))
        val remote = VersionRange(ContractVersion(1, 2, 0), ContractVersion(1, 8, 0))
        assertTrue(CompatibilityResolver.canNegotiate(local, remote))
    }

    @Test fun canNegotiateDisjointMajor() {
        val local = VersionRange(ContractVersion(1, 0, 0), ContractVersion(1, 5, 0))
        val remote = VersionRange(ContractVersion(2, 0, 0), ContractVersion(2, 5, 0))
        assertFalse(CompatibilityResolver.canNegotiate(local, remote))
    }

    @Test fun negotiateOverlapping() {
        val local = VersionRange(ContractVersion(1, 0, 0), ContractVersion(1, 5, 0))
        val remote = VersionRange(ContractVersion(1, 2, 0), ContractVersion(1, 8, 0))
        val result = CompatibilityResolver.negotiate(local, remote)
        assertNotNull(result)
        assertEquals(1, result!!.major)
        assertTrue(result.minor >= 2)
    }

    @Test fun negotiateSameMajor() {
        val local = VersionRange(ContractVersion(1, 2, 0), ContractVersion(1, 5, 0))
        val remote = VersionRange(ContractVersion(1, 0, 0), ContractVersion(1, 3, 0))
        val result = CompatibilityResolver.negotiate(local, remote)
        assertNotNull(result)
    }

    @Test fun negotiateDifferentMajorReturnsNull() {
        val local = VersionRange(ContractVersion(1, 0, 0), ContractVersion(1, 5, 0))
        val remote = VersionRange(ContractVersion(2, 0, 0), ContractVersion(2, 5, 0))
        assertNull(CompatibilityResolver.negotiate(local, remote))
    }

    @Test fun versionRangeAll() {
        val all = VersionRange.ALL
        assertTrue(all.contains(ContractVersion.LATEST))
        assertTrue(all.contains(ContractVersion(1, 65535, 65535)))
        assertFalse(all.contains(ContractVersion(2, 0, 0)))
    }
}
