import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Random;

/**
* Algorithm to anonymize a given dataset. The anonymization is a generalization and 
* independent of the data itself.
*
* @author Yara Schuett
*/

public class IndependentAnonym{
	//TODO: statics entfernen
	// change the amount of sections
	private int numSections = 16;
	
	private int numQI;
	private ArrayList<double[]> data;
	
	/**
	* Constructor where the data to anonymize is set. Also extracting the number of dimensions for numQI. 
	*
	* @param data The data to anonymize
	*/
	public IndependentAnonym(ArrayList<double[]> data){
		this.numQI = data.get(0).length;
		this.data = data;
	}
	
	/**
	* Method to anonymize the given data by checking for each element and dimension in which section it is.
	* When an elements value in a dimension is in a section it gets is representative. The representative is
	* the average of the section thereby it is data independet.
	*
	* @return The anonymized data with the data independent algorithm
	*/
	public ArrayList<double[]> anonymize(){
		
		ArrayList<double[]> anonymizedData = new ArrayList<double[]>();
		// for each datapoint anonymize independently
		for(int i = 0; i < data.size(); i++){
			
			// array to store the new anonymized value in
			double[] temp = new double[numQI];
			
			// for each QI check in which dimension the value lays
			for(int j = 0; j < numQI; j++){
				for(int k = 0; k < numSections; k++){
					double min = - 5 + 10.0/numSections * k;
					double max = - 5 + 10.0/numSections * (k + 1);
					
					// when the section is found set the value to the mean and stop the search for the current dimension
					if(min <= data.get(i)[j] && data.get(i)[j] < max){
						temp[j] = (min + max)/2;
						break;
					}
				}
				
			}
			// add to anonymized data
			anonymizedData.add(temp);
		}
		return anonymizedData;
	}
	
}