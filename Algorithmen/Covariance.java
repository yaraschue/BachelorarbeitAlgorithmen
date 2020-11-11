import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
* Includes methods to calculate the covariance of a given matrix and its means .
*
* @author Yara Schuett
*/
public class Covariance{
	
	private int numQI;
	
	private double[][] matrix;
	
	private double[][] covMatrix;
	
	private double[] attributeMean;
	
	private double[] standardDerivation;
	
	/**
	* Constructor where the matrix, its mean and its standard derivation is set.
	*
	* @param matrix The matrix to calculate the covariance matrix of
	* @param attributeMean Array with the mean of each attribute in the matrix
	* @param standardDerivation Array with the standard derivation of each attribute in the matrix
	*/
	public Covariance(double[][] matrix, double[] attributeMean, double[] standardDerivation){
		this.matrix = matrix;
		this.numQI = matrix[0].length;
		this.attributeMean = attributeMean;
		this.standardDerivation = standardDerivation;
	}
	/**
	* Returns the covariance matrix of the given matrix. The cavariance matrix has the dimensions:
	* size matrix[].length times matrix[].length (dim times dim)
	*
	* @return The cavariance matrix of matrix
	*/
	public double[][] calculateCovMatrix(){
		
		// initialize covariance matrix
		double[][] covMatrix = new double[matrix[0].length][matrix[0].length];
		
		// calculate covariance for each combination of dimensions
		for(int i = 0; i < numQI; i++){
			for(int j = 0; j < numQI; j++){
				
				// calculate the covariance of two dimensions with the following equation (sum over all values (valueDim1-mean)(valueDim2-mean) / (amount values - 1)
				double sum = 0;
				for(int k = 0; k < matrix.length; k++){
					sum += (matrix[k][i] - attributeMean[i]) * (matrix[k][j] - attributeMean[j]);
				}
				covMatrix[i][j] = sum/(matrix.length-1);
				
			}
		}
		return covMatrix;
	}
	
	
}