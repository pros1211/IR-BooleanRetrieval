import java.util.*;

public class QueryProcessor {
    private InvertedIndex dictionary;
    private List<Document> allDocuments;
    // Wadah untuk menyimpan daftar term yang ter-highlight di pencarian
    private Set<String> matchedTerms = new LinkedHashSet<>();

    public QueryProcessor(InvertedIndex dictionary, List<Document> allDocuments) {
        this.dictionary = dictionary;
        this.allDocuments = allDocuments;
    }

    // getter
    public Set<String> getMatchedTerms() {
        return matchedTerms;
    }

    // untuk urutan prioritas operator
    private int getPrecedence(String op) {
        if (op.equals("NOT"))
            return 3;
        if (op.equals("AND"))
            return 2;
        if (op.equals("OR"))
            return 1;
        return 0;
    }

    public LinkedList<Document> intersect(LinkedList<Document> pL1, LinkedList<Document> pL2) {
        LinkedList<Document> result = new LinkedList<>();
        if (pL1 == null || pL2 == null)
            return result;

        if (pL1.size() > pL2.size()) {
            LinkedList<Document> temp = pL1;
            pL1 = pL2;
            pL2 = temp;
        }

        Iterator<Document> p1 = pL1.iterator();
        Iterator<Document> p2 = pL2.iterator();
        if (!p1.hasNext() || !p2.hasNext())
            return result;

        Document doc1 = p1.next();
        Document doc2 = p2.next();

        while (true) {
            if (doc1.getId() == doc2.getId()) {
                result.add(doc1);
                if (!p1.hasNext() || !p2.hasNext())
                    break;
                doc1 = p1.next();
                doc2 = p2.next();
            } else if (doc1.getId() < doc2.getId()) {
                if (!p1.hasNext())
                    break;
                doc1 = p1.next();
            } else {
                if (!p2.hasNext())
                    break;
                doc2 = p2.next();
            }
        }
        return result;
    }

    public LinkedList<Document> union(LinkedList<Document> pL1, LinkedList<Document> pL2) {
        LinkedList<Document> result = new LinkedList<>();
        if (pL1 == null)
            return pL2 == null ? result : pL2;
        if (pL2 == null)
            return pL1;

        Iterator<Document> p1 = pL1.iterator();
        Iterator<Document> p2 = pL2.iterator();
        Document doc1 = p1.hasNext() ? p1.next() : null;
        Document doc2 = p2.hasNext() ? p2.next() : null;

        while (doc1 != null || doc2 != null) {
            if (doc1 != null && doc2 != null) {
                if (doc1.getId() == doc2.getId()) {
                    result.add(doc1);
                    doc1 = p1.hasNext() ? p1.next() : null;
                    doc2 = p2.hasNext() ? p2.next() : null;
                } else if (doc1.getId() < doc2.getId()) {
                    result.add(doc1);
                    doc1 = p1.hasNext() ? p1.next() : null;
                } else {
                    result.add(doc2);
                    doc2 = p2.hasNext() ? p2.next() : null;
                }
            } else if (doc1 != null) {
                result.add(doc1);
                doc1 = p1.hasNext() ? p1.next() : null;
            } else {
                result.add(doc2);
                doc2 = p2.hasNext() ? p2.next() : null;
            }
        }
        return result;
    }

    public LinkedList<Document> not(LinkedList<Document> pL) {
        LinkedList<Document> result = new LinkedList<>();
        if (pL == null)
            return new LinkedList<>(allDocuments);

        Iterator<Document> p = pL.iterator();
        Document excludeDoc = p.hasNext() ? p.next() : null;

        for (Document doc : allDocuments) {
            if (excludeDoc != null && doc.getId() == excludeDoc.getId())
                excludeDoc = p.hasNext() ? p.next() : null;
            else
                result.add(doc);
        }
        return result;
    }

