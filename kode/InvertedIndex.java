import java.util.*;

import java.io.*;
import java.nio.file.Files;

public class InvertedIndex {
    private File folderPath;
    // pointer angka unik setiap dokumen
    private static int documentId = 0;
    // trie node sebagai inverted index
    private Trie trie = new Trie();
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
            List<String> terms = TextProcessor.tokenize(content);

            // ambil setiap token kata
            for (String term : terms) {
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

    // method untuk mendapatkan trie
    public Trie getTrie() {
        return this.trie;
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
