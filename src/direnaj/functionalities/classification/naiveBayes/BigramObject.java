package direnaj.functionalities.classification.naiveBayes;
import java.util.Locale;

public class BigramObject implements Comparable<BigramObject> {

    private String prior;
    private String posterior;

    public BigramObject(String prior, String posterior) {
        this.prior = prior.toLowerCase(Locale.US);
        this.posterior = posterior.toLowerCase(Locale.US);
    }

    public String getPrior() {
        return prior;
    }

    public void setPrior(String prior) {
        this.prior = prior;
    }

    public String getPosterior() {
        return posterior;
    }

    public void setPosterior(String posterior) {
        this.posterior = posterior;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            if (obj instanceof BigramObject) {
                BigramObject bigram = (BigramObject) obj;
                if (getPrior().equalsIgnoreCase(bigram.getPrior())
                        && getPosterior().equalsIgnoreCase(bigram.getPosterior())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 5;
    }

    @Override
    public String toString() {
        return getPosterior() + " - " + getPrior();
    }

    @Override
    public int compareTo(BigramObject bigramObj) {
        String bigramString = bigramObj.toString();
        return this.toString().compareTo(bigramString);
    }

}
