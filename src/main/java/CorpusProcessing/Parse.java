package CorpusProcessing;
import javafx.util.Pair;

import java.beans.PropertyEditorSupport;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Parse {

    private static final double Kilo = 1000;
    private static final double Million = 1000000;
    private static final double Trillion = 1000000000;


    /**
     * Mapping between months names and their numbers.
     */
    private static final HashMap<String, String> MonthMap = new HashMap() {{
        put("January","01"); put("JANUARY","01"); put("january","01"); put("Jan","01");
        put("February","02"); put("FEBRUARY","02"); put("february","02"); put("Feb","02");
        put("March","03"); put("MARCH","03"); put("march","03"); put("Mar","03");
        put("April","04"); put("APRIL","04"); put("april","04"); put("Apr","04");
        put("May","05"); put("MAY","05"); put("may","05");
        put("June","06"); put("JUNE","06"); put("june","06");
        put("July","07"); put("JULY","07"); put("july","07");
        put("August","08"); put("AUGUST","08"); put("august","08"); put("Aug","08");
        put("September","09"); put("SEPTEMBER","09"); put("september","09"); put("Sept","09");
        put("October","10"); put("OCTOBER","10"); put("october","10"); put("Oct","10");
        put("November","11"); put("NOVEMBER","11"); put("november","11"); put("Nov","11");
        put("December","12"); put("DECEMBER","12"); put("december","12"); put("Dec","12");
    }};

    public Parse() {

    }

    /**
     * Receives a document and parses it, removes stop words and applies stemmer if directed to.
     * @param document - Document - a document to be parsed
     * @param useStemmer - boolean - indicate whether to use stemmer. if true stemmer is used.
     * @return - ArrayList<String> - all the words from the text of the document after parsing
     */
    public static ArrayList<String> parseDocument(Document document, boolean useStemmer) {
        String [] tokens = document.getText().split(" ");
        ArrayList<String> terms = new ArrayList<>();

        //Start of parsing
        String [] lastProcessed={""}; // array of strings in which the first ia always the latest generated term and the others entries are the next tokens that were processed to generate that term
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Pair<String,Integer> result = new Pair<>("",0);
            /*
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
            }*/

            //Removing empty token
            if(token.isEmpty() || token.matches("\n+") || token.matches("\t+")) {
                continue;
            }
            //Striping irrelevant symbols
            token = Parse.strip(token);
            //Numbers
            if(token.matches(".*\\d.*")){ //Token contains numbers //TODO: Check the regular expression
                //Dollar Detection
                if(token.matches(".*[$mbn].*")){ //checks for $ m b n
                    String firstNextToken = "";
                    if(i<tokens.length-1) {
                        firstNextToken = tokens[i + 1];
                    }
                    result = Parse.generateTokenDollar(token, firstNextToken);
                    terms.add(result.getKey());
                    i=i+result.getValue();

                }
                //Percentage
                else if(token.matches(".*%.*")){
                    terms.add(token);
                }
                //Number/s with -
                else if (token.matches(".*-.*"))
                {
                    terms.add(token);
                    terms.add(token.substring(0,token.indexOf("-")));
                    terms.add(token.substring(token.indexOf("-")+1));

                }
                //Numbers dependent on next token -   nightmare!!!!!!! help me :'(
                else
                {
                    String firstNextToken = "";
                    if(i<tokens.length-1) {
                        firstNextToken = tokens[i + 1];
                    }
                    //Fractions
                    if(token.matches("\\d+/\\d+")){
                        if (firstNextToken.equals("dollars") ||firstNextToken.equals("Dollars")){ //<<<Fraction Dollars>>>
                            terms.add(token+" Dollars");
                            i++;
                        }else{ // <<<Fraction>>>
                            terms.add(token);
                        }
                    }
                    //Percentage
                    else if(firstNextToken.equals("percent") || firstNextToken.equals("percentage"))
                    {
                        token=token+"%";
                        terms.add(token);
                        lastProcessed=new String[3];
                        lastProcessed[0]=token;
                        lastProcessed[1]="percent";
                        lastProcessed[2]="percentage";
                    }
                    //Date
                    else if(MonthMap.containsKey(firstNextToken)) // <<<DD Month>>>
                    {
                        token=MonthMap.get(firstNextToken)+"-"+token;
                        terms.add(token);
                        i++;
                    }
                    //Simple numbers and Prices
                    else if(firstNextToken.equals("thousand") ||firstNextToken.equals("Thousand") ){ // <<<Number Thousand>>>
                        token  = token +"K";
                        terms.add(token);
                        i++;
                    }else if (firstNextToken.equals("dollars") ||firstNextToken.equals("Dollars")){
                        result = Parse.generateTokenPrice(token);
                        terms.add(result.getKey());
                        i=i+result.getValue();
                    }
                    else if (firstNextToken.equals("Trillion") ||firstNextToken.equals("trillion")){ // <<<Price trillion U.S. Dollars>>>
                        token = token + "000000 M Dollars";
                        terms.add(token);
                        i = i+3;
                    }
                    else if(firstNextToken.equals("Million") ||firstNextToken.equals("million") ||firstNextToken.equals("Billion") ||firstNextToken.equals("billion")) {
                        String secondNextToken = "";
                        if(i<tokens.length-2) {
                            secondNextToken = tokens[i + 2];
                        }
                        String thirdNextToken = "";
                        if(i<tokens.length-3) {
                            thirdNextToken = tokens[i + 3];
                        }
                        lastProcessed = Parse.generateTokenLargeNumbers(token,firstNextToken,secondNextToken,thirdNextToken);
                        terms.add(lastProcessed[0]);
                    } else if((firstNextToken.matches("\\d+/\\d+"))){
                        String secondNextToken = "";
                        if(i<tokens.length-2) {
                            secondNextToken = tokens[i + 2];
                        }
                        if(secondNextToken.equals("dollars") ||secondNextToken.equals("Dollars")){ //<<<Price Fraction Dollars>>>
                            terms.add(token+" "+firstNextToken+" Dollars");
                            i = i+2;
                        }else{
                            terms.add(token+" "+firstNextToken); // <<<Number Fraction>>>
                            i++;
                        }
                    }
                    else //<<<Simple Number>>>
                    {
                        while (token.matches(".*,.*")) { //Delete all the ,
                            token = token.substring(0, token.indexOf(',')) + token.substring(token.indexOf(',') + 1);
                        }
                        if(token.matches("\\d+.?\\d*")) {
                            double numberToken = Double.parseDouble(token); //TODO: Write more tests in order of avoiding try\catch
                            if (numberToken >= Kilo && numberToken < Million) //token between Kilo and Million
                            {
                                numberToken = numberToken / Kilo;
                                token = Parse.doubleDecimalFormat(numberToken) + "K";
                                terms.add(token);
                            } else if (numberToken >= Million && numberToken < Trillion) //token between Million to Trillion
                            {
                                numberToken = numberToken / Million;
                                token = Parse.doubleDecimalFormat(numberToken) + "M";
                                terms.add(token);
                            } else if (numberToken >= Trillion) //token up to Trillion
                            {
                                numberToken = numberToken / Trillion;
                                token = Parse.doubleDecimalFormat(numberToken) + "B";
                                terms.add(token);
                            } else //token lower to Kilo
                            {
                                terms.add(token);
                            }
                        }else{
                            terms.add(token);
                        }


                    }
                }
            }
            //DATE
            else if(Parse.MonthMap.containsKey(token))
            {
                String firstNextToken = "";
                if(i<tokens.length-1) {
                    firstNextToken = Parse.strip(tokens[i + 1]);
                }
                result = generateTokenDate(token, firstNextToken);
                terms.add(result.getKey());
                i=i+result.getValue();
            }
            //Between number and number
            else if(token.equals("Between") || token.equals("between"))
            {
                String firstNextToken = "";
                if(i<tokens.length-1) {
                    firstNextToken = tokens[i + 1];
                }
                if(firstNextToken.matches("\\d+"))
                {
                    String secondNextToken = "";
                    if(i<tokens.length-2) {
                        secondNextToken = tokens[i + 2];
                    }
                    if(secondNextToken.equals("and"))
                    {
                        String thirdNextToken = "";
                        if(i<tokens.length-3) {
                            thirdNextToken = tokens[i + 3];
                        }
                        if(thirdNextToken.matches("\\d+"))
                        {
                            token = firstNextToken+"-"+thirdNextToken;
                            terms.add(token);
                            terms.add(firstNextToken);
                            terms.add(thirdNextToken);
                            i=i+3;
                        }
                        else
                        {
                            terms.add(token);
                        }
                    }
                    else
                    {
                        terms.add(token);
                    }
                }
                else
                {
                    terms.add(token);
                }
            }
            //First custom addition <<<Word / word>>>
            else if(token.contains("/")){
                while (token.indexOf('/') > 0){
                    String term = token.substring(0,token.indexOf('/'));
                    token = token.substring(token.indexOf('/')+1);
                    terms.add(term);
                }
                terms.add(token);
            }
            else if (token.matches(".*-.*-.*"))
            {
                terms.add(token);
                terms.add(token.substring(0,token.indexOf("-")));
                terms.add(token.substring(token.indexOf("-")+1,token.lastIndexOf("-")));
                terms.add(token.substring(token.lastIndexOf("-")+1));

            }
            else if (token.matches(".*-.*"))
            {
                terms.add(token);
                terms.add(token.substring(0,token.indexOf("-")));
                terms.add(token.substring(token.indexOf("-")+1));
            }
            else
            {
                terms.add(token);
            }
        }


        //STOPWORDS
        //TODO: Remove stop words

        //STEMMER
        if(useStemmer){
            //TODO:Stemmer
        }


        return terms;
    }

    /**
     * Receives a token that matches a month and formats it.
     * Recognizes the patterns: <<<Month DD>>> <<<Month year>>>
     * @param token - String - month
     * @param firstNextToken - String - the following token
     * @return - Pair<String,Integer> - <token after processing, additionalTokensProcessed>
     */
    private static Pair<String,Integer> generateTokenDate(String token, String firstNextToken) {
        //String[] newTokenWithLastTokenProcessed = {"",""};
        int additionalTokensProcessed = 0;

        if(firstNextToken.matches("\\d+")) //<<<Month DD and Month year>>>
        {
            int firstNextTokenValue = Integer.parseInt(firstNextToken);
            if(firstNextTokenValue <10 && firstNextTokenValue > 0) //single digit days {1-9}
            {
                token= Parse.MonthMap.get(token)+"-0"+firstNextTokenValue;
            }
            else if(firstNextTokenValue <32 && firstNextTokenValue > 9) //double digit days {10-31}
            {
                token=Parse.MonthMap.get(token)+"-"+firstNextTokenValue;
            }
            else // firstNextToken is a year
            {
                token=firstNextToken+"-"+Parse.MonthMap.get(token);
            }
            additionalTokensProcessed++;
        }

        Pair<String,Integer> result = new Pair<>(token,additionalTokensProcessed);
        return result;
    }

    /**
     * Striping irrelevant symbols. Removing all the symbols deemed unimportant for the indexing.
     * @param token - String - a word with irrelevant symbols
     * @return - String -a word without irrelevant symbols
     */
    private static String strip(String token) {
        String result = "";
        char[] charArray = token.toCharArray();
        for (char character: charArray) {
            //Parenthesis
            if(character == ')' || character == '(' || character == '{' || character == '}' || character == '[' || character == ']'){
                continue;
            }
            //Symbols
            else if(character == ':' || character == '"' || character == '*' || character == '#'|| character == '\t'|| character == '\n'){
                continue;
            } else {
                result = result + character;
            }
        }
        //Removing dot in the end of the token
        if(result.indexOf('.')==result.length()-1 || result.indexOf(',')==result.length()-1 || result.indexOf('!')==result.length()-1 || result.indexOf('?')==result.length()-1)
        {
            result=result.substring(0,result.length()-1);
        }
        return result;
    }

    /**
     *
     * @param token
     * @param firstNextToken
     * @return - Pair<String,Integer> - <token after processing, additionalTokensProcessed>
     */
    private static Pair<String,Integer> generateTokenDollar(String token , String firstNextToken) {
        //String[] newTokenWithLastTokenProcessed = {"",""};
        int additionalTokensProcessed = 0;

        if(token.indexOf('$') == 0) { // <<<$price>>>
            token = token.substring(1); //removing the $ sign
            if (firstNextToken.equals("million")){ // <<<$price million>>>
                token = token+ " M Dollars";
                //newTokenWithLastTokenProcessed[1]=firstNextToken;
                additionalTokensProcessed++;
            } else if(firstNextToken.equals("billion")){ // <<<$price billion>>>
                token = token+ "000 M Dollars";
                //newTokenWithLastTokenProcessed[1]=firstNextToken;
                additionalTokensProcessed++;
            }
            else { // <<<$price>>>
                while (token.indexOf(',') >= 0) {
                    token = token.substring(0, token.indexOf(',')) + token.substring(token.indexOf(',')+1);
                }
                double value = Double.parseDouble(token); //TODO: Write more tests in order of avoiding try\catch
                if (value >= Million) {
                    value = value / Million;
                    token = Parse.doubleDecimalFormat(value) + " M Dollars";
                } else {
                    token = Parse.doubleDecimalFormat(value) + " Dollars";
                }
            }
        } else if (token.matches("[mM]")){ // <<<Price'm' Dollars>>>
            if (firstNextToken.equals("Dollars")){
                token = token.substring(0,token.length()-1) + " M Dollars";
                //newTokenWithLastTokenProcessed[1]=firstNextToken;
                additionalTokensProcessed++;
            }
        } else if (token.matches("\\d+bn")){ // <<<Price'bn' Dollars>>>
            if (firstNextToken.equals("Dollars")){
                token = token.substring(0,token.length()-2) + "000 M Dollars";
                //newTokenWithLastTokenProcessed[1]=firstNextToken;
                additionalTokensProcessed++;
            }
        }

        Pair<String,Integer> result = new Pair<>(token,additionalTokensProcessed);
        return result;
    }

    /**
     *
     * @param token
     * @return
     */
    private static Pair<String,Integer> generateTokenPrice(String token){
        // <<<Price Dollars>>>
        int additionalTokensProcessed = 0;
        token.replaceAll(",","");
        /*
        while (token.indexOf(',') >= 0) {
            token = token.substring(0, token.indexOf(',')) + token.substring(token.indexOf(',')+1);
        }
        
        */
        double value = Double.parseDouble(token); //TODO: Write more tests in order of avoiding try\catch
        if (value >= Million) {
            value = value / Million;
            token = Parse.doubleDecimalFormat(value) + " M Dollars";
        } else {
            token = Parse.doubleDecimalFormat(value) + " Dollars";
        }

        additionalTokensProcessed++;

        Pair<String,Integer> result = new Pair<>(token,additionalTokensProcessed);
        return result;
    }


    private static String[] generateTokenLargeNumbers(String token , String firstNextToken, String secondNextToken,String thirdNextToken) {
        String[] newTokenWithLastTokenProcessed = new String[0];
        if(firstNextToken.equals("Million") ||firstNextToken.equals("million")){
            if(secondNextToken.equals("U.S.")) {
                if(thirdNextToken.equals("Dollars") || thirdNextToken.equals("dollars")){ // <<<Price million U.S. dollars>>>
                    token= token + " M Dollars";
                    newTokenWithLastTokenProcessed = new String[]{token,firstNextToken,secondNextToken,thirdNextToken};
                }
            }else{
                token = token+"M";
                newTokenWithLastTokenProcessed = new String[]{token,firstNextToken};
            }
        } else if(firstNextToken.equals("Billion") ||firstNextToken.equals("billion")) {
            if (secondNextToken.equals("U.S.")) {
                if (thirdNextToken.equals("Dollars") || thirdNextToken.equals("dollars")) { // <<<Price million U.S. dollars>>>
                    token = token + "000 M Dollars";
                    newTokenWithLastTokenProcessed = new String[]{token, firstNextToken, secondNextToken, thirdNextToken};
                }
            } else {
                token = token + "B";
                newTokenWithLastTokenProcessed = new String[]{token, firstNextToken};
            }
        }
        return newTokenWithLastTokenProcessed;
    }

    /**
     * https://stackoverflow.com/questions/153724/how-to-round-a-number-to-n-decimal-places-in-java
     * @param number - double
     * @return - a number with three numbers after the point
     */
    private static String doubleDecimalFormat(double number){
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.FLOOR);
        String result = df.format(number);
        return result;
    }
}
