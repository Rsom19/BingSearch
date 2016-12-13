package summarizer;


import java.io.*;
import java.util.ArrayList;
/*
 * D:\D-Packup\D_packup\Downloads\summarizer>perl summarizer.pl 
 * "{
 * \"texts\":
 * [\"One is that when we are bored or tired, we just don't breathe as deeply as we usually do. As this theory goes, our bodies take in less oxygen because our breathing has slowed. Therefore, yawning helps us bring more oxygen into the blood and move more carbon dioxide out of the blood.\", 
 * \"When you start to yawn, powerful stretching of the jaw increases blood flow in the neck, face, and head. The deep intake of breath during a yawn forces downward flow of spinal fluid and blood from the brain. Cool air breathed into the mouth cools these fluids.\",
 * \"reference book reference book reference book computer science.\"] 
 * , \"question\":  
 * {\"title\": \"Why Do People yawn?\", \"body\": \"\"}
 * ,\"limit\":600}"
 */



public class RunSummarizationPerlScript {
	public static String summarize(ArrayList<String> strList, String question, String body) {
		String s="";
		String SummaryOut="";
		String command="perl summarizer.pl ";
		String argu1="{\"texts\":[\""+strList.get(0).replace("\"", "\\\"")+"\",\""+strList.get(1).replace("\"", "\\\"")+"\",\""+strList.get(2).replace("\"", "\\\"")+"\"]";
		String argu2=",\"question\":{\"title\": \""+question+"\",\"body\":\""+body+"\"}";
		String argu3=",\"limit\":800}";
		String One_ar=" "+ argu1+ " "+ argu2+" "+ argu3;
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
//		String dummy= "perl summarizer.pl \"{\\\"texts\\\":[\\\"One is that when we are bored or tired, we just don't breathe as deeply as we usually do. As this theory goes, our bodies take in less oxygen because our breathing has slowed. Therefore, yawning helps us bring more oxygen into the blood and move more carbon dioxide out of the blood.\\\",\\\"When you start to yawn, powerful stretching of the jaw increases blood flow in the neck, face, and head. The deep intake of breath during a yawn forces downward flow of spinal fluid and blood from the brain. Cool air breathed into the mouth cools these fluids.\\\",\\\"reference book reference book reference book computer science.\\\"] ,\\\"question\\\":{\\\"title\\\": \\\"Why do people yawn?\\\",\\\"body\\\":\\\"\\\"} ,\\\"limit\\\":600}\"";
		long startTime = System.currentTimeMillis();
//		argu1 = argu1.replace(" ", "\t");
		try {
			String[] commands = new String[]{"perl","summarizer.pl",One_ar};
			Process p = Runtime.getRuntime().exec(commands,null);
			System.out.println(command+" "+ argu1+ " "+ argu2+" "+ argu3);
//			System.out.println(dummy);
//			Process p = Runtime.getRuntime().exec(command+" "+ argu1+ " "+ argu2+" "+ argu3);
//			Process p = Runtime.getRuntime().exec(dummy);

			BufferedReader stdInput = new BufferedReader(new  InputStreamReader(p.getInputStream()));
	 
	        BufferedReader stdError = new BufferedReader(new    InputStreamReader(p.getErrorStream()));
	 
	            // read the output from the command
	            System.out.println("Here is the standard output of the command:\n");
	            while ((s = stdInput.readLine()) != null) {
	            	SummaryOut+=s;	                
	            	System.out.println(s);
	            }
	            int summStart=SummaryOut.indexOf("***");
	            if(summStart>0){	    		
	            	SummaryOut=SummaryOut.substring(summStart+3,SummaryOut.length());	
	            			}
	            else SummaryOut=strList.get(0);
	             
	            // read any errors from the attempted command
	            System.out.println("Here is the standard error of the command (if any):\n");
	            while ((s = stdError.readLine()) != null) {
	                System.out.println(s);
	            }
		}
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println(estimatedTime);				

		

		return SummaryOut;
	}
    public static void main(String args[]) {
 
        String s = null;
        ArrayList<String> strList= new ArrayList<String>();
        String q="Why do people yawn?";
        String body="";
//        String scrPath="D:\\D-Packup\\D_packup\\Downloads\\summarizer";
        strList.add("One is that when we are bored or tired, we just don't breathe as deeply as we usually do. As this theory goes, our bodies take in less oxygen because our breathing has slowed. Therefore, yawning helps us bring more oxygen into the blood and move more carbon dioxide out of the blood.");
        strList.add("When you start to yawn, powerful stretching of the jaw increases blood flow in the neck, face, and head. The deep intake of breath during a yawn forces downward flow of spinal fluid and blood from the brain. Cool air breathed into the mouth cools these fluids.");
        strList.add("reference book reference book reference book computer science.");
        summarize(strList,q,body);
    }
}


