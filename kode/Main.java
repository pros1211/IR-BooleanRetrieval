import java.io.*;
import java.util.*;

public class Main {
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
            for (Integer id : result)
                System.out.println(invertIndex.docList.get(id));
        }
        input.close();
    }
}