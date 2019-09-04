package Protocol;

import java.util.List;

public class TimelinePosts extends Interaction {
    private List<Post> posts;
    private int chunks;

    public TimelinePosts(List<Post> posts, int chunks) {
        this.posts = posts;
        this.chunks = chunks;
    }

    public int getChunks() {
        return chunks;
    }

    public List<Post> getPosts() {
        return posts;
    }
}
