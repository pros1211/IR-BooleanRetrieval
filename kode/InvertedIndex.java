import java.util.*;
import java.io.*;
import java.nio.file.Files;

public class InvertedIndex {
    protected HashMap<String, LinkedList<Integer>> invertIndex = new HashMap<>();
    protected ArrayList<String> docList = new ArrayList<>();
    private File path;

    public InvertedIndex(File path) {
        this.path = path;
    }

    // method untuk membuat inverted index
    public void buildIndex() throws IOException {
        int docId = 0;
        for (File dokumen : this.path.listFiles()) {
            String teks = Files.readString(dokumen.toPath());
            // tambahkan nama file ke arraylist untuk mapping doc id ke nama file asli
            docList.addLast(dokumen.getName());
            // parsing dan tokenisasi berdasarkan karakter non - huruf dan normalisasi
            String[] parsing = teks.toLowerCase().split("\\W+");
            // ambil setiap token kata
            for (String kata : parsing) {
                if (kata.isEmpty()) {
                    continue;
                }
                // jika pada inverted index dictionary sudah ada kata
                if (this.invertIndex.containsKey(kata)) {
                    // tambahkan docid ke linked list selama ia belum ada di post list
                    LinkedList<Integer> docList = this.invertIndex.get(kata);
                    if (!docList.getLast().equals(docId)) {
                        docList.addLast(docId);
                    }
                } else {
                    // jika kata belum ada di dictionary maka inisialisasi posting list
                    LinkedList<Integer> newList = new LinkedList<>();
                    newList.addLast(docId);
                    this.invertIndex.put(kata, newList);
                }

            }
            docId++;
        }
    }

    // method print post list dari dataset
    public void getPostList() {
        for (Map.Entry<String, LinkedList<Integer>> entry : this.invertIndex.entrySet()) {

            String term = entry.getKey();
            LinkedList<Integer> postingList = entry.getValue();
            ArrayList<String> listNama = new ArrayList<>();

            for (Integer docId : postingList) {

                String namaFile = this.docList.get(docId);

                listNama.add(namaFile);
            }
            System.out.println(term + " -> " + listNama);
        }
    }
}
