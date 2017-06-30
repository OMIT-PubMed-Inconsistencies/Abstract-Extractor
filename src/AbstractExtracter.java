import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Created by Nisansa on 16/05/02.
 */
public class AbstractExtracter {


    ArrayList<String> pubMedIds=new ArrayList<String>();


    public static void main(String[] args) {
        AbstractExtracter ax=new AbstractExtracter();
    }

    public AbstractExtracter() {
        readList();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<pubMedIds.size();i++) {
            String pubMedid=pubMedIds.get(i);
            System.out.println((i+1)+" out of "+pubMedIds.size()+ " is "+  pubMedid);
            createXML(pubMedid);
            sb.append("../output/01_Abstracts/"+pubMedid);
            if(i!=pubMedIds.size()-1){
                sb.append("\n");
            }
        }
        writeToFile("../","pubmed-list_path.txt",sb.toString());
    }

    private void writeToFile(String path,String pubMedid,String content){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path+pubMedid, "UTF-8");
            writer.println(content);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private String createXML(String pubMedid) {
        try {
            String inputLine;

            URL u = new URL("https://www.ncbi.nlm.nih.gov/pubmed/"+pubMedid+"?report=xml&format=text");
            BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));
            String summary="\n<DOI>";
            boolean collect=false;
            int itemIndex=0; //0 = DOI, 1=title, 2=Authors, 3=Author info, 4=abstract, 5=pubmed details
            StringBuilder sb=new StringBuilder();
            boolean appending=false;

            while ((inputLine = in.readLine()) != null) {
               // System.out.println(inputLine);
                if(inputLine.contains("<pre>")) {
                    collect=true;
                }
                else if (inputLine.contains("</pre>")) {
                    break;
                }
                else{
                    if(collect){

                        inputLine=StringEscapeUtils.unescapeXml(inputLine);

                    //    System.out.println("Lo");
                        if(inputLine.contains("<AbstractText")){
                            if(inputLine.contains("<AbstractText>")) {
                                inputLine = inputLine.substring(inputLine.indexOf(">") + 1, inputLine.lastIndexOf("<"));
                                writeToFile("../output/01_Abstracts/", pubMedid, inputLine);
                            }
                            else{ //Abstract is not free text
                                appending=true;
                                inputLine = inputLine.substring(inputLine.indexOf(">") + 1, inputLine.lastIndexOf("<"));
                                sb.append(inputLine);
                                sb.append(" ");
                            }
                        }
                        else if(appending && inputLine.contains("</Abstract>")){ //Finished appending. Now write to file
                            writeToFile("../output/01_Abstracts/", pubMedid, sb.toString());
                            sb=new StringBuilder();
                            appending=false;
                        }

                        /*
                        //System.out.println(inputLine);
                        if(inputLine.isEmpty()){
                            if(itemIndex==0){
                                summary=summary+"</DOI>\n<TITLE>" ;
                            }
                            else if (itemIndex==1){
                                summary=summary+"</TITLE>\n<AUTHORS>" ;
                            }
                            else if (itemIndex==2){
                                summary=summary+"</AUTHORS>\n<AUTHOR_INFO>" ;
                            }
                            else if (itemIndex==3){
                                summary=summary+"</AUTHOR_INFO>\n<ABSTRACT>" ;
                            }
                            else if (itemIndex==4){
                                summary=summary+"</ABSTRACT>\n<PUBMED>" ;
                            }
                            else{
                                summary=summary+"\n" ;
                            }
                           // System.out.println(inputLine.length());
                            itemIndex++;
                        }
                        summary=summary+inputLine;*/

                    }
                }
            }
            summary="\n<Summary>"+summary+"</PUBMED></Summary>";
            //System.out.println(summary);
            //System.in.read();
            return summary;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void readList(){
        FileReader fileReader = null;
        try {
            fileReader = new FileReader("../pubmed-list.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line=null;
            while((line = bufferedReader.readLine()) != null) {
                //  System.out.println(line);
                pubMedIds.add(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
