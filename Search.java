
import java.util.ArrayList;
import java.util.Arrays;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Text;

import java.util.Iterator;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;


public class Search {
    private InvertedIndex index;
    TextCleaner textCleaner = new TextCleaner();
    /**
     * Create a Search object that reads from the given filename
     *
     * @param filename The name of the file to read the saved inverted index
     */
    public Search(String filename) throws IOException, ClassNotFoundException {
        FileInputStream is = new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(is);
        InvertedIndex index = (InvertedIndex) in.readObject();
        this.index = index;

    }

    /**
     * Create a search object that also crawls the web with the given
     * values. This function should create a Crawler, have the crawler create
     * an inverted index, then save the inverted index.
     *
     * @param link The link to start crawling from
     * @param hostPattern The pattern to limit host names for links
     * @param depth The number of levels to crawl
     */
    public Search(String link, String hostPattern, int depth) {
        // create crawler
        Crawler crawler = new Crawler("inverted_index.ser");
        InvertedIndex index = crawler.visit(link, hostPattern, depth);
        this.index = index;
    }

    /**
     * Search the inverted index for the given query words. The result
     * contains Pages where all words are found, sorted with the highest
     * rank first.
     *
     * @param queryWords An array of Strings that are the query words
     * @return An array of Pages that is the query result.
     */
    public Page[] search(String[] queryWords) {
        // creating an ArrayList first, then will convert to array later
        ArrayList<PageSet> foundPages = new ArrayList<PageSet>();
        
        // clean every word in query
        textCleaner.clean(queryWords);

        // use InvertedIndex to look up the PageSets for each word, and add them to foundPages
        for (String word : queryWords) {
            PageSet result = index.lookup(word);
            if (result != null) {
                foundPages.add(result);
            }
        }

        if (foundPages.isEmpty()) {
            return new Page[0]; // No results found
        }

        // find the intersection of all the PageSets
        PageSet intersection = foundPages.get(0);
        for(int i = 1; i<foundPages.size(); i++){
            intersection = intersection.intersect(foundPages.get(i));
        }

        if (intersection.size() == 0) {
            return new Page[0];
        }

        // Sort the intersection set by rank
        Page[] sort = new Page[intersection.size()];
        PageSet sorted = new PageSet();
        
        Iterator<Page> page = intersection.iterator();
        while (page.hasNext()) {
            Page currPage = page.next();
            sorted.add(currPage);
        }

        // Convert sorted pageset to page array
        Iterator<Page> s = sorted.iterator();
        for (int i = 0; i < sorted.size(); i++) {
            Page currPage = s.next();
            sort[i] = currPage;
        }

        return sort;

        /*
        Page[] sort;
        PageSet sorted = new PageSet();
        sort = new Page[intersection.size()];

        Iterator<Page> page = intersection.iterator();
        // sort intersection set so that higher rank pages come first
        while(page.hasNext()){
            Page currPage = page.next();
            if(currPage.getRank() == 3){
                // add to top of sorted
                sorted.add(currPage);
            }
        }
        page = intersection.iterator();
        while(page.hasNext()){
            Page currPage = page.next();
            if(currPage.getRank() == 2){
                // add to top of sorted
                sorted.add(currPage);
            }
        }
        page = intersection.iterator();
        while(page.hasNext()){
            Page currPage = page.next();
            if(currPage.getRank() == 1){
                // add to top of sorted
                sorted.add(currPage);
            }
        }
        // iterator for sorted PageSet
        // convert sorted pageset to page array
        Iterator<Page> s = sorted.iterator();
        for(int i = 0; i<sorted.size(); i++){
            Page currPage = s.next();
            sort[i] = currPage;
        }
        return sort;
        */
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Search");
        Search search = new Search("inverted_index.ser");
        String[] queryWords = new String[] { "research", "funding" };
        Page[] sorted = search.search(queryWords);
        for (Page p: sorted) {
            System.out.println(p);
        }

    }
}