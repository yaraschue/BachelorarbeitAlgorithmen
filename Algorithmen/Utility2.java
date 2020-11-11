import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/**
* Evaluates a given synthetic database in comparison to the original database concerning the utility
* by comparing the relative frequency. Therefore the range of the values is partioned in sections considering 
* that the given databases are stanardized.
*
* @author Yara Schuett
*/
public class Utility2 extends Utility{
	
	private double[][] origDatabase;
	
	private double[][] synthDatabase;
	
	private int sizeQueryClass;
	
	private int numSections;
	
	private int numQI;
	
	private double utilityValue;
	
	/**
	* Constructor where the given original and synthetic database are set. Also the amount of QIs is calculated
	* and the arrays for the mean and the standard derivatiion are initialized
	*
	* @param origDatabase The original database for this method.
	* @param synthDatabase The synthetic database for this method.
	*/
	public Utility2(double[][] origDatabase, double[][] synthDatabase){
		
		super(origDatabase, synthDatabase);
		
		this.origDatabase = super.origDatabase;
		this.synthDatabase = super.synthDatabase;
		numQI = super.numQI;
		
		// set the amount of sections to 100
		numSections = 100;
		// calculation differs with the chosen allowed queries
		sizeQueryClass = numSections * numQI;

	}
	
	/**
	* Calculates the utility by calculating the average difference of the relative frequencies in each section.
	* Dividing the range into sections, the assumption of standardized data is made.
	* Therefor it compares the original database and the generated synthetic database.
	*
	* @return The value for the utility of the synthetic database concerning the original database
	*/
	public double utility(){
		
		// check if a synthetic database was created
		if(synthDatabase.length > 1){
			
			// calculate for each section the considered range and store it as min and max value in an array
			double[][] range = new double[numSections][2];
			for(int i = 0; i < numSections; i++){
				range[i] = calculateRangeDatabase(i);
			}
		
			// calculate the average margin of responses to a query by the original and the synthetic database
			// a query returns the relative frequency of elements in a considered range
			double sum = 0;
			for(int i = 0; i < numQI; i++){
				for(int j = 0; j < numSections; j++){
					double difference = Math.abs(query(origDatabase, i, j, range) - query(synthDatabase, i, j, range));
					sum += difference;
				}
			}
			utilityValue = (double) sum / (numQI*numSections);
		}
		// if it was not possible to output a synthetic database the default utility value is -1
		else{
			utilityValue = -1;
		}
		// return average difference between the two databases
		return utilityValue;
	}
	
	/**
	* Gets the range of a database. For  each dimension it stores the min and the max value in an array.
	*
	* @param int section The section to calculate the range.
	*/
	public double[] calculateRangeDatabase(int section){
		
		// 2D array with length numQID and for each QID the min and max value
		double[] range = new double[2];
		
		// excluding the two fixed sections ([-5,-3] and [3,5])
		int rest = numSections - 2;
		
		
		// calculation: min + length/numSections in the range * regarded section
		
		// one section is from -5 to -3 fixed
		if(section == 0){
			range[0] = -5;
			range[1] = -3;
		}
		
		// one section is from -5 to -3 fixed
		else if(section == numSections - 1){
			range[0] = 3;
			range[1] = 5;
		}
		
		// in [-3,-2] are two percent of the elements-> 2 percent of the sections are in this range
		else if(1 <= section && section <= Math.ceil(rest * 0.02)){
			range[0] = -3 + 1.0/Math.abs(Math.ceil(rest * 0.02) - 1) * section;
			range[1] = -3 + 1.0/Math.abs(Math.ceil(rest * 0.02) - 1) * (section + 1);
		}
		
		// in [2, 3] are two percent of the elements-> 2 percent of the sections are in this range
		else if(section <= rest && rest - Math.ceil(rest * 0.02) <= section){
			range[0] = 2 + 1.0/Math.abs(Math.ceil(rest * 0.02) - 1) * section;
			range[1] = 2 + 1.0/Math.abs(Math.ceil(rest * 0.02) - 1) * (section + 1);
		}
		
		// in [-2, -1] are 13.5 percent of the elements-> 13.5 percent of the sections are in this range
		else if(section <= Math.ceil(rest * 0.155) && Math.ceil(rest * 0.02) < section){
			range[0] = -2 + 1.0/Math.abs(Math.ceil(rest * 0.02) - Math.ceil(rest * 0.155)) * section;
			range[1] = -2 + 1.0/Math.abs(Math.ceil(rest * 0.02) - Math.ceil(rest * 0.155)) * (section + 1);
		}
		
		// in [1, 2] are 13.5 percent of the elements-> 13.5 percent of the sections are in this range
		else if(section < rest - Math.ceil(rest * 0.02) && rest - Math.ceil(rest * 0.155) <= section){
			range[0] = 1 + 1.0/Math.abs(Math.ceil(rest * 0.02) - Math.ceil(rest * 0.155)) * section;
			range[1] = 1 + 1.0/Math.abs(Math.ceil(rest * 0.02) - Math.ceil(rest * 0.155)) * (section + 1);
		}
		// in [-1, -1] are 68.27 percent so the rest of the sections are in this range
		else{
			range[0] = -1 + 2.0/Math.ceil(rest * 0.62) * section;
			range[1] = -1 + 2.0/Math.ceil(rest * 0.62) * (section + 1);
		}
		
		// return array with min and max values for  a requested section equally for each qid
		return range;
	}
	
	/**
	* Implementation of the queries we are allowing on our given database.
	* Currently: How many elements have a value as a given attribute in a given section.
	*
	* @param database The considered database.
	* @param indexQuery The index of the query out of the query class that is considered.
	* @param range The range of the values for all dimensions
	*
	* @return proportion Proportion of elements with attribute i in this range
	*/
	public double query(double[][] database, int regardedQI, int regardedSection, double[][] range){
		
		// store the begin and the end of the range
		double min = range[regardedSection][0];
		double max = range[regardedSection][1];
		
		// counter for the amount of elements
		int numElements = 0;
		
		// check for each element in the database if it has the value in the regarded QI that is in the regarded section
		for(int k = 0; k < database.length; k++){
			
			// store the current considered entry
			double temp = database[k][regardedQI];
			
			// check if it is the last because then the range is [from, to]
			// otherwise it ist [from, to)
			if(regardedSection == numSections - 1){
				
				//check if the considered value is in the regarded section
				if(temp >= min && temp <= max){
					// increase the amount of elements
					numElements += 1;
				}
			}else{
				
				//check if the considered value is in the regarded section
				if(temp >=  min && temp < max){
					// increase the amount of elements
					numElements += 1;
				}
			}	
		}
		
		// return proportion of elements with value in the given section
		return (double) numElements/database.length;
	}
}

