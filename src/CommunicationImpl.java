import Protocol.SerializerProtocol;
import Protocol.Subscription;
import io.atomix.utils.serializer.Serializer;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadMessage;

import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.System.exit;

public class CommunicationImpl<T> implements Communication<T> {
    private SpreadConnection connection;
    private Serializer serializer;
    private ConcurrentHashMap<String,Group> groups;
    private String defaultGroupName;


    public CommunicationImpl(String defaultGroupName, String connectionName) {
        this.defaultGroupName = defaultGroupName;
        this.connection = new SpreadConnection();
        Group group = new Group(this.connection);
        this.groups = new ConcurrentHashMap<>();
        this.serializer = SerializerProtocol.newSerializer();
        try {
            this.connection.connect(InetAddress.getByName("localhost") , 4803, connectionName, false, true);
            group.join(defaultGroupName);
            if (group != null) {
                this.groups.put(defaultGroupName, group);
            }
        } catch (SpreadException | UnknownHostException e) {
            e.printStackTrace();
            exit(-1);
        }
    }

    public void multicast(T data) {
        multicastImpl(data, this.defaultGroupName);
    }

    public void multicast(T data, String groupName) {
        multicastImpl(data, groupName);
    }

    private void multicastImpl(T data, String sendGroup) {
        byte[] packet = this.serializer.encode(data);
        SpreadMessage message = new SpreadMessage();
        message.setData(packet);
        message.setAgreed();
        message.setReliable();
        message.setSelfDiscard(true);
        message.addGroup(sendGroup);
        try {
            this.connection.multicast(message);
        } catch (SpreadException e) {
            System.out.println();
            e.printStackTrace();
        }
    }


    @Override
    public T receive() {
        SpreadMessage message = null;
        try {
            message = this.connection.receive();
        } catch (SpreadException e) {
            System.out.println("Spread SpreadExc in receive");
            e.printStackTrace();
        } catch (InterruptedIOException e) {
            System.out.println("Spread IIOExc in receive");
            e.printStackTrace();
        }
        if(message.isRegular()) {
            T protocolMessage = serializer.decode(message.getData());
            return protocolMessage;
        }else {
            String nameGroup = message.getSender().toString();
            Group  group = this.groups.get(nameGroup);
            if (group != null) {
                List<String> view = Arrays
                        .stream(message.getMembershipInfo().getMembers())
                        .map(name -> name.toString().split("#")[1])
                        .collect(Collectors.toList());
                T messages = (T) new Subscription(view, new ArrayList<>(group.getView()), nameGroup);
                group.setView(view);
                return messages;
            }
            else
                return null;
        }
    }


    @Override
    public void join(String groupName) {
        Group group = new Group(this.connection);
        group.join(groupName);
        if (group != null) {
            this.groups.put(groupName, group);
        }
    }

    @Override
    public void leave(String groupName) {
        if (this.groups.containsKey(groupName))
            this.groups.get(groupName).leave();
            this.groups.remove(groupName);
        }




}
