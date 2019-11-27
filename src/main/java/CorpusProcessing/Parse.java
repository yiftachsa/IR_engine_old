package CorpusProcessing;
import javafx.util.Pair;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class Parse {

    private static final double Kilo = 1000;
    private static final double Million = 1000000;
    private static final double Billion = 1000000000;


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
    private static HashSet<String> stopwords;

    public Parse() {

    }

    /**
     * Receives Query  , splits it into tokens and parses it.
     * Return ArrayList of Strings after parse
     * @param query - String - user Query.
     * @param useStemmer- boolean - indicate whether to use stemmer. if true stemmer is used.
     * @return
     */
    public static ArrayList<String> parseQuery(String query , boolean useStemmer)
    {
        String [] tokens = query.split(" ");
        ArrayList<String> terms = Parse.parseText(tokens , useStemmer);

        return terms;
    }
    /**
     * Receives Document  , splits it into tokens and sends it to parseText function.
     * @param document - Document
     * @param useStemmer - boolean - indicate whether to use stemmer. if true stemmer is used.
     * @return ArrayList<String> of all the terms in the document after parse
     */
    public static ArrayList<String> parseDocument(Document document , boolean useStemmer)
    {
        String [] tokens = document.getText().split(" ");
        ArrayList<String> terms = Parse.parseText(tokens , useStemmer);

        return terms;
    }

    /**
     * Receives a document and parses it, removes stop words and applies stemmer if directed to.
     * @param tokens - String [] - array of tokens
     * @param useStemmer - boolean - indicate whether to use stemmer. if true stemmer is used.
     * @return - ArrayList<String> - all the words from the text of the document after parsing
     */
    public static ArrayList<String> parseText(String [] tokens, boolean useStemmer) {
        ArrayList<String> terms = new ArrayList<>();

        //Start of parsing

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Pair<String,Integer> result = new Pair<>("",0);

            //Removing empty token
            if(token.isEmpty() || token.matches("\n+") || token.matches("\t+")) {
                continue;
            }

            //Striping irrelevant symbols
            token = Parse.strip(token);

            //Numbers
            if(token.matches(".*\\d.*")){ //Token contains numbers

                //Dollar Detection
                if(token.matches(".*[$mbn].*")){ //checks for $ m b n FIXME: change to recognise bn and not b or n
                    String firstNextToken = "";
                    if(i<tokens.length-1) {
                        firstNextToken = Parse.strip(tokens[i + 1]);
                    }

                    result = Parse.generateTokenDollar(token, firstNextToken);
                    terms.add(result.getKey());
                    i=i+result.getValue();
                }

                //Percentage
                else if(token.matches(".*%.*")){
                    terms.add(token);
                }

                //Number/s with hyphens
                else if (token.matches(".*-.*"))
                {
                    terms.add(token);
                    terms.add(token.substring(0,token.indexOf("-")));
                    terms.add(token.substring(token.indexOf("-")+1));
                }

                //Numbers dependent on next token
                else
                {
                    String firstNextToken = "";
                    if(i<tokens.length-1) {
                        firstNextToken = Parse.strip(tokens[i + 1]);
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
                        i++;
                    }

                    //Date
                    else if(MonthMap.containsKey(firstNextToken)) // <<<DD Month>>>
                    {
                        token=MonthMap.get(firstNextToken)+"-"+token;
                        terms.add(token);
                        i++;
                    }

                    //Simple numbers and Prices
                    //Thousand
                    else if(firstNextToken.equals("thousand") ||firstNextToken.equals("Thousand") ) { // <<<Number Thousand>>>
                        token = token + "K";
                        terms.add(token);
                        i++;
                    }

                    //Trillion Dollars
                    else if (firstNextToken.equals("Trillion") ||firstNextToken.equals("trillion")){ // <<<Price trillion U.S. Dollars>>>
                        token = token + "000000 M Dollars";
                        terms.add(token);
                        i = i+3;
                    }

                    else if (firstNextToken.equals("dollars") ||firstNextToken.equals("Dollars")){

                        result = Parse.generateTokenPrice(token);

                        terms.add(result.getKey());
                        i=i+result.getValue();
                    }
                    else if(firstNextToken.equals("Million") ||firstNextToken.equals("million") ||firstNextToken.equals("Billion") ||firstNextToken.equals("billion")) {
                        String secondNextToken = "";
                        if(i<tokens.length-2) {
                            secondNextToken = Parse.strip(tokens[i + 2]);
                        }
                        String thirdNextToken = "";
                        if(i<tokens.length-3) {
                            thirdNextToken = Parse.strip(tokens[i + 3]);
                        }

                        result = Parse.generateTokenLargeNumbers(token,firstNextToken,secondNextToken,thirdNextToken);
                        terms.add(result.getKey());
                        i=i+result.getValue();

                    }

                    //Number Fraction
                    else if((firstNextToken.matches("\\d+/\\d+"))){
                        String secondNextToken = "";
                        if(i<tokens.length-2) {
                            secondNextToken = Parse.strip(tokens[i + 2]);
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
                        terms.add(generateTokenNumber(token));
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

                result = generateTokenMonth(token, firstNextToken);
                terms.add(result.getKey());
                i=i+result.getValue();
            }

            //Between number and number
            else if(token.equals("Between") || token.equals("between"))
            {
                String firstNextToken = "";
                if(i<tokens.length-1) {
                    firstNextToken = Parse.strip(tokens[i + 1]);
                }
                String secondNextToken = "";
                if(i<tokens.length-2) {
                    secondNextToken = Parse.strip(tokens[i + 2]);
                }
                String thirdNextToken = "";
                if(i<tokens.length-3) {
                    thirdNextToken = Parse.strip(tokens[i + 3]);
                }

                if(firstNextToken.matches("\\d+"))
                {

                    if(secondNextToken.equals("and"))
                    {

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
                            terms.add(token); //FIXME:: BETWEEN IS STOPWORD NEED TO CHECK IF ITS PART OF A TERM
                        }
                    }
                    else
                    {
                        terms.add(token); //FIXME:: BETWEEN IS STOPWORD NEED TO CHECK IF ITS PART OF A TERM
                    }
                }
                else
                {
                    terms.add(token); //FIXME:: BETWEEN IS STOPWORD NEED TO CHECK IF ITS PART OF A TERM
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
            // Second custom addition <<<Word-Word-Word>>>
            else if (token.matches(".*-.*-.*"))
            {
                terms.add(token);
                terms.add(token.substring(0,token.indexOf("-")));
                terms.add(token.substring(token.indexOf("-")+1,token.lastIndexOf("-")));
                terms.add(token.substring(token.lastIndexOf("-")+1));

            }
            //Third custom addition <<<Word-Word>>> and <<<Number-Word>>> and <<<Word-Number>>>
            else if (token.matches(".*-.*"))
            {
                terms.add(token);
                terms.add(token.substring(0,token.indexOf("-")));
                terms.add(token.substring(token.indexOf("-")+1));
            }
            else if(token.matches("^[A-Z].*"))
            {
                String entity=token;
                String nextToken = "";
                if(i<tokens.length-1) {
                    nextToken = Parse.strip(tokens[i + 1]);
                }
                for (int j = 0; j < tokens.length && nextToken.matches("^[A-Z].*"); j++) {
                    if(!Parse.isStopWord(entity))
                    {
                        terms.add(entity);
                    }
                    entity = entity + " " +nextToken;
                    i++;
                }
                if(!Parse.isStopWord(entity)) //TODO:CHECK!!!
                {
                    terms.add(entity);
                }
            }
            else
            {
                if(!Parse.isStopWord(token))
                terms.add(token);
            }
        }


        //STEMMER
        if(useStemmer){
            //TODO:Stemmer
            for (String term : terms) {
                term = Parse.stemm(term);
            }
        }


        return terms;
    }

    //TODO:fill function!!!
    private static String stemm(String term) {
        return term;
    }

    private static boolean isStopWord(String entity) {

        return false;
    }

    /**
     * Receives a token that contains a number and formats it based on it's size
     * @param token - String - number token
     * @return - String - formatted token
     */
    private static String generateTokenNumber(String token) {
        token = token.replaceAll(",",""); //remove commas 1,000 -> 1000

        if(token.matches("\\d+.?\\d*")) {
            double numberToken = Double.parseDouble(token); //TODO: Write more tests in order of avoiding try\catch
            if (numberToken >= Kilo && numberToken < Million) //token between Kilo and Million
            {
                numberToken = numberToken / Kilo;
                token = Parse.doubleDecimalFormat(numberToken) + "K";
            } else if (numberToken >= Million && numberToken < Billion) //token between Million to Trillion
            {
                numberToken = numberToken / Million;
                token = Parse.doubleDecimalFormat(numberToken) + "M";
            } else if (numberToken >= Billion) //token up to Trillion
            {
                numberToken = numberToken / Billion;
                token = Parse.doubleDecimalFormat(numberToken) + "B";
            } 
        }

        return token;
    }

    /**
     * Receives a token that matches a month and formats it.
     * Recognizes the patterns: <<<Month DD>>> <<<Month year>>>
     * @param token - String - month
     * @param firstNextToken - String - the following token
     * @return - Pair<String,Integer> - <token after processing, additionalTokensProcessed>
     */
    private static Pair<String,Integer> generateTokenMonth(String token, String firstNextToken) {
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
     * Receives a token that contains a number and a symbol indicates a price and formats it.
     * Recognizes the patterns: <<<$price>>> <<<$price million>>> <<<$price billion>>> <<<Price'm' Dollars>>>  <<<Price'bn' Dollars>>>
     * @param token - String - token containing a number and [$mbn]
     * @param firstNextToken - String - the following token
     * @return - Pair<String,Integer> - <token after processing, additionalTokensProcessed>
     */
    private static Pair<String,Integer> generateTokenDollar(String token , String firstNextToken) {
        int additionalTokensProcessed = 0;

        if(token.indexOf('$') == 0) {
            token = token.substring(1); //removing the $ sign
            if (firstNextToken.equals("million") || firstNextToken.equals("Million")){ // <<<$price million>>>
                token = token + " M Dollars";
                additionalTokensProcessed++;
            } else if(firstNextToken.equals("billion") || firstNextToken.equals("Billion")){ // <<<$price billion>>>
                token = token + "000 M Dollars";
                additionalTokensProcessed++;
            }
            else { // <<<$price>>>
                token = token.replaceAll(",","");
                double value = Double.parseDouble(token); //TODO: Write more tests in order of avoiding try\catch
                if (value >= Million) {
                    value = value / Million;
                    token = Parse.doubleDecimalFormat(value) + " M Dollars";
                } else {
                    token = Parse.doubleDecimalFormat(value) + " Dollars";
                }
            }
        } else if (token.matches("\\d+[mM]|\\d+.\\d+[mM]")){ // <<<Price'm' Dollars>>>
            if (firstNextToken.equals("Dollars")){
                token = token.substring(0,token.length()-1) + " M Dollars";
                additionalTokensProcessed++;
            }
        } else if (token.matches("\\d+bn|\\d+.\\d+bn")){ // <<<Price'bn' Dollars>>>
            if (firstNextToken.equals("Dollars")){
                token = token.substring(0,token.length()-2) + "000 M Dollars";
                additionalTokensProcessed++;
            }
        }

        Pair<String,Integer> result = new Pair<>(token,additionalTokensProcessed);
        return result;
    }

    /**
     * Receives a token that contains a number which already had been recognized as part of a price and formats it.
     * @param token - String - price
     * @return - Pair<String,Integer> - <token after processing, additionalTokensProcessed>
     */
    private static Pair<String,Integer> generateTokenPrice(String token){
        // <<<Price Dollars>>>
        int additionalTokensProcessed = 0;
        token = token.replaceAll(",","");
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

    /**
     * Receives a token with either "million" or "billion" following it and formats it as a large number or large price.
     * @param token - String - number
     * @param firstNextToken - String - the next token, containing million or billion
     * @param secondNextToken - String - the second next token
     * @param thirdNextToken - String - the third next token
     * @return - Pair<String,Integer> - <token after processing, additionalTokensProcessed>
     */
    private static Pair<String, Integer> generateTokenLargeNumbers(String token , String firstNextToken, String secondNextToken, String thirdNextToken) {
        int additionalTokensProcessed = 0;

        if(firstNextToken.equals("Million") ||firstNextToken.equals("million")){
            if(secondNextToken.equals("U.S.")) {
                if(thirdNextToken.equals("Dollars") || thirdNextToken.equals("dollars")){ // <<<Price million U.S. dollars>>>
                    token= token + " M Dollars";
                    additionalTokensProcessed = 3;
                } else {
                    token = token+"M";
                    additionalTokensProcessed = 1;
                }
            }else{
                token = token+"M";
                additionalTokensProcessed = 1;
            }
        } else if(firstNextToken.equals("Billion") ||firstNextToken.equals("billion")) {
            if (secondNextToken.equals("U.S.")) {
                if (thirdNextToken.equals("Dollars") || thirdNextToken.equals("dollars")) { // <<<Price million U.S. dollars>>>
                    token = token + "000 M Dollars";
                    additionalTokensProcessed = 3;
                }else {
                    token = token + "B";
                    additionalTokensProcessed = 1;
                }
            } else {
                token = token + "B";
                additionalTokensProcessed = 1;
            }
        }

        Pair<String,Integer> result = new Pair<>(token,additionalTokensProcessed);
        return result;
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

    /**
     * Receives a path to directory containing stop-word file and loads it to the "stopwords" Hash-set
     * @param corpusPath
     */
    public static void loadStopWords(String corpusPath) {
        if(stopwords != null)
        {
            stopwords = new HashSet<>();
            File file  = new File(corpusPath);
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line = "";
                while ((line = bufferedReader.readLine()) != null)
                {
                    stopwords.add(line);
                }
            }catch (FileNotFoundException e)
            {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
