package clarity.internship.backend_api.models;

public class DataAnalyzer {
    String text;

    public DataAnalyzer(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
