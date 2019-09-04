import Protocol.Interaction;
import Protocol.Post;
import Protocol.Subscription;
import Protocol.TimelinePosts;
import javafx.geometry.Pos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Timeline {
    private final String username;
    private ConcurrentHashMap<String, List<Post>> subsPosts;
    private CommunicationImpl<Interaction> comm;
    private boolean inGroup;
    private int acks;

    public Timeline(String username) {
        this.username = username;
        this.subsPosts = new ConcurrentHashMap<String, List<Post>>();
        this.subsPosts.put(this.username, new ArrayList<>());
        this.comm = new CommunicationImpl<>(username,username);
        this.inGroup = false;
        this.acks=0;
        new Thread(this::receiver).start();
    }

    public String getUsername() {
        return username;
    }

    public List<Post> getUserPosts() {
        return this.subsPosts.get(this.username);
    }

    public ConcurrentHashMap<String, List<Post>> getSubsPosts() {
        return subsPosts;
    }

    public List<Post> getPostsByUser(String username){
        return this.subsPosts.get(username);
    }

    private void receiver(){
        while(true){
            Interaction interaction = this.comm.receive();
            if (interaction instanceof Subscription){
                Subscription subscription = (Subscription) interaction;
                this.handleSubscription(subscription);
            }
            else if (interaction instanceof Post) {
                Post post = (Post) interaction;
                this.handlePost(post);
            }
            else if (interaction instanceof TimelinePosts) {
                TimelinePosts timelinePosts = (TimelinePosts) interaction;
                this.handleTimeline(timelinePosts);
            }
        }
    }

    private void handleTimeline(TimelinePosts timelinePosts) {
        if (!timelinePosts.getPosts().isEmpty()){
            List<Post> subposts = subsPosts.getOrDefault(timelinePosts.getPosts().get(0).getUsername(), null);
            subposts.addAll(timelinePosts.getPosts());
            subposts.sort(Comparator.comparing(Post::getPostDate));
            this.acks+=1;
            if(this.acks==timelinePosts.getChunks()) {
                for(Post post : subposts) {
                    System.out.println(post.toString());
                }
                this.acks=0;
            }
        }
    }

    private void handlePost(Post post) {
        List<Post> subposts = subsPosts.getOrDefault(post.getUsername(), new ArrayList<>());
        subposts.add(post);
        System.out.println(post.toString());
    }

    private List<Post> filterPosts(List<Post> posts) {
        List<Post> filteredPosts = new ArrayList<>(posts);
        for(Post p :posts) {
            if(p.getExpiration()!= null && p.getExpiration().before(new Date(System.currentTimeMillis()))) {
                filteredPosts.remove(p);
            }
        }
        return filteredPosts;
    }

    private void handleSubscription(Subscription subscription){
        String groupName = subscription.getGroup();
        List<String> nextView = new ArrayList<>(subscription.getNextView());
        List<String> currentView = new ArrayList<>(subscription.getCurrentView());
        nextView.removeAll(currentView);

        if (this.inGroup && !nextView.isEmpty() && !nextView.contains(this.username)) {
            List<Post> posts = filterPosts(this.subsPosts.get(groupName));
            int rank = currentView.indexOf(this.username);
            if (currentView.size() >= posts.size()) {
                if (rank < posts.size()) {
                    nextView.forEach(e -> this.comm.multicast(posts.get(rank), e));
                }
            } else {
                int numPost = posts.size();
                int offset = numPost / currentView.size();
                int chunks = currentView.size();
                if (rank < chunks - 1) {
                    final List<Post> payload = new ArrayList<>(posts.subList(offset * rank, offset * (rank + 1)));
                    nextView.forEach(e -> this.comm.multicast(new TimelinePosts(payload,chunks), e));
                } else {
                    final List<Post> payload = new ArrayList<>(posts.subList(offset * rank, numPost));
                    nextView.forEach(e -> this.comm.multicast(new TimelinePosts(payload,chunks), e));
                }
            }
            //System.out.println(rank);
            //System.out.println(this.username);
        }
        this.inGroup = true;
    }

    private void list(String username) {
        if(!this.subsPosts.containsKey(username) || this.subsPosts.get(username).size()==0) {
            System.out.println("No posts to list from user " + username);
        }
        else {
            System.out.println("Posts from user " + username + ":");
            List<Post> filteredPosts = new ArrayList<>(this.subsPosts.get(username));
            for(Post post : this.subsPosts.get(username)) {
                if(post.getExpiration()==null || post.getExpiration().after(new Date(System.currentTimeMillis()))) {
                    System.out.println(post.toString());
                }
                else {
                    filteredPosts.remove(post);
                }
            }
            this.subsPosts.put(username,filteredPosts);
        }
    }

    private void cli(){
        Scanner scanner = new Scanner(System.in);
        String input;
        do {
            input = scanner.nextLine();
            if(input.startsWith("sub:"))
            {
                input = input.replaceFirst("sub:","");
                System.out.println("User: " + input + " subscribed");
                this.subsPosts.put(input, new ArrayList<>());
                this.comm.join(input);
            }else if (input.startsWith("unsub:")){
                input = input.replaceFirst("unsub:","");
                this.subsPosts.remove(input);
                this.comm.leave(input);
            }else if (input.startsWith("list:")){
                input = input.replaceFirst("list:","");
                this.list(input);
            }else if (input.startsWith("temp:")) {
                System.out.println("Expiration (segs):");
                input = scanner.nextLine();
                int expiration = Integer.parseInt(input);
                System.out.println("Content:");
                input = scanner.nextLine();
                Post post = new Post(this.username, input,new Date(System.currentTimeMillis()+expiration*1000));
                this.subsPosts.get(this.username).add(post);
                System.out.println("Posted:\n"+post.toString());
                this.comm.multicast(post);
            }else{
                Post post = new Post(this.username, input);
                this.subsPosts.get(this.username).add(post);
                System.out.println("Posted:\n"+post.toString());
                this.comm.multicast(post);
            }
        }while(!input.equals("exit"));
        scanner.close();
    }

    public static void main(String[] args){
        new Timeline(args[0]).cli();
    }
}
