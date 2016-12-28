package Search;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class OnlineSearcher {
	
	public static void main(String args[]){
	  
		BingProgrammaticSearchMultithread bps = new BingProgrammaticSearchMultithread();

	      String searchQuery="";
	      int searchResultsNeeded=5;
	      boolean summarize = false;
	      ArrayList<String> searchResults = new ArrayList<>();
	      String answer="";
	    
		
		for (int i = 0; i < args.length; i++) {
			if ("-query".equals(args[i])) {
				searchQuery = args[i + 1];
				i++;
			}if ("-searchResultsNeeded".equals(args[i])) {
				searchResultsNeeded = Integer.parseInt(args[i + 1]);
				i++;
			}if ("-summarize".equals(args[i])) {
				summarize = Boolean.parseBoolean(args[i + 1]);
				i++;
			} 
		} 
		
		
		
		try{
			
			if(searchQuery.isEmpty()){
				System.out.println("No Query found or query is an empty string!");
			}else{
				
				//System.out.println("Search query : " + searchQuery);
		if(summarize){
			answer = bps.getSummarizedBingResults(searchQuery, searchResultsNeeded);
			System.out.println("Answer : " + answer);
		}else{
			//answer = bps.getTopBingResult(searchQuery);
			 searchResults = bps.getBingResults(searchQuery,searchResultsNeeded);
   	      	 int index = 0;
   	      	 int resultCount = 0;
   	      
			 while(index < searchResults.size()){
			  resultCount++;
			  int endIndex = searchResults.get(index).indexOf("bing...");
   	          System.out.println("Result " + resultCount + ": " +searchResults.get(index).substring(0, endIndex));
   	          index++;
			 }  
			
		}
		
			}
		
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}