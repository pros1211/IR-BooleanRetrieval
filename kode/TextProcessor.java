import java.util.*;

public class TextProcessor {
    // method utama untuk menjalankan proses dari string mentah
    // menjadi daftar bersih token
    public static LinkedList<String> tokenize(String text) {
        LinkedList<String> result = new LinkedList<>();
        // ubah teks menjadi huruf kecil semua
        text = text.toLowerCase();
        // pecah menjadi term dan buang karakter selain huruf dan angka
        String[] tokens = text.split("[^a-z0-9]+");
        
        // perulangan setiap token untuk di-stemming
        for (String token : tokens) {
            // pastikan token bukan string kosong
            if (token.isEmpty()) 
                continue;
            
            String stemmedTerm = PorterStemmer.stem(token);
            // masukkan hasil stem yang tidak kosong ke dalam list
            if (stemmedTerm.isEmpty() == false)
                result.add(stemmedTerm);
        }
        
        return result;
    }

    // kelas untuk stemming menggunakan algoritma Porter
    class PorterStemmer {
        public static String stem(String term) {
            // jika term terlalu pendek, biasanya tidak perlu di-stem
            if (term.length() <= 2) 
                return term;

            // Step 1a
            if (term.endsWith("sses")) {
                term = term.substring(0, term.length() - 2);
            } 
            else if (term.endsWith("ies")) {
                term = term.substring(0, term.length() - 2);
            } 
            else if (term.endsWith("ss")) {
                // biarkan tetap ss
            } 
            else if (term.endsWith("s")) {
                term = term.substring(0, term.length() - 1);
            }

            // Step 1b
            boolean step1bDone = false;
            if (term.endsWith("eed")) {
                if (getMeasure(term.substring(0, term.length() - 3)) > 0) {
                    term = term.substring(0, term.length() - 1);
                }
            } 
            else if (term.endsWith("ed")) {
                if (containsVowel(term.substring(0, term.length() - 2))) {
                    term = term.substring(0, term.length() - 2);
                    step1bDone = true;
                }
            } 
            else if (term.endsWith("ing")) {
                if (containsVowel(term.substring(0, term.length() - 3))) {
                    term = term.substring(0, term.length() - 3);
                    step1bDone = true;
                }
            }

            // penyesuaian lanjutan untuk step 1b
            if (step1bDone) {
                if (term.endsWith("at")) 
                    term = term + "e";
                else if (term.endsWith("bl")) 
                    term = term + "e";
                else if (term.endsWith("iz")) 
                    term = term + "e";
                else if (endsWithDoubleConsonant(term) && !(term.endsWith("l") || term.endsWith("s") || term.endsWith("z"))) {
                    term = term.substring(0, term.length() - 1);
                } else if (getMeasure(term) == 1 && endsWithCVC(term)) {
                    term = term + "e";
                }
            }

            // Step 1c
            if (term.endsWith("y") && containsVowel(term.substring(0, term.length() - 1))) {
                term = term.substring(0, term.length() - 1) + "i";
            }

            return term;
        }

        // method untuk menyatakan apakah huruf vokal atau bukan
        private static boolean isVowel(char c) {
            return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
        }

        // method untuk menyatakan apakah mengandung huruf vokal
        private static boolean containsVowel(String str) {
            for (char c : str.toCharArray())
                if (isVowel(c)) 
                    return true;
            return false;
        }

        // method untuk menyatakan apakah berakhiran konsonan ganda
        private static boolean endsWithDoubleConsonant(String str) {
            // sesuai aturan di mana panjang term tidak kurang dari 2 huruf
            if (str.length() < 2) 
                return false;

            char c1 = str.charAt(str.length() - 1);
            char c2 = str.charAt(str.length() - 2);

            return c1 == c2 && !isVowel(c1);
        }

        // method untuk menyatakan apakah berakhiran konsonan-vokal-konsonan
        private static boolean endsWithCVC(String str) {
            // sesuai aturan di mana panjang term tidak kurang dari 3 huruf
            if (str.length() < 3) 
                return false;

            char c1 = str.charAt(str.length() - 3);
            char c2 = str.charAt(str.length() - 2);
            char c3 = str.charAt(str.length() - 1);
            return !isVowel(c1) && isVowel(c2) && !isVowel(c3) && c3 != 'w' && c3 != 'x' && c3 != 'y';
        }

        // menghitung berapa kali pola vokal-konsonan diulang
        private static int getMeasure(String str) {
            int m = 0;
            boolean lastWasVowel = false;

            for (int i = 0; i < str.length(); i++) {
                boolean currentIsVowel = isVowel(str.charAt(i));
                // jika sebelumnya vokal dan sekarang konsonan, hitung sebagai 1 blok VC
                if (lastWasVowel && !currentIsVowel)
                    m++;
                lastWasVowel = currentIsVowel;
            }

            return m;
        }
    }
}