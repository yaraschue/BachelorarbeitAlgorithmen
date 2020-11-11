import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/**
* Evaluates a given synthetic database in comparison to the original database concerning the utility. 
*
* @author Yara Schuett
*/

public abstract class Utility{
	
	protected double[][] origDatabase;
	
	protected double[][] synthDatabase;
	
	protected int numQI;
	
	// muss immer gesetzt werden
	protected int numSections;
	
	// zu vergleichende Datensätze müssen übergeben werden
	public Utility(double[][] origDatabase, double[][] synthDatabase){
		this.origDatabase = origDatabase;
		this.synthDatabase = synthDatabase;
		numQI = origDatabase[0].length;
	}
	
	/**
	* Calculate utility for the given databases origDatabase and synthDatabase. 
	*/
	public abstract double utility();
	
	//das geht glaube ich nicht, sind zu unterschiedlich
	//public abstract double query(database[][]);
	
	// Umbenennen zu calculate Range
	// public abstract double[] calculateRangeDatabase(int section);
}