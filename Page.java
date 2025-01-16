import java.io.Serializable;


/**
 * Page represents a web page. It has a link, and a numeric rank
 */
public class Page implements Serializable, Comparable<Page> {
    private int rank;
    private String link;

    /**
     * Create a Page with the given link. The rank is automatically set to 1.
     * Pages should be created with base URLs only.
     *
     * @param link The link for this web page
     */
    public Page(String link) {
        this.link = link;
        this.rank = 1;
    }

    public String getLink() {
        return link;
    }

    public int getRank() {
        return rank;
    }

    /**
     * Call increaseRank when you find that this page is linked to by
     * a newly discovered link
     */
    public void increaseRank() {
        this.rank += 1;
    }

    /**
     * This method helps to print out a page in a human readable way
     */
    public String toString() {
        return "Page(" + link + ", rank=" + rank + ")";
    }

    /**
     * A page is equal to another when the links are equal
     */
    public boolean equals(Object o) {
        if (o.getClass() != Page.class) {
            return false;
        }
        Page p = (Page) o;
        return link.equals(p.getLink());
    }

    /**
     * Compare is necessary in order to sort pages by rank
     */
    public int compareTo(Page other) {
        return other.rank - rank;
    }

    /**
     * HashCode is necessary if we want to hash pages. This implementation
     * just uses the hash of the link
     */
    public int hashCode() {
        return link.hashCode();
    }
}
