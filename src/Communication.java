public interface Communication<T> {
    public void multicast(T message);
    public T receive();
    public void join(String user);
    public void leave(String user);
}
