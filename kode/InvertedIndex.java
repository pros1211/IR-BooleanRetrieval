import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class InvertedIndex {
    private File folderPath;
    // pointer angka unik setiap dokumen
    private static int documentId = 0;
    // trie node sebagai inverted index
    private Trie trie = new Trie();
    // trie node untuk kata tanpa stemming (khusus wildcard)
    private Trie rawTrie = new Trie();
    // list untuk daftar dokumen
    private LinkedList<Document> documentList = new LinkedList<>();

    public InvertedIndex(File folderPath) {
        this.folderPath = folderPath;
    }

    // method untuk membuat inverted index
    public void buildIndex() throws IOException {
        // baca seluruh dokumen di dalam satu folder
        for (File file : this.folderPath.listFiles()) {
            // buat instance dokumen baru
            Document document = new Document(documentId, file.getName());
            // tambahkan ke daftar dokumen
            documentList.add(document);
            // ambil isi dokumen
            String content = Files.readString(file.toPath());
            // tokenisasi isi dokument
            List<String> termsStem = TextProcessor.tokenizeStem(content);
            List<String> rawTerms = TextProcessor.tokenizeRaw(content);
            // ambil setiap token kata
            for (String term : rawTerms) {
                // pastikan term tidak kosong
                if (term.isEmpty())
                    continue;
                // masukkan dokumen berdasarkan term
                this.rawTrie.insertToPostingList(document, term);
            }
            for (String term : termsStem) {
                // pastikan term tidak kosong
                if (term.isEmpty())
                    continue;
                // masukkan dokumen berdasarkan term
                this.trie.insertToPostingList(document, term);
            }

            // increment documentId untuk dokumen selanjutnya
            documentId++;
        }
    }

    // method untuk mendapatkan trie yang berisi hasil stemming
    public Trie getStemTrie() {
        return this.trie;
    }

    // method untuk mendapatkan trie yang belum di stemming

    public Trie getRawTrie() {
        return this.rawTrie;
    }

    // method untuk mendapatkan keseluruhan dokumen
    public LinkedList<Document> getAllDocuments() {
        return this.documentList;
    }

    // method mendapatkan semua posting list
    public LinkedList<TermEntry> getAllPostingLists() {
        return this.trie.getAllTerms();
    }

    // method print semua posting list
    public void printAllPostingLists() {
        // ambil semua daftar term pada trie
        LinkedList<TermEntry> terms = this.trie.getAllTerms();
        // baca setiap term
        for (TermEntry term : terms) {
            System.out.print(term.getTerm() + "\t-> ");
            // ekstrak daftar dokumen pada posting list
            for (Document document : term.getPostingList())
                System.out.print("[" + document.getName() + "] ");
            System.out.println();
        }
    }
}