    public LinkedList<Document> retreiveDocuments(String query) {
        // kosongkan set setiap kali query dipanggil ulang
        matchedTerms.clear();

        // operator ( dan ) diberikan spasi karena termasuk operator
        query = query
                .replace("(", " ( ")
                .replace(")", " ) ");
        String[] queries = query.trim().split("\\s+");

        List<String> postfixQuery = new ArrayList<>();
        Stack<String> operator = new Stack<>();

        // sekarang pecah string per spasi untuk melihat operan dan operator
        for (String term : queries) {
            // untuk operator (
            if (term.equals("(")) {
                operator.push(term);
            }
            // untuk operator AND, OR, atau NOT
            else if (term.equals("AND") || term.equals("OR") || term.equals("NOT")) {
                // perulangan untuk mengurutkan urutan operator
                while (operator.isEmpty() == false && getPrecedence(operator.peek()) >= getPrecedence(term)) {
                    postfixQuery.add(operator.pop());
                }
                operator.push(term);
            }
            // untuk operator )
            else if (term.equals(")")) {
                // perulangan untuk mengurutkan urutan ()
                while (operator.isEmpty() == false && operator.peek().equals("(") == false) {
                    postfixQuery.add(operator.pop());
                }
                if (operator.isEmpty() == false && operator.peek().equals("(")) {
                    operator.pop();
                }
            }
            // untuk non-operator, alias term
            else {
                postfixQuery.add(term);
            }
        }

        // jika masih ada sisa di dalam operator, segera pindahkan ke postfix query
        while (operator.isEmpty() == false)
            postfixQuery.add(operator.pop());

        Stack<LinkedList<Document>> listId = new Stack<>();
        LinkedList<TermEntry> allTerms = dictionary.getAllPostingLists();

        // operasi untuk setiap term dalam postfix query
        for (String term : postfixQuery) {
            // jika berupa wildcard
            if (term.contains("*")) {
                // ubah tanda wildcard menjadi regex .*
                String wildcard = term.replace("*", ".*").toLowerCase();
                LinkedList<Document> postListWild = new LinkedList<>();
                // ambil terms tanpa stem dari raw Trie
                LinkedList<TermEntry> rawTerms = dictionary.getRawTrie().getAllTerms();
                // untuk setiap term, lakukan penggabungan
                for (TermEntry entry : rawTerms) {
                    if (entry.getTerm().matches(wildcard)) {
                        postListWild = union(postListWild, entry.getPostingList());
                        // catat semua term yang match dengan pola wildcard
                        matchedTerms.add(entry.getTerm());
                    }
                }
                listId.push(postListWild);

            }
            // jika berupa AND, maka lakukan intersection
            else if (term.equals("AND")) {
                LinkedList<Document> postList1 = listId.pop();
                LinkedList<Document> postList2 = listId.pop();
                listId.push(intersect(postList1, postList2));

            }
            // jika berupa OR, maka lakukan union
            else if (term.equals("OR")) {
                LinkedList<Document> postList1 = listId.pop();
                LinkedList<Document> postList2 = listId.pop();
                listId.push(union(postList1, postList2));

            }
            // jika berupa NOT, maka harus baca seluruh daftar dokumen
            else if (term.equals("NOT")) {
                LinkedList<Document> postList = listId.pop();
                listId.push(not(postList));

            }
            // selebihnya lakukan tokenisasi
            else {
                // pasti banyak elemen 0..1, karena input term hanya satu
                LinkedList<String> tokenizedTerms = TextProcessor.tokenizeStem(term);
                // ambil hasil tokenisasi
                String tokenizedTerm = tokenizedTerms.getFirst();
                // pastikan hasil tokenisasi tidak kosong
                if (tokenizedTerm.isEmpty())
                    // jika kosong, maka ambil term aslinya saja
                    tokenizedTerm = term;

                LinkedList<Document> normalList = dictionary.getStemTrie().getPostingList(tokenizedTerm);

                // jika tidak ada di dalam dictionary
                if (normalList == null || normalList.isEmpty()) {
                    System.out.print("Tidak ada term '" + term + "' di dictionary. ");
                    // cek kemungkinan kesalahan ketik
                    TermEntry correctedTerm = SpellCheckProcessor.getSpellingCorrection(tokenizedTerm, allTerms);
                    // jika ya
                    if (correctedTerm != null) {
                        // maka tampilkan term yang paling terdekat
                        System.out.println("Menampilkan dokumen dengan '" + correctedTerm.getTerm() + "'");
                        normalList = correctedTerm.getPostingList();
                        // catat term hasil koreksi typo
                        matchedTerms.add(correctedTerm.getTerm());
                    } else {
                        System.out.println();
                    }
                }
                // jika ada
                else {
                    matchedTerms.add(tokenizedTerm);
                }

                listId.push(normalList);
            }
        }

        // ambil hasil daftar pencarian yang terbaik
        return listId.isEmpty() ? new LinkedList<>() : listId.pop();
    }

    // method untuk mencari term apa saja yang ada di dalem spesifik dokumen
    public LinkedList<String> getTermsInDocument(Document document) {
        LinkedList<String> result = new LinkedList<>();
        // perulangan term yang tampil di hasil pencarian
        for (String term : matchedTerms) {
            LinkedList<Document> postingList = dictionary.getStemTrie().getPostingList(term);
            // jika posting list untuk term kosong pada Trie berisi hasil stem maka itu
            // adalah term asli tanpa stem
            if (postingList == null || postingList.isEmpty()) {
                // ambil posting list term tanpa stem
                postingList = dictionary.getRawTrie().getPostingList(term);
            }
            if (postingList != null) {
                for (Document doc : postingList) {
                    if (doc.getId() == document.getId()) {
                        // ambil dokumen yang tampil di hasil pencarian
                        // di mana juga dianggap cocok dengan query
                        result.add(term);
                        break;
                    }
                }
            }
        }
        return result;
    }
}