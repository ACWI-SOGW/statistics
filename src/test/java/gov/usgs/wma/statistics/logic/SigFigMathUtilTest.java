package gov.usgs.wma.statistics.logic;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.statistics.logic.SigFigMathUtil.CustomRoundingRule;
import gov.usgs.wma.statistics.logic.SigFigMathUtil.JavaDefaultRoundingRule;


/**
 *
 * @author smlarson
 * @author duselman
 */
public class SigFigMathUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SigFigMathUtilTest.class);
    private static final String[] args = {"12.000000", ".1", "0.0", "4.40", "560"}; //total 576.5
    private static final BigDecimal expResult_default_half_even = new BigDecimal("576");
    private static final BigDecimal expResult_default = new BigDecimal("577");
    private static List<BigDecimal> bdList;
    
    private static final CustomRoundingRule RM_UP   = new JavaDefaultRoundingRule(RoundingMode.HALF_UP);
    private static final CustomRoundingRule RM_DOWN = new JavaDefaultRoundingRule(RoundingMode.HALF_DOWN);
    private static final CustomRoundingRule RM_EVEN = new JavaDefaultRoundingRule(RoundingMode.HALF_EVEN);

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
        BigDecimal result = SigFigMathUtil.sigFigAdd(bdList, RM_EVEN);
        assertEquals(expResult_default_half_even.toPlainString(), result.toPlainString());

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
        BigDecimal result = SigFigMathUtil.sigFigAdd(bdList);
        assertEquals(expResult_default.toPlainString(), result.toPlainString());

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
    public void testSqigFigAdd_round() {
    	// 03-30-2017 need to round up for negative numbers -2.90 rather than the Java default -2.91 
    	// 07-31-2018 need to revert back to Java default rounding. Those who instructed us to round differently were misinformed.
    	BigDecimal expect = new BigDecimal("-2.91");
    	BigDecimal sum = SigFigMathUtil.sigFigAdd(new BigDecimal("-2.97"), new BigDecimal("-2.84"));
    	BigDecimal actual = SigFigMathUtil.sigFigDivide(sum, new BigDecimal("2.00"));
    	assertEquals(expect, actual);
    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     */
    @Test
    public void testSigFigSub_List() {
        BigDecimal expResult = new BigDecimal("-553"); // 07-31-2018 for standard Java HALF_UP for negative numbers
        BigDecimal result = SigFigMathUtil.sigFigSubtract(bdList);
        assertEquals(expResult.toPlainString(), result.toPlainString());

        expResult = new BigDecimal("-503");
        BigDecimal subANegBD = new BigDecimal("-50");
        bdList.add(subANegBD);
        result = SigFigMathUtil.sigFigSubtract(bdList);
        assertEquals(expResult.toPlainString(), result.toPlainString());
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
        BigDecimal bd2 = new BigDecimal("0.25");

        BigDecimal expResult = new BigDecimal("130001.3");  // half_up
        BigDecimal result = SigFigMathUtil.sigFigAdd(bd1, bd2);
        assertEquals(expResult.toPlainString(), result.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigAdd(bd1, null);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigAdd(null, bd2);
        assertNull(nullResult2);
    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     */
    @Test
    public void testSigFigSub_BigDecimal_BigDecimal() {
        BigDecimal bd1 = new BigDecimal("130001.0");
        BigDecimal bd2 = new BigDecimal("0.25");

        BigDecimal expResult = new BigDecimal("130000.8");
        BigDecimal result = SigFigMathUtil.sigFigSubtract(bd1, bd2);
        assertEquals(expResult.toPlainString(), result.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigSubtract(bd1, null);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigSubtract(null, bd2);
        assertNull(nullResult2);
    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     */
    @Test
    public void testSigFigAdd_3args() {
        BigDecimal bd1 = new BigDecimal("130001.0");
        BigDecimal bd2 = new BigDecimal("0.25");

        // check that a negative number rounds correctly
        BigDecimal expResult_up_a = new BigDecimal("-2");
        BigDecimal result_up_a = SigFigMathUtil.sigFigAdd(new BigDecimal(-1.5), BigDecimal.ZERO, RM_EVEN);
        assertEquals(expResult_up_a.toPlainString(), result_up_a.toPlainString());

        BigDecimal expResult = new BigDecimal("130001.2");
        BigDecimal result = SigFigMathUtil.sigFigAdd(bd1, bd2, RM_EVEN);
        assertEquals(expResult, result);

        BigDecimal bd1a = new BigDecimal("4057.7");
        BigDecimal bd2a = new BigDecimal("-27.85");

        BigDecimal expResulta = new BigDecimal("4029.8");
        BigDecimal resulta = SigFigMathUtil.sigFigAdd(bd1a, bd2a, RM_EVEN);
        assertEquals(expResulta, resulta);

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigAdd(null, bd2, RM_EVEN);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigAdd(bd1, null, RM_EVEN);
        assertNull(nullResult2);

        BigDecimal nullRmResult = SigFigMathUtil.sigFigAdd(bd1, bd2, null);
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
        BigDecimal bd2 = new BigDecimal("0.25");

        BigDecimal expResult = new BigDecimal("509.8");
        BigDecimal result = SigFigMathUtil.sigFigSubtract(bd1, bd2, RM_EVEN);
        assertEquals(expResult, result);

        BigDecimal bd1a = new BigDecimal("130001.0");
        BigDecimal expResulta = new BigDecimal("130000.8");
        BigDecimal resulta = SigFigMathUtil.sigFigSubtract(bd1a, bd2, RM_EVEN);
        assertEquals(expResulta, resulta);

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigSubtract(null, bd2, RM_EVEN);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigSubtract(bd1, null, RM_EVEN);
        assertNull(nullResult2);

        BigDecimal nullRmResult = SigFigMathUtil.sigFigSubtract(bd1, bd2, null);
        assertNull(nullRmResult);
    }

    /**
     * Test of sigFigAdd method, of class SigFigMathUtil.
     */
    @Test
    public void testSigFigSub_2args() {
        BigDecimal bd1 = new BigDecimal("510.0");
        BigDecimal bd2 = new BigDecimal("0.25");

        BigDecimal expResult = new BigDecimal("509.8");
        BigDecimal result = SigFigMathUtil.sigFigSubtract(bd1, bd2);
        assertEquals(expResult, result);

        BigDecimal bd1a = new BigDecimal("130001.0");
        BigDecimal expResulta = new BigDecimal("130000.8");
        BigDecimal resulta = SigFigMathUtil.sigFigSubtract(bd1a, bd2);
        assertEquals(expResulta, resulta);

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigSubtract(null, bd2);
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
        BigDecimal bd2 = new BigDecimal("0.25");
        BigDecimal expResult = new BigDecimal("33000");

        BigDecimal result = SigFigMathUtil.sigFigMultiply(bd1, bd2, RM_EVEN);
        LOGGER.info("result is: " + result.toPlainString());
        assertEquals(expResult.toPlainString(), result.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigMultiply(null, bd2, RM_EVEN);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigMultiply(bd1, null, RM_EVEN);
        assertNull(nullResult2);

        BigDecimal nullRmResult = SigFigMathUtil.sigFigMultiply(bd1, bd2, null);
        assertNull(nullRmResult);

    }

    /**
     * Test of sigFigMultiply method, of class SigFigMathUtil.
     *
     */
    @Test
    public void testSigFigMultiply_2args() {
        BigDecimal bd1 = new BigDecimal("130001.0");
        BigDecimal bd2 = new BigDecimal("0.25");
        BigDecimal expResult = new BigDecimal("33000");

        BigDecimal result = SigFigMathUtil.sigFigMultiply(bd1, bd2);
        LOGGER.info("result is: " + result.toPlainString());
        assertEquals(expResult.toPlainString(), result.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigMultiply(null, bd2);
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
        BigDecimal bd2 = new BigDecimal("0.25");
        BigDecimal expResult = new BigDecimal("520000");

        BigDecimal result = SigFigMathUtil.sigFigDivide(bd1, bd2, RM_EVEN);
        LOGGER.info("result is: " + result.toPlainString());
        assertEquals(expResult.toPlainString(), result.toPlainString());

        //null check
        BigDecimal nullResult = SigFigMathUtil.sigFigDivide(null, bd2, RM_EVEN);
        assertNull(nullResult);

        BigDecimal nullResult2 = SigFigMathUtil.sigFigDivide(bd1, null, RM_EVEN);
        assertNull(nullResult2);

        BigDecimal nullRmResult = SigFigMathUtil.sigFigDivide(bd1, bd2, null);
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

        BigDecimal expResult = new BigDecimal(".3");

        BigDecimal result = SigFigMathUtil.sigFigDivide(bd1, bd2);
        LOGGER.info("result is: " + result.toPlainString());
        assertEquals(expResult.toPlainString(), result.toPlainString());

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
        assertEquals(bd1.toString(), bd1.toPlainString());

        BigDecimal bd2 = new BigDecimal("12345678901234567890.1234567890");
        int precision2 = 30;
        assertEquals(precision2, bd2.precision());
        assertEquals(bd2.toString(), bd2.toPlainString());

        BigDecimal bd3 = new BigDecimal("0.25");
        int precision3 = 2;
        assertEquals(precision3, bd3.precision());
        assertEquals(bd3.toString(), bd3.toPlainString());

        BigDecimal bd4 = new BigDecimal("0.250");
        int precision4 = 3;
        assertEquals(precision4, bd4.precision());
        assertEquals(bd4.toString(), bd4.toPlainString());
        
        BigDecimal bd5 = new BigDecimal("-0.2502");
        int precision5 = 4;
        assertEquals(precision5, bd5.precision());
        assertEquals(bd5.toString(), bd5.toPlainString());

        BigDecimal bd6 = new BigDecimal("-1.23E-10");
        int precision6 = 3;
        assertEquals(precision6, bd6.precision());
        assertNotSame(bd6.toString(), bd6.toPlainString());

        BigDecimal bd7 = new BigDecimal("1.23E-10");
        int precision7 = 3;
        assertEquals(precision7, bd7.precision());
        assertNotSame(bd7.toString(), bd6.toPlainString()); //note that plain string will present as .000000000123
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
		BigDecimal actual = SigFigMathUtil.getLeastScale(null);
		assertNull(actual);
		
		actual = SigFigMathUtil.getLeastScale(new LinkedList<>());
		assertNull(actual);
	}
	
	@Test
	public void test_getLeastPrecise_null() {
		BigDecimal actual = SigFigMathUtil.getLeastPrecise(null, BigDecimal.ONE);
		assertNull(actual);
		
		actual = SigFigMathUtil.getLeastPrecise(BigDecimal.ONE, null);
		assertNull(actual);
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
	public void test_MathmaticsPositiveInfinityRoundingRule() {
		SigFigMathUtil.MathmaticsPositiveInfinityRoundingRule rule = new SigFigMathUtil.MathmaticsPositiveInfinityRoundingRule();
		
		RoundingMode rmPositive = rule.valueRule(new BigDecimal("1"));
		assertEquals("Round up to positive infinity for positive numbers.", RoundingMode.HALF_UP, rmPositive);
		
		RoundingMode rmNegative = rule.valueRule(new BigDecimal("-1"));
		assertEquals("Round down toward zero for negative numbers.", RoundingMode.HALF_DOWN, rmNegative);
		
		rmPositive = rule.productRule(new BigDecimal("-1"), new BigDecimal("-1"));
		assertEquals("Round up to positive infinity for positive numbers.", RoundingMode.HALF_UP, rmPositive);
		rmPositive = rule.productRule(new BigDecimal("1"), new BigDecimal("1"));
		assertEquals("Round up to positive infinity for positive numbers.", RoundingMode.HALF_UP, rmPositive);
		
		rmNegative = rule.productRule(new BigDecimal("-1"),new BigDecimal("1"));
		assertEquals("Round down toward zero for negative numbers.", RoundingMode.HALF_DOWN, rmNegative);
		rmNegative = rule.productRule(new BigDecimal("1"),new BigDecimal("-1"));
		assertEquals("Round down toward zero for negative numbers.", RoundingMode.HALF_DOWN, rmNegative);
	}
	
    
    @Test
    public void test_BigDecimal_1000() {
    	BigDecimal thousand = new BigDecimal("1000");
    	BigDecimal one = new BigDecimal("1.111");
    	
    	BigDecimal thousandOne = SigFigMathUtil.sigFigMultiply(thousand, one);
    	
    	assertEquals("This is wrong the real value should be 1000", "1111", thousandOne.toPlainString());
    }

}
