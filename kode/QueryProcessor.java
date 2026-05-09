import java.util.*;

public class QueryProcessor {
    private InvertedIndex invertedIndex;
    private List<Document> allDocuments; 

    public QueryProcessor(InvertedIndex invertIndex, List<Document> allDocuments) {
        this.invertedIndex = invertIndex;
        this.allDocuments = allDocuments;
    }

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
        if (!p1.hasNext() || !p2.hasNext()) return result;
        
        Document doc1 = p1.next();
        Document doc2 = p2.next();

        while (true) {
            if (doc1.getId() == doc2.getId()) {
                result.add(doc1);
                if (!p1.hasNext() || !p2.hasNext()) 
                    break;
                doc1 = p1.next();
                doc2 = p2.next();
            } 
            else if (doc1.getId() < doc2.getId()) {
                if (!p1.hasNext()) 
                    break;
                doc1 = p1.next();
            } 
            else {
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
                } 
                else if (doc1.getId() < doc2.getId()) {
                    result.add(doc1);
                    doc1 = p1.hasNext() ? p1.next() : null;
                } 
                else {
                    result.add(doc2);
                    doc2 = p2.hasNext() ? p2.next() : null;
                }
            } 
            else if (doc1 != null) {
                result.add(doc1);
                doc1 = p1.hasNext() ? p1.next() : null;
            } 
            else {
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
        LinkedList<TermEntry> allTerms = invertedIndex.getAllPostingLists();

        // operasi untuk setiap term dalam postfix query
        for (String term : postfixQuery) {
            // jika berupa wildcard
            if (term.contains("*")) {
                String wildcard = term.replace("*", "").toLowerCase();
                LinkedList<Document> postListWild = new LinkedList<>();
                
                // untuk setiap term, lakukan penggabungan
                for (TermEntry entry : allTerms) {
                    if (entry.getTerm().startsWith(wildcard)) {
                        postListWild = union(postListWild, entry.getPostingList());
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
                LinkedList<String> tokenizedTerms = TextProcessor.tokenize(term);
                // ambil hasil tokenisasi
                String tokenizedTerm = tokenizedTerms.getFirst();
                // pastikan hasil tokenisasi tidak kosong
                if (tokenizedTerm.isEmpty())
                    // jika kosong, maka ambil term aslinya saja
                    tokenizedTerm = term;
                
                LinkedList<Document> normalList = invertedIndex.getTrie().getPostingList(tokenizedTerm);

                // jika tidak ada di dalam dictionary
                if (normalList == null || normalList.isEmpty())
                    // cek kemungkinan kesalahan ketik
                    normalList = SpellCheckProcessor.getSpellingCorrection(tokenizedTerm, allTerms);

                listId.push(normalList);
            }
        }

        // ambil hasil daftar pencarian yang terbaik
        return listId.isEmpty() ? new LinkedList<>() : listId.pop();
    }
}