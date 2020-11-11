import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Random;

/**
* Algorithm to anonymize a given dataset. The anonymization is independent of the data itself 
*
* @author Yara Schuett
*/

public class IndependentAnonym2{
	//TODO: statics entfernen
	// erstmal ganz einfach aufteilen
	private static int numSections = 8;
	
	private static int numQI;
	private static ArrayList<double[]> data;
	
	public IndependentAnonym2(ArrayList<double[]> data){
		this.numQI = data.get(0).length;
		this.data = data;
	}
	
	public static ArrayList<double[]> anonymize(){
		ArrayList<double[]> anonymizedData = new ArrayList<double[]>();
		int rest = numSections - 2;
		for(int i = 0; i < data.size(); i++){
			double[] temp = new double[numQI];
			for(int j = 0; j < numQI; j++){
				for(int k = 0; k < numSections; k++){
					double min;
					double max;
					if(k == 0){
						min = -5;
						max = -3;
						if(min < data.get(i)[j] && data.get(i)[j] < max){
							temp[j] = (min + max)/2;
							break;
						}
					}else if(k == numSections - 1){
						min = 3;
						max = 5;
						if(min < data.get(i)[j] && data.get(i)[j] < max){
							temp[j] = (min + max)/2;
							break;
						}
					}else if(1 <= k && k <= Math.ceil(rest * 0.02)){
						min = -3 + 1.0/Math.abs(Math.ceil(rest * 0.02) - 1) * k;
						max = -3 + 1.0/Math.abs(Math.ceil(rest * 0.02) - 1) * (k + 1);
						if(min < data.get(i)[j] && data.get(i)[j] < max){
							temp[j] = (min + max)/2;
							break;
						}
					}else if(k <= rest && rest - Math.ceil(rest * 0.02) <= k){
						min = 2 + 1.0/Math.abs(Math.ceil(rest * 0.02) - 1) * k;
						max = 2 + 1.0/Math.abs(Math.ceil(rest * 0.02) - 1) * (k + 1);
						if(min < data.get(i)[j] && data.get(i)[j] < max){
							temp[j] = (min + max)/2;
							break;
						}
					}else if(k < Math.ceil(rest * 0.155) && Math.ceil(rest * 0.02) <= k){
						min = -2 + 1.0/Math.abs(Math.ceil(rest * 0.02) - Math.ceil(rest * 0.155)) * k;
						max = -2 + 1.0/Math.abs(Math.ceil(rest * 0.02) - Math.ceil(rest * 0.155)) * (k + 1);
						if(min < data.get(i)[j] && data.get(i)[j] < max){
							temp[j] = (min + max)/2;
							break;
						}
					}else if(k <= rest - Math.ceil(rest * 0.02) && rest - Math.ceil(rest * 0.155) < k){
						min = 1 + 1.0/Math.abs(Math.ceil(rest * 0.02) - Math.ceil(rest * 0.155)) * k;
						max = 1 + 1.0/Math.abs(Math.ceil(rest * 0.02) - Math.ceil(rest * 0.155)) * (k + 1);
						if(min < data.get(i)[j] && data.get(i)[j] < max){
							temp[j] = (min + max)/2;
							break;
						}
					}else{
						min = -1 + 2.0/Math.ceil(rest * 0.31) * k;
						max = -1 + 2.0/Math.ceil(rest * 0.31) * (k + 1);
						if(min < data.get(i)[j] && data.get(i)[j] < max){
							temp[j] = (min + max)/2;
							break;
						}
					}
				}
				
			}
			anonymizedData.add(temp);
		}
		return anonymizedData;
	}
	
}