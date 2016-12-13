package Search;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import it.sauronsoftware.base64.Base64;
import summarizer.RunSummarizationPerlScript;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import org.apache.commons.codec.binary.Base64;



import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 *
 * @author Rahma
 */
public class BingProgrammaticSearchMultithread {
  
	
	public static boolean metadataSnippet;
	
	static WebSearchCommonMethods wscm;
	static SearchResult searchResult;
	
	

	public BingProgrammaticSearchMultithread() {
		wscm = new WebSearchCommonMethods();
		searchResult = new SearchResult();
		

	}
   

	//Returns 5 search results of the the type ArrayList<String>
    public ArrayList<String> getBingAnswers(String searchQuery){
    	
    	int searchResultsNeeded=5;
      
          ArrayList<String> searchResults = new ArrayList<>(); //variable where the search results are stored
          searchResults = getBingResults(searchQuery, searchResultsNeeded);
        return searchResults;
    }  
    
   
    public ArrayList<SearchResult> innerSnippetThread(ArrayList<SearchResult> searchResults){
    	
    	ExecutorService executorService = Executors.newFixedThreadPool(searchResults.size());
    	Future<ArrayList<SearchResult>> result =null;
    	
    	//Creating and calling the threads to do their tasks
    	for(int counter=0; counter<searchResults.size();counter++){
    		result= executorService.submit(new innerSnippetCapture(counter,searchResults));
    	}
        
    	//Getting the results for all the threads
    	for(int counter=0; counter<searchResults.size();counter++){
        try {
        	   result.get();
        } catch (Exception e) {
            // interrupts if there is any possible error
            result.cancel(true);
        }
    	}
        executorService.shutdown();
        try {
			executorService.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return searchResults;
    }
   
    //Returns the requested number of search results of the the type ArrayList<SearchResults>   
    public ArrayList<SearchResult> getBingAnswers(String searchQuery, int searchResultsNeeded){
    	String description;
        String link;
     
        String title;
  
         
        SearchResult sr = new SearchResult();
        SearchResult searchResult;
        ArrayList<SearchResult> searchResults = new ArrayList<>(); //variable where the search results are stored
       
        
        String secondaryText ="";
        
        
        try{
        
        	final String accountKey = "pjwMZzZK6io/LTCJO27Fgq5byS9ydfKpbMgaPxhpujk";
            //final String bingUrlPattern = "https://api.datamarket.azure.com/Bing/Search/Web?Query=%%27%s%%27&$format=JSON"; //For any type of results
        	
        	final String bingUrlPattern = "https://api.datamarket.azure.com/Bing/SearchWeb/Web?Query=%%27%s%%27&$format=JSON"; //For only web results
        	
          final String query = URLEncoder.encode(searchQuery , Charset.defaultCharset().name());
          final String bingUrl = String.format(bingUrlPattern, query);

          
          final byte[] accountKeyByt = Base64.encode((accountKey + ":" + accountKey).getBytes());
          final String accountKeyEnc = new String(accountKeyByt);
          
          final URL url = new URL(bingUrl);
          final URLConnection connection = url.openConnection();
          connection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

          try (final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
              String inputLine;
              final StringBuilder response = new StringBuilder();
              while ((inputLine = in.readLine()) != null) {
                  response.append(inputLine);
              }
              final JSONObject json = new JSONObject(response.toString());
              final JSONObject d = json.getJSONObject("d");
              final JSONArray results = d.getJSONArray("results");
              
              for (int i = 0; i < searchResultsNeeded; i++) {
                  
                  final JSONObject aResult = results.getJSONObject(i);
                  
                  title = aResult.get("Title").toString();
                  description = aResult.get("Description").toString();
                  link = aResult.get("Url").toString();
                  
                  searchResult = new SearchResult(sr.type.SEARCHRESULT,title, description, link, metadataSnippet, secondaryText);
                  searchResults.add(searchResult);
                  
                  
              }
              
              searchResults = innerSnippetThread(searchResults);
              
            
          }     
               
    }catch(org.json.JSONException jse){
    	//qaServer.writeToLogFile(jse.getStackTrace());
  	  return searchResults;
    }catch(Exception e){
    	//qaServer.writeToLogFile(e.getStackTrace());
  	  return searchResults;
  }  
          return searchResults;

    }
    
    public ArrayList<String> getBingResults(String searchQuery, int searchResultsNeeded){

        ArrayList<SearchResult> searchResults = new ArrayList<>(); //variable where the search results are stored
        ArrayList<String> searchResultsStringList = new ArrayList<>(); //variable where the search results are stored
        
        searchResults = getBingAnswers(searchQuery, searchResultsNeeded);
        
              for(int i=0;i<searchResults.size();i++){    
                	 if(!searchResults.get(i).getSecondaryText().isEmpty()&&!searchResults.get(i).getSecondaryText().equals("<snippet not found in HTML>"))
                		 searchResultsStringList.add(searchResults.get(i).getSecondaryText() + " \t" + "bing..." + searchResults.get(i).getLink());
                	 else
                		 searchResultsStringList.add(searchResults.get(i).getText() + " \t" + "bing..." + searchResults.get(i).getLink());
                   
             }
              
          return searchResultsStringList;
    }
    
    public String getTopBingResult(String searchQuery){

    	ArrayList<String> searchResultsStringList = getBingResults(searchQuery,1);
              int endIndex = searchResultsStringList.get(0).indexOf("bing...");
          return searchResultsStringList.get(0).substring(0, endIndex);
    }
    
    
    public String getSummarizedBingResults(String searchQuery, int searchResultsNeeded){

        ArrayList<SearchResult> searchResults = new ArrayList<>(); //variable where the search results are stored
        ArrayList<String> searchResultsStringList = new ArrayList<>(); //variable where the search results are stored
        
        searchResults = getBingAnswers(searchQuery, searchResultsNeeded);
        
              for(int i=0;i<searchResults.size();i++){    
                	 if(!searchResults.get(i).getSecondaryText().isEmpty()&&!searchResults.get(i).getSecondaryText().equals("<snippet not found in HTML>"))
                		 searchResultsStringList.add(searchResults.get(i).getSecondaryText());
                	 else
                		 searchResultsStringList.add(searchResults.get(i).getText());
                   
             }
             
            String summarizedAns =  RunSummarizationPerlScript.summarize(searchResultsStringList,searchQuery,"");
          return summarizedAns;
    }
    public static void main(final String[] args) throws Exception {
    	
		
		BingProgrammaticSearchMultithread bps = new BingProgrammaticSearchMultithread();


    	      ArrayList<String> sr = new ArrayList<>();
    	      ArrayList<SearchResult> searchResults = new ArrayList<>();
    	      
    	      String searchQuery = "أين هو كأس العالم المقبلة؟";
    	      int searchResultsNeeded=10;
    	      int counter = 0;
    	      
    	      System.out.println("concurrently");
    	   
    	    //for results in the structure of SearchResult data-type
    	      counter = 0;
    	      searchResults = bps.getBingAnswers(searchQuery, searchResultsNeeded);
    	      while(counter < searchResults.size()){
    	    	  searchResult.printSearchResult(searchResults.get(counter).type,searchResults.get(counter).getTitle(), searchResults.get(counter).getText(), searchResults.get(counter).getLink(), searchResults.get(counter).getMetadataSnippet(),searchResults.get(counter).getSecondaryText());
    	          counter++;
    	      }
    	      
    	     
	          
    	      counter = 0;
    	      sr = bps.getBingResults(searchQuery,5);
    	      while(counter < sr.size()){
    	          System.out.println("Result " + counter + ": " +sr.get(counter));
    	          counter++;
    	      }    
    	            
    	      
    	    }
  
    public static String completeString(String text){
	   	 
        Pattern p = Pattern.compile("(.*)[\\.\\!\\?]"); //can't include :) (: or similar cause it might be a start of a bracket
        Matcher m = p.matcher(text);
         
        while (m.find()) {
         	text = m.group(0);
          }
    	return text;
    }

	 public static String completeString(String text, int limit){
		//answer = (answer.length() > 1000) ? answer.substring(0, 999) : answer;	
		 text = (text.length() > limit) ? text.substring(0, limit-1) : text;
	        Pattern p = Pattern.compile("(.*)[\\.\\!\\?]"); //can't include :) (: or similar cause it might be a start of a bracket
	        Matcher m = p.matcher(text);
	         
	        while (m.find()) {
	         	text = m.group(0);
	          }
	    	return text;
	    }
	 
	
    public static class innerSnippetCapture implements Callable<ArrayList<SearchResult>> {
		int iterator;
		ArrayList<SearchResult> searchResultArray;

        
        public innerSnippetCapture(int iterator, ArrayList<SearchResult> searchResultArray) {
            
            this.iterator =  iterator;
    		this.searchResultArray =searchResultArray;
        }
       
        
        @Override
        public ArrayList<SearchResult> call() throws Exception {
            	int innerSnippetSize = 1000;
                Document innerDocument;
                String common,  taglessInnerDoc, afterSnippet;
                int snippetStart, snippetEnd, bodyLength,  innerLength;
                String secondaryText ="";
                String metaContent="";
                
                String snippet = searchResultArray.get(iterator).getText();
                String absUrl = searchResultArray.get(iterator).getLink();
                try{
                  
                	if(!snippet.isEmpty()&& !absUrl.isEmpty()){
                		
                          
                                innerDocument =  Jsoup.connect(absUrl).ignoreContentType(true).timeout(0).userAgent("Mozilla").get();
                                
                                taglessInnerDoc = innerDocument.text(); //This HTML without the tags
                                
                                common = wscm.longestCommonSubstring(taglessInnerDoc, snippet);
                                
                                //check if there is no common
                                if(common.isEmpty())
                                	return searchResultArray;
                                
                               // String metaContent = innerDocument.select("meta[name=description]").first().attr("content"); 
                                Boolean metaCheck = innerDocument.select("meta[name=description]").hasAttr("content");
                                
                                if(metaCheck){
                                	metaContent = innerDocument.select("meta[name=description]").first().attr("content"); 
                                }
                                
                                else
                                	metaContent="";
                                
                                String bodyContent="";
                                String elementText ="";
                                
                                
                                Elements bodyElements = innerDocument.body().children(); //returns all the first level tags within the body element
                               
                                //remove tags that have content less than 4 words
                            	for (Element element : bodyElements) {
                            		elementText = element.text();
                            		if(wscm.noOfWords(elementText)>=4)
                            				bodyContent= bodyContent + " "+elementText;
                            		
                            	}
                            	
                            	
                               
                                //if snippet contains any time or date format then we remove it. The format of the date is JUN 14, 2015 or 6 days ago
                                String[] splitSec = snippet.split("(?:[.]{3})");
                                String dateRegex1 = "(?:[a-zA-Z]{3}[ ][\\d]{1,2}[\\,][ ][\\d]{4}[ ][.]{3}[ ])";
                            	String dateRegex2 = "(?:[\\d]{1,2}[ ][a-zA-Z]{3,6}[ ][a-zA-Z]{3,6}[ ][.]{3}[ ])";
                            	Pattern p = Pattern.compile(dateRegex1);
                            	Matcher m = p.matcher(snippet);
                            	Matcher m1 = Pattern.compile(dateRegex2).matcher(snippet);
                            	if(m.find())
                            		snippet = snippet.replace(m.group(), "");
                            	else if (m1.find())
                            		snippet = snippet.replace(m1.group(), "");
                            	
                            	
                            	
                            	if(wscm.longestCommonSubstring(bodyContent,snippet).length()>=0.4*snippet.length()){	
                                	//Snippet is not from meta, so we look for the snippet in the HTML text and return succeeding characters
                                	
                                	//Getting first part of text from the snippet if it contains '...'
                            		if(snippet.contains("...")){
                                    	splitSec = snippet.split("(?:[.]{3})");
                                    		snippet = splitSec[0];
                                    		if(snippet.isEmpty())
                                    			snippet = splitSec[1];		
                                    }
                            		
                            		
                                    snippetStart = bodyContent.indexOf(common); //finding the start of the snippet in the HTML. common is used as opposed to snippet because the complete snippet might not be in the HTML 
                                    snippetEnd = snippetStart + snippet.length(); //finding the end of the snippet in the HTML = start of innerSnippet
                                    
                                    
                                    bodyLength = bodyContent.length(); //size of the body tag
                                    
                                    innerLength = bodyLength - snippetEnd; //size of the contents that come after the snippet in the body tag
                                    
                                    
                                    if(wscm.minInt(innerLength,innerSnippetSize)==innerSnippetSize)
                                         afterSnippet = bodyContent.substring(snippetEnd, snippetEnd + innerSnippetSize);
                                    else
                                        afterSnippet = bodyContent.substring(snippetEnd, bodyLength);
                                   
                                   
                                  //If the afterSnippet ends half way through a paragraph, go back to the last complete sentence 
                                   afterSnippet = completeString(afterSnippet,innerSnippetSize);
                                  
                                   //To remove any incomplete sentences at the beginning of the afterSnippet
                                   
                                 //Method one
               					 /*
               					int pos = -2;

               					//System.out.println("pos is " + pos);
               					if(afterSnippet.indexOf('.')!=-1){
               						pos = afterSnippet.indexOf('.');
               					}
               					else if(afterSnippet.indexOf('!')!=-1){
               						if(afterSnippet.indexOf('!')<pos)
               							pos = afterSnippet.indexOf('!');
               					}
               					else if(afterSnippet.indexOf('?')!=-1){
               						if(afterSnippet.indexOf('?')<pos)
               							pos = afterSnippet.indexOf('?');
               					}
               					else
               						pos=-1;
               					
               					afterSnippet = afterSnippet.substring(pos+1,afterSnippet.length()-1);

               					
               					//Sometimes the sentences start with closing qoutes. So wanted to do the below step but decided against it since it could be an opening one too
               					//if(afterSnippet.charAt(0)=='\''||afterSnippet.charAt(0)=='’'||afterSnippet.charAt(0)=='”'||afterSnippet.charAt(0)=='"')

               					if(afterSnippet.charAt(0)=='’'||afterSnippet.charAt(0)=='”')
               						afterSnippet = afterSnippet.substring(1,afterSnippet.length()-1);
               					
               					 */
                                
               					//Method two
               					// /*
               					afterSnippet = afterSnippet.replaceFirst("[A-Za-z,;'’“”\"\\s\\-\\)\\(]*[\\.\\!\\?]{1}[”]*", ""); 
               					//*/
               					
               					
               					//Method three
               					// /*
               					  afterSnippet = afterSnippet.replaceFirst("(.*?[\\.\\!\\?]{1}[\\s]{1})", "");
               					// */
                                    metadataSnippet = false;
                                    secondaryText = afterSnippet;
                                    
                            	}else if(wscm.longestCommonSubstring(metaContent,snippet).length()>=0.4*snippet.length()){	
                                	metadataSnippet = true;
                                    
                                	//Here the snippet is from the meta, so the innerSnippet will be the taken from <body>
                                	
                                	
                                    if(bodyContent.length()>=innerSnippetSize)
                                    	afterSnippet = bodyContent.substring(0, innerSnippetSize-1);
                                    else
                                    	afterSnippet = bodyContent.substring(0);
                                    
                                    
                                  //If the substring ends half way through a paragraph, go back to the last complete sentence 
                                    afterSnippet = completeString(afterSnippet,innerSnippetSize);
                                    
                                    //To remove any incomplete sentences at the beginning of the afterSnippet
                                    
                                    //Method one
                  					 /*
                  					int pos = -2;

                  					//System.out.println("pos is " + pos);
                  					if(afterSnippet.indexOf('.')!=-1){
                  						pos = afterSnippet.indexOf('.');
                  					}
                  					else if(afterSnippet.indexOf('!')!=-1){
                  						if(afterSnippet.indexOf('!')<pos)
                  							pos = afterSnippet.indexOf('!');
                  					}
                  					else if(afterSnippet.indexOf('?')!=-1){
                  						if(afterSnippet.indexOf('?')<pos)
                  							pos = afterSnippet.indexOf('?');
                  					}
                  					else
                  						pos=-1;
                  					
                  					afterSnippet = afterSnippet.substring(pos+1,afterSnippet.length()-1);

                  					
                  					//Sometimes the sentences start with closing qoutes. So wanted to do the below step but decided against it since it could be an opening one too
                  					//if(afterSnippet.charAt(0)=='\''||afterSnippet.charAt(0)=='’'||afterSnippet.charAt(0)=='”'||afterSnippet.charAt(0)=='"')

                  					if(afterSnippet.charAt(0)=='’'||afterSnippet.charAt(0)=='”')
                  						afterSnippet = afterSnippet.substring(1,afterSnippet.length()-1);
                  					
                  					 */
                                    
                  					//Method two
                  					// /*
                  					
                  					afterSnippet = afterSnippet.replaceFirst("[A-Za-z,;'’“”\"\\s\\-\\)\\(]*[\\.\\!\\?]{1}[”]*", ""); 
                  					//*/
                  					
                  					
                  					//Method three
                  					// /*
                  					  afterSnippet = afterSnippet.replaceFirst("(.*?[\\.\\!\\?]{1}[\\s]{1})", "");
                  					// */
                                    
                  					
                  					secondaryText = afterSnippet;
                                    
                                }else{
                                    //common or snippet not in HTML text
                                    metadataSnippet = false;
                                    secondaryText ="<snippet not found in HTML>";    
                                }
                      }
                	
                  
                 }catch(org.jsoup.HttpStatusException hse){  
                	 //qaServer.writeToLogFile(hse.getStackTrace());
                			return searchResultArray;
                }catch(javax.net.ssl.SSLHandshakeException she){  
                	//qaServer.writeToLogFile(she.getStackTrace());
                			return searchResultArray;
                }catch(java.net.UnknownHostException uhe){   
                	//qaServer.writeToLogFile(uhe.getStackTrace());
                			return searchResultArray;
                }catch(Exception e){
                	//qaServer.writeToLogFile(e.getStackTrace());
                			return searchResultArray;
                }
                
                 searchResultArray.get(iterator).secondaryText = secondaryText;
                 
                 return searchResultArray;
            
        }
    }
}

