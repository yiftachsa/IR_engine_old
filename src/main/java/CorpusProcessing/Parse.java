package CorpusProcessing;

import javafx.util.Pair;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.LinkedList;


public class Parse {

    private static final double Kilo = 1000;
    private static final double Million = 1000000;
    private static final double Billion = 1000000000;
    private static final int MAXENTITYLENGTH = 3;

    /**
     * Mapping between months names and their numbers.
     */
    private static final HashMap<String, String> MONTHMAP = new HashMap() {{
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
    private HashSet<String> entities;
    private HashSet<String> singleAppearanceEntities;
    private boolean useStemmer; // indicate whether to use stemmer. if true stemmer is used.


    public Parse(HashSet<String> entities, HashSet<String> singleAppearanceEntities, boolean useStemmer) {
        this.entities = entities;
        this.singleAppearanceEntities = singleAppearanceEntities;
        this.useStemmer = useStemmer;
    }

    /**
     * Receives Query  , splits it into tokens and parses it.
     * Return LinkedList of Strings after parse
     *
     * @param query - String - user Query.
     * @return
     */
    public HashMap<String , Pair<String , Integer>>  parseQuery(String query) {
        String[] tokens = query.split(" ");
        HashMap<String , Pair<String , Integer>>  terms = parseText(tokens, useStemmer ,"");
        return terms;
    }

    /**
     * Receives Document  , splits it into tokens and sends it to parseText function.
     *
     * @param document - Document
     * @return ArrayList<String> of all the terms in the document after parse
     */
    public    HashMap<String , Pair<String , Integer>>  parseDocument(Document document) {
        String[] tokens = document.getText().split(" ");
        HashMap<String , Pair<String , Integer>> terms = parseText(tokens, useStemmer , document.getId());
        return terms;
    }

    /**
     * Receives a document and parses it, removes stop words and applies stemmer if directed to.
     *
     * @param tokens     - String [] - array of tokens
     * @param useStemmer - boolean - indicate whether to use stemmer. if true stemmer is used.
     * @return - ArrayList<String> - all the words from the text of the document after parsing
     */
    public HashMap<String , Pair<String , Integer>> parseText(String[] tokens, boolean useStemmer , String docID) {
        HashMap<String , Pair<String , Integer>> terms = new HashMap<>();

        //Start of parsing
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Pair<String, Integer> result = new Pair<>("", 0);

            //Removing empty token
            if (token.isEmpty() || token.matches("\n+") || token.matches("\t+") || token.equals("") || token.equals("--")) {
                continue;
            }

            //Striping irrelevant symbols
            token = strip(token);

            //Numbers
            if (token.matches(".*\\d.*")) { //Token contains numbers

                if (token.matches("\\d+s")) {
                    token = token.substring(0, token.length() - 1);
                }

                //Dollar Detection
                if (token.matches(".*[$m].*|.*bn.*")) { //checks for $ m b n
                    String firstNextToken = "";
                    if (i < tokens.length - 1) {
                        firstNextToken = strip(tokens[i + 1]);
                    }
                    result = generateTokenDollar(token, firstNextToken);
                    if(terms.containsKey(result.getKey()))
                    {
                        int frequency =terms.get(result.getKey()).getValue();
                        terms.put(result.getKey() , new Pair<>(docID ,frequency+1));
                    }
                    else
                    {
                        terms.put(result.getKey() ,new Pair<>(docID ,1));
                    }
                    i = i + result.getValue();
                }
                //Percentage
                else if (token.matches(".*%.*")) {
                    if(terms.containsKey(token))
                    {
                        int frequency =terms.get(token).getValue();
                        terms.put(token , new Pair<>(docID , frequency+1));
                    }
                    else
                    {
                        terms.put(token, new Pair<>(docID , 1));
                    }
                }

                //Number/s with hyphens
                else if (token.matches(".*-.*")) {

                    if(terms.containsKey(token))
                    {
                        int frequency =terms.get(token).getValue();
                        terms.put(token , new Pair<>(docID , frequency+1));
                    }
                    else
                    {
                        terms.put(token, new Pair<>(docID , 1));
                    }
                    if(terms.containsKey(token.substring(0, token.indexOf("-"))))
                    {
                        int frequency =terms.get(token.substring(0, token.indexOf("-"))).getValue();
                        terms.put(token.substring(0, token.indexOf("-")) , new Pair<>(docID , frequency+1));
                    }
                    else
                    {
                        terms.put(token.substring(0, token.indexOf("-")), new Pair<>(docID ,1));
                    }
                    if(terms.containsKey(token.substring(token.indexOf("-") + 1)))
                    {
                        int frequency =terms.get(token.substring(token.indexOf("-") + 1)).getValue();
                        terms.put((token.substring(token.indexOf("-") + 1)), new Pair<>(docID , frequency+1));
                    }
                    else
                    {
                        terms.put(token.substring(token.indexOf("-") + 1), new Pair<>(docID ,1));
                    }
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
                            if(terms.containsKey(token + " Dollars"))
                            {
                                int frequency =terms.get(token + " Dollars").getValue();
                                terms.put( token + " Dollars", new Pair<>(docID ,frequency+1));
                            }
                            else
                            {
                                terms.put(token + " Dollars", new Pair<>(docID ,1));
                            }
                            i++;
                        } else { // <<<Fraction>>>
                            if(terms.containsKey(token))
                            {
                                int frequency =terms.get(token).getValue();
                                terms.put( token, new Pair<>(docID , frequency+1));
                            }
                            else
                            {
                                terms.put(token, new Pair<>(docID  ,1));
                            }
                        }
                    }
                    //Percentage
                    else if (firstNextToken.equals("percent") || firstNextToken.equals("percentage")) {
                        token = token + "%";
                        if(terms.containsKey(token))
                        {
                            int frequency =terms.get(token).getValue();
                            terms.put( token, new Pair<>(docID , frequency+1));
                        }
                        else
                        {
                            terms.put(token, new Pair<>(docID ,1 ));
                        }
                        i++;
                    }
                    //Date
                    else if (token.matches("\\d+") && MONTHMAP.containsKey(firstNextToken)) // <<<DD Month>>>
                    {
                        result = generateTokenDayMonth(token, firstNextToken);
                        if(terms.containsKey(result.getKey()))
                        {
                            int frequency =terms.get(result.getKey()).getValue();
                            terms.put(result.getKey(), new Pair<>(docID ,frequency+1));
                        }
                        else
                        {
                            terms.put(result.getKey(), new Pair<>(docID ,  1));
                        }
                        i = i + result.getValue();
                    }
                    //Simple numbers and Prices
                    //Thousand
                    else if (firstNextToken.equals("thousand") || firstNextToken.equals("Thousand")) { // <<<Number Thousand>>>
                        token = token + "K";
                        if(terms.containsKey(token))
                        {
                            int frequency =terms.get(token).getValue();
                            terms.put( token, new Pair<>(docID ,frequency+1));
                        }
                        else
                        {
                            terms.put(token, new Pair<>( docID ,1));
                        }

                        i++;
                    }
                    //Trillion Dollars
                    else if (firstNextToken.equals("Trillion") || firstNextToken.equals("trillion")) { // <<<Price trillion U.S. Dollars>>>
                        token = token + "000000 M Dollars";
                        if(terms.containsKey(token))
                        {
                            int frequency =terms.get(token).getValue();
                            terms.put( token, new Pair<>(docID ,frequency+1));
                        }
                        else
                        {
                            terms.put(token, new Pair<>(docID , 1));
                        }
                        i = i + 3;
                    }
                    //Prices - Dollars
                    else if (firstNextToken.equals("dollars") || firstNextToken.equals("Dollars")) {
                        result = generateTokenPrice(token);
                        token = result.getKey();
                        if (!token.equals("")) {
                            if(terms.containsKey(token))
                            {
                                int frequency =terms.get(token).getValue();
                                terms.put( token, new Pair<>(docID , frequency+1));
                            }
                            else
                            {
                                terms.put(token, new Pair<>(docID ,1));
                            }
                            i = i + result.getValue();
                        }
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
                        if(terms.containsKey(result.getKey()))
                        {
                            int frequency =terms.get(result.getKey()).getValue();
                            terms.put( result.getKey(), new Pair<>(docID , frequency+1));
                        }
                        else
                        {
                            terms.put(result.getKey(),new Pair<>(docID , 1));
                        }
                        i = i + result.getValue();
                    }
                    //Number Fraction
                    else if ((firstNextToken.matches("\\d+/\\d+"))) {
                        String secondNextToken = "";
                        if (i < tokens.length - 2) {
                            secondNextToken = strip(tokens[i + 2]);
                        }
                        if (secondNextToken.equals("dollars") || secondNextToken.equals("Dollars")) { //<<<Price Fraction Dollars>>>
                            if(terms.containsKey(token + " " + firstNextToken + " Dollars"))
                            {
                                int frequency =terms.get(token + " " + firstNextToken + " Dollars").getValue();
                                terms.put( token + " " + firstNextToken + " Dollars", new Pair<>(docID , frequency+1));
                            }
                            else
                            {
                                terms.put(token + " " + firstNextToken + " Dollars", new Pair<>(docID , 1));
                            }
                            i = i + 2;
                        } else {
                            if(terms.containsKey(token + " " + firstNextToken))
                            {
                                int frequency =terms.get(token + " " + firstNextToken).getValue();
                                terms.put(token + " " + firstNextToken, new Pair<>(docID ,frequency+1));
                            }
                            else
                            {
                                terms.put(token + " " + firstNextToken, new Pair<>(docID , 1));
                            }
                            i++;
                        }
                    } else //<<<Simple Number>>>
                    {
                        if (token.matches("^[0-9]+$"))
                        {
                            if(terms.containsKey(generateTokenSimpleNumber(token)))
                            {
                                int frequency =terms.get(generateTokenSimpleNumber(token)).getValue();
                                terms.put(generateTokenSimpleNumber(token), new Pair<>(docID ,frequency+1));
                            }
                            else
                            {
                                terms.put(generateTokenSimpleNumber(token), new Pair<>(docID,1));
                            }
                        }
                        else continue; //todo:check!
                    }
                }
            }
            //DATE
            else if (MONTHMAP.containsKey(token)) {
                String firstNextToken = "";
                if (i < tokens.length - 1) {
                    firstNextToken = strip(tokens[i + 1]);
                }
                result = generateTokenMonth(token, firstNextToken);
                if(terms.containsKey(result.getKey()))
                {
                    int frequency =terms.get(result.getKey()).getValue();
                    terms.put( result.getKey(), new Pair<>(docID ,frequency+1));
                }
                else
                {
                    terms.put(result.getKey(), new Pair<>(docID , 1));
                }
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

                            if(terms.containsKey(token))
                            {
                                int frequency =terms.get(token).getValue();
                                terms.put( token, new Pair<>(docID , frequency+1));
                            }
                            else
                            {
                                terms.put(token, new Pair<>(docID ,1));
                            }
                            if(terms.containsKey(firstNextToken))
                            {
                                int frequency =terms.get(firstNextToken).getValue();
                                terms.put( firstNextToken, new Pair<>(docID , frequency+1));
                            }
                            else
                            {
                                terms.put(firstNextToken, new Pair<>(docID ,1));
                            }
                            if(terms.containsKey(thirdNextToken))
                            {
                                int frequency =terms.get(thirdNextToken).getValue();
                                terms.put( thirdNextToken, new Pair<>(docID , frequency+1));
                            }
                            else
                            {
                                terms.put(thirdNextToken, new Pair<>(docID , 1));
                            }
                            i = i + 3;
                        }
                    }
                } else {
                    //BETWEEN IS STOPWORD NO NEED TO ADD TO TERMS
                }
            }
            //First custom addition <<<Word / word>>>
            else if (token.matches(".+/.+")) {
                while (token.indexOf('/') > 0) {
                    String term = token.substring(0, token.indexOf('/'));
                    token = token.substring(token.indexOf('/') + 1);
                    if (!isStopWord(term.toLowerCase())) {
                        if (useStemmer) {
                            if(terms.containsKey(Stemmer.stem(term).toLowerCase()))
                            {
                                int frequency =terms.get(Stemmer.stem(term.toLowerCase())).getValue();
                                terms.put( Stemmer.stem(term.toLowerCase()), new Pair<>(docID ,frequency+1));
                            }
                            else
                            {
                                terms.put(Stemmer.stem(term.toLowerCase()), new Pair<>(docID ,1));
                            }
                        } else {
                            if(terms.containsKey(term))
                            {
                                int frequency =terms.get(term).getValue();
                                terms.put( term, new Pair<>(docID ,frequency+1));
                            }
                            else
                            {
                                terms.put(term, new Pair<>(docID , 1));
                            }
                        }
                    }
                }
                if (!isStopWord(token.toLowerCase())) {
                    if (useStemmer) {
                        if(terms.containsKey(Stemmer.stem(token).toLowerCase()))
                        {
                            int frequency =terms.get(Stemmer.stem(token.toLowerCase())).getValue();
                            terms.put( Stemmer.stem(token.toLowerCase()), new Pair<>(docID ,frequency+1));
                        }
                        else
                        {
                            terms.put(Stemmer.stem(token.toLowerCase()), new Pair<>(docID , 1));
                        }
                    } else {
                        if(terms.containsKey(token))
                        {
                            int frequency =terms.get(token).getValue();
                            terms.put( token, new Pair<>(docID ,frequency+1));
                        }
                        else
                        {
                            terms.put(token, new Pair<>(docID , 1));
                        }
                    }
                }
            }
            // Hyphens <<<Word-Word-Word>>>
            else if (token.matches(".*-.*-.*") || token.matches(".*-.*") || token.contains("--")) {
                LinkedList<String> resultHyphenList = generateTokenHyphens(token);
                for (String term : resultHyphenList) {
                    if (!isStopWord(term.toLowerCase())) {
                        if (useStemmer) {
                            if(terms.containsKey(Stemmer.stem(term.toLowerCase())))
                            {
                                int frequency =terms.get(Stemmer.stem(term.toLowerCase())).getValue();
                                terms.put(Stemmer.stem(term.toLowerCase()), new Pair<>(docID ,frequency+1));
                            }
                            else
                            {
                                terms.put(Stemmer.stem(term.toLowerCase()), new Pair<>(docID ,1));
                            }

                        } else {
                            if (term.matches("^[A-Z].*"))
                            {
                                if(terms.containsKey(term.toUpperCase()))
                                {
                                    int frequency =terms.get(term.toUpperCase()).getValue();
                                    terms.put( term.toUpperCase(), new Pair<>(docID ,frequency+1));
                                }
                                else
                                {
                                    terms.put(term.toUpperCase(), new Pair<>(docID ,1));
                                }

                            }
                            else
                            {
                                if(terms.containsKey(term))
                                {
                                    int frequency =terms.get(term).getValue();
                                    terms.put( term, new Pair<>(docID ,frequency+1));
                                }
                                else
                                {
                                    terms.put(term, new Pair<>(docID ,1));
                                }

                            }
                        }
                    }
                }
            }
            //Entity Recognition
            //TOKEN CONTAIN ONLY CAPITAL LETTERS
            else if (token.matches("^[A-Z]+([-/]?[A-Z]+)*")) {
                LinkedList<String> entityTokensCandidates = new LinkedList<>();
                entityTokensCandidates.add(token);
                String nextToken = "";
                if (i < tokens.length - 1) {
                    if (!(tokens[i + 1].contains("\n")) && !(tokens[i + 1].contains("\t"))) {
                        nextToken = strip(tokens[i + 1]);
                    }
                }
                for (int j = 1; j + i < tokens.length && nextToken.matches("^^[A-Z]+([-/]?[A-Z]+)*"); j++) {
                    entityTokensCandidates.add(nextToken);
                    if (i + j < tokens.length - 1) {
                        nextToken = strip(tokens[i + j + 1]); //strip the next token
                    }
                }
                //MORE THEN MAXENTITYLENGTH.
                if (entityTokensCandidates.size() > MAXENTITYLENGTH) {
                    int counter = i;
                    for (String term : entityTokensCandidates) {
                        tokens[counter] = term.toLowerCase();
                        counter++;
                    }
                    i--;
                } else {
                    String term = "";
                    for (String string : entityTokensCandidates) {
                        if (term.equals("")) {
                            term = string;
                        } else {
                            term = term + " " + string;
                        }

                    }
                    if (!isStopWord(term.toLowerCase())) {
                        if (useStemmer) {
                            if(terms.containsKey(Stemmer.stem(term.toLowerCase())))
                            {
                                int frequency =terms.get(Stemmer.stem(term.toLowerCase())).getValue();
                                terms.put( Stemmer.stem(term.toLowerCase()), new Pair<>(docID ,frequency+1));
                            }
                            else
                            {
                                terms.put(Stemmer.stem(term.toLowerCase()), new Pair<>(docID , 1));
                            }
                        } else {
                            if(terms.containsKey(term))
                            {
                                int frequency =terms.get(term).getValue();
                                terms.put( term, new Pair<>(docID ,frequency+1));
                            }
                            else
                            {
                                terms.put(term, new Pair<>(docID ,1));
                            }
                        }
                        if (!entities.contains(term)) {
                            if (!singleAppearanceEntities.contains(term)) {
                                singleAppearanceEntities.add(term);
                            } else {
                                singleAppearanceEntities.remove(term);
                                entities.add(term);
                            }
                        }
                    }

                    if (entityTokensCandidates.size() > 1) {
                        for (String string : entityTokensCandidates) {
                            if (!isStopWord(string.toLowerCase()))
                                if (useStemmer) {
                                    if(terms.containsKey(Stemmer.stem(string.toLowerCase())))
                                    {
                                        int frequency =terms.get(Stemmer.stem(string.toLowerCase())).getValue();
                                        terms.put( Stemmer.stem(string.toLowerCase()), new Pair<>(docID ,frequency+1));
                                    }
                                    else
                                    {
                                        terms.put(Stemmer.stem(string.toLowerCase()), new Pair<>(docID ,1));
                                    }
                                } else {
                                    if(terms.containsKey(string))
                                    {
                                        int frequency =terms.get(string).getValue();
                                        terms.put( string, new Pair<>(docID ,frequency+1));
                                    }
                                    else
                                    {
                                        terms.put(string, new Pair<>(docID ,1));
                                    }
                                }
                        }
                    }
                    i = i + entityTokensCandidates.size() - 1;//TODO:CHECK
                }
            } else if (token.matches("^[A-Z][a-z]+([-/]+[A-Z]?[a-z]+)*")) {
                LinkedList<String> entityTokensCandidates = new LinkedList<>();
                entityTokensCandidates.add(token);
                String nextToken = "";
                if (i < tokens.length - 1) {
                    nextToken = strip(tokens[i + 1]);
                }
                //Get all the following words which begins with a capital letter
                for (int j = 1; j + i < tokens.length && nextToken.matches("^[A-Z][a-z]+([-/]+[A-Z]?[a-z]+)*"); j++) {
                    entityTokensCandidates.add(nextToken);
                    if (i + j < tokens.length - 1) {
                        nextToken = strip(tokens[i + j + 1]); //strip the next token
                    }
                }
                Pair<LinkedList<String>, Integer> resultList = generateTokensEntity(entityTokensCandidates);
                LinkedList<String> entityTokens = resultList.getKey();
                if (entityTokens.size() > 0) {
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
                            if(terms.containsKey(entityToken.toUpperCase()))
                            {
                                int frequency =terms.get(entityToken.toUpperCase()).getValue();
                                terms.put( entityToken.toUpperCase(), new Pair<>(docID ,frequency+1));
                            }
                            else
                            {
                                terms.put(entityToken.toUpperCase(), new Pair<>(docID , 1));
                            }
                        }
                    }
                }
                i = i + resultList.getValue();
            } else {
                if (token.matches("^[a-z]+$") && !isStopWord(token.toLowerCase()))
                    if (useStemmer) {
                        if(terms.containsKey(Stemmer.stem(token.toLowerCase())))
                        {
                            int frequency =terms.get(Stemmer.stem(token.toLowerCase())).getValue();
                            terms.put( Stemmer.stem(token.toLowerCase()), new Pair<>(docID ,frequency+1));
                        }
                        else
                        {
                            terms.put(Stemmer.stem(token.toLowerCase()), new Pair<>(docID ,1));
                        }
                    } else {
                        if(terms.containsKey(token))
                        {
                            int frequency =terms.get(token).getValue();
                            terms.put( token, new Pair<>(docID ,frequency+1));
                        }
                        else
                        {
                            terms.put(token, new Pair<>(docID ,1));
                        }
                    }
            }
        }
        return terms;
    }

    /**
     * Receives a list and check if all the words in the list are all constructed only from capital letters
     *
     * @param wordList - LinkedList<String> - list of words
     * @return - boolean - true if all the words are all capital letters
     */
    private boolean isAllCapsSequence(LinkedList<String> wordList) {
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
     * @param entityCandidates - LinkedList<String> - a list of tokens with all capital letters
     * @return - Pair<LinkedList<String>,Integer> - <entity and individual tokens, additionalTokensProcessed>
     */
    private Pair<LinkedList<String>, Integer> generateTokensEntity(LinkedList<String> entityCandidates) {
        int additionalTokensProcessed = 0;
        LinkedList<String> resultList = new LinkedList<>();
        String entity = "";
        for (int i = 0; i < entityCandidates.size(); i++) {
            String candidate = entityCandidates.get(i);
            if (i > 0) {
                entity = entity + " " + candidate;
                additionalTokensProcessed++;
            } else {
                entity = candidate;
            }
            //IN CASE THE CANDIDATE IS NOT A STOP WORD ADD IT TO RESULT LIST
            if (!isStopWord(candidate.toLowerCase())) {
                resultList.add(candidate);
            }
        }
        if (!entityCandidates.contains(entity)) {
            resultList.add(0, entity);
        }
        Pair<LinkedList<String>, Integer> result = new Pair<>(resultList, additionalTokensProcessed);
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
            else if (character == '!' || character == '?' || character == ';' || character == ':' || character == '"' || character == '*' || character == '#' || character == '\t' || character == '\n') {
                continue;
            } else {
                result = result + character;
            }
        }
        //Removing dot in the end of the token
        if ( (result.indexOf('-') == result.length() - 1 || result.indexOf('.') == result.length() - 1 || result.indexOf(',') == result.length() - 1 || result.indexOf('!') == result.length() - 1 || result.indexOf('?') == result.length() - 1) && !result.isEmpty()) {
            result = result.substring(0, result.length() - 1); //FIXME:!!! Check what's happening here
        }
        else
        {
           while( (result.indexOf('-') == 0))
               result = result.substring(1);
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

        if (token.matches("\\d+\\.?\\d*")) {
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
                token = MONTHMAP.get(token) + "-0" + firstNextTokenValue;
            } else if (firstNextTokenValue < 32 && firstNextTokenValue > 9) //double digit days {10-31}
            {
                token = MONTHMAP.get(token) + "-" + firstNextTokenValue;
            } else // firstNextToken is a year
            {
                token = firstNextToken + "-" + MONTHMAP.get(token);
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
            token = MONTHMAP.get(firstNextToken) + "-0" + tokenValue;
        } else if (tokenValue < 32 && tokenValue > 9) //double digit days {10-31}
        {
            token = MONTHMAP.get(firstNextToken) + "-" + tokenValue;
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
            if (token.matches("\\d+\\.?\\d*-.*") && (token.contains("Million") || token.contains("Million") || token.contains("billion") || token.contains("Billion"))) {
                firstNextToken = token.substring(token.indexOf("-") + 1);
                token = token.substring(0, token.indexOf("-"));
                additionalTokensProcessed--;
            }
            if (firstNextToken.contains("million") || firstNextToken.contains("Million")) { // <<<$price million>>>
                token = token + " M Dollars";
                additionalTokensProcessed++;
            } else if (firstNextToken.contains("billion") || firstNextToken.contains("Billion")) { // <<<$price billion>>>
                token = token + "000 M Dollars";
                additionalTokensProcessed++;
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
            } else { // <<<$price>>>
                token = token.replaceAll(",", "");
                if (token.matches("^\\d+$")) {
                    double value = Double.parseDouble(token); //TODO: Write more tests in order of avoiding try\catch
                    if (value >= Million) {
                        value = value / Million;
                        token = doubleDecimalFormat(value) + " M Dollars";
                    } else {
                        token = doubleDecimalFormat(value) + " Dollars";
                    }
                } else {
                    token = "";
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
        if ((token.matches("^[0-9]*$"))) {

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
        Pair<String, Integer> result = new Pair<>("", 0);
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

    private LinkedList<String> generateTokenHyphens(String token) {
        LinkedList<String> resultList = new LinkedList<>();
        if(token.contains("--"))
        {
            resultList.add(token.substring(0, token.indexOf("-")));
            resultList.add(token.substring(token.indexOf("-") +2));
        }
        else if (token.matches(".*-.*-.*")) {
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
     * @param stopWordsPath
     */
    public static void loadStopWords(String stopWordsPath) {
        if (stopwords == null) {
            stopwords = new HashSet<>();
            File file = new File(stopWordsPath);
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    stopwords.add(line);
                }
                stopwords.add("");
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

    public void setUseStemmer(boolean useStemmer) {
        this.useStemmer = useStemmer;
    }
}
