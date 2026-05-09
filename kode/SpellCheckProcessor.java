import java.util.*;

public class SpellCheckProcessor {
    private static final int MINIMUM_DISTANCE = 2;

    // method untuk mendapatkan value minimum edit distance
    private static int getEditDistance(String s1, String s2) {
        // membuat tabel
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        // memasukkan karakter untuk kedua string
        for (int i = 0; i <= s1.length(); i++) 
            dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) 
            dp[0][j] = j;

        // mencari distance
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                // jika ada kesamaan karakter pada index yang sama 
                if (s1.charAt(i - 1) == s2.charAt(j - 1))
                    // do nothing
                    dp[i][j] = dp[i - 1][j - 1];
                // sebaliknya, ambil terminimum di antara:
                // kiri, kiri-atas, atau atas
                else 
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }

        // kembalikan nilai minimum edit distance
        return dp[s1.length()][s2.length()];
    }

    // method untuk mendapatkan daftar dokumen jika terdeteksi adanya typo
    public static TermEntry getSpellingCorrection(String term, LinkedList<TermEntry> allTerms) {
        String closestTerm = "";
        LinkedList<Document> result = new LinkedList<>();
        int minimumDistance = Integer.MAX_VALUE;

        for (TermEntry entry : allTerms) {
            int distance = getEditDistance(term, entry.getTerm());
            // jika jarak lebih kecil dari minimum distance
            if (distance < minimumDistance) {
                // masukkan ke dalam hasil
                result = entry.getPostingList();
                // kemudian update term terdekatnya itu
                closestTerm = entry.getTerm();
                // beserta nilai minimum distancenya
                minimumDistance = distance;
            }
        }

        // jika minimum distance lebih kecil dari konstanta
        if (minimumDistance <= MINIMUM_DISTANCE) {
            // maka kembalikan daftar dokumen yang terdekat
            return new TermEntry(closestTerm, result);
        }
        else {
            return null;
        }
    }

}
