import java.io.IOException;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.net.URL;
import java.util.List;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;


/*
 * Efficiency:
 * On my machine, for a depth of 1:
 * takes about 5 seconds using 8 threads,
 * around 7 seconds using 4 threads,
 * and about 20 seconds using 1 thread.
 */

/**
 * The Worker exists in order to implement multi-threading. It does
 * much of the heavy-lifting that the crawler normally would do, including
 * recursion. Each Worker represents a thread that recursively crawls, and
 * it is created and run in Crawler.visit().
 */
class Worker extends Thread {
    String startLink;
    int maxDepth;
    String hostPattern;
    Crawler crawler;
    int section;

    /**
     * Creates a new worker object. It will not do anything until it
     * is started by calling start().
     * 
     * @param startLink The link to start from
     * @param maxDepth How many levels to follow links
     * @param hostPattern The regex pattern to limit indexing to
     * @param crawler The crawler object that this worker will communicate with
     * @param section The portion of the homepage this worker is responsible for
     */
    public Worker(String startLink, String hostPattern, int maxDepth, Crawler crawler, int section) {
        this.startLink = startLink;
        this.hostPattern = hostPattern;
        this.maxDepth = maxDepth;
        this.crawler = crawler;
        this.section = section;
    }

    public void run() {
        crawl(startLink, maxDepth);
    }

    /**
     * Recursively crawls the given link to the given depth, adding to the
     * Inverted Index of the crawler object that was given to it in the constructor.
     * 
     * @param link the url to crawl from
     * @param depth the number of levels to crawl down
     */
    private void crawl(String link, int depth) {
        // Check if the page has already been visited already:
        // this also increases the page's rank automatically if true
        if (crawler.hasBeenVisited(PageDigest.toBaseUrl(link)) == true) {
            return;
        }

        // Create PageDigest and get its links:
        PageDigest digest = getDigest(link);
        if (digest == null) {
            // because PageDigest seemingly throws errors indexing pdfs, images, or unresponsive
            // pages, this will cancel indexing any pages that encounter IOExceptions
            return; 
        }
        List<String> links = digest.getLinks();

        // Create a new page and add it to the crawler's inverted index:
        // excludes every section except 0 from adding the homepage.
        // i.e. for the home page, only section 0 adds the homepage, the
        // other sections just get the links and crawl down
        if (section == 0 || (section != 0 && depth != maxDepth)) {
            Page page = new Page(digest.getBaseUrl());
            TextCleaner cleaner = new TextCleaner();
            String[] words = cleaner.clean(digest.getWords());
            crawler.addPage(words, page);
        }

        // if it's the first time crawling (we're on the homepage), split links:
        if (depth == maxDepth) {
            // splits the homepage into the worker's respective section:
            int end;
            int start = section * (links.size() / Crawler.MAX_THREADS);
            if (section == Crawler.MAX_THREADS - 1) {
                end = links.size();
            }
            else {
                end = (section + 1) * (links.size() / Crawler.MAX_THREADS);
            }
            links = links.subList(start, end);
        }

        // recursively crawl:
        depth--;
        if (depth >= 0) { // recursive case
            for (String newLink : links) {
                // if this is a link within wwu.edu:
                String hostname = getHostname(newLink);
                if (Pattern.matches(hostPattern, hostname)) {
                    crawl(newLink, depth);
                }
                // does nothing if link is not in domain
            }
        }
        return; // base case
    }

    private String getHostname(String link) {
        URL linkUrl = null; // has to start null in case the try-catch fails
        try {
            linkUrl = new URL(link);
        }
        catch (Exception e) {
            System.out.println("FATAL: URL threw an exception");
        }
        return linkUrl.getHost();
    }

    protected PageDigest getDigest(String link) {
        PageDigest digest = null; // has to start out null in case the try-catch fails
        try {
            digest = new PageDigest(link);
        }
        catch (IOException e) {
            System.out.println("ERROR: PageDigest threw an IOException loading " + link + "\n" + e);
        }
        return digest;
    }
}


/**
 * The Crawler implements web crawling by starting from a link
 * and then indexing those pages. It limits itself to a host pattern
 * expression, and only crawls to a specified depth
 */
public class Crawler {
    String filename;
    ArrayList<Page> visitedPages;
    InvertedIndex invInd;
    Worker[] threads;
    static final int MAX_THREADS = 8;
    
    /**
     * Create a web crawler that saves the inverted index to the given
     * filename
     *
     * @param filename The file to save the inverted index to
     */
    public Crawler(String filename) {
        this.filename = filename;
        visitedPages = new ArrayList<Page>();
        invInd = new InvertedIndex();
    }

    /**
     * Visit and index the page at the link given. Recursively index the pages
     * given by links on the page up to a given depth from the starting
     * point. Return an inverted index that was created by the indexing.
     *
     * @param link The link to start from.
     * @param hostPattern The java.util.regex.Pattern to limit indexing to
     * @param depth How many levels to follow links
     * @return An inverted index formed by indexing the pages
     */
    public InvertedIndex visit(String link, String hostPattern, int depth){
        // create threads and let them go hog wild on their section
        threads = new Worker[MAX_THREADS];
        long start = System.currentTimeMillis();
        for (int i = 0; i < MAX_THREADS; i++) {
            Worker worker = new Worker(link, hostPattern, depth, this, i);
            threads[i] = worker;
            worker.start();
        }

        // wait for all threads to finish:
        try {
            for (int i = 0; i < MAX_THREADS; i++) {
                threads[i].join();
            }
        }
        catch (Exception e) {
            System.out.println("ERROR: A thread was interrupted");
        }
        long end = System.currentTimeMillis(); // start/end used just to track performance
        System.out.println("Total time creating InvertedIndex: " + (end - start) + "ms");

        // then just return the completed inverted index:
        return invInd;
    }

    // The following two methods are needed in order to prevent threads
    // from interfering with each other when writing to invInd and visitedPages:

    /**
     * Adds a page to visitedPages and invInd. This method should be called
     * instead of directly adding the page in order to prevent threads
     * from interfering with each other.
     * 
     * @param words A cleaned array of words associated with page
     * @param page A page object to be added
     */
    protected synchronized void addPage(String[] words, Page page) {
        // checks again to make sure the page has not been visited yet:
        if (hasBeenVisited(page.getLink()) == true) {
            return;
        }
        visitedPages.add(page);
        invInd.add(words, page);
    }
    /**
     * Returns true if the given url has been visited before, i.e. it
     * is in visitedPages. Increases the page's rank if it has been visited
     * before. Thread safe.
     * 
     * @param baseUrl The base url to search for
     */
    protected synchronized boolean hasBeenVisited(String baseUrl) {
        for (Page page : visitedPages) {
            if (page.getLink() == baseUrl) {
                page.increaseRank();
                return true;
            }
        }
        return false;
    }

    /**
     * Save the inverted index to disk. The filename to save to was
     * given in the constructor
     */
    public void saveInvertedIndex() throws IOException {
        FileOutputStream fs = new FileOutputStream(filename);
        ObjectOutputStream out = new ObjectOutputStream(fs);
        out.writeObject(invInd);
        out.close();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Crawling...");
        Crawler crawler = new Crawler("inverted_index.ser");


        crawler.visit("https://wwu.edu", ".*wwu.edu", 1);
        crawler.saveInvertedIndex();
    }
}