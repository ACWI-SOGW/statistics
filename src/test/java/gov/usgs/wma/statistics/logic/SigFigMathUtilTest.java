package gov.usgs.wma.statistics.logic;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author smlarson
 * @author duselman
 */
public class SigFigMathUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SigFigMathUtilTest.class);
    private static final String[] args = {"12.000000", ".200", "-0.100", "560", "4.40"}; //total 576.700 w/o rounding
    private static final BigDecimal expect_default_half_down = new BigDecimal("576"); // total with rounding down
    private static final BigDecimal expect_default = new BigDecimal("577"); // total with rounding up
    private static List<BigDecimal> bdList;
    
    private static final RoundingMode RM_UP   = RoundingMode.HALF_UP;
    private static final RoundingMode RM_DOWN = RoundingMode.HALF_DOWN;
    private static final RoundingMode RM_EVEN = RoundingMode.HALF_EVEN;


    final BigDecimal pt250 = new BigDecimal("0.2500000");
    final BigDecimal pt25 = new BigDecimal("0.25");



    public SigFigMathUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

        bdList = new ArrayList<>();

        //load the list with the BD using the args as the values
        for (String arg : args) {
            BigDecimal bd = new BigDecimal(arg);
            bdList.add(bd);
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     *
     */
    @Test
    public void testSigFigAdd_List_RoundingMode() {
        BigDecimal actual = SigFigMathUtil.sigFigAdd(bdList, RM_EVEN);
        assertEquals(expect_default_half_down.toPlainString(), actual.toPlainString());

        BigDecimal expResult_down = new BigDecimal("576");
        BigDecimal result_down = SigFigMathUtil.sigFigAdd(bdList, RM_DOWN);
        assertEquals(expResult_down.toPlainString(), result_down.toPlainString());

        BigDecimal expResult_up = new BigDecimal("577");
        BigDecimal result_up = SigFigMathUtil.sigFigAdd(bdList, RM_UP);
        assertEquals(expResult_up.toPlainString(), result_up.toPlainString());

    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     */
    @Test
    public void testSigFigAdd_List() {
        BigDecimal actual = SigFigMathUtil.sigFigAdd(bdList);
        assertEquals("When the value end in point 5 then round down (if it has further non-zero digits then round up)",
                expect_default_half_down.toPlainString(), actual.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigAdd(null, RM_UP);
        assertNull(nullResult);

        BigDecimal nullRmResult = SigFigMathUtil.sigFigAdd(bdList, null);
        assertNull(nullRmResult);
        nullRmResult = SigFigMathUtil.sigFigAdd(null, RM_UP);
        assertNull(nullRmResult);
        nullRmResult = SigFigMathUtil.sigFigAdd(new LinkedList<>(), RM_UP);
        assertNull(nullRmResult);
    }
    
    @Test
    public void testSqigFigAdd_roundPoint5() {
    	// 2017-03-30 need to round up for negative numbers -2.90 rather than the Java default -2.91
    	// 2018-07-31 need to revert back to Java default rounding. Those who instructed us to round differently were misinformed.
        // 2020-01-31 Round as Java default with the exception of exactly half way between numbers round down.
    	BigDecimal expect = new BigDecimal("-2.90");
    	BigDecimal sum = SigFigMathUtil.sigFigAdd(new BigDecimal("-2.97"), new BigDecimal("-2.84"));
    	BigDecimal actual = SigFigMathUtil.sigFigDivide(sum, new BigDecimal("2.00"));
    	assertEquals("-2.905 should round down to -2.90 rather than up to -2.91 " +
                "because it is exactly half way between values.", expect, actual);
    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     */
    @Test
    public void testSigFigSubtractionOfListOfNumbers() {
        // 2020-01-31 rounding change from HALF_UP to HALF_DOWN chnaged this to -552 from -553
        // 2020-02-11 new rounding and figures management for addtion/substraction rounds to -550
        //    This is because during the subtraction at one point the result is 7.5; only two figures
        BigDecimal expect = new BigDecimal("-550");
        BigDecimal actual = SigFigMathUtil.sigFigSubtract(bdList);
        assertEquals(expect.toPlainString(), actual.toPlainString());

        BigDecimal subANegBD = new BigDecimal("-50.0");
        bdList.add(subANegBD);
        actual = SigFigMathUtil.sigFigSubtract(bdList);
        expect = new BigDecimal("-500"); // see previous assert comments
        assertEquals(expect.toPlainString(), actual.toPlainString());
        bdList.remove(subANegBD);

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigSubtract(null, RM_UP);
        assertNull(nullResult);

        BigDecimal nullRmResult = SigFigMathUtil.sigFigSubtract(bdList, null);
        assertNull(nullRmResult);
        nullRmResult = SigFigMathUtil.sigFigSubtract(null, RM_UP);
        assertNull(nullRmResult);
        nullRmResult = SigFigMathUtil.sigFigSubtract(new LinkedList<>(), RM_UP);
        assertNull(nullRmResult);
    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     */
    @Test
    public void testSigFigAdd_BigDecimal_BigDecimal() {
        BigDecimal bd1 = new BigDecimal("130001.0");

        // 2020-02-05 round HALF_DOWN changes this to .2 from .3 when rounding HALF_UP
        BigDecimal expect = new BigDecimal("130001.2");  // half_down
        BigDecimal actual = SigFigMathUtil.sigFigAdd(bd1, pt250);
        assertEquals(expect.toPlainString(), actual.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigAdd(bd1, null);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigAdd(null, pt250);
        assertNull(nullResult2);
    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     */
    @Test
    public void testSigFigSub_BigDecimal_BigDecimal() {
        BigDecimal bd1 = new BigDecimal("130001.0");

        // 2020-02-05 round HALF_DOWN changes this to .7 from .8 when rounding HALF_UP
        BigDecimal expect = new BigDecimal("130000.7");
        BigDecimal actual = SigFigMathUtil.sigFigSubtract(bd1, pt250);
        assertEquals(expect.toPlainString(), actual.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigSubtract(bd1, null);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigSubtract(null, pt250);
        assertNull(nullResult2);
    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     */
    @Test
    public void testSigFigAdd_3args() {
        BigDecimal bd1 = new BigDecimal("130001.0");

        // check that a negative number rounds correctly
        BigDecimal expResult_up_a = new BigDecimal("-2");
        BigDecimal result_up_a = SigFigMathUtil.sigFigAdd(new BigDecimal(-1.5), BigDecimal.ZERO, RM_EVEN);
        assertEquals(expResult_up_a.toPlainString(), result_up_a.toPlainString());

        BigDecimal expect = new BigDecimal("130001.2");
        BigDecimal actual = SigFigMathUtil.sigFigAdd(bd1, pt250, RM_EVEN);
        assertEquals(expect, actual);

        BigDecimal bd1a = new BigDecimal("4057.7");
        BigDecimal bd2a = new BigDecimal("-27.850");

        expect = new BigDecimal("4029.8");
        actual = SigFigMathUtil.sigFigAdd(bd1a, bd2a, RM_EVEN);
        assertEquals(expect, actual);

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigAdd(null, pt250, RM_EVEN);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigAdd(bd1, null, RM_EVEN);
        assertNull(nullResult2);

        BigDecimal nullRmResult = SigFigMathUtil.sigFigAdd(bd1, pt250, null);
        assertNull(nullRmResult);
        
        BigDecimal doubleTest = new BigDecimal(".1");
        List<BigDecimal>bigList = new ArrayList<>();
        bigList.add(doubleTest);
        bigList.add(doubleTest);
        bigList.add(doubleTest);
        assertEquals(new BigDecimal(".3").toPlainString(), SigFigMathUtil.sigFigAdd(bigList).toPlainString());
    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     */
    @Test
    public void testSigFigSub_3args() {
        BigDecimal bd1 = new BigDecimal("510.0");
        BigDecimal expect = new BigDecimal("509.8");

        BigDecimal actual = SigFigMathUtil.sigFigSubtract(bd1, pt250, RM_EVEN);
        assertEquals(expect, actual);

        BigDecimal bd1a = new BigDecimal("130001.0");
        expect = new BigDecimal("130000.8");
        actual = SigFigMathUtil.sigFigSubtract(bd1a, pt250, RM_EVEN);
        assertEquals(expect, actual);

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigSubtract(null, pt250, RM_EVEN);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigSubtract(bd1, null, RM_EVEN);
        assertNull(nullResult2);

        BigDecimal nullRmResult = SigFigMathUtil.sigFigSubtract(bd1, pt250, null);
        assertNull(nullRmResult);
    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     */
    @Test
    public void testSigFigSub_2args() {
        BigDecimal bd1 = new BigDecimal("510.0");

        // 2020-02-05 round HALF_DOWN changes this to .7 from .8 when rounding HALF_UP
        BigDecimal expect = new BigDecimal("509.7");
        BigDecimal actual = SigFigMathUtil.sigFigSubtract(bd1, pt250);
        assertEquals(expect, actual);

        // 2020-02-05 round HALF_DOWN changes this to .7 from .8 when rounding HALF_UP
        BigDecimal bd1a = new BigDecimal("130001.0");
        expect = new BigDecimal("130000.7");
        actual = SigFigMathUtil.sigFigSubtract(bd1a, pt250);
        assertEquals(expect, actual);

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigSubtract(null, pt250);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigSubtract(bd1, null);
        assertNull(nullResult2);
    }

    /**
     * Test of sigFigMultiply method, of class SigFigMathUtil.
     *
     */
    @Test
    public void testSigFigMultiply_3args() {
        BigDecimal bd1 = new BigDecimal("130001.0");
        BigDecimal expect = new BigDecimal("33000");

        BigDecimal actual = SigFigMathUtil.sigFigMultiply(bd1, pt25, RM_EVEN);
        LOGGER.info("result is: " + actual.toPlainString());
        assertEquals(expect.toPlainString(), actual.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigMultiply(null, pt250, RM_EVEN);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigMultiply(bd1, null, RM_EVEN);
        assertNull(nullResult2);

        BigDecimal nullRmResult = SigFigMathUtil.sigFigMultiply(bd1, pt250, null);
        assertNull(nullRmResult);

    }

    /**
     * Test of sigFigMultiply method, of class SigFigMathUtil.
     *
     */
    @Test
    public void testSigFigMultiply_2args() {
        BigDecimal bd1 = new BigDecimal("130001.0");
        BigDecimal expect = new BigDecimal("33000");

        BigDecimal actual = SigFigMathUtil.sigFigMultiply(bd1, pt25);
        LOGGER.info("result is: " + actual.toPlainString());
        assertEquals(expect.toPlainString(), actual.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigMultiply(null, pt250);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigMultiply(bd1, null);
        assertNull(nullResult2);
    }

    /**
     * Test of sigFigMultiply method, of class SigFigMathUtil.
     *
     */
    @Test
    public void testSigFigDivide_3args() {
        BigDecimal bd1 = new BigDecimal("130001.0");
        BigDecimal expect = new BigDecimal("520000");

        BigDecimal actual = SigFigMathUtil.sigFigDivide(bd1, pt25, RM_EVEN);
        LOGGER.info("result is: " + actual.toPlainString());
        assertEquals(expect.toPlainString(), actual.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigDivide(null, pt250, RM_EVEN);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigDivide(bd1, null, RM_EVEN);
        assertNull(nullResult2);

        BigDecimal nullRmResult = SigFigMathUtil.sigFigDivide(bd1, pt250, null);
        assertNull(nullRmResult);
    }

    /**
     * Test of sigFigMultiply method, of class SigFigMathUtil.
     *
     */
    @Test
    public void testSigFigDivide_2args() {
        BigDecimal bd1 = new BigDecimal("1.00");
        BigDecimal bd2 = new BigDecimal("3");
        BigDecimal expect = new BigDecimal(".3");

        BigDecimal actual = SigFigMathUtil.sigFigDivide(bd1, bd2);
        LOGGER.info("result is: " + actual.toPlainString());
        assertEquals(expect.toPlainString(), actual.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigDivide(null, bd2);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigDivide(bd1, null);
        assertNull(nullResult2);

    }

    /**
     * Test of testSigFigDivide_0 method, of class SigFigMathUtil.
     *
     */
    @Test(expected = Exception.class)
    public void testSigFigDivide_0() {
        BigDecimal bd1 = new BigDecimal("3.00");
        BigDecimal bd2 = new BigDecimal("0.0");

        SigFigMathUtil.sigFigDivide(bd1, bd2);
    }
  
    /**
     * Test of sigFig, of class SigFigMathUtil.
     *
     */
    @Test
    public void testSigFigs() {
        BigDecimal bd1 = new BigDecimal("130001.0");
        int precision1 = 7;
        assertEquals(precision1, bd1.precision());
        assertEquals(bd1.toPlainString(), bd1.toPlainString());

        BigDecimal bd2 = new BigDecimal("12345678901234567890.1234567890");
        int precision2 = 30;
        assertEquals(precision2, bd2.precision());
        assertEquals(bd2.toPlainString(), bd2.toPlainString());

        BigDecimal bd3 = new BigDecimal("0.25");
        int precision3 = 2;
        assertEquals(precision3, bd3.precision());
        assertEquals(bd3.toPlainString(), bd3.toPlainString());

        BigDecimal bd4 = new BigDecimal("0.250");
        int precision4 = 3;
        assertEquals(precision4, bd4.precision());
        assertEquals(bd4.toPlainString(), bd4.toPlainString());
        
        BigDecimal bd5 = new BigDecimal("-0.2502");
        int precision5 = 4;
        assertEquals(precision5, bd5.precision());
        assertEquals(bd5.toPlainString(), bd5.toPlainString());

        BigDecimal bd6 = new BigDecimal("-1.23E-10");
        int precision6 = 3;
        assertEquals(precision6, bd6.precision());
        assertNotSame(bd6.toPlainString(), bd6.toPlainString());

        BigDecimal bd7 = new BigDecimal("1.23E-10");
        int precision7 = 3;
        assertEquals(precision7, bd7.precision());
        assertNotSame(bd7.toPlainString(), bd6.toPlainString()); //note that plain string will present as .000000000123
    }
    
    @Test
    public void testsigFigDivideByExact(){
        //if you had to determine an average for example
        BigDecimal b1 = new BigDecimal("1.1");
        BigDecimal b2 = new BigDecimal("2.020");
        BigDecimal b3 = new BigDecimal("4.41");
        List<BigDecimal> bigList = new ArrayList<>();
        bigList.add(b1);
        bigList.add(b2);
        bigList.add(b3);
        
        BigDecimal total = SigFigMathUtil.sigFigAdd(bigList);
        assertEquals("7.5", total.toPlainString());
        
        BigDecimal quotient = SigFigMathUtil.sigFigDivideByExact(total, new BigDecimal(bigList.size()), RM_UP);
        assertEquals("2.5", quotient.toPlainString());
    }
    
        
    @Test
    public void testsigFigDivideByExact2arg(){
        //if you had to determine an average for example
        BigDecimal b1 = new BigDecimal("1.1");
        BigDecimal b2 = new BigDecimal("2.020");
        BigDecimal b3 = new BigDecimal("4.41");
        List <BigDecimal> bigList = new ArrayList<>();
        bigList.add(b1);
        bigList.add(b2);
        bigList.add(b3);
        
        BigDecimal total = SigFigMathUtil.sigFigAdd(bigList);
        assertEquals("7.5", total.toPlainString());
        
        BigDecimal quotient = SigFigMathUtil.sigFigDivideByExact(total, new BigDecimal(bigList.size()));
        assertEquals("2.5", quotient.toPlainString());
    }
         
    @Test
    public void testsigFigMultiplyByExact(){
        //if you had to determine an average for example
        BigDecimal b1 = new BigDecimal("10.1");
        BigDecimal exact = new BigDecimal(".33");        
        
        BigDecimal quotient = SigFigMathUtil.sigFigMultiplyByExact(b1, exact, RM_UP);
        assertEquals("3.33", quotient.toPlainString());
    }
    
             
    @Test
    public void testsigFigMultiplyByExact2arg(){
        //if you had to determine an average for example
        BigDecimal b1 = new BigDecimal("10.1");
        BigDecimal exact = new BigDecimal(".33");        
        
        BigDecimal quotient = SigFigMathUtil.sigFigMultiplyByExact(b1, exact);
        assertEquals("3.33", quotient.toPlainString());
    }
        
	
	@Test
	public void test_sigFigAdd_nullEntry() {
		List<BigDecimal> bdlist = new LinkedList<>();
		bdlist.add(BigDecimal.ONE);
		bdlist.add(null);
		
		BigDecimal actual = SigFigMathUtil.sigFigAdd(bdlist, SigFigMathUtil.DEFAULT_ROUNDING_RULE);
		assertNull(actual);
	}

	@Test
	public void test_sigFigSubtract_nullEntry() {
		List<BigDecimal> bdlist = new LinkedList<>();
		bdlist.add(BigDecimal.ONE);
		bdlist.add(null);
		
		BigDecimal actual = SigFigMathUtil.sigFigSubtract(bdlist, SigFigMathUtil.DEFAULT_ROUNDING_RULE);
		assertNull(actual);
	}
	@Test
	public void test_sigFigSubtract_nullFirstEntry() {
		List<BigDecimal> bdlist = new LinkedList<>();
		bdlist.add(null);
		bdlist.add(BigDecimal.ONE);
		
		BigDecimal actual = SigFigMathUtil.sigFigSubtract(bdlist, SigFigMathUtil.DEFAULT_ROUNDING_RULE);
		assertNull(actual);
	}
	
	
	@Test
	public void test_getLeastScale_null() {
		int actual = SigFigMathUtil.getLeastScale((List)null);
		assertEquals(0, actual);
		
		actual = SigFigMathUtil.getLeastScale(new LinkedList<>());
		assertEquals(0, actual);

        actual = SigFigMathUtil.getLeastScale((BigDecimal[]) null);
        assertEquals(0, actual);
	}
	
	@Test
	public void test_getLeastPrecise_null() {
		int actual = SigFigMathUtil.getLeastPrecise(null, BigDecimal.ONE);
		assertEquals(0, actual);
		
		actual = SigFigMathUtil.getLeastPrecise(BigDecimal.ONE, null);
		assertEquals(0, actual);
	}
	
	@Test
	public void test_sigFigDivideByExact_null() {
		BigDecimal actual = SigFigMathUtil.sigFigDivideByExact(null, BigDecimal.ONE);
		assertNull(actual);
		
		actual = SigFigMathUtil.sigFigDivideByExact(BigDecimal.ONE, null);
		assertNull(actual);
	}
	
	@Test
	public void test_sigFigMultiplyByExact_null() {
		BigDecimal actual = SigFigMathUtil.sigFigMultiplyByExact(null, BigDecimal.ONE);
		assertNull(actual);
		
		actual = SigFigMathUtil.sigFigMultiplyByExact(BigDecimal.ONE, null);
		assertNull(actual);
	}	
    
	@Test
	public void test_sigFigDivideByExact_withRoundngRule_null() {
		BigDecimal actual = SigFigMathUtil.sigFigDivideByExact(null, BigDecimal.ONE, SigFigMathUtil.DEFAULT_ROUNDING_RULE);
		assertNull(actual);
		
		actual = SigFigMathUtil.sigFigDivideByExact(BigDecimal.ONE, null, SigFigMathUtil.DEFAULT_ROUNDING_RULE);
		assertNull(actual);
		
		actual = SigFigMathUtil.sigFigDivideByExact(BigDecimal.ONE, BigDecimal.ONE, null);
		assertNull(actual);
	}
	
	@Test
	public void test_sigFigMultiplyByExact_withRoundngRule_null() {
		BigDecimal actual = SigFigMathUtil.sigFigMultiplyByExact(null, BigDecimal.ONE, SigFigMathUtil.DEFAULT_ROUNDING_RULE);
		assertNull(actual);
		
		actual = SigFigMathUtil.sigFigMultiplyByExact(BigDecimal.ONE, null, SigFigMathUtil.DEFAULT_ROUNDING_RULE);
		assertNull(actual);
		
		actual = SigFigMathUtil.sigFigMultiplyByExact(BigDecimal.ONE, BigDecimal.ONE, null);
		assertNull(actual);
	}

    @Test
    public void test_addition() {
        // TODO asdf move to BigDecimalDeficienciesTest
        BigDecimal tenth = new BigDecimal("0.1");
        System.out.println("BigDecimal tenth " + tenth); // 0.1

        List<BigDecimal> thenths = new ArrayList<>(10);
        BigDecimal one = BigDecimal.ZERO;
        for (int i=0; i<10; i++) {
            one = one.add(tenth);
            thenths.add(tenth);
        }
        // BigDecimal tenth added 10 times is "1.0"; however, it should be "1"
        BigDecimal expect = new BigDecimal("1.0");
        // Addition is a two step process. Not only aligning the decimal points, but
        // it is also supposed to round to minimum precision after each addition.
        // "This is wrong but the test must pass. adding 0.1 ten times goes from 1 digit to 2"
        assertEquals("This demonstrates that BigDecimal is insufficient with precision management for addition",
                expect, one);

        BigDecimal actual = SigFigMathUtil.sigFigAdd(thenths);
        // supposed to add from most scale to least, rounding after each to the least scale. (scale is mantissa)
        assertEquals("Low precision added many times should yield low precision.",
                "1", actual.toPlainString());
    }
    @Test
    public void test_trailingZerosBeforeMissingDecimalPoint() {
        BigDecimal thousand = new BigDecimal("1000");
        BigDecimal onePoint111 = new BigDecimal("1.111");

        BigDecimal thousandOne = SigFigMathUtil.sigFigMultiply(thousand, onePoint111);

        assertEquals("This is wrong the real value should be 1000", "1111", thousandOne.toPlainString());
    }
    @Test
    public void test_BigDecimalHasPrecisionIssuesWithDecimalPoint() {
        BigDecimal thousand = new BigDecimal("1000");
        BigDecimal denominator = new BigDecimal("100.001");
        BigDecimal actual = SigFigMathUtil.sigFigDivide(thousand, denominator);
        assertEquals("But maybe correct for GWW needs? 1000/100.001 rather than 10", "10.00", actual.toPlainString());
        assertEquals("precision should really be 2", 4, actual.precision());
    }
    @Test
    public void test_BigDecimalPrecisionNotScientific() { // TODO asdf fix
        // This is incorrect! 1000./100.00 is 10.00 not 1E+1 (or 10)
        BigDecimal numerator = new BigDecimal("1000.");
        BigDecimal denominator = new BigDecimal("100.00");
        BigDecimal actual = SigFigMathUtil.sigFigDivide(numerator, denominator);

        assertEquals("Correct! 1000./100.00 should be 10.00", "10.00", actual.toPlainString());
        assertNotEquals("Correct! 1000./100.00 should be 10.00", "1E+1", actual.toString());
        assertEquals("Correct! 1000./100.00 have 4 precision", 4, actual.precision());
    }

    @Test
    public void test_oneHalfWithEnoughPrecision() {
        BigDecimal one = new BigDecimal("1");
        BigDecimal two = new BigDecimal("2.0");

        BigDecimal actual = SigFigMathUtil.sigFigDivide(one, two);
        assertEquals("0.5", actual.toPlainString());

        BigDecimal three = new BigDecimal("3.0");

        actual = SigFigMathUtil.sigFigDivide(three, two);
        assertEquals("1.5", actual.toPlainString());
    }
    @Test
    public void test_roundPoint5Down() {
        BigDecimal three = new BigDecimal("3");
        BigDecimal two = new BigDecimal("2.0");

        BigDecimal actual = SigFigMathUtil.sigFigDivide(three, two);
        assertEquals("Because the USGS rounding rule is to round 0.5 down to 0 when there isn't " +
                "enough precision, 3/2 is 1 and not 2 much less 1.5 which increase precision.",BigDecimal.ONE, actual);
    }

    @Test
    public void test_roundPoint5PlusUp() {
        BigDecimal oneOne = new BigDecimal("1.1");
        BigDecimal two = new BigDecimal("2");

        BigDecimal actual = SigFigMathUtil.sigFigDivide(oneOne, two);
        assertEquals("The standard is to round numbers to the least precision and round up for numbers" +
                        "greater than half way to the next.","0.5", actual.toPlainString());
    }

    @Test
    public void test_USGS_roungingRule_roundUp() {
        BigDecimal actual;

        MathContext mc1 = new MathContext(1, RoundingMode.HALF_DOWN);
        MathContext mc2 = new MathContext(2, RoundingMode.HALF_DOWN);

        // 2020-01-31 now we found a document that suggests USGS rounds up for past halfway mark
        actual = new BigDecimal("1.5001").round(mc1);
        assertEquals("Round up for 1.5001 numbers, numbers past halfway.", "2", actual.toPlainString());

        actual = new BigDecimal("1.51").round(mc1);
        assertEquals("2", actual.toPlainString());

//        actual = new BigDecimal("1.515").round(mc1);
//        assertEquals(RoundingMode.HALF_UP, actual);

        actual = new BigDecimal("1.51").round(mc2);
        assertEquals("1.5", actual.toPlainString());
    }

    @Test
    public void test_USGS_roungingRule_roundDown() {
        BigDecimal actual;

        MathContext mc1 = new MathContext(1, RoundingMode.HALF_DOWN);
        MathContext mc2 = new MathContext(2, RoundingMode.HALF_DOWN);
        MathContext mc3 = new MathContext(3, RoundingMode.HALF_DOWN);
        MathContext mc4 = new MathContext(4, RoundingMode.HALF_DOWN);

        // 2020-01-31 now we found a document that suggests USGS rounds perfect point 5 values down
        actual = new BigDecimal("1.5").round(mc1);
        assertEquals("1", actual.toPlainString());
        actual = new BigDecimal("1.5000").round(mc1);
        assertEquals("Round down for 1.5 numbers.", "1", actual.toPlainString());

        actual = new BigDecimal("1.5").round(mc2);
        assertEquals("1.5", actual.toPlainString());
        actual = new BigDecimal("1.234500").round(mc2);
        assertEquals("the point 5 rule should not effect typical rounding.", "1.2", actual.toPlainString());

        actual = new BigDecimal("1.515").round(mc3);
        assertEquals("1.51", actual.toPlainString());

        // 2020-01-31 now we found a document that suggests USGS rounds up for past halfway mark
        actual = new BigDecimal("1.234500").round(mc4);
        assertEquals("Round down for 1.xxx5 numbers.", "1.234", actual.toPlainString());
        /*
        This might seem odd to return HALF_UP because we do not want the result 2.0
        when 1.49 is rounded. However, it is just telling Java which rule to use and
        Java does the proper action with 1.49 using HALF_UP and it is the default rule
         */
        actual = new BigDecimal("1.49").round(mc1);
        assertEquals("1", actual.toPlainString());
    }

    @Test
    public void test_updateSigFigs() {
        BigDecimal numerator = new BigDecimal("1000.");
        BigDecimal denominator = new BigDecimal("100.00");
        BigDecimal actual = numerator.divide(denominator, RoundingMode.HALF_UP); // this is fixed in SigFigMathUtil

        // first show the issues
        assertEquals("Incorrect! 1000./100.00 should be 10.00", "10", actual.toPlainString());

        // show one solution
        BigDecimal updated = SigFigMathUtil.updateSigFigs(actual, 4);
        assertEquals("Correct! 1000./100.00 should be 10.00", "10.00", updated.toPlainString());

        // show another use case
        BigDecimal ten = new BigDecimal("10");
        assertEquals(2, ten.precision());
        updated = SigFigMathUtil.updateSigFigs(ten, 4);
        assertEquals("10.00", updated.toPlainString());

        // show more settings including compatibility with subclass
        ScientificDecimal hundred = new ScientificDecimal("100");
        assertEquals(1, hundred.precision());
        updated = SigFigMathUtil.updateSigFigs(hundred, 4);
        assertEquals("100.0", updated.toPlainString());
        updated = SigFigMathUtil.updateSigFigs(hundred, 7);
        assertEquals("100.0000", updated.toPlainString());
    }


    @Test
    public void test_leastPrecision() {
        BigDecimal numbers[] = null;

        int actual = SigFigMathUtil.getLeastPrecise(numbers);
        assertEquals("No entries yields zero precision",0 , actual);

        numbers = new BigDecimal[0];
        actual = SigFigMathUtil.getLeastPrecise(numbers);
        assertEquals("Null entry yields zero precision",0 , actual);

        actual = SigFigMathUtil.getLeastPrecise(null, new BigDecimal("1.234"));
        assertEquals("Null entry yields zero precision",0 , actual);
    }

    @Test
    public void test_divideZeroResultWithPrecision() {
        BigDecimal actual = SigFigMathUtil.sigFigDivide(ScientificDecimal.ZERO, new BigDecimal("1.234"));
        assertEquals("zero divide by anything is zero",0 , BigDecimal.ZERO.compareTo(actual));
        assertEquals("least precision is 4",4 , actual.precision());
        assertEquals("zero has scale of 4",4 , actual.scale());
    }

    @Test
    public void test_SubtractList_size() {
        SigFigMathUtil.SubtractList list = new SigFigMathUtil.SubtractList(bdList);

        int expect = bdList.size();
        int size = list.size();
        assertEquals(expect, size);
    }
    @Test
    public void test_SubtractList_getFirst(){
        SigFigMathUtil.SubtractList list = new SigFigMathUtil.SubtractList(bdList);
        assertEquals("-"+bdList.get(0).toPlainString(), list.get(0).toPlainString());
    }
    @Test
    public void test_SubtractList_getOthers(){
        // SETUP
        SigFigMathUtil.SubtractList list = new SigFigMathUtil.SubtractList(bdList);
        Iterator<BigDecimal> expect = bdList.iterator();
        Iterator<BigDecimal> actual = list.iterator();
        // skip first
        expect.next();
        actual.next();

        while (expect.hasNext()) {
            assertEquals(expect.next(), actual.next());
        }
    }

}
