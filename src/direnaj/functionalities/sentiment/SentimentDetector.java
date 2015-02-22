package direnaj.functionalities.sentiment;

import direnaj.functionalities.classification.ClassificationMethods;
import direnaj.functionalities.classification.naiveBayes.NaiveClassifier;

public class SentimentDetector {

    private ClassificationMethods sentimentClassificationMethod;

    public SentimentDetector(ClassificationMethods classificationMethod) {
        sentimentClassificationMethod = classificationMethod;
    }

    public boolean isTextNegative(String text) {
        Sentiment result = Sentiment.NEUTRAL;
        try {
            switch (sentimentClassificationMethod) {
            case NaiveBayesUnigram:
                result = NaiveClassifier.getInstance().classifyWithUnigram(text);
                break;
            case NaiveBayesBigram:
                result = NaiveClassifier.getInstance().classifyWithBigram(text);
                break;
            case SVM:
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result.equals(Sentiment.NEGATIVE)) {
            return true;
        }
        return false;
    }
}
