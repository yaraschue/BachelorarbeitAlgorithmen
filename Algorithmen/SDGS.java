import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Comparator;
import java.io.IOException;

/**
* Implementing the algorithm (k, beta)-SDGS presented in the paper 
* "Provably Private Data Anonymization: Or, k-Anonymity Meets Differential Privacy " 
* published by Ninghui Li, Wahbeh Qardaji, Dong Su in 2010. Like the name is suggesting 
* it is a combination of (epsilon, delta)-dp and k-anonymity.
*
* @author Yara Schuett
*/

public class SDGS{
	
	// probability beta each tuple of the dataset is chosen with
	private double beta;
	
	// parameter k for the k-anonymity part
	private int k;
	
	private int numQI;
	
	private ArrayList<double[]> suppressedAnonymizedData;
	
	private Mondrian mondrian ;
	
	private int size;
	
	private String path;
	
	
	/**
	* Constructor where all variable parameters for the algorithms are set. 
	* Also the path where the synthetic dabase will be stored is set.
	* 
	* @param beta The probability beta each tuple of the dataset is chosen with
	* @param k The parameter k for the k-anonymity part
	* @param numQI The amount of dimensions of the original database
	*/
	public SDGS(double beta, int k, int numQI){
		this.beta = beta;
		this.k = k;
		this.numQI = numQI;
		path = "../SyntheticData/SDGS/SynthSDGS.csv";
	}
	
	/**
	* Method that implements the (k,beta)-SDGS with an independent generalization as second step.
	* First step is drawing with probability beta and third step is eliminating clusters with less than k elements
	*
	* @param data The original database to anonymize
	*/
	public double[][] algorithmSDGSIndependent(double[][] data){
		
		// first step: sampling from the input dataset with probability beta
		
		ArrayList<double[]> chosenTuples = new ArrayList<double[]>();
		
		// comparing with a random value between 0 and 1 to select with probability beta
		Random random = new Random();
		for(int i = 0; i < data.length; i++){
			// <= to select all tuples in the case beta is 1, when probability 0?
			if(random.nextDouble() <= beta){
				chosenTuples.add(data[i]);
			}	
		}
		
		// Second step: applying a data-independent procedure to each tuple
		
		IndependentAnonym anonym = new IndependentAnonym(chosenTuples);
		
		// get the anonymized data and store it for the next step
		ArrayList<double[]> anonymizedData = anonym.anonymize();
		
		// third step: suppressing any tuple that appears less than k times
		
		suppressedAnonymizedData = new ArrayList<double[]>();
		
		// recursively sort all elements in all dimensions
		sortArrayList(anonymizedData, 0);
		
		double[][] convData;
		
		// check if after the last step there are still elements for the output
		if(suppressedAnonymizedData.size() != 0){
			convData = toArray(suppressedAnonymizedData);
			writeData(convData);
			size = suppressedAnonymizedData.size();
		}else{
			convData = new double[1][numQI];
			writeData(convData);
			size = 1;
		}
		return convData;
	}
	
	/**
	* Sorts a given arrayList recursively by sorting it in every step in a new dimension.
	* Therefor the current considered dimension is given. 
	* For all elements with the same value in this dimension call the method again.
	* When it is sorted in all dimensions and more than k elements are leftover make a cluster with these elements.
	*
	* @param data The data that needs to be sorted
	* @param dimension The dimension to sort in
	*/
	public void sortArrayList(ArrayList<double[]> data, int dimension){
		
		// check if there are elements left to sort
		if(dimension != numQI - 1){
			
			// check if it was already sorted in every dimension
			if(data.size() != 0){
			
			// sort the elements recording to their value in the considered dimension
			Collections.sort(data, new Comparator<double[]>() {
				@Override
				public int compare(double[] first, double[] second) {
					return Double.compare(first[dimension], second[dimension]);
				}
			});
			
			// temporary list to store elements in with the same value for the considered dimension
			ArrayList<double[]> temp = new ArrayList<double[]>();
			
			// add the first element and go through the rest of the given data
			temp.add(data.get(0));
			for(int i = 1; i < data.size(); i++){
				
				// when the element has the same value in the considered dimension add it to temporary and check the next element
				if(Arrays.equals(data.get(i), data.get(i-1))){
					temp.add(data.get(i));
				}
				
				// if the next value differs from before call the method for all elements in temp restart the temp list
				else{
					sortArrayList(temp, dimension + 1);
					temp.clear();
				}
			}
			
			// for the last elements also call the method
			sortArrayList(temp, dimension + 1);
			
			}
		}
		
		// when the list is already sorted for each dimension check if the amount of the leftover elements is greanter than k
		else{
			if(data.size() >= k){
				suppressedAnonymizedData.addAll(data);
			}
		}
	}
	
	/**
	* Method that implements the (k,beta)-SDGS with Mondrian as second step.
	* First step is drawing with probability beta and third step is eliminating clusters with less than k elements
	*
	* @param data The original database to anonymize
	*/
	public double[][] algorithmSDGS(double[][] data){
		
		// first step: sampling from the input dataset with probability beta
		
		ArrayList<double[]> chosenTuples = new ArrayList<double[]>();
		
		// comparing with a random value between 0 and 1 to select  with probability beta
		Random random = new Random();
		for(int i = 0; i < data.length; i++){
			// <= to select all tuples in the case beta is 1, when probability 0?
			if(random.nextDouble() <= beta){
				chosenTuples.add(data[i]);
			}	
		}
		// second step: applying a data-independent procedure to each tuple
		// currently: mondrian with returning the median of a cluster
		mondrian = new Mondrian(data[0].length, k);
		
		mondrian.mondrian(chosenTuples);
		
		// get the anonymized data and store it for the next step
		ArrayList<double[]> anonymizedData = mondrian.getAnonymizedPartition();
		
		// third step: suppressing any tuple that appears less than k times
		// list to store the elements to suppress in
		ArrayList<double[]> elementsToRemove = new ArrayList<double[]>();
		
		// check for all elements if they appear more than k times and if they are already stored to delete
		for(int i = 0; i < mondrian.getClusterSet().size(); i++){
			if(mondrian.getClusterSet().get(i).size() < k){
				elementsToRemove.add(mondrian.getClusterSet().get(i).get(0));
			}
		}
		
		// check if after the last step there are still elements for the output
		size = anonymizedData.size();
		double[][] convData;
		if(size != 0){
			convData = toArray(anonymizedData);
			writeData(convData);
		}else{
			convData = new double[1][numQI];
			writeData(convData);
			size = 1;
		}
		return convData;
	}
	
	public double[][] toArray(ArrayList<double[]> list){
		// convert ArrayList back to array of arrays
		double[][] newData = new double[list.size()][list.get(0).length];
		
		for(int i = 0; i < list.size(); i++){
			for(int j = 0; j < list.get(0).length; j++){
				newData[i][j] = list.get(i)[j];
			}
		}
		return newData;
	}
	
	/**
	* Write the information of an array into a given file.
	*
	* @param data The data to write into the file
	*/
	private void writeData(double[][] data){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
			
			for (int k = 0; k < data.length; k++) {
				
				for (int j = 0; j < numQI; j++) {
					if (j != numQI - 1) {
						bw.write(Double.toString(data[k][j]) + ",");
					} else {
						bw.write(Double.toString(data[k][j]) + "\n");
					}
				}
			}
			bw.close();
		} catch (IOException e) {
			System.out.println("Oooops!");
		}
	}
	
	public int getSize(){
		return size;
	}
	
}