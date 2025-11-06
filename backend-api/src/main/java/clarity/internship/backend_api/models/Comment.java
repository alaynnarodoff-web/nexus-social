package clarity.internship.backend_api.models;

import java.time.Instant;

public class Comment {

    private String author;
    private String authorAvatar;
    private String text;
    private Instant timestamp;

    public Comment() {
    }

    public Comment(String author, String text) {
        this.author = author;
        this.text = text;
        this.timestamp = Instant.now();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
