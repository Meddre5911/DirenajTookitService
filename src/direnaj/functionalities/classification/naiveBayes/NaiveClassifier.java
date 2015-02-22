package direnaj.functionalities.classification.naiveBayes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import direnaj.functionalities.sentiment.Sentiment;
import direnaj.util.TextUtils;

public class NaiveClassifier {

    private static NaiveClassifier instance = null;

    private TreeMap<String, TreeMap<String, Double>> unigramLaplaceProbabilities;
    private TreeMap<String, TreeMap<BigramObject, Double>> bigramLaplaceProbabilities;
    // class probabilities
    private HashMap<String, Double> classProbabilities;
    private Tokenizer tokenizer;
    private Vector<String> toBeIgnoredCharacters;

    private NaiveClassifier() throws Exception {
        unigramLaplaceProbabilities = new TreeMap<String, TreeMap<String, Double>>();
        bigramLaplaceProbabilities = new TreeMap<String, TreeMap<BigramObject, Double>>();
        classProbabilities = new HashMap<>();
        // load class probabilities
        classProbabilities.put(Sentiment.IRRELEVANT.name(), 0.12942633099463474d);
        classProbabilities.put(Sentiment.NEGATIVE.name(), 0.13215022699133305d);
        classProbabilities.put(Sentiment.NEUTRAL.name(), 0.4759389186958316d);
        classProbabilities.put(Sentiment.POSITIVE.name(), 0.2624845233182006d);

        unigramLaplaceProbabilities.put(Sentiment.IRRELEVANT.name(), new TreeMap<String, Double>());
        unigramLaplaceProbabilities.put(Sentiment.NEGATIVE.name(), new TreeMap<String, Double>());
        unigramLaplaceProbabilities.put(Sentiment.NEUTRAL.name(), new TreeMap<String, Double>());
        unigramLaplaceProbabilities.put(Sentiment.POSITIVE.name(), new TreeMap<String, Double>());

        bigramLaplaceProbabilities.put(Sentiment.IRRELEVANT.name(), new TreeMap<BigramObject, Double>());
        bigramLaplaceProbabilities.put(Sentiment.NEGATIVE.name(), new TreeMap<BigramObject, Double>());
        bigramLaplaceProbabilities.put(Sentiment.NEUTRAL.name(), new TreeMap<BigramObject, Double>());
        bigramLaplaceProbabilities.put(Sentiment.POSITIVE.name(), new TreeMap<BigramObject, Double>());

        toBeIgnoredCharacters = new Vector<>();
        toBeIgnoredCharacters.add(",");
        toBeIgnoredCharacters.add("\"");
        toBeIgnoredCharacters.add("\'");
        toBeIgnoredCharacters.add(";");
        toBeIgnoredCharacters.add(".");
        toBeIgnoredCharacters.add("<");
        toBeIgnoredCharacters.add(">");
        toBeIgnoredCharacters.add("(");
        toBeIgnoredCharacters.add(")");
        toBeIgnoredCharacters.add("&lt;");
        toBeIgnoredCharacters.add("/");
        toBeIgnoredCharacters.add("\\");
        toBeIgnoredCharacters.add("-");
        toBeIgnoredCharacters.add(":");
        toBeIgnoredCharacters.add("@");
        toBeIgnoredCharacters.add("&");

        // load unigram probabilities
        loadUnigramModel();
        // load bigram probabilities
        loadBigramModel();

        InputStream isToken = getClass().getClassLoader().getResourceAsStream("en-token.bin");
        // InputStream isToken =
        // getClass().getResourceAsStream("models/en-token.bin");
        TokenizerModel tokenModel = new TokenizerModel(isToken);
        tokenizer = new TokenizerME(tokenModel);

    }

