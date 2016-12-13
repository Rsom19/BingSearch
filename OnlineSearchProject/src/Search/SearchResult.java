package Search;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Rahma
 */
public class SearchResult {
    
    

    public enum Type {ANSWER, SEARCHRESULT, SIDEBOX} //data-type to store search result tyoe
    public Type type;
    public String title; //Title of the search result
    public String link; //url of the search result
    public String text; //snippet or answer depending on if the search result is from the search result list or if it is an answer
    public Boolean metadataSnippet; //true if the snippet is from the meta, false otherwise
    public String secondaryText; //inner snippet obtained from the beginning of body tag (in case the snippet is from the meta) or following the snippet part in the HTML
   // public enum Source {GOOGLE, INDEX, BING} //source where the search results were returned from

   
     public SearchResult(){
       
    }
     
    //FIXME: to include source
    public SearchResult(Type type, String title, String text, String link, Boolean metadataSnippet, String secondaryText){
        this.type = type;
        this.title = title;
        this.link = link;
        this.text = text;
        this.metadataSnippet = metadataSnippet;
        this.secondaryText = secondaryText;
        
        
    }

     public Type getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }
 
    public String getText() {
        return text;
    }

     public Boolean getMetadataSnippet() {
        return metadataSnippet;
    }

    public String getSecondaryText() {
        return secondaryText;
    }
    
    public void printSearchResult(SearchResult.Type type, String title, String snippet, String absUrl, Boolean meta, String innerSnippet){
		System.out.println("Type: " + type.name());
		System.out.println("Title: " + title);
		System.out.println("Link: " + absUrl);
		System.out.println("Snippet: " + snippet);
		System.out.println("Snippet from meta: " + meta);
		System.out.println("Inner Snippet: " + innerSnippet);
		System.out.println("\n");
	}
	
}
