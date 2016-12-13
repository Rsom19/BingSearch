package Search;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebSearchCommonMethods {

	
	//Returns the number of words in a given text
	public int noOfWords(String text){


		int wordCount = 0;

		Pattern pat = Pattern.compile("\\S+");
		Matcher m = pat.matcher(text);
		while (m.find()) {
			wordCount++;
		}       

		return wordCount;
	}
	
	//Returns the longest string common to two strings.
	public String longestCommonSubstring(String S1, String S2){
		int Start = 0;
		int Max = 0;
		String lcs ="";
		for (int i = 0; i < S1.length(); i++)
		{
			for (int j = 0; j < S2.length(); j++)
			{
				int x = 0;
				while (S1.charAt(i + x) == S2.charAt(j + x))
				{
					x++;
					if (((i + x) >= S1.length()) || ((j + x) >= S2.length())) break;
				}
				if (x > Max)
				{
					Max = x;
					Start = i;
				}
			}
		}
		lcs = S1.substring(Start, (Start + Max));
		return lcs;
	}

	public Document htmlObtainer(String absURL){
		Document innerDocument = null;
		try {
			innerDocument = Jsoup.connect(absURL).ignoreContentType(true).timeout(0).userAgent("Mozilla").get();
		} catch(org.jsoup.HttpStatusException hse){   
			//qaServer.writeToLogFile(hse.getStackTrace());
			return innerDocument;
		}catch(javax.net.ssl.SSLHandshakeException she){ 
			//qaServer.writeToLogFile(she.getStackTrace());
			return innerDocument;
		}catch(java.net.UnknownHostException uhe){ 
			//qaServer.writeToLogFile(uhe.getStackTrace());
			return innerDocument;
		}catch(IOException ioe){ 
			//qaServer.writeToLogFile(ioe.getStackTrace());
			return innerDocument;
		}

		return innerDocument; //This is HTML without the tags

	}

	//To return the smaller integer
	public int minInt (int num1, int num2){

		if(num1 > num2)
			return num2;

		else if (num1 < num2)
			return num1;

		else // They are equal
		return num1;

	}

}
