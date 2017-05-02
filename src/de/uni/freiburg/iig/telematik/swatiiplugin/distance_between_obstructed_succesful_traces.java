import java.util.ArrayList;

public class distance_between_obstructed_succesful_traces {
	
	public static void main(String[] args) {
		int[][] sources = { { 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1 }, { 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0 },
				{ 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1 }, { 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1 },
				{ 0, 1, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1 }, { 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 1, 0 },
				{ 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0 }, { 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 1, 0 },
				{ 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0 }, { 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0 },
				{ 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0 } };

		int[] target = { 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0 };
		int j=0;
		ArrayList<int[]> list1 = new ArrayList<int[]>();
		for (int i = 0; i < 11; i++)
			list1.add(sources[i]);
        System.out.println("The distances between source and target are as follows\n");
        /*
		 * for(int[]element :list1) { for(int j=0;j<element.length;j++)
		 * System.out.println("\t"+element[j]); System.out.println("\n"); }
		 */
		for (int[] element : list1) {
			calculateDistance(element, sources[j], target);
			j++;
			// System.out.println("element");
		}
		//j++;
	}

	private static void calculateDistance(int[]element,int[]sr, int[] target) {
		int squareddistance=0;
		StringBuilder sb=  new StringBuilder("");
		if(element.length==target.length)
		{
       for(int i=0;i<element.length;i++)
       {
    	  squareddistance+= (int) Math.pow(Math.abs(element[i]-target[i]),2);
       }
       
       for(int k=0;k<sr.length;k++)
       {
    	   sb.append(sr[k]);
       }
   
    	  double distance = Math.sqrt(squareddistance);
    	  System.out.println(""+sb.toString()+"  "+distance);
       }
	
	else
		 System.out.println("Distance can't be computed between source and target");
 }
}


