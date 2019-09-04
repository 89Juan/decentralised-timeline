package Protocol;

import io.atomix.utils.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;

public class SerializerProtocol {
    public static Serializer newSerializer() {
        return Serializer.builder().withCompatibleSerialization()
                .withTypes(
                        Post.class,
                        Subscription.class,
                        Interaction.class,
                        ArrayList.class,
                        List.class,
                        TimelinePosts.class,
                        java.util.Date.class
                )
                .build();
    }
}
