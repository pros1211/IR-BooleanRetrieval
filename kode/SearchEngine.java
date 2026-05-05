import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

class InvertedIndex {
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

class QueryProcess {
    protected String[] queryParse;
    private InvertedIndex docInvertedIndex;
    Stack<String> operator = new Stack<>();
    List<String> postfixQuery = new ArrayList<>();

    // method skala prioritas operator
    private int getPrecedence(String op) {
        if (op.equals("NOT"))
            return 3;
        if (op.equals("AND"))
            return 2;
        if (op.equals("OR"))
            return 1;
        return 0;
    }

    // method intersect
    public LinkedList<Integer> intersect(LinkedList<Integer> pL1, LinkedList<Integer> pL2) {
        LinkedList<Integer> result = new LinkedList<>();
        // optimisasi pemrosesan intersect berdasarkan panjang posting list
        if (pL1.size() > pL2.size()) {
            // jika post list 2 lebih pendek maka ubah post list 1 menjadi postlist 2
            LinkedList<Integer> temp = pL1;
            pL1 = pL2;
            pL2 = temp;
        }
        // iterator
        Iterator<Integer> p1 = pL1.iterator();
        Iterator<Integer> p2 = pL2.iterator();
        // jika iterator sudah sampai ujung maka langsung return
        if (!p1.hasNext() || !p2.hasNext()) {
            return result;
        }
        Integer docId1 = p1.next();
        Integer docId2 = p2.next();

        while (true) {
            // jika doc1 sama dengan doc2 maka tambahkan ke result
            if (docId1.equals(docId2)) {
                result.add(docId1);

                if (!p1.hasNext() || !p2.hasNext()) {
                    break;
                }
                docId1 = p1.next();
                docId2 = p2.next();
            } else if (docId1 < docId2) {
                // jika doc 1 lebih kecil maka next iterator doc 1
                if (!p1.hasNext())
                    break;
                docId1 = p1.next();
            } else {
                // jika doc id 2 lebih kecil maka next iterator doc id 2
                if (!p2.hasNext())
                    break;
                docId2 = p2.next();
            }
        }

        return result;
    }

    public QueryProcess(InvertedIndex invertIndex, String query) {
        this.docInvertedIndex = invertIndex;
        String querySpasi = query.replace("(", " ( ").replace(")", " ) ");
        // parsing query user berdasarkan spasi
        queryParse = querySpasi.trim().split("\\s+");
        // traverse tiap kata hasil parsing query untuk mengubah ke postfix
        for (String term : queryParse) {
            if (term.equals("(")) {
                operator.push(term);
            }
            // jika operator boolean tambah ke stack
            else if (term.equals("AND") || term.equals("OR") || term.equals("NOT")) {
                while (!operator.isEmpty() && getPrecedence(operator.peek()) >= getPrecedence(term)) {
                    postfixQuery.add(operator.pop());
                }
                operator.push(term);
            } else if (term.equals(")")) {
                // jika kurung tutup maka tambahkan operator di stack ke akhir list
                while (!operator.isEmpty() && !operator.peek().equals("(")) {
                    postfixQuery.addLast(operator.pop());
                }
                if (!operator.isEmpty() && operator.peek().equals("(")) {
                    operator.pop();
                }
            } else {
                // jika operand/term biasa maka tambahkan ke list
                postfixQuery.addLast(term);
            }
        }
        // jika masih ada sisa operator di stack maka add ke query postfix
        while (!operator.isEmpty()) {
            postfixQuery.add(operator.pop());
        }
    }

    public LinkedList<Integer> processPostFix() {
        // stack linked list untuk menyimpan postLISt pemrosesan postfix query
        Stack<LinkedList<Integer>> listID = new Stack<>();
        // loop setiap term dan logical operator di postfix
        for (String term : postfixQuery) {
            // jika mengandung wildcard di akhir
            if (term.contains("*")) {
                // buang tanda wildcard * dan ubah term ke lowercase
                String wildcard = term.replace("*", "").toLowerCase();
                // ambil seluruh kata dalam dictionary inverted index
                Set<String> termSet = docInvertedIndex.invertIndex.keySet();
                // tree set untuk menyimpan postlist term wildcard agar langsung terurut
                TreeSet<Integer> postListWild = new TreeSet<>();
                // loop tiap term pada inverted index
                for (String dictTerm : termSet) {
                    // jika term inverted index diawali oleh wildcard
                    if (dictTerm.startsWith(wildcard)) {
                        // masukkan post list term dari inverted index ke treeset
                        LinkedList<Integer> postListTerm = docInvertedIndex.invertIndex.get(dictTerm);
                        postListWild.addAll(postListTerm);
                    }
                }
                // push treeset ke stack of linked list hasil postlist
                listID.push(new LinkedList<>(postListWild));
            } else if (term.equals("AND")) {
                // jika operator AND maka ambil 2 postlist terakhir di stack linked list
                LinkedList<Integer> postList1 = listID.pop();
                LinkedList<Integer> postList2 = listID.pop();
                // intersect kedua postlist dan kembalikan ke stack linked list
                listID.push(intersect(postList1, postList2));
            } else if (term.equals("OR")) {
                // jika operator OR maka ambil 2 postlist terakhir di stack linked list
                LinkedList<Integer> postList1 = listID.pop();
                LinkedList<Integer> postList2 = listID.pop();
                // gabungkan kedua post list
                TreeSet<Integer> unionSet = new TreeSet<>(postList1);
                unionSet.addAll(postList2);
                // kembalikan gabungan kedua post list ke stack linked list
                listID.push(new LinkedList<>(unionSet));
            } else if (term.equals("NOT")) {
                LinkedList<Integer> postList = listID.pop();
                LinkedList<Integer> notResult = new LinkedList<>();

                Iterator<Integer> excludeIt = postList.iterator();
                Integer excludeId = excludeIt.hasNext() ? excludeIt.next() : -1;

                int totalDocs = docInvertedIndex.docList.size();

                for (int i = 0; i < totalDocs; i++) {
                    if (excludeId != -1 && i == excludeId) {
                        excludeId = excludeIt.hasNext() ? excludeIt.next() : -1;
                    } else {
                        notResult.add(i);
                    }
                }
                listID.push(notResult);
            } else {
                // jika term biasa maka ambil post list term di inverted index
                LinkedList<Integer> normalList = docInvertedIndex.invertIndex.get(term.toLowerCase());
                // jika tidak ada di post list maka buat linked list kosong
                // levenhstein distance harusnya
                if (normalList == null) {
                    normalList = new LinkedList<>();
                }
                // push ke stack list id
                listID.push(normalList);
            }
        }
        return listID.isEmpty() ? new LinkedList<>() : listID.pop();

    }

}

public class SearchEngine {
    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        InvertedIndex invertIndex = new InvertedIndex(new File("datasets"));
        invertIndex.buildIndex();
        invertIndex.getPostList();
        String query = input.nextLine();
        QueryProcess process = new QueryProcess(invertIndex, query);
        LinkedList<Integer> result = process.processPostFix();

        System.out.println("Dokumen relevan:");
        if (result.isEmpty()) {
            System.out.println("Tidak ada dokumen yang relevan.");
        } else {
            for (Integer id : result) {
                System.out.println(invertIndex.docList.get(id));
            }
        }
    }
}