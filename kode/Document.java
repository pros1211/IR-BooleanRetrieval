// kelas untuk menyimpan informasi dokumen
public class Document {
    private int id;
    private String name;

    public Document(int id, String name) {
        this.id = id;
        this.name = name;
    }    

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
}
