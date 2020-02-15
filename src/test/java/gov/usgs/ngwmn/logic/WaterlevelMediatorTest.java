package gov.usgs.ngwmn.logic;

import static gov.usgs.ngwmn.logic.WaterlevelMediator.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

public class WaterlevelMediatorTest {

    final BigDecimal FIVE     = new BigDecimal(  "5.0");
    final BigDecimal NEG_FIVE = new BigDecimal( "-5.0");
    final BigDecimal TEN      = new BigDecimal( "10.0");
    final BigDecimal NEG_TEN  = new BigDecimal("-10.0");
    final BigDecimal FIFTEEN  = new BigDecimal( "15.0");

    //When the PCode is 72019 (downward from land surface), the value does not need to be
    //converted, so should be unmodified and agreement w/ the site datum is not needed.
    //Measurement datum and Unit are optional, but cannot be a mismatch to the PCode.
    //A null PCode is assumed to be downward from land surface - this is how
    //non-USGS sites handle these measures, since they do not have PCodes.
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_A() throws Exception {
        BigDecimal actual = WaterlevelMediator.mediateToDistanceBelowGroundLevel(
                "10.0", "ft",
                "72019", "LandSurface",
                "5.0", "NGVD29");
        assertEquals("Site datum != the measurement datum, but that should be ok for landsSurface measures", TEN, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_B() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("-10.0", "ft", "72019", "LandSurface", "5.0", null);
        assertEquals("OK, for the site datum to be null for landsSurface measures", NEG_TEN, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_C() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("10.0", "ft", "72019", null, "5.0", null);
        assertEquals("site and measure datum may be null for landsSurface measures", TEN, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_D() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("10.0", "ft   ft", "72019", null, "5.0", null);
        assertEquals("Currently allow double units w/ whitespace between due to legacy data in cache.  FIX ME.", TEN, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_E() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("10.0", null, "72019", null, "5.0", null);
        assertEquals("The unit may be null since it is implied in the pcode", TEN, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_F() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("10.0", null, "72019", null, "Not a Number", null);
        assertEquals("Offset should be ignored, even if not readable, if not conversion is needed", TEN, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_G() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("10.0", null, "72019", null, null, null);
        assertEquals("Offset should be ignored, even if null, if not conversion is needed", TEN, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_H() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("10.0", null, "72019", null, "", null);
        assertEquals("Offset should be ignored, even if empty, if not conversion is needed", TEN, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_I() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("10.0", null, "", null, "", null);
        assertEquals("If no PCode, assume non-usgs site w/ depth below surface measure - pass value through", TEN, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_J() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("10.0", "xxx", null, "xxx", "xxx", "xxx");
        assertEquals("If no PCode, assume non-usgs site w/ depth below surface measure - pass value through", TEN, actual);
    }

    // Unconvertable errors of the above scenarios
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_Unconvertable_A() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "ft", "72019", "NGVD29", "5.0", null);
        } catch (Exception e) {
            assertTrue("If non-null, the measurement datum and the PCode datum must be the same. Actual="+e.getMessage(),
                    e.getMessage().startsWith(NA_PCODE_MEASURE_DATUM_MISMATCH));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_Unconvertable_B() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "ft", "72019", "Something Else", "5.0", null);
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The measurement datum must be parsable. Actual="+actual,
                    actual.startsWith(NA_NO_MEASURE_DATUM));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_Unconvertable_C() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "m", "72019", null, "5.0", null);
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The unit must match the PCode. Actual="+actual,
                    actual.startsWith(NA_PCODE_UNIT_DECLAIRED_UNIT_MISMATCH));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_Unconvertable_D() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "Something Else", "72019", null, "5.0", null);
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The unit must be recognized. Actual="+actual,
                    actual.startsWith(NA_NO_UNIT));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_Unconvertable_E() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("XXX", null, "72019", null, "5.0", null);
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The value is unreadable. Actual="+actual,
                    actual.startsWith(NA_NO_VALUE));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromALandSurfaceMeasurement_Unconvertable_F() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "ft", "Something Else", "LandSurface", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("Unreadable PCode.  Note that null or empty PCodes would be OK. Actual="+actual,
                    actual.startsWith(NA_NO_PCODE));
        }
    }

    //When the measurement is 62610 (upward from NGVD29 datum), the value needs to be
    //converted to depth downward from local surface level.
    //In this case, the site datum is required and must match the measurement datum,
    //which must also match the datum of the PCODE (NGVD29).
    //Measurement datum is optional, but cannot be a mismatch to the PCode.
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_A() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("10.0", "ft", "62610", "NGVD29", "5.00", "NGVD29");
        assertEquals("-1 * 10 + 5 == -5", NEG_FIVE, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_B() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("-10.0", "ft", "62610", null, "5.00", "NGVD29");
        assertEquals("-1 * -10 + 5 == 15.  OK for measure datum to be null", FIFTEEN, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_C() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("-10.0", "ft   ft", "62610", "NGVD29", "5.00", "NGVD29");
        assertEquals("Currently allow double units w/ whitespace between due to legacy data in cache.  FIX ME.", FIFTEEN, actual);
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_D() throws Exception {
        BigDecimal actual = mediateToDistanceBelowGroundLevel("-10.0", null, "62610", "NGVD29", "5.00", "NGVD29");
        assertEquals("The unit may be null since it is implied in the pcode", FIFTEEN, actual);
    }

    //Unconvertable errors of the above scenario
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_A() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "ft", "62610", "HILocal", "5.00", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("If non-null, the measurement datum and the PCode datum must be the same. Actual="+actual,
                    actual.startsWith(NA_PCODE_MEASURE_DATUM_MISMATCH) );
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_B() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "ft", "62610", "Something else", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The measurement datum must be parsable. Actual="+actual,
                    actual.startsWith(NA_NO_MEASURE_DATUM));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_C() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "ft", "62610", "NGVD29", "5.0", null);
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("site datum is required. Actual="+actual,
                    actual.startsWith(NA_NO_SITE_DATUM));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_D() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "m", "62610", "NGVD29", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("unit must match pcode. Actual="+actual,
                    actual.startsWith(NA_PCODE_UNIT_DECLAIRED_UNIT_MISMATCH));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_E() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "Something Else", "62610", "NGVD29", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("unit must be recognized. Actual="+actual,
                    actual.startsWith(NA_NO_UNIT));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_F() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("XXX", "ft", "62610", "NGVD29", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The value is unreadable. Actual="+actual,
                    actual.startsWith(NA_NO_VALUE));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_G() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("", "ft", "62610", "NGVD29", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The value is empty. Actual="+actual,
                    actual.startsWith(NA_NO_VALUE));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_H() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel(null, "ft", "62610", "NGVD29", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The value is null. Actual="+actual,
                    actual.startsWith(NA_NO_VALUE));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_I() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "ft", "62610", "NGVD29", "Not A Number", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The site elevation is required. Actual="+actual,
                    actual.startsWith(NA_NO_SITE_ELEVATION));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_J() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "ft", "62610", "NGVD29", "", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The site elevation is required. Actual="+actual,
                    actual.startsWith(NA_NO_SITE_ELEVATION));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_K() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "ft", "62610", "NGVD29", null, "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The site elevation is required. Actual="+actual,
                    actual.startsWith(NA_NO_SITE_ELEVATION));
        }
    }
    @Test
    public void mediateToDistanceBelowGroundLevel_FromAnExternalDatumBasedMeasurement_Unconvertable_L() throws Exception {
        try {
            mediateToDistanceBelowGroundLevel("10.0", "ft", "62610", "NGVD29", "5.0", "HILocal");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The site datum must match the PCODE and measure datum. Actual="+actual,
                    actual.startsWith(NA_SITE_PCODE_DATUM_MISMATCH)	);
        }
    }

    //When the PCode is 72019 (downward from landsSurface), the value
    //needs to be converted.  A Null PCode is treated the same (non-USGS sites
    //have no PCode and are assumed to be downward from land surface).
    //Measurement datum and Unit are optional, but cannot be a mismatch to the PCode.
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_A() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", "ft", "72019", "LandSurface", "5.0", "NGVD29");
        assertEquals("Site datum != the measurement datum is expected for a landsSurface measures", NEG_FIVE, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_B() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("-10.0", "ft", "72019", "LandSurface", "5.00", "NGVD29");
        assertEquals("-10 is distance above site + site is 5 up from datum.", FIFTEEN, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_C() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("-10.0", "ft", "72019", "LandSurface", "-5.0", "NGVD29");
        assertEquals("-10 is distance above site + site is 5 DOWN from datum.", FIVE, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_D() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", "ft", "72019", null, "5.0", "NGVD29");
        assertEquals("OK for the measure datum to be null (assumed to be landsSurface due to PCode)", NEG_FIVE, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_E() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", "ft   ft", "72019", null, "5.0", "NGVD29");
        assertEquals("Currently allow double units w/ whitespace between due to legacy data in cache.  FIX ME.", NEG_FIVE, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_F() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", null, "72019", null, "5.0", "NGVD29");
        assertEquals("The unit may be null since it is implied in the pcode", NEG_FIVE, actual);
    }
    //The next set are the same as above, but now w/ null/empty PCode
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_noPcode_A() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", "ft", null, "LandSurface", "5.0", "NGVD29");
        assertEquals("Site datum != the measurement datum is expected for a landsSurface measures", NEG_FIVE, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_noPcode_B() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("-10.0", "ft", "", "LandSurface", "5.00", "NGVD29");
        assertEquals("-10 is distance above site + site is 5 up from datum.", FIFTEEN, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_noPcode_C() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("-10.0", "ft", "     ", "LandSurface", "-5.0", "NGVD29");
        assertEquals("-10 is distance above site + site is 5 DOWN from datum.", FIVE, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_noPcode_D() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", "ft", null, null, "5.0", "NGVD29");
        assertEquals("OK for the measure datum to be null (assumed to be landsSurface due to PCode)", NEG_FIVE, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_noPcode_E() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", "ft   ft", "      ", null, "5.0", "NGVD29");
        assertEquals("Currently allow double units w/ whitespace between due to legacy data in cache.  FIX ME.", NEG_FIVE, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_noPcode_F() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", null, "", null, "5.0", "NGVD29");
        assertEquals("The unit may be null since it is implied in the pcode", NEG_FIVE, actual);
    }
    //Unconvertable errors
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_Unconvertable_A() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("10.0", "ft", "72019", "NGVD29", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("If non-null, the measurement datum and the PCode datum must be the same. Actual="+actual,
                    actual.startsWith(NA_PCODE_MEASURE_DATUM_MISMATCH) );
        }
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_Unconvertable_B() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("10.0", "ft", "72019", "Something Else", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The measurement datum must be parsable. Actual="+actual,
                    actual.startsWith(NA_NO_MEASURE_DATUM) );
        }
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_Unconvertable_C() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("10.0", "m", "72019", null, "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The unit must match the PCode - In this case if will fail immediately when the unit is not in feet. Actual="+actual,
                    actual.startsWith(NA_PCODE_UNIT_DECLAIRED_UNIT_MISMATCH) ); // used to be NA_SITE_UNIT_DECLAIRED_UNIT_MISMATCH -> hard coded FEET
        }
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_Unconvertable_D() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("10.0", "Something Else", "72019", null, "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The unit must be recognized. Actual="+actual,
                    actual.startsWith(NA_NO_UNIT) );
        }
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_Unconvertable_E() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("XXX", null, "72019", null, "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The value is unreadable. Actual="+actual,
                    actual.startsWith(NA_NO_VALUE) );
        }
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_Unconvertable_F() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("10.0", "ft", "Something Else", "LandSurface", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("Unreadable PCode.  Note that null or empty PCodes would be OK. Actual="+actual,
                    actual.startsWith(NA_NO_PCODE) );
        }
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_Unconvertable_G() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("10.0", null, "72019", null, "Not a Number", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("Offset should be readable. Actual="+actual,
                    actual.startsWith(NA_NO_SITE_ELEVATION));
        }
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromLandSurfaceMeasurement_Unconvertable_H() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("10.0", null, "72019", null, null, "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("Offset should be ignored, even if null. Actual="+actual,
                    actual.startsWith(NA_NO_SITE_ELEVATION));
        }
    }


    //When the PCode is 62610, 62611 or other datum based measures (upward from datum),
    //no conversion is needed.  A Null PCode is treated the same (non-USGS sites
    //have no PCode and are assumed to be downward from land surface).
    //Measurement datum and Unit are optional, but cannot be a mismatch to the PCode.
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_A() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", "ft", "62610", "NGVD29", "5.0", "NGVD29");
        assertEquals("Std w/ everything specified.  Water is 10ft above datum", TEN, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_B() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("-10.0", "ft", "62611", "NAVD88", "5.0", "NAVD88");
        assertEquals("Basic case.  Water is 10 ft below datum.", NEG_TEN, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_C() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("-10.0", "ft", "62611", "NAVD88", "-5.0", "NAVD88");
        assertEquals("Basic case.  Water is 10 ft below the datum", NEG_TEN, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_D() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", "ft", "62610", null, "5.0", "NGVD29");
        assertEquals("OK for the measure datum to be null (assumed to be the same as the PCode datum)", TEN, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_E() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", "ft   ft", "62610", null, "5.0", "NGVD29");
        assertEquals("Currently allow double units w/ whitespace between due to legacy data in cache.  FIX ME.", TEN, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_F() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", null, "62610", null, "5.0", "NGVD29");
        assertEquals("The unit may be null since it is implied in the pcode", TEN, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_G() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", null, "62610", null, "Not a Number", "NGVD29");
        assertEquals("Offset is ignored - doesn't matter if not parsable", TEN, actual);
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_I() throws Exception {
        BigDecimal actual = mediateToDistanceAboveSiteDatum("10.0", null, "62610", null, null, "NGVD29");
        assertEquals("Offset should be ignored, even if null", TEN, actual);
    }
    //Unconvertable errors
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_Unconvertable_A() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("10.0", "ft", "62610", "NAVD88", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("If non-null, the measurement datum and the PCode datum must be the same. Actual="+actual,
                    actual.startsWith(NA_PCODE_MEASURE_DATUM_MISMATCH) );
        }
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_Unconvertable_B() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("10.0", "ft", "62610", "Something Else", "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The measurement datum must be parsable. Actual="+actual,
                    actual.startsWith(NA_NO_MEASURE_DATUM) );
        }
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_Unconvertable_C() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("10.0", "m", "62610", null, "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The unit must match the PCode - In this case it will fail immediately when the unit is not in feet. Actual="+actual,
                    actual.startsWith(NA_PCODE_UNIT_DECLAIRED_UNIT_MISMATCH)); // used to be NA_SITE_UNIT_DECLAIRED_UNIT_MISMATCH -> hard coded FEET
        }
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_Unconvertable_D() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("10.0", "Something Else", "62610", null, "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The unit must be recognized. Actual="+actual,
                    actual.startsWith(NA_NO_UNIT) );
        }
    }
    @Test
    public void testMediateToDistanceAboveSiteDatum_FromDatumBasedMeasurement_Unconvertable_E() throws Exception {
        try {
            mediateToDistanceAboveSiteDatum("XXX", null, "62610", null, "5.0", "NGVD29");
        } catch (Exception e) {
            String actual = e.getMessage();
            assertTrue("The value is unreadable. Actual="+actual,
                    actual.startsWith(NA_NO_VALUE) );
        }
    }
}
