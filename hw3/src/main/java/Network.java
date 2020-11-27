import org.jctools.queues.atomic.SpscLinkedAtomicQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.lang.String.format;
import static java.util.Map.of;

public class Network {
    private final int N;
    private final Map<Integer, Map<Integer, SpscLinkedAtomicQueue<Message>>> channels;

    public Network(int n) {
        N = n;
        channels = new HashMap<>();
        for (int i = 0; i < N; i++) {
            channels.put(i, circularLinksForNode(i));
        }
    }

    private Map<Integer, SpscLinkedAtomicQueue<Message>> circularLinksForNode(int i) {
        return of((i + 1) % N, new SpscLinkedAtomicQueue<>());
    }

    public RequestFactory from(final int source) {
        if (source < 0 || source >= N)
            throw new IllegalArgumentException(format("src must be between 0 and %d, got %d", N, source));
        return destination -> {
            if (destination < 0 || destination >= N || !channels.get(source).containsKey(destination))
                throw new IllegalArgumentException(format("dst must be a valid address between 0 and %d, got %d", N, destination));

            return new RequestTemplate() {
                @Override
                public void send(Message msg) {
                    channels.get(source).get(destination).offer(msg);
                }

                @Override
                public Message tryReceive() {
                    try {
                        return channels.get(source).get(destination).remove();
                    } catch (NoSuchElementException e) {
                        return null;
                    }
                }
            };
        };
    }

    public interface RequestFactory {
        RequestTemplate to(int destination);
    }

    public interface RequestTemplate {
        void send(Message msg);

        Message tryReceive();
    }
}
