import java.io.FileInputStream;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * This class cleans up text with a combination of lowercasing,
 * non-alpha removal, stop word removal and stemming.
 */
public class TextCleaner {
    ArrayList<String> stopwords;
    
    /**
     * Create a TextCleaner object
     */
    public TextCleaner() {
        stopwords = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream("Lib/stopwords.txt");
            Scanner s = new Scanner(fis);
            while (s.hasNextLine()) {
                stopwords.add(s.nextLine());
            }
            s.close();
        }
        catch (Exception e) {
            System.out.println("ERROR: filestream threw an exception " + e);
        }
    }

    /**
     * Clean a word. Return the cleaned word, or "" if it was a stop word
     *
     * @param word The word to clean
     * @return The cleaned word
     */
    public String clean(String word) {
        String new_string = "";
        for (char c: word.toCharArray()){
            if(Character.isAlphabetic(c)){
                new_string += c;
            }
        }
        word = new_string;
        
        // converting the word to lowercase
        word = word.toLowerCase();

        // check if a stopword
        if (stopwords.contains(word)) {
            return "";
        }
        
        // stemming the word
        PorterStemmer stemmer = new PorterStemmer();
        String result = stemmer.stem(word);
        return result;
    }


    /**
     * Clean an array of words by applying clean to each word.
     *
     * @param words An array of words to clean
     * @return A new array of words which are all cleaned.
     */
    public String[] clean(String[] words) {
        for (int i = 0; i < words.length; i++){
            words[i] = clean(words[i]);
        }
        return words;
    }
}
