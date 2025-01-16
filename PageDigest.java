import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Attributes;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.net.URL;
import java.util.HashMap;
import java.io.IOException;


/**
 * A digest of a web page at the given link, including the words on the page,
 * the links present on the page, and the base URL of the page.
 */
public class PageDigest {
    private String[] words;
    private String baseUrl;
    private ArrayList<String> links;

    /**
     * Create a PageDigest of the given link. Throw an IOException if
     * the page could not be read for whatever reason.
     */
    public PageDigest(String link) throws IOException {
        baseUrl = toBaseUrl(link);

        Document doc = Jsoup.connect(link).get();
        Elements doclinks = doc.select("a[href]");
        Element body = doc.body();
        words = body.text().split(" ");

        links = new ArrayList<String>();
        for (int i = 0; i < doclinks.size(); i++) {
            Element alink = doclinks.get(i);
            Attributes attributes = alink.attributes();
            String href = attributes.get("href");
            try {
                URL context = new URL(link);
                URL url = new URL(context, href);
                String protocol = url.getProtocol();
                if (! protocol.equals("https")) {
                    continue;
                }
                links.add(url.toString());
            } catch (Exception e) {
                // don't do anything
            }
        }
    }

    /**
     * Return the words on the page
     */
    public String[] getWords() {
        return words;
    }

    /**
     * Return the base URL for the page
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Return all the links on the page
     */
    public ArrayList<String> getLinks() {
        return links;
    }

    /**
     * Return a base URL from a link
     */
    public static String toBaseUrl(String link) {
        try {
            URL url = new URL(link);
            String baseUrl = url.getProtocol() + "://" + url.getHost();
            if (! url.getPath().equals("")) {
                baseUrl += url.getPath();
            }
            return baseUrl;
        } catch (Exception e) {
            return "";
        }
    }

    public static void main(String[] args) throws IOException {
        PageDigest digest = new PageDigest("http://wwu.edu");
        for (String link: digest.getLinks()) {
            System.out.println(link);
        }
        System.out.println("Base URL=" + digest.getBaseUrl());
    }
}
