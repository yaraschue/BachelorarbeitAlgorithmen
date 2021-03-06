import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/**
* Evaluates a given synthetic database in comparison to the original database concerning the utility
* by comparing the relative frequency. Therefore the range of the values is partioned in sections.
*
* @author Yara Schuett
*/
public class Utility1 extends Utility{
	
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
	public Utility1(double[][] origDatabase, double[][] synthDatabase){
		
		super(origDatabase, synthDatabase);
		
		this.origDatabase = super.origDatabase;
		this.synthDatabase = super.synthDatabase;
		numQI = super.numQI;
		
		numSections = 8;
		// calculation differs with the chosen allowed queries
		sizeQueryClass = numSections * numQI;

	}
	
	/**
	* Calculates the utility by calculating the average difference of the relative frequencies in each section.
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
		
		// in the range [-5,5] is the probability close to 1 that an element is in this range
		range[0] = - 5 + 10.0/numSections * section;
		range[1] = - 5 + 10.0/numSections * (section + 1);
		
		// return array with min and max values for each qid
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

