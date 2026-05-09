import java.util.*;

// kelas untuk struktur data Trie
public class Trie {
    private TrieNode root = new TrieNode();

    // method untuk memasukkan term ke dalam posting list
    public void insertToPostingList(Document document, String key) {
        int length = key.length();
        int pos;
        TrieNode currentNode = root;

        // perulangan untuk membaca huruf per huruf pada key
        for(int i = 0; i < length; i++) {
            pos = this.convertCharacterToIndex(key.charAt(i));
            
            // jika node saat ini adalah leaf, maka buat child baru
            if (currentNode.getChildren()[pos] == null)
                currentNode.getChildren()[pos] = new TrieNode();

            currentNode = currentNode.getChildren()[pos];
        }

        // begitu sudah sampai di akhir huruf pada key,
        // masukkan dokumen ke dalam posting list
        currentNode.addToPostingList(document);
    }
    // method untuk mendapatkan posting list berdasarkan term
    public LinkedList<Document> getPostingList(String key) {
        int length = key.length();
        int pos;
        TrieNode currentNode = root;

        // perulangan untuk membaca huruf per huruf pada key
        for(int i = 0; i < length; i++) {
            pos = this.convertCharacterToIndex(key.charAt(i));

            // jika node saat ini adalah leaf, berarti tidak ditemukan
            if (currentNode.getChildren()[pos] == null)
                return null;

            currentNode = currentNode.getChildren()[pos];
        }

        // begitu sudah sampai di akhir huruf pada key,
        // maka return posting list
        return currentNode.getPostingList();
    }

    // method untuk mengubah char menjadi index pada child node
    private int convertCharacterToIndex(char character) {
        // mapping ke index 0 - 25
        if (character >= 'a' && character <= 'z')
            return character - 'a'; 
        // mapping ke index 26 - 35
        else if (character >= '0' && character <= '9')
            return character - '0' + 26; 
        // jika karakter di luar alphanumeric
        return -1; 
    }

    // method untuk mengubah index menjadi character
    private char convertIndexToCharacter(int index) {
        if (index < 26)
            return (char) (index + 'a');
        else
            return (char) (index - 26 + '0');
    }

    // method untuk mendaftarkan semua posting list pada trie
    public LinkedList<TermEntry> getAllTerms() {
        LinkedList<TermEntry> result = new LinkedList<>();
        // mendaftarkan dengan cara penelusuran menggunakan dfs
        this.depthFirstSearch(root, "", result);
        return result;
    }

    // method rekursif buat menelusuri node trie
    private void depthFirstSearch(TrieNode node, String currentTerm, LinkedList<TermEntry> result) {
        // base case apabila sudah mencapai leaf
        if (node == null)
            return;

        // cek apakah posting list kosong.
        // jika tidak, berarti node ini menyimpan dokumen yang valid
        if (node.getPostingList().isEmpty() == false) {
            TermEntry term = new TermEntry(currentTerm, node.getPostingList());
            result.add(term);
        }

        // telusuri semua anak pada node
        TrieNode[] children = node.getChildren();
        for (int i = 0; i < 36; i++) {
            if (children[i] != null) {
                char nextChar = this.convertIndexToCharacter(i);
                this.depthFirstSearch(children[i], currentTerm + nextChar, result);
            }
        }
    }
}

// kelas untuk node
class TrieNode {
    // array untuk menyimpan anak-anak, sebanyak 36 (a-z) dan (0-9).
    private TrieNode[] children = new TrieNode[36];

    // menandai apakah sampai node tertentu terbentuk sebuah kata.
    // boolean isEndOfWord = false;

    // secara dasar memang menggunakan boolean. 
    // namun, untuk mengimplementasikan inverted index, agar dapat melakukan 
    // wildcard search, maka diganti menjadi posting list.
    
    LinkedList<Document> postingList = new LinkedList<>();

    // getter
    public TrieNode[] getChildren() {
        return this.children;
    }
    public LinkedList<Document> getPostingList() {
        return this.postingList;
    }
    
    // setter: menambahkan dokumen baru ke dalam posting list
    public void addToPostingList(Document document) {
        // memastikan dokumen belum ada di node tersebut agar tidak duplikat
        if (this.postingList.isEmpty() || this.postingList.getLast().getId() != document.getId())
            this.postingList.add(document);
    }
}