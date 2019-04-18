package gov.usgs.ngwmn.logic;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.usgs.ngwmn.model.MediationType;

public class MediationTypeTest {

	@Test
	public void test_equalNull() {
		assertFalse(MediationType.DEFAULT.equal(null));
		assertFalse(MediationType.ASCENDING.equal(null));
		assertFalse(MediationType.AboveDatum.equal(null));
		assertFalse(MediationType.DESCENDING.equal(null));
		assertFalse(MediationType.BelowLand.equal(null));
	}
	@Test
	public void test_equalOther() {
		assertFalse(MediationType.DEFAULT.equal(""));
		assertFalse(MediationType.ASCENDING.equal(""));
		assertFalse(MediationType.AboveDatum.equal(""));
		assertFalse(MediationType.DESCENDING.equal(""));
		assertFalse(MediationType.BelowLand.equal(""));
	}
	@Test
	public void test_equalAscending() {
		assertTrue(MediationType.ASCENDING.equal(MediationType.ASCENDING));
		assertTrue(MediationType.ASCENDING.equal(MediationType.AboveDatum));
		assertTrue(MediationType.AboveDatum.equal(MediationType.ASCENDING));
		assertTrue(MediationType.AboveDatum.equal(MediationType.AboveDatum));
		
		assertFalse(MediationType.ASCENDING.equal(MediationType.DESCENDING));
		assertFalse(MediationType.AboveDatum.equal(MediationType.BelowLand));
	}
	@Test
	public void test_equalDescending() {
		assertTrue(MediationType.DESCENDING.equal(MediationType.DESCENDING));
		assertTrue(MediationType.DESCENDING.equal(MediationType.BelowLand));
		assertTrue(MediationType.BelowLand.equal(MediationType.DESCENDING));
		assertTrue(MediationType.BelowLand.equal(MediationType.BelowLand));
		
		assertFalse(MediationType.BelowLand.equal(MediationType.ASCENDING));
		assertFalse(MediationType.DESCENDING.equal(MediationType.AboveDatum));
	}
	@Test
	public void test_equalDefault() {
		// ASCENDING seems more natural sort for non-well data and is default for this reason
		assertFalse(MediationType.BelowLand.equal(MediationType.DEFAULT));
		assertFalse(MediationType.DESCENDING.equal(MediationType.DEFAULT));
		
		assertTrue(MediationType.AboveDatum.equal(MediationType.DEFAULT));
		assertTrue(MediationType.ASCENDING.equal(MediationType.DEFAULT));
	}

}
