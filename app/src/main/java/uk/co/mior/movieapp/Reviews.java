package uk.co.mior.movieapp;

/**
 * Represents movie reviews
 */
public class Reviews {
    private final String author;
    private final String content;

    public Reviews(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
