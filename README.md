# 🔍 Java Boolean Retrieval Engine

Sebuah mesin pencari (_Search Engine_) berbasis teks yang mengimplementasikan **Inverted Index** dan evaluasi **Boolean Logic**. Mesin ini mampu memproses _query_ kompleks yang melibatkan operator `AND`, `OR`, `NOT`, tanda kurung, serta fitur pencarian _wildcard_.

## ✨ Fitur Utama

- **Inverted Indexing:** Membangun kamus (_dictionary_) yang memetakan setiap kata ke dalam daftar ID dokumen (_Posting List_) secara efisien.
- **Boolean Query Evaluation:**
  - Mendukung operator logika dasar: `AND` (Irisan), `OR` (Gabungan), dan `NOT` (Komplemen).
  - setiap query input dikonversi dari bentuk _infix_ menjadi bentuk _Postfix_, untuk penanganan tanda kurung `()` dan memudahkan prioritas operator logika.
- **Wildcard Search:** Mendukung operator `*` di akhir kata (contoh: `comput*`)
- **Case-Insensitive:** Memproses _query_ dan dokumen tanpa mempedulikan huruf besar/kecil.

## 🛠️ Prasyarat (Prerequisites)

- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) versi 8 atau yang lebih baru.
- Direktori berisi kumpulan dokumen teks (`.txt`) yang akan diindeks.

## 🚀 Cara Menjalankan Program (How to Run)

1.  **Clone Repository ini:**
    ```bash
    git clone [https://github.com/pros1211/IR-BooleanRetrieval.git]
    ```
