package com.android.prodx.tests.contract

import com.android.prodx.contract.ContractVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContractVersionTest {
    @Test fun parseValid() {
        val v = ContractVersion.parse("1.2.3").getOrThrow()
        assertEquals(1, v.major)
        assertEquals(2, v.minor)
        assertEquals(3, v.patch)
        assertEquals("1.2.3", v.toString())
    }

    @Test fun parseWithPreRelease() {
        val v = ContractVersion.parse("2.0.0-rc1").getOrThrow()
        assertEquals(2, v.major)
        assertEquals(0, v.minor)
        assertEquals(0, v.patch)
    }

    @Test fun parseWithBuildMeta() {
        val v = ContractVersion.parse("1.0.0+build.42").getOrThrow()
        assertEquals(1, v.major)
    }

    @Test fun parseInvalidReturnsFailure() {
        assertTrue(ContractVersion.parse("not-a-version").isFailure)
        assertTrue(ContractVersion.parse("").isFailure)
        assertTrue(ContractVersion.parse("a.b.c").isFailure)
    }

    @Test fun compatibilitySameMajor() {
        assertTrue(ContractVersion(1, 0, 0).isCompatible(ContractVersion(1, 5, 0)))
        assertTrue(ContractVersion(1, 5, 0).isCompatible(ContractVersion(1, 0, 0)))
    }

    @Test fun compatibilityDifferentMajor() {
        assertFalse(ContractVersion(1, 0, 0).isCompatible(ContractVersion(2, 0, 0)))
        assertFalse(ContractVersion(2, 0, 0).isCompatible(ContractVersion(1, 0, 0)))
    }

    @Test fun latestIs1_0_0() {
        assertEquals(ContractVersion(1, 0, 0), ContractVersion.LATEST)
    }
}
