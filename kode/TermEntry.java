import java.util.*;

// kelas untuk menyimpan pasangan term dan posting list-nya
public class TermEntry {
    private String term;
    private LinkedList<Document> postingList;

    public TermEntry(String term, LinkedList<Document> postingList) {
        this.term = term;
        this.postingList = postingList;
    }

    public String getTerm() {
        return term;
    }

    public LinkedList<Document> getPostingList() {
        return postingList;
    }
}