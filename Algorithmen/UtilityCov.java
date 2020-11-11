import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/**
* Evaluates a given synthetic database in comparison to the original database concerning 
* the utility by comparing their covariance matrices
*
* @author Yara Schuett
*/
public class UtilityCov extends Utility{
	
	private double[][] origDatabase;
	
	private double[][] synthDatabase;
	
	private int numQI;
	
	private double utilityValue;
	
	private double[] attributeMean;
	
	private double[] standardDerivation;
	
	/**
	* Constructor where the given original and synthetic database are set. Also the amount of QIs is calculated
	* and the arrays for the mean and the standard derivatiion are initialized
	*
	* @param origDatabase The original database for this method.
	* @param synthDatabase The synthetic database for this method.
	*/
	public UtilityCov(double[][] origDatabase, double[][] synthDatabase){
		
		super(origDatabase, synthDatabase);
	
		this.origDatabase = super.origDatabase;
		this.synthDatabase = super.synthDatabase;
		numQI = super.numQI;
		attributeMean = new double[numQI];
		standardDerivation = new double[numQI];
	}
	
	/**
	* Calculates the utility by calculating and comparing the covariance matrices of two datasets
	* Therefor it compares the original database and the generated synthetic database.
	*
	* @return The value for the utility of the synthetic database concerning the original database
	*/
	public double utility(){
		
	// check if a synthetic database was created
	if(synthDatabase.length > 1){
		
		// original database is standardized and we don't have to calculate the mean and the standard derivation
		for(int i = 0; i < numQI; i++){
			attributeMean[i] = 0;
			standardDerivation[i] = 1;
		}
		
		// calculate covariance matrix of the original database
		Covariance origCov = new Covariance(origDatabase, attributeMean, standardDerivation);
		double[][] origCovMatrix = origCov.calculateCovMatrix();
		
		// calculate mean and standard derivation of the dimensions for the synthetic database
		double[] synthAttributeMean = calcAttributeMean(synthDatabase);
		double[] synthStandardDerivation = calcStandardDerivation(synthDatabase, synthAttributeMean);
		
		// calculate covariance matrix of the synthetic database
		Covariance synthCov = new Covariance(synthDatabase, synthAttributeMean, synthStandardDerivation);
		double[][] synthCovMatrix = synthCov.calculateCovMatrix();
		
		// calculate the average difference of the covariance matrices
		double sum = 0;
		for(int i = 0; i < numQI; i++){
			for(int j = 0; j < numQI; j++){
				sum += Math.abs(synthCovMatrix[i][j] - origCovMatrix[i][j]);
			}
		}
		utilityValue = sum/Math.pow(numQI, 2);
	}
	// if it was not possible to output a synthetic database the default utility value is -1
	else{
		utilityValue = -1;
	}
		return utilityValue;
	}
	
	/**
	* Calculates the mean for a given database in every dimension. 
	* Returns an array with all the means.
	*
	* @param database The considered database
	* @return Array with the mean for each dimension of the given database
	*/
	public double[] calcAttributeMean(double[][] database){
		
		// initialize array and temporary variable
		double[] attributeMean = new double[numQI];
		double temp;
		
		// for each dimension calculate mean
		for(int i = 0; i < numQI; i++){
			temp = 0;
			
			// add all values in a considered dimension and set as mean the average of these values
			for(int j = 0; j < database.length; j++){
				temp += database[j][i];
			}
			attributeMean[i] = (double) temp/database.length;
		}
		return attributeMean;
	}
	
	/**
	* Calculates the standard derivation for a given database in every dimension. 
	* Returns an array with all the standard derivations.
	*
	* @param database The considered database
	* @param attributeMean Array with the calculated mean for each dimension
	* @return Array with the standard derivation for each dimension of the given database
	*/
	public double[] calcStandardDerivation(double[][] database, double[] attributeMean){
		
		// initialize array and temporary variable
		double[] standardDerivation = new double[numQI];
		double temp;
		
		// for each dimension calculate standard derivation
		for(int i = 0; i < numQI; i++){
			temp = 0;
			
			// calculate standard derivation by adding all margins between value and the dimensions mean square 2
			for(int j = 0; j < database.length; j++){
				temp += Math.pow((database[j][i] - attributeMean[i]),2);
			}
			
			// for receiving the standard derivation take root of the average of the sum before
			standardDerivation[i] = Math.sqrt(temp / database.length);
		}
		return standardDerivation;
	}
}

