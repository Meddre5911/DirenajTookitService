package tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.labeler;

import java.util.List;

import tr.edu.boun.cmpe.direnaj.processor.twitter.tweets.tokenization.Token;


public interface Labeler {

  public void init() throws Exception;
  
  public List<Token> label(List<Token> tokens);
}
