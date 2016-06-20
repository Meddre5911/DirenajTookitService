package direnaj.functionalities.organizedBehaviour;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class StopWordsUtil {

	private static StopWordsUtil stopWordsUtil;
	private List<String> allStopWords;

	private StopWordsUtil() {
		allStopWords = new ArrayList<>(800);
		BufferedReader br = null;
		String sCurrentLine;
		try {
			Logger.getLogger(StopWordsUtil.class).debug("Stop Words are getting inited");
			br = new BufferedReader(new FileReader("/home/direnaj/toolkit/toolkitConfig/stopwords.txt"));
			while ((sCurrentLine = br.readLine()) != null) {
				Logger.getLogger(StopWordsUtil.class).debug("Stop Word : " + sCurrentLine);
				allStopWords.add(sCurrentLine);
			}
		} catch (IOException e) {
			Logger.getLogger(StopWordsUtil.class).debug("Error During init.", e);
		}
	}

	public static StopWordsUtil getInstance() {
		if (stopWordsUtil == null) {
			stopWordsUtil = new StopWordsUtil();
		}
		return stopWordsUtil;
	}

	public List<String> getAllStopWords() {
		return allStopWords;
	}

	public void setAllStopWords(List<String> allStopWords) {
		this.allStopWords = allStopWords;
	}

	public static void main(String[] args) {
		StopWordsUtil.getInstance();
	}
}
