import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        
        InvertedIndex dictionary = new InvertedIndex(new File("datasets"));
        dictionary.buildIndex();
        dictionary.printAllPostingLists();
        
        System.out.print("\nQuery: ");
        String query = input.nextLine();

        QueryProcessor queryProcessor = new QueryProcessor(dictionary, dictionary.getAllDocuments());
        LinkedList<Document> result = queryProcessor.retreiveDocuments(query);

        System.out.println("\nDokumen yang cocok:");
        if (result == null || result.isEmpty()) {
            System.out.println("Tidak ada dokumen yang cocok.");
        } 
        else {
            for (Document document : result) {
                // tampilkan nama dokumen
                System.out.print(document.getName() + " -> ");
                // beserta term-term yang membuatnya dianggap cocok
                LinkedList<String> highlightedTerms = queryProcessor.getTermsInDocument(document);
                for (String term : highlightedTerms)
                    System.out.print("[" + term + "] ");
                System.out.println();
            }
        }
        input.close();
    }
}