package direnaj.functionalities.nlp;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import wordnet.WordNetHelper;
import direnaj.domain.User;
import direnaj.util.CollectionUtil;
import direnaj.functionalities.nlp.TweetParseUtil;

public class ConceptElicitor {

    
    public static <E> double conceptSetSimilarity(ArrayList<Entry<String, Integer>> concepts1,
                                        ArrayList<Entry<String, Integer>> concepts2,boolean mock) throws JWNLException {
        
        if(mock){
            return Math.random();
        }
        double simScore = 0;
        
        for (Entry<String, Integer> c1 : concepts1) {
            for (Entry<String, Integer> c2 : concepts2) {
                String cName1 = c1.getKey();
                String cName2 = c2.getKey();
                
                POS[] pos1 = WordNetHelper.getPOS(cName1);
                POS[] pos2 = WordNetHelper.getPOS(cName2);
                
                for (POS p1 : pos1) {
                    for (POS p2 : pos2) {
                        IndexWord w1 = WordNetHelper.getWord(p1, cName1);
                        IndexWord w2 = WordNetHelper.getWord(p2, cName2);
                        
                        Synset[] synset1 = w1.getSenses();
                        // caner, similarity int geliyor hac�. Double filan olmas� gerekmez mi ?
                        Synset[] synset2 = w2.getSenses();
                        
                        for (Synset syn1 : synset1) {
                            for (Synset syn2 : synset2) {
                                RelationshipList reList = RelationshipFinder.getInstance().findRelationships(syn1, syn2, PointerType.SIMILAR_TO);
                                Iterator<E> i = reList.iterator();
                                while(i.hasNext()) {
                                    Relationship rel = (Relationship) i.next();
                                    simScore += rel.getDepth();
                                }
                            }
                        }
                    }
                }
            }
        }
        
        
        /*
        RelationshipList list = 
                RelationshipFinder.getInstance().
                findRelationships(synset1, synset2, PointerType.SYNONYM);
        if (!list.isEmpty())  {
          Relationship rel = (Relationship) list.get(0);
          System.out.println(rel);
        }
        */

        return simScore;
    }
    
    /* getCommunityConcept
     * 
     * Extracts concepts from the tweets of the users in the given community,
     * sorted by the concept usage counts
     */
   public static ArrayList<Entry<String, Integer>> getCommunityConcept(Vector<User> users, int k) throws JWNLException {
        List<String> tweets = new ArrayList<String>();
        for (User usr : users) {
            tweets.addAll(usr.getPosts());
        }
        
        String[] arr = new String[tweets.size()];
        
        for (int i = 0;i<tweets.size();i++) {
            arr[i] = tweets.get(i);
        }
        
        return ConceptElicitor.tweetCollectionConcepts(arr, k);
    }
    
    /* getUserConcept
     * 
     * Extracts concepts from the tweets of the user,
     * sorted by the concept usage counts
     */
   public static ArrayList<Entry<String, Integer>> getUserConcept(User user, int k) throws JWNLException {
        
        List<String> tweets = user.getPosts();
        
        String[] userTweets = new String[tweets.size()];
        
        for (int i = 0;i<tweets.size();i++) {
            userTweets[i] = tweets.get(i);
        }
        
        return ConceptElicitor.tweetCollectionConcepts(userTweets, k);
    }

    /* tweetCollectionConcepts
     * 
     * Extracts concepts from the given tweet collection,
     * sorted by the concept usage counts
     */
    public static ArrayList<Entry<String, Integer>> tweetCollectionConcepts(String[] tweets, int k) throws JWNLException {
        
        Hashtable<String, Integer> conceptCounts = new Hashtable<String, Integer>();
        
        for (String tweet : tweets) {
            ArrayList<Entry<String, Integer>> twConcepts = ConceptElicitor.tweetRelatedConcepts(tweet, Integer.MAX_VALUE);
            
            for (Entry<String, Integer> twSingleConcept : twConcepts) {
                String cStr = twSingleConcept.getKey();
                int count = 0;
                if(conceptCounts.containsKey(cStr)) {
                    count = conceptCounts.get(cStr) + 1;
                } else {
                    count = 1;
                }
                conceptCounts.put(cStr, count);
            }
        }
        
        if (k > conceptCounts.size()) {
        	k = conceptCounts.size();
        }
        
        List<Entry<String, Integer>> lst = CollectionUtil.sortCounts(conceptCounts).subList(0, k);
        
        ArrayList<Entry<String, Integer>> arr = new ArrayList<Entry<String, Integer>>();
        
        for (Entry<String, Integer> e : lst) {
        	arr.add(e);
        }
        
        return arr;
    }
    
