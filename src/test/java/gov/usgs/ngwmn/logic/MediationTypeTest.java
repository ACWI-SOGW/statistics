package gov.usgs.ngwmn.logic;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.usgs.ngwmn.model.MediationType;

public class MediationTypeTest {

	@Test
	public void test_equalNull() {
		assertFalse(MediationType.DEFAULT.equalSortOrder(null));
		assertFalse(MediationType.ASCENDING.equalSortOrder(null));
		assertFalse(MediationType.AboveDatum.equalSortOrder(null));
		assertFalse(MediationType.DESCENDING.equalSortOrder(null));
		assertFalse(MediationType.BelowLand.equalSortOrder(null));
	}
	@Test
	public void test_equalOther() {
		assertFalse(MediationType.DEFAULT.equalSortOrder(""));
		assertFalse(MediationType.ASCENDING.equalSortOrder(""));
		assertFalse(MediationType.AboveDatum.equalSortOrder(""));
		assertFalse(MediationType.DESCENDING.equalSortOrder(""));
		assertFalse(MediationType.BelowLand.equalSortOrder(""));
	}
	@Test
	public void test_equalAscending() {
		assertTrue(MediationType.ASCENDING.equalSortOrder(MediationType.ASCENDING));
		assertTrue(MediationType.ASCENDING.equalSortOrder(MediationType.AboveDatum));
		assertTrue(MediationType.AboveDatum.equalSortOrder(MediationType.ASCENDING));
		assertTrue(MediationType.AboveDatum.equalSortOrder(MediationType.AboveDatum));
		
		assertFalse(MediationType.ASCENDING.equalSortOrder(MediationType.DESCENDING));
		assertFalse(MediationType.AboveDatum.equalSortOrder(MediationType.BelowLand));
	}
	@Test
	public void test_equalDescending() {
		assertTrue(MediationType.DESCENDING.equalSortOrder(MediationType.DESCENDING));
		assertTrue(MediationType.DESCENDING.equalSortOrder(MediationType.BelowLand));
		assertTrue(MediationType.BelowLand.equalSortOrder(MediationType.DESCENDING));
		assertTrue(MediationType.BelowLand.equalSortOrder(MediationType.BelowLand));
		
		assertFalse(MediationType.BelowLand.equalSortOrder(MediationType.ASCENDING));
		assertFalse(MediationType.DESCENDING.equalSortOrder(MediationType.AboveDatum));
	}
	@Test
	public void test_equalDefault() {
		// ASCENDING seems more natural sort for non-well data and is default for this reason
		assertFalse(MediationType.BelowLand.equalSortOrder(MediationType.DEFAULT));
		assertFalse(MediationType.DESCENDING.equalSortOrder(MediationType.DEFAULT));
		
		assertTrue(MediationType.AboveDatum.equalSortOrder(MediationType.DEFAULT));
		assertTrue(MediationType.ASCENDING.equalSortOrder(MediationType.DEFAULT));
	}

}
