import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        
        InvertedIndex invertedIndex = new InvertedIndex(new File("datasets"));
        invertedIndex.buildIndex();
        invertedIndex.printAllPostingLists();
        
        System.out.print("\nMasukkan query: ");
        String query = input.nextLine();

        QueryProcessor queryProcessor = new QueryProcessor(invertedIndex, invertedIndex.getAllDocuments());
        LinkedList<Document> result = queryProcessor.retreiveDocuments(query);

        System.out.println("\nDokumen relevan:");
        if (result.isEmpty()) {
            System.out.println("Tidak ada dokumen yang relevan.");
        } 
        else {
            for (Document document : result)
                System.out.println(document.getName());
        }
        input.close();
    }
}