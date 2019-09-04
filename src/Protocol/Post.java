package Protocol;

import Protocol.Interaction;

import java.util.Date;

public class Post extends Interaction {
    private final String username;
    private final String content;
    private final Date postDate;
    private final Date expiration;

    public Post(String username, String content, Date expiration) {
        this.username = username;
        this.content = content;
        this.postDate = new Date();
        this.expiration=expiration;
    }

    public Post(String username, String content) {
        this.username = username;
        this.content = content;
        this.postDate = new Date();
        this.expiration=null;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public Date getPostDate() {
        return postDate;
    }

    public Date getExpiration() {
        return expiration;
    }

    public boolean isValid(){
        return (this.postDate.getTime() + 1800000) < new Date().getTime();
    }

    @Override
    public String toString() {
        if(this.expiration!=null) {
            return "User: " + username + ' ' +
                    "\nDate: " + postDate + '\n' +
                    "Available until: " + expiration + '\n' +
                    "Content: " + content + '\n';
        }
        else {
            return "User: " + username + ' ' +
                    "\nDate: " + postDate + '\n' +
                    "Content: " + content + '\n';
        }
    }
}
