package de.uni.freiburg.iig.telematik.swatiiplugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReliefAlgorithm {

	@SuppressWarnings("null")
	public static void main(String[] args) throws IOException {

		String line;
		double out[]=new double[9];
		double min;
		ArrayList<String> mini = new ArrayList<String>();
		int obstructedtrace[] = {1,0,0,0,0,1,0,1,0,1,0,0},j=0,i;
		BufferedReader br1 = new BufferedReader(
				new FileReader("C:/Users/telematik/MasterThesis/codes/succesfulltraces.txt"));
		BufferedReader br2 = new BufferedReader(
				new FileReader("C:/Users/telematik/MasterThesis/codes/obstructedtraces.txt"));
		
		while ((line = br1.readLine()) != null)
		{   int weight=0;
		    double weightsqrt=0;
			String[] words = line.split("\\W+");
			for(i=0; i<words.length;i++)
			{  
				weight+=Math.pow(Integer.parseInt(words[i])-obstructedtrace[i],2);
			}
			
			weightsqrt=Math.sqrt(weight);
			//System.out.println(weightsqrt);
			for (int k = 0; k < 9; k++) {
			out[k]=weightsqrt;
	
			System.out.println(out[k]); 
			}		
		}
       /* min=out[0];
        for(i=0;i<out.length;i++)
        {
        	if(out[i]<min)
        		min=out[i];
        }
        System.out.println(min);*/
		  // min=out[0];
       /* for(i=0;i<out.length-1;i++)
        {
        	System.out.println(out[i]);
        }*/
		/*int min = list.get(0);
		int max = list.get(0);

		for(Integer i: list) {
		    if(i < min) min = i;
		    if(i > max) max = i;
		}

		System.out.println("min = " + min);
		System.out.println("max = " + max);*/
	}

}
