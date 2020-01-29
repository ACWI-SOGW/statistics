package gov.usgs.wma.statistics.logic;

/**
 * This is an NGWMN model of significant figures that has been simplified.
 * 
 * Data from the providers does not include precision information 
 * when it comes to integers. For example: 1000 could be 1 or it 
 * could be as many as 4 figures. The policy of the NGWMN project is to 
 * treat this as 4 figures. Only leading zeros are insignificant. 
 * 
 * @author duselman
 *
 */
@SuppressWarnings("serial")
public class UsgsDecimal extends ScientificDecimal {
	
	public UsgsDecimal(String value, int sigfigs) {
		super(value, sigfigs);
	}

	public UsgsDecimal(String value) {
		super(value);
	}
	
	@Override
	protected void sigfigRules(String value) {
		sigfigs = super.precision();
		
		String figs = value;
		figs = figs.replace(".", ""); // replace is not regex
		if (doubleValue() != 0.0) {
			// do not remove zeros for zero itself
			figs = figs.replaceAll("^0+", "");
		}
		sigfigs = figs.length();
	}
}