    private void loadUnigramModel() throws IOException {
        TreeMap<String, Double> unigramWords = new TreeMap<String, Double>();
        Sentiment sentiment = Sentiment.IRRELEVANT;

        InputStream is = getClass().getClassLoader().getResourceAsStream("unigramModel4SentimentAnalysis.txt");

        //        InputStream is = new FileInputStream("unigramModel4SentimentAnalysis.txt");
        InputStreamReader isr = new InputStreamReader(is);
        try (BufferedReader br = new BufferedReader(isr)) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                try {
                    String[] split = sCurrentLine.split("-");
                    if (split.length > 2 && !TextUtils.isEmpty(split[0]) && !TextUtils.isEmpty(split[1])) {
                        if (sentiment != Sentiment.valueOf(split[0])) {
                            sentiment = Sentiment.valueOf(split[0]);
                            unigramWords = new TreeMap<String, Double>();
                            unigramLaplaceProbabilities.put(sentiment.name(), unigramWords);
                        }
                        if (split[2].contains("E")) {
                            unigramWords.put(split[1], Double.valueOf(split[2] + "-5"));
                        } else {
                            unigramWords.put(split[1], Double.valueOf(split[2]));
                        }
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
                unigramLaplaceProbabilities.put(sentiment.name(), unigramWords);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isr.close();
        }
    }

    private void loadBigramModel() throws IOException {
        TreeMap<BigramObject, Double> bigramWords = new TreeMap<BigramObject, Double>();
        Sentiment sentiment = Sentiment.IRRELEVANT;

        InputStream is = getClass().getClassLoader().getResourceAsStream("unigramModel4SentimentAnalysis.txt");
        InputStreamReader isr = new InputStreamReader(is);
        try (BufferedReader br = new BufferedReader(isr)) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                try {
                    String[] split = sCurrentLine.split("-");
                    if (split.length > 3 && !TextUtils.isEmpty(split[0]) && !TextUtils.isEmpty(split[1])) {
                        if (sentiment != Sentiment.valueOf(split[0])) {
                            sentiment = Sentiment.valueOf(split[0]);
                            bigramWords = new TreeMap<BigramObject, Double>();
                            bigramLaplaceProbabilities.put(sentiment.name(), bigramWords);
                        }
                        if (split[3].contains("E")) {
                            bigramWords.put(new BigramObject(split[2], split[1]), Double.valueOf(split[3] + "-4"));
                        } else {
                            bigramWords.put(new BigramObject(split[2], split[1]), Double.valueOf(split[3]));
                        }
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            bigramLaplaceProbabilities.put(sentiment.name(), bigramWords);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isr.close();
        }
    }

    public Sentiment classifyWithUnigram(String text) {
        List<String> unigrams = getUnigrams(text);
        String mostPossibleTopic4Bigram = findMostPossibleTopic4Unigram(unigrams, unigramLaplaceProbabilities, true);
        return Sentiment.valueOf(mostPossibleTopic4Bigram);
    }

    public Sentiment classifyWithBigram(String text) {
        List<BigramObject> bigrams = getBigrams(text);
        String mostPossibleTopic4Bigram = findMostPossibleTopic4Bigram(bigrams, bigramLaplaceProbabilities, true);
        return Sentiment.valueOf(mostPossibleTopic4Bigram);
    }

    private void analyzeText4Unigram(TreeMap<String, Integer> unigramWords, String text) {
        // get sentences
        // System.out.println("Sentences - " + titleSentences[i]);
        String[] titleTokens = tokenizer.tokenize(text);
        // System.out.println("Tokens : ");
        for (int tokenIndex = 0; tokenIndex < titleTokens.length; tokenIndex++) {
            String token = titleTokens[tokenIndex];
            token = checkToken(token);
            if (!TextUtils.isEmpty(token)) {
                // System.out.println(token);
                int tokenCount = 1;
                if (unigramWords.containsKey(token)) {
                    tokenCount = unigramWords.get(token);
                    tokenCount++;
                }
                // put to hashMap
                unigramWords.put(token, tokenCount);
            }
        }
    }

    private List<String> checkAllTokens(String[] titleTokens) {
        Vector<String> tokens = new Vector<>();
        for (int i = 0; i < titleTokens.length; i++) {
            String checkedToken = checkToken(titleTokens[i]);
            if (!TextUtils.isEmpty(checkedToken)) {
                tokens.add(checkedToken);
            }
        }
        return tokens;
    }

    private void analyzeText4Bigram(TreeMap<BigramObject, Integer> bigramWords, String text) {
        // tokenize titles
        String[] sentenceTokens = tokenizer.tokenize(text);
        List<String> allTokens = checkAllTokens(sentenceTokens);
        int bigramIndex = 1;
        // System.out.println("Bigram Tokens : ");
        while (bigramIndex <= allTokens.size() - 1) {
            // System.out.println("Vector Size : " + allTokens.size() +
            // " - Bigram Index : " + bigramIndex);
            String token1 = allTokens.get(bigramIndex - 1);
            String token2 = allTokens.get(bigramIndex);
            BigramObject bigramObject = new BigramObject(token1, token2);
            // System.out.println(bigramObject);
            // calculate count
            int count = 1;
            if (bigramWords.containsKey(bigramObject)) {
                count = bigramWords.get(bigramObject);
                count++;
            }
            bigramWords.put(bigramObject, count);
            bigramIndex++;
        }
    }

    public String checkToken(String token) {
        // crop ignored characters from start & end
        for (String ignoredCharacter : toBeIgnoredCharacters) {
            if (token.startsWith(ignoredCharacter)) {
                token = token.substring(ignoredCharacter.length());
            }
            if (token.endsWith(ignoredCharacter)) {
                token = token.substring(0, token.length() - 1);
            }
        }
        return token.toLowerCase(Locale.US);
    }

    private List<String> getUnigrams(String document) {
        TreeMap<String, Integer> unigramWords = new TreeMap<>();
        analyzeText4Unigram(unigramWords, document);
        return new ArrayList<String>(unigramWords.keySet());
    }

    private List<BigramObject> getBigrams(String document) {
        TreeMap<BigramObject, Integer> bigramWords = new TreeMap<>();
        analyzeText4Bigram(bigramWords, document);
        return new ArrayList<BigramObject>(bigramWords.keySet());
    }

    public String findMostPossibleTopic4Unigram(List<String> unigrams,
            TreeMap<String, TreeMap<String, Double>> unigramProbabilities, boolean isLaplaceCalculation) {
        double minPerplexityValue = 100000;
        String mostPossibleTopic = null;
        int unigramSize = unigrams.size();
        // get unigram probabilities
        Set<String> searchedTopics = unigramProbabilities.keySet();
        for (String topic : searchedTopics) {
            double tmpPerplexityValue = 1;
            int exitIndex = 0;
            TreeMap<String, Double> unigramHashMap = unigramProbabilities.get(topic);
            // get probability of each unigram
            for (String unigramWord : unigrams) {
                if (!isLaplaceCalculation) {
                    if (unigramHashMap.containsKey(unigramWord)) {
                        tmpPerplexityValue *= 1 / unigramHashMap.get(unigramWord);
                    } else {
                        exitIndex = 1;
                        break;
                    }
                } else {
                    if (unigramHashMap.containsKey(unigramWord)) {
                        tmpPerplexityValue *= 1 / unigramHashMap.get(unigramWord);
                    } else {
                        double laplaceEmptyProbability = getLaplaceProbability4EmptyUnigram(topic);
                        tmpPerplexityValue *= 1 / laplaceEmptyProbability;
                    }
                }
            }
            if (exitIndex != 1 && unigramSize > 0) {
                tmpPerplexityValue *= 1 / classProbabilities.get(topic);
                tmpPerplexityValue = Math.pow(tmpPerplexityValue, 1 / (double) unigramSize);
                if (tmpPerplexityValue < minPerplexityValue) {
                    minPerplexityValue = tmpPerplexityValue;
                    mostPossibleTopic = topic;
                }
            }
        }
        return mostPossibleTopic;
    }

    private String findMostPossibleTopic4Bigram(List<BigramObject> bigrams,
            TreeMap<String, TreeMap<BigramObject, Double>> bigramProbabilities, boolean isLaplaceCalculation) {
        double minPerplexityValue = 100000;
        String mostPossibleTopic = null;
        // get unigram probabilities
        Set<String> searchedTopics = bigramProbabilities.keySet();
        int bigramSize = bigrams.size();
        for (String topic : searchedTopics) {
            double tmpPerplexityValue = 1;
            int exitIndex = 0;
            TreeMap<BigramObject, Double> bigramHashMap = bigramProbabilities.get(topic);
            // get probability of each unigram
            for (BigramObject bigramWord : bigrams) {
                if (!isLaplaceCalculation) {
                    if (bigramHashMap.containsKey(bigramWord)) {
                        tmpPerplexityValue *= 1 / bigramHashMap.get(bigramWord);
                    } else {
                        exitIndex = 1;
                        break;
                    }
                } else {
                    if (bigramHashMap.containsKey(bigramWord)) {
                        tmpPerplexityValue *= 1 / bigramHashMap.get(bigramWord);
                    } else {
                        tmpPerplexityValue *= 1 / getLaplaceProbability4EmptyBigram(topic, bigramWord);
                    }
                }
            }
            if (exitIndex != 1) {
                tmpPerplexityValue *= 1 / classProbabilities.get(topic);
                tmpPerplexityValue = Math.pow(tmpPerplexityValue, 1 / (double) bigramSize);
                if (tmpPerplexityValue < minPerplexityValue) {
                    minPerplexityValue = tmpPerplexityValue;
                    mostPossibleTopic = topic;
                }
            }
        }
        return mostPossibleTopic;
    }

    public double getLaplaceProbability4EmptyUnigram(String topic) {
        TreeMap<String, Double> unigramHash = unigramLaplaceProbabilities.get(topic);
        Set<String> unigramKeys = unigramHash.keySet();
        // count all words
        double totalWords = 0;
        for (String unigramWord : unigramKeys) {
            totalWords += unigramHash.get(unigramWord);
        }
        return 1 / (double) (totalWords + 1);
    }

    private double getLaplaceProbability4EmptyBigram(String topic, BigramObject bigramWord) {
        TreeMap<String, Double> unigramHash = unigramLaplaceProbabilities.get(topic);
        int laplaceVocabularySize = unigramHash.keySet().size();
        double priorCounts = 0;
        if (unigramHash.containsKey(bigramWord.getPrior())) {
            priorCounts = unigramHash.get(bigramWord.getPrior());
        }
        return 1 / (double) (priorCounts + laplaceVocabularySize);
    }

    public static NaiveClassifier getInstance() throws Exception {
        if (instance == null) {
            instance = new NaiveClassifier();
        }
        return instance;
    }
}