    /* tweetRelatedConcepts
     * 
     * Extracts concepts from the given tweet,
     * sorted by the concept usage counts
     */
    public static ArrayList<Entry<String, Integer>> tweetRelatedConcepts(String tweet, int k) throws JWNLException {
        
        List<String> tags = TweetParseUtil.getHashtagsInText(tweet);
        
        ArrayList<String> words = getCleanWords(tweet);
        
        Hashtable<String, Integer> conceptCounts = new Hashtable<String, Integer>();
        
        
        for (String word : words) {
            
            WordNetHelper.initialize("file_properties.xml");
            
            POS[] pos = WordNetHelper.getPOS(word);
            /*
            POS.ADJECTIVE;
            POS.ADVERB;
            POS.NOUN;
            POS.VERB;
            */
            if (conceptCounts.containsKey(word)) {
            	conceptCounts.put(word, conceptCounts.get(word) + 1);
            } else {
            	conceptCounts.put(word, 1);
            }
            
            for (POS ps : pos) {
                
                //System.out.println("\n\nWorking on : " + word + " -- " + ps.getLabel() + "\n");
                
                IndexWord w = WordNetHelper.getWord(ps, word);
                
                ArrayList<Synset> a = WordNetHelper.getRelated(w,PointerType.SIMILAR_TO);
                
                if (a!=null && !a.isEmpty()){
                    for(Synset sense : a) {
                        //System.out.println("Similar Words : ");
                        for (Word wrd : sense.getWords()) {
                            if (conceptCounts.containsKey(wrd.getLemma())) {
                                // increment its count value
                                conceptCounts.put(wrd.getLemma(), conceptCounts.get(wrd.getLemma()) + 1);
                            } else {
                                // else, just put the tag into the table with value 1
                                conceptCounts.put(wrd.getLemma(), 1);
                            }
                        }
                        
                    }
                } else {
                    //System.out.println("no similars");
                }
                
                ArrayList<Synset> b = WordNetHelper.getRelated(w,PointerType.HYPERNYM);
                
                if (b!=null && !b.isEmpty()){
                    for(Synset sense : b) {
                        //System.out.println("\nHypernym Words : ");
                        for (Word wrd : sense.getWords()) {
                            if (conceptCounts.containsKey(wrd.getLemma())) {
                                // increment its count value
                                conceptCounts.put(wrd.getLemma(), conceptCounts.get(wrd.getLemma()) + 1);
                            } else {
                                // else, just put the tag into the table with value 1
                                conceptCounts.put(wrd.getLemma(), 1);
                            }
                        }
                    }
                } else {
                    //System.out.println("\nno hypernyms");
                }
            }
        }
        
        if (k == Integer.MAX_VALUE) {
            k = conceptCounts.size();
        } else if  (k > conceptCounts.size()) {
        	k = conceptCounts.size();
        }
        
        List<Entry<String, Integer>> lst = CollectionUtil.sortCounts(conceptCounts).subList(0, k);
        
        ArrayList<Entry<String, Integer>> arr = new ArrayList<Entry<String, Integer>>();
        
        for (Entry<String, Integer> e : lst) {
        	arr.add(e);
        }
        
        return arr;
    }
    
    /* getCleanWords
     * 
     * Extracts usable words from the given raw tweet string.
     * Usable word is a word that can be queried to wordNet.
     * 
     * removes stopwords
     * normalizes "Tweet Words" -> 4u - for you
     * removes hashtags
     * removes urls
     * removes punctuations
     */
   public static ArrayList<String> getCleanWords(String rawTweet) {
        String cleanTweet = WordStringUtils.normalizeTweetWords(rawTweet);
        cleanTweet = WordStringUtils.removeStopwords(cleanTweet);
        
        cleanTweet = TweetParseUtil.onlyText(cleanTweet);
        
        ArrayList<String> words = new ArrayList<String>();
        
        for(String word : cleanTweet.split(" ")) {
            if (word.length() > 1) {
                word = word.replaceAll(":", "");
                words.add(word);
            }
        }
        
        return words;
    }
    
    
    
    public static void main(String[] args) throws JWNLException {
        ArrayList<Entry<String, Integer>> concepts = ConceptElicitor.tweetRelatedConcepts("Who was Albert 2day Kobalt Wohlstetter? What was \"Team B\"? Who wants to lead the U.S. into China Syria? You should know: 'In the... http://t.co/QkF6Ojozjx", 10);
        
        for (Entry<String, Integer> concept : concepts) {
            System.out.println(concept.getKey() + " - " + concept.getValue());
        }
    }
    
}
