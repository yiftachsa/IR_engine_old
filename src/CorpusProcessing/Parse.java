package CorpusProcessing;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Parse {

    //private Map<String , String> dictionary;

    /*private enum Month{
        January,
        February,
        March,
        April,
        May,
        June,
        July,
        August,
        September,
        October,
        November,
        December,
        january,
        february,
        march,
        april,
        may,
        june,
        july,
        august,
        september,
        october,
        november,
        december,
        Jan,
        Feb,
        Mar,
        Apr,
        Aug,
        Sept,
        Oct,
        Nov,
        Dec,
        jan,
        feb,
        mar,
        apr,
        aug,
        sept,
        oct,
        nov,
        dec
    }
*/
    private static final HashMap<String, Integer> MonthMap = new HashMap() {{
        put("January",1); put("JANUARY",1); put("january",1); put("Jan",1);
        put("February",2); put("FEBRUARY",2); put("february",2); put("Feb",2);
        put("March",3); put("MARCH",3); put("march",3); put("Mar",3);
        put("April",4); put("APRIL",4); put("april",4); put("Apr",4);
        put("May",5); put("MAY",5); put("may",5);
        put("June",6); put("JUNE",6); put("june",6);
        put("July",7); put("JULY",7); put("july",7);
        put("August",8); put("AUGUST",8); put("august",8); put("Aug",8);
        put("September",9); put("SEPTEMBER",9); put("september",9); put("Sept",9);
        put("October",10); put("OCTOBER",10); put("october",10); put("Oct",10);
        put("November",11); put("NOVEMBER",11); put("november",11); put("Nov",11);
        put("December",12); put("DECEMBER",12); put("december",12); put("Dec",12);
    }};

    public Parse() {

    }

    public static ArrayList<String> parseDocument(Document document) {
        String [] tokens = document.getText().split(" ");
        ArrayList<String> terms = new ArrayList<>();

        //Start of parsing
        String [] lastProcessed={""}; // array of strings in which the first ia always the latest generated term and the others entries are the next tokens that were processed to generate that term
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            boolean alreadyProcessed=false;
            for (int j = 1; j <lastProcessed.length ; j++) {
                if(lastProcessed[j].equals(token))
                {
                    alreadyProcessed=true;
                }
            }
            if(alreadyProcessed)
            {
                continue;
            }
            //Removing empty token
            if(token.isEmpty()) {
                continue;
            }
            //Numbers
            if(token.matches(".*\\d.*")){ //Token contains numbers
                //Dollar Detection
                if(Parse.containsDollar(token)){
                    String nextToken = "";
                    if(i<tokens.length-1) {
                        nextToken = tokens[i + 1];
                    }
                    lastProcessed = Parse.generateTokenDollar(token, nextToken);
                    terms.add(lastProcessed[0]);

                }
                //Percentage
                else if (Parse.containsPercentage(token)){
                    terms.add(token);
                }
                //Number/s with -
                else if (Parse.containsHyphen(token))
                {
                    terms.add(token);
                }
                //Numbers dependent on next token -   nightmare!!!!!!! help me :'(
                else
                {
                    String nextToken = "";
                    if(i<tokens.length-1) {
                        nextToken = tokens[i + 1];
                    }
                    //Percentage
                    if(nextToken.equals("percent") || nextToken.equals("percentage"))
                    {
                        token=token+"%";
                        terms.add(token);
                        lastProcessed=new String[3];
                        lastProcessed[0]=token;
                        lastProcessed[1]="percent";
                        lastProcessed[2]="percentage";
                    }
                    //Date
                    if(MonthMap.containsKey(nextToken))
                    {
                        token="0"+MonthMap.get(nextToken)+"-"+token;
                        terms.add(token);
                        i++;
                    }


                }


            }
        }
        //MyModel.addToDictionary("token","posting file number");
        return terms;
    }

    private static boolean containsHyphen(String token) {
        if(token.indexOf('-')>=0)
        {
            return true;
        }
        return false;
    }

    private static String[] generateTokenDollar(String token , String nextToken) {
        String[] newTokenWithLastTokenProcessed = {"",""};
        String newToken = "";
        if(token.indexOf('$') >= 0) {
            newToken = token.substring(1); //removing the $ sign
            if (nextToken.equals("million")){ // <<<$price million>>>
                newToken = newToken+ " M Dollars";
                newTokenWithLastTokenProcessed[1]=nextToken;
            } else if(nextToken.equals("billion")){ // <<<$price billion>>>
                newToken = newToken+ "000 M Dollars";
                newTokenWithLastTokenProcessed[1]=nextToken;
            }
            else { // <<<$price>>>
                while (newToken.indexOf(',') >= 0) {
                    newToken = newToken.substring(0, newToken.indexOf(',')) + newToken.substring(newToken.indexOf(',')+1);
                }
                int value = Integer.parseInt(newToken);
                if (value >= 1000000) {
                    value = value / 1000000;
                    newToken = value + " M Dollars";
                } else {
                    newToken = value + " Dollars";
                }
            }
        } else if (token.indexOf('m') >= 0){ // <<<Price m Dollars>>>
            if (nextToken.equals("Dollars")){
                newToken = token.substring(0,token.length()-1) + " M Dollars";
                newTokenWithLastTokenProcessed[1]=nextToken;
            }
        } else if (token.indexOf('b') >= 0 && token.indexOf('n') >= 0){ // <<<Price bn Dollars>>>
            if (nextToken.equals("Dollars")){
                newToken = token.substring(0,token.length()-2) + "000 M Dollars";
                newTokenWithLastTokenProcessed[1]=nextToken;
            }
        }
        newTokenWithLastTokenProcessed[0]=newToken;
        return newTokenWithLastTokenProcessed;
    }


    private static boolean containsDollar(String token) {
        if(token.indexOf('$') >= 0 || token.indexOf('m') >= 0 || (token.indexOf('b') >= 0 && token.indexOf('n') >= 0)){
            return true;
        }
        return false;
    }

    private static boolean containsPercentage(String token) {
        if(token.indexOf('%') >= 0){
            return true;
        }
        return false;
    }
}
