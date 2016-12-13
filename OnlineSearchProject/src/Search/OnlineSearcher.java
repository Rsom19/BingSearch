package Search;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class OnlineSearcher {
	
	public static void main(String args[]){
	  
		BingProgrammaticSearchMultithread bps = new BingProgrammaticSearchMultithread();

	      String searchQuery=null;
	      int searchResultsNeeded=10;
	     
	    boolean summarize = true;
	    String answer="";
	    
		
		for (int i = 0; i < args.length; i++) {
			if ("-summarize".equals(args[i])) {
				summarize = Boolean.parseBoolean(args[i + 1]);
				i++;
			} else if ("-searchResultsNeeded".equals(args[i])) {
				searchResultsNeeded = Integer.parseInt(args[i + 1]);
				i++;
			}
		} 
		
		
		try{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		
		while (true) {
			
			if (searchQuery == null) { // prompt the user
				System.out.println("Enter your question: ");
			}

			String line = searchQuery != null ? searchQuery : in.readLine();
			//String line = "أين هو كأس العالم المقبلة؟";
			
			if (line == null || line.length() == -1) {
				break;
			}

			line = line.trim();
			if (line.length() == 0) {
				break;
			}
			
		if(summarize)
			answer = bps.getSummarizedBingResults(line, searchResultsNeeded);
		else
			answer = bps.getTopBingResult(line);
		
		System.out.println("Answer : " + answer);
		if (searchQuery != null) {
			break;
		}
		}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}