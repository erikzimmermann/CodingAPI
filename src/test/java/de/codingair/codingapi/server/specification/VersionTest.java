package de.codingair.codingapi.server.specification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionTest {

    // ---- Legacy 1.X.Y scheme (must be preserved) ----

    @Test
    void parsesLegacyVersion_1_21_11() {
        assertEquals(21.11, Version.parseVersionId("1.21.11"), 0.0001);
    }

    @Test
    void parsesLegacyVersion_1_21_4() {
        assertEquals(21.04, Version.parseVersionId("1.21.4"), 0.0001);
    }

    @Test
    void parsesLegacyVersion_withoutPatch_1_8() {
        // The current regex-fallback path can match "1.8" (no third segment).
        assertEquals(8.0, Version.parseVersionId("1.8"), 0.0001);
    }

    @Test
    void parsesLegacyVersion_1_20_5() {
        // Boundary: Paper's mojangMapped() check uses 20.5.
        assertEquals(20.05, Version.parseVersionId("1.20.5"), 0.0001);
    }

    // ---- New year-based scheme ----

    @Test
    void parsesYearScheme_26_1_2() {
        assertEquals(2601.02, Version.parseVersionId("26.1.2"), 0.0001);
    }

    @Test
    void parsesYearScheme_26_1_0() {
        // A hypothetical 26.1.0 (initial drop release before any patch).
        assertEquals(2601.00, Version.parseVersionId("26.1.0"), 0.0001);
    }

    @Test
    void parsesYearScheme_26_1_noPatch() {
        assertEquals(2601.00, Version.parseVersionId("26.1"), 0.0001);
    }

    @Test
    void parsesYearScheme_26_2_0_ordersAbove_26_1_x() {
        // Pin exact encoded values, not just ordering — many wrong encodings would also satisfy ordering.
        assertEquals(2601.02, Version.parseVersionId("26.1.2"), 0.0001);
        assertEquals(2602.00, Version.parseVersionId("26.2.0"), 0.0001);
        assertTrue(Version.parseVersionId("26.2.0") > Version.parseVersionId("26.1.2"),
            "26.2.0 must order strictly above 26.1.2");
    }

    @Test
    void parsesYearScheme_26_10_0_doesNotCollideWith_27_0_0() {
        // Drop ≥ 10 within a single year must still sort below the next year's first drop.
        assertTrue(Version.parseVersionId("27.0.0") > Version.parseVersionId("26.10.0"),
            "27.0.0 must order strictly above 26.10.0 (no encoding collision)");
    }

    @Test
    void parsesYearScheme_27_1_0_ordersAbove_26_x() {
        double v26_2_0 = Version.parseVersionId("26.2.0");
        double v27_1_0 = Version.parseVersionId("27.1.0");
        assertTrue(v27_1_0 > v26_2_0, "27.1.0 must order strictly above 26.2.0");
    }

    // ---- Cross-scheme ordering (year-scheme must always exceed any legacy) ----

    @Test
    void yearScheme_alwaysAboveAnyLegacyVersion() {
        double maxLegacyLike = Version.parseVersionId("1.99.99"); // synthetic ceiling
        double minYearScheme = Version.parseVersionId("26.1.0");
        assertTrue(minYearScheme > maxLegacyLike,
            "Year-scheme floor (26.1.0=" + minYearScheme + ") must exceed legacy ceiling (1.99.99=" + maxLegacyLike + ")");
    }

    @Test
    void yearScheme_26_1_2_clearsLegacyCallSiteCutoffs() {
        // mojangMapped() requires atLeast(20.5); many Version.choose() sites switched at 21.11.
        // Year-scheme values must clear both cutoffs for the dispatch to land on modern branches.
        double encoded = Version.parseVersionId("26.1.2");
        assertTrue(encoded >= 20.5,
            "26.1.2 must satisfy the mojangMapped() atLeast(20.5) threshold (got " + encoded + ")");
        assertTrue(encoded >= 21.11,
            "26.1.2 must satisfy the 21.11 cutoff used in many Version.choose() calls (got " + encoded + ")");
    }

    // ---- Snapshot / pre-release inputs (documented to throw) ----

    @Test
    void parsesSnapshotId_throwsNumberFormatException() {
        // Pin javadoc'd contract: snapshot ids fail loudly rather than encode to surprising values.
        assertThrows(NumberFormatException.class, () -> Version.parseVersionId("23w31a"));
    }
}
