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
    private static final int MAXENTITYLENGTH = 3;


    /**
     * Mapping between months names and their numbers.
     */
    private static final HashMap<String, String> MonthMap = new HashMap() {{
        put("January", "01");
        put("JANUARY", "01");
        put("january", "01");
        put("Jan", "01");
        put("February", "02");
        put("FEBRUARY", "02");
        put("february", "02");
        put("Feb", "02");
        put("March", "03");
        put("MARCH", "03");
        put("march", "03");
        put("Mar", "03");
        put("April", "04");
        put("APRIL", "04");
        put("april", "04");
        put("Apr", "04");
        put("May", "05");
        put("MAY", "05");
        put("may", "05");
        put("June", "06");
        put("JUNE", "06");
        put("june", "06");
        put("July", "07");
        put("JULY", "07");
        put("july", "07");
        put("August", "08");
        put("AUGUST", "08");
        put("august", "08");
        put("Aug", "08");
        put("September", "09");
        put("SEPTEMBER", "09");
        put("september", "09");
        put("Sept", "09");
        put("October", "10");
        put("OCTOBER", "10");
        put("october", "10");
        put("Oct", "10");
        put("November", "11");
        put("NOVEMBER", "11");
        put("november", "11");
        put("Nov", "11");
        put("December", "12");
        put("DECEMBER", "12");
        put("december", "12");
        put("Dec", "12");
    }};
    private static HashSet<String> stopwords;
    private HashSet<String> entities = new HashSet<>();
    private HashSet<String> singleAppearanceEntities = new HashSet<>();


    public Parse() {
    }

    public Parse(HashSet<String> entities, HashSet<String> singleAppearanceEntities) {
        this.entities = entities;
        this.singleAppearanceEntities = singleAppearanceEntities;
    }

    /**
     * Receives Query  , splits it into tokens and parses it.
     * Return ArrayList of Strings after parse
     *
     * @param query       - String - user Query.
     * @param useStemmer- boolean - indicate whether to use stemmer. if true stemmer is used.
     * @return
     */
    public  ArrayList<String> parseQuery(String query, boolean useStemmer) {
        String[] tokens = query.split(" ");
        ArrayList<String> terms = parseText(tokens, useStemmer);

        return terms;
    }

    /**
     * Receives Document  , splits it into tokens and sends it to parseText function.
     *
     * @param document   - Document
     * @param useStemmer - boolean - indicate whether to use stemmer. if true stemmer is used.
     * @return ArrayList<String> of all the terms in the document after parse
     */
    public ArrayList<String> parseDocument(Document document, boolean useStemmer) {
        String[] tokens = document.getText().split(" ");
        ArrayList<String> terms = parseText(tokens, useStemmer);

        return terms;
    }

    /**
     * Receives a document and parses it, removes stop words and applies stemmer if directed to.
     *
     * @param tokens     - String [] - array of tokens
     * @param useStemmer - boolean - indicate whether to use stemmer. if true stemmer is used.
     * @return - ArrayList<String> - all the words from the text of the document after parsing
     */
    public ArrayList<String> parseText(String[] tokens, boolean useStemmer) {
        ArrayList<String> terms = new ArrayList<>();

        //Start of parsing

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Pair<String, Integer> result = new Pair<>("", 0);

            //Removing empty token
            if (token.isEmpty() || token.matches("\n+") || token.matches("\t+")) {
                continue;
            }

            //Striping irrelevant symbols
            token = strip(token);

            //Numbers
            if (token.matches(".*\\d.*")) { //Token contains numbers

                //Dollar Detection
                if (token.matches(".*[$m].*|.*bn.*")) { //checks for $ m b n FIXME: change to recognise bn and not b or n
                    String firstNextToken = "";
                    if (i < tokens.length - 1) {
                        firstNextToken = strip(tokens[i + 1]);
                    }

                    result = generateTokenDollar(token, firstNextToken);
                    terms.add(result.getKey());
                    i = i + result.getValue();
                }
                //Percentage
                else if (token.matches(".*%.*")) {
                    terms.add(token);
                }

                //Number/s with hyphens
                else if (token.matches(".*-.*")) {
                    terms.add(token);
                    terms.add(token.substring(0, token.indexOf("-")));
                    terms.add(token.substring(token.indexOf("-") + 1));
                }
                //Numbers dependent on next token
                else {
                    String firstNextToken = "";
                    if (i < tokens.length - 1) {
                        firstNextToken = strip(tokens[i + 1]);
                    }
                    //Fractions
                    if (token.matches("\\d+/\\d+")) {
                        if (firstNextToken.equals("dollars") || firstNextToken.equals("Dollars")) { //<<<Fraction Dollars>>>
                            terms.add(token + " Dollars");
                            i++;
                        } else { // <<<Fraction>>>
                            terms.add(token);
                        }
                    }
                    //Percentage
                    else if (firstNextToken.equals("percent") || firstNextToken.equals("percentage")) {
                        token = token + "%";
                        terms.add(token);
                        i++;
                    }
                    //Date
                    else if (MonthMap.containsKey(firstNextToken)) // <<<DD Month>>>
                    {
                        result = generateTokenDayMonth(token, firstNextToken);
                        terms.add(result.getKey());
                        i = i + result.getValue();
                    }
                    //Simple numbers and Prices
                    //Thousand
                    else if (firstNextToken.equals("thousand") || firstNextToken.equals("Thousand")) { // <<<Number Thousand>>>
                        token = token + "K";
                        terms.add(token);
                        i++;
                    }
                    //Trillion Dollars
                    else if (firstNextToken.equals("Trillion") || firstNextToken.equals("trillion")) { // <<<Price trillion U.S. Dollars>>>
                        token = token + "000000 M Dollars";
                        terms.add(token);
                        i = i + 3;
                    }
                    //Prices - Dollars
                    else if (firstNextToken.equals("dollars") || firstNextToken.equals("Dollars")) {

                        result = generateTokenPrice(token);

                        terms.add(result.getKey());
                        i = i + result.getValue();
                    }
                    //Large number dependent on next token
                    else if (firstNextToken.equals("Million") || firstNextToken.equals("million") || firstNextToken.equals("Billion") || firstNextToken.equals("billion")) {
                        String secondNextToken = "";
                        if (i < tokens.length - 2) {
                            secondNextToken = strip(tokens[i + 2]);
                        }
                        String thirdNextToken = "";
                        if (i < tokens.length - 3) {
                            thirdNextToken = strip(tokens[i + 3]);
                        }
                        result = generateTokenLargeNumbers(token, firstNextToken, secondNextToken, thirdNextToken);
                        terms.add(result.getKey());
                        i = i + result.getValue();
                    }
                    //Number Fraction
                    else if ((firstNextToken.matches("\\d+/\\d+"))) {
                        String secondNextToken = "";
                        if (i < tokens.length - 2) {
                            secondNextToken = strip(tokens[i + 2]);
                        }
                        if (secondNextToken.equals("dollars") || secondNextToken.equals("Dollars")) { //<<<Price Fraction Dollars>>>
                            terms.add(token + " " + firstNextToken + " Dollars");
                            i = i + 2;
                        } else {
                            terms.add(token + " " + firstNextToken); // <<<Number Fraction>>>
                            i++;
                        }
                    } else //<<<Simple Number>>>
                    {
                        terms.add(generateTokenSimpleNumber(token));
                    }
                }
            }
            //DATE
            else if (MonthMap.containsKey(token)) {
                String firstNextToken = "";
                if (i < tokens.length - 1) {
                    firstNextToken = strip(tokens[i + 1]);
                }
                result = generateTokenMonth(token, firstNextToken);
                terms.add(result.getKey());
                i = i + result.getValue();
            }
            //Between number and number - less memory complexity if left here instead of in a separate function.
            else if (token.equals("Between") || token.equals("between")) {
                String firstNextToken = "";
                if (i < tokens.length - 1) {
                    firstNextToken = strip(tokens[i + 1]);
                }
                String secondNextToken = "";
                if (i < tokens.length - 2) {
                    secondNextToken = strip(tokens[i + 2]);
                }
                String thirdNextToken = "";
                if (i < tokens.length - 3) {
                    thirdNextToken = strip(tokens[i + 3]);
                }
                if (firstNextToken.matches("\\d+")) {

                    if (secondNextToken.equals("and")) {

                        if (thirdNextToken.matches("\\d+")) {
                            token = firstNextToken + "-" + thirdNextToken;
                            terms.add(token);
                            terms.add(firstNextToken);
                            terms.add(thirdNextToken);
                            i = i + 3;
                        }
                    }
                } else {
                    //BETWEEN IS STOPWORD NO NEED TO ADD TO TERMS
                }
            }
            //First custom addition <<<Word / word>>>
            else if (token.contains("/")) {
                while (token.indexOf('/') > 0) {
                    String term = token.substring(0, token.indexOf('/'));
                    token = token.substring(token.indexOf('/') + 1);
                    if (useStemmer) {
                        terms.add(Stemmer.stem(term));
                    } else {
                        terms.add(term);
                    }
                }
                if (useStemmer) {
                    terms.add(Stemmer.stem(token.toLowerCase()));
                } else {
                    terms.add(token);
                }
            }
            // Hyphens <<<Word-Word-Word>>>
            else if (token.matches(".*-.*-.*") || token.matches(".*-.*")) {
                ArrayList<String> resultHyphenList = generateTokenHyphens(token);
                for (String term : resultHyphenList) {
                    if (!isStopWord(term.toLowerCase())) {
                        if (useStemmer) {
                            terms.add(Stemmer.stem(term.toLowerCase()));
                        } else {
                            terms.add(term);
                        }
                    }
                }
            }
            //Entity Recognition
            else if (token.matches("^[A-Z].*")) {
                ArrayList<String> entityTokensCandidates = new ArrayList<>();
                entityTokensCandidates.add(token);

                String nextToken = "";
                if (i < tokens.length - 1) {
                    nextToken = strip(tokens[i + 1]);
                }

                //Get all the following words which begins with a capital letter
                for (int j = 1; j + i < tokens.length - 1 && nextToken.matches("^[A-Z].*"); j++) {
                    entityTokensCandidates.add(nextToken);
                    nextToken = strip(tokens[i + j + 1]);
                    if (i + j == tokens.length - 2) {
                        entityTokensCandidates.add(nextToken);
                    }
                }


                boolean isCapsSequence = isAllCapsSequence(entityTokensCandidates);

                //  if (!isCapsSequence) { //at least a single word entity
                Pair<ArrayList<String>, Integer> resultList = generateTokensEntity(entityTokensCandidates);

                ArrayList<String> entityTokens = resultList.getKey();
                if (entityTokens.size() > 0 && (!isCapsSequence || (isCapsSequence && !resultList.getKey().get(0).matches("^[a-z].*")))) { // Do not add token which contain all caps letters
                    //Add to entities the first element which is the entity
                    String entity = entityTokens.get(0);
                    if (!entities.contains(entity)) {
                        if (!singleAppearanceEntities.contains(entity)) {
                            singleAppearanceEntities.add(entity);
                        } else {
                            singleAppearanceEntities.remove(entity);
                            entities.add(entity);
                        }
                    }
                    for (String entityToken : entityTokens) {
                        if (!isStopWord(entityToken.toLowerCase())) {
                            terms.add(entityToken.toUpperCase());
                        }
                    }
                } else {
                    for (String entityToken : entityTokens) {
                        if (!isStopWord(entityToken.toLowerCase())) {
                            terms.add(entityToken.toLowerCase());
                        }
                    }
                }


                i = i + resultList.getValue();
            } else {
                if (!isStopWord(token.toLowerCase()))
                    if (useStemmer) {
                        terms.add(Stemmer.stem(token.toLowerCase()));
                    } else {
                        terms.add(token);
                    }
            }
        }
        return terms;
    }

    /**
     * Receives a list and check if all the words in the list are all constructed only from capital letters
     *
     * @param wordList - ArrayList<String> - list of words
     * @return - boolean - true if all the words are all capital letters
     */
    private boolean isAllCapsSequence(ArrayList<String> wordList) {
        boolean areAllCaps = true;
        for (String word : wordList) {
            if (!word.matches("[A-Z]+")) {
                areAllCaps = false;
            }
        }
        return areAllCaps;
    }

    /**
     * Receives a list of words which start with a capital letter,
     * decides if they are a part of an entity and if so
     * extracts the entity and the individual tokens which construct it.
     *
     * @param entityCandidates - ArrayList<String> - a list of tokens with all capital letters
     * @return - Pair<ArrayList<String>,Integer> - <entity and individual tokens, additionalTokensProcessed>
     */
    private Pair<ArrayList<String>, Integer> generateTokensEntity(ArrayList<String> entityCandidates) {
        int additionalTokensProcessed = 0;
        ArrayList<String> resultList = new ArrayList<>();
        String entity = "";
        int countConsecutiveAllCaps = 0;

        for (int i = 0; i < entityCandidates.size(); i++) {
            String candidate = entityCandidates.get(i);
            if (i > 0) {
                entity = entity + " " + candidate;
                additionalTokensProcessed++;
            } else {
                entity = candidate;
            }
            if (!isStopWord(candidate.toLowerCase())) {
                resultList.add(candidate);
            }

            if (candidate.matches("[A-Z]+")) {
                countConsecutiveAllCaps++;
            } else {
                countConsecutiveAllCaps = 0;
            }
        }

        if (countConsecutiveAllCaps > MAXENTITYLENGTH) {
            ArrayList<String> lowerCaseResultList = new ArrayList<>();
            for (String token : resultList) {
                lowerCaseResultList.add(token.toLowerCase());
            }
            resultList = lowerCaseResultList;
        } else if (!entityCandidates.contains(entity)) {
            resultList.add(0, entity);
        }

        /*
        if (additionalTokensProcessed > 0) {
            additionalTokensProcessed = additionalTokensProcessed - 1; //TODO: Check!!!
        }
*/
        Pair<ArrayList<String>, Integer> result = new Pair<>(resultList, additionalTokensProcessed);
        return result;
    }


    /**
     * Striping irrelevant symbols. Removing all the symbols deemed unimportant for the indexing.
     *
     * @param token - String - a word with irrelevant symbols
     * @return - String -a word without irrelevant symbols
     */
    private String strip(String token) {
        String result = "";
        char[] charArray = token.toCharArray();
        for (char character : charArray) {
            //Parenthesis
            if (character == ')' || character == '(' || character == '{' || character == '}' || character == '[' || character == ']') {
                continue;
            }
            //Symbols
            else if (character == ':' || character == '"' || character == '*' || character == '#' || character == '\t' || character == '\n') {
                continue;
            } else {
                result = result + character;
            }
        }
        //Removing dot in the end of the token
        if ((result.indexOf('.') == result.length() - 1 || result.indexOf(',') == result.length() - 1 || result.indexOf('!') == result.length() - 1 || result.indexOf('?') == result.length() - 1) && !result.isEmpty()) {
            result = result.substring(0, result.length() - 1); //FIXME:!!! Check what's happening here
        }
        return result;
    }

    /**
     * Receives a token that contains a number and formats it based on it's size
     *
     * @param token - String - number token
     * @return - String - formatted token
     */
    private String generateTokenSimpleNumber(String token) {
        token = token.replaceAll(",", ""); //remove commas 1,000 -> 1000

        if (token.matches("\\d+.?\\d*")) {
            double numberToken = Double.parseDouble(token); //TODO: Write more tests in order of avoiding try\catch
            if (numberToken >= Kilo && numberToken < Million) //token between Kilo and Million
            {
                numberToken = numberToken / Kilo;
                token = doubleDecimalFormat(numberToken) + "K";
            } else if (numberToken >= Million && numberToken < Billion) //token between Million to Trillion
            {
                numberToken = numberToken / Million;
                token = doubleDecimalFormat(numberToken) + "M";
            } else if (numberToken >= Billion) //token up to Trillion
            {
                numberToken = numberToken / Billion;
                token = doubleDecimalFormat(numberToken) + "B";
            }
        }

        return token;
    }

    /**
     * Receives a token that matches a month and formats it.
     * Recognizes the patterns: <<<Month DD>>> <<<Month year>>>
     *
     * @param token          - String - month
     * @param firstNextToken - String - the following token
     * @return - Pair<String,Integer> - <token after processing, additionalTokensProcessed>
     */
    private Pair<String, Integer> generateTokenMonth(String token, String firstNextToken) {
        //String[] newTokenWithLastTokenProcessed = {"",""};
        int additionalTokensProcessed = 0;

        if (firstNextToken.matches("\\d+")) //<<<Month DD and Month year>>>
        {
            int firstNextTokenValue = Integer.parseInt(firstNextToken);
            if (firstNextTokenValue < 10 && firstNextTokenValue > 0) //single digit days {1-9}
            {
                token = MonthMap.get(token) + "-0" + firstNextTokenValue;
            } else if (firstNextTokenValue < 32 && firstNextTokenValue > 9) //double digit days {10-31}
            {
                token = MonthMap.get(token) + "-" + firstNextTokenValue;
            } else // firstNextToken is a year
            {
                token = firstNextToken + "-" + MonthMap.get(token);
            }
            additionalTokensProcessed++;
        }

        Pair<String, Integer> result = new Pair<>(token, additionalTokensProcessed);
        return result;
    }

    /**
     * Receives a number token and the following token which matches a month and formats them.
     * Recognizes the patterns: <<<DD Month>>>
     *
     * @param token          - String - number token
     * @param firstNextToken - String - the following token
     * @return - Pair<String,Integer> - <token after processing, additionalTokensProcessed>
     */
    private Pair<String, Integer> generateTokenDayMonth(String token, String firstNextToken) {
        int additionalTokensProcessed = 0;
        int tokenValue = Integer.parseInt(token);
        if (tokenValue < 10 && tokenValue > 0) //single digit days {1-9}
        {
            token = MonthMap.get(firstNextToken) + "-0" + tokenValue;
        } else if (tokenValue < 32 && tokenValue > 9) //double digit days {10-31}
        {
            token = MonthMap.get(firstNextToken) + "-" + tokenValue;
        }
        additionalTokensProcessed++;

        Pair<String, Integer> result = new Pair<>(token, additionalTokensProcessed);
        return result;
    }

    /**
     * Receives a token that contains a number and a symbol indicates a price and formats it.
     * Recognizes the patterns: <<<$price>>> <<<$price million>>> <<<$price billion>>> <<<Price'm' Dollars>>>  <<<Price'bn' Dollars>>>
     *
     * @param token          - String - token containing a number and [$mbn]
     * @param firstNextToken - String - the following token
     * @return - Pair<String,Integer> - <token after processing, additionalTokensProcessed>
     */
    private Pair<String, Integer> generateTokenDollar(String token, String firstNextToken) {
        int additionalTokensProcessed = 0;

        if (token.indexOf('$') == 0) {
            token = token.substring(1); //removing the $ sign
            if (firstNextToken.equals("million") || firstNextToken.equals("Million")) { // <<<$price million>>>
                token = token + " M Dollars";
                additionalTokensProcessed++;
            } else if (firstNextToken.equals("billion") || firstNextToken.equals("Billion")) { // <<<$price billion>>>
                token = token + "000 M Dollars";
                additionalTokensProcessed++;
            } else { // <<<$price>>>
                token = token.replaceAll(",", "");
                double value = Double.parseDouble(token); //TODO: Write more tests in order of avoiding try\catch
                if (value >= Million) {
                    value = value / Million;
                    token = doubleDecimalFormat(value) + " M Dollars";
                } else {
                    token = doubleDecimalFormat(value) + " Dollars";
                }
            }
        } else if (token.matches("\\d+[mM]|\\d+.\\d+[mM]")) { // <<<Price'm' Dollars>>>
            if (firstNextToken.equals("Dollars")) {
                token = token.substring(0, token.length() - 1) + " M Dollars";
                additionalTokensProcessed++;
            }
        } else if (token.matches("\\d+bn|\\d+.\\d+bn")) { // <<<Price'bn' Dollars>>>
            if (firstNextToken.equals("Dollars")) {
                token = token.substring(0, token.length() - 2) + "000 M Dollars";
                additionalTokensProcessed++;
            }
        }

        Pair<String, Integer> result = new Pair<>(token, additionalTokensProcessed);
        return result;
    }

    /**
     * Receives a token that contains a number which already had been recognized as part of a price and formats it.
     *
     * @param token - String - price
     * @return - Pair<String,Integer> - <token after processing, additionalTokensProcessed>
     */
    private Pair<String, Integer> generateTokenPrice(String token) {
        // <<<Price Dollars>>>
        int additionalTokensProcessed = 0;
        token = token.replaceAll(",", "");

        double value = Double.parseDouble(token); //TODO: Write more tests in order of avoiding try\catch
        if (value >= Million) {
            value = value / Million;
            token = doubleDecimalFormat(value) + " M Dollars";
        } else {
            token = doubleDecimalFormat(value) + " Dollars";
        }

        additionalTokensProcessed++;

        Pair<String, Integer> result = new Pair<>(token, additionalTokensProcessed);
        return result;
    }

    /**
     * Receives a token with either "million" or "billion" following it and formats it as a large number or large price.
     *
     * @param token           - String - number
     * @param firstNextToken  - String - the next token, containing million or billion
     * @param secondNextToken - String - the second next token
     * @param thirdNextToken  - String - the third next token
     * @return - Pair<String,Integer> - <token after processing, additionalTokensProcessed>
     */
    private Pair<String, Integer> generateTokenLargeNumbers(String token, String firstNextToken, String secondNextToken, String thirdNextToken) {
        int additionalTokensProcessed = 0;

        if (firstNextToken.equals("Million") || firstNextToken.equals("million")) {
            if (secondNextToken.equals("U.S.")) {
                if (thirdNextToken.equals("Dollars") || thirdNextToken.equals("dollars")) { // <<<Price million U.S. dollars>>>
                    token = token + " M Dollars";
                    additionalTokensProcessed = 3;
                } else {
                    token = token + "M";
                    additionalTokensProcessed = 1;
                }
            } else {
                token = token + "M";
                additionalTokensProcessed = 1;
            }
        } else if (firstNextToken.equals("Billion") || firstNextToken.equals("billion")) {
            if (secondNextToken.equals("U.S.")) {
                if (thirdNextToken.equals("Dollars") || thirdNextToken.equals("dollars")) { // <<<Price million U.S. dollars>>>
                    token = token + "000 M Dollars";
                    additionalTokensProcessed = 3;
                } else {
                    token = token + "B";
                    additionalTokensProcessed = 1;
                }
            } else {
                token = token + "B";
                additionalTokensProcessed = 1;
            }
        }

        Pair<String, Integer> result = new Pair<>(token, additionalTokensProcessed);
        return result;
    }

    private ArrayList<String> generateTokenHyphens(String token) {
        ArrayList<String> resultList = new ArrayList<>();
        if (token.matches(".*-.*-.*")) {
            resultList.add(token);
            resultList.add(token.substring(0, token.indexOf("-")));
            resultList.add(token.substring(token.indexOf("-") + 1, token.lastIndexOf("-")));
            resultList.add(token.substring(token.lastIndexOf("-") + 1));

        }
        //Hyphens <<<Word-Word>>> and <<<Number-Word>>> and <<<Word-Number>>>
        else if (token.matches(".*-.*")) {
            resultList.add(token);
            resultList.add(token.substring(0, token.indexOf("-")));
            resultList.add(token.substring(token.indexOf("-") + 1));
        }
        return resultList;
    }

    /**
     * https://stackoverflow.com/questions/153724/how-to-round-a-number-to-n-decimal-places-in-java
     *
     * @param number - double
     * @return - a number with three numbers after the point
     */
    private String doubleDecimalFormat(double number) {
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.FLOOR);
        String result = df.format(number);
        return result;
    }

    /**
     * Receives a path to directory containing stop-word file and loads it to the "stopwords" Hash-set
     *
     * @param corpusPath
     */
    public static void loadStopWords(String corpusPath) {
        if (stopwords != null) {
            stopwords = new HashSet<>();
            File file = new File(corpusPath);
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    stopwords.add(line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Receives a token and checks if it's a stop word against the "stopwords" set
     *
     * @param token - String - a word to check
     * @return - boolean - true if "stopwords" contains the token, else false
     */
    private boolean isStopWord(String token) {
        boolean isStopWord = false;
        if (stopwords != null) {
            if (stopwords.contains(token)) {
                isStopWord = true;
            }
        }
        return isStopWord;
    }

}
