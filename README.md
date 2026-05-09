# 🔍 Java Boolean Retrieval Engine

Sebuah mesin pencari (_Search Engine_) berbasis teks yang mengimplementasikan **Inverted Index** dan evaluasi **Boolean Logic**. Mesin ini mampu memproses _query_ kompleks yang melibatkan operator `AND`, `OR`, `NOT`, `(`, `)`, serta fitur pencarian _wildcard_.

## ✨ Fitur Utama

- **Inverted Indexing:** Membangun _dictionary_ yang memetakan setiap _term_ ke dalam daftar  dokumen (_Posting List_) secara efisien menggunakan struktur data **Trie**.
- **Boolean Query Evaluation:**
  - Mendukung operator logika dasar: `AND` (Irisan), `OR` (Gabungan), dan `NOT` (Komplemen).
  - Setiap query input dikonversi dari bentuk _infix_ menjadi bentuk _postfix_, untuk penanganan tanda kurung `()` dan memudahkan prioritas operator logika.
- **Wildcard Search:** Mendukung operator `*` di awal, tengah, atau akhir term (contoh: `comput*`, `*uter`, `*mpu*`)
- **Case-Insensitive:** Memproses _query_ dan dokumen tanpa mempedulikan huruf besar/kecil.
- **Porter Stemmer:** ...

## 🛠️ Prasyarat

- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) versi 8 atau yang lebih baru.
- Direktori berisi kumpulan dokumen teks (`.txt`) yang akan diindeks.

## 🚀 Cara Menjalankan Program

1.  **Clone Repository ini:**
    ```bash
    git clone [https://github.com/pros1211/IR-BooleanRetrieval.git]
    ```
2. **Jalankan program `Main.java`**