import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.valueOf;
import static java.time.LocalDateTime.now;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
public class Node {
    private final int i;
    private final int N;
    private final LocalDateTime timeToStop;
    private final double newMsgProbability;

    private final Network.RequestTemplate ingress;
    private final Network.RequestTemplate egress;
    private final Logger logger;

    public void run() {
        while (now().isBefore(timeToStop)) {
            tryReceive();
            if (ThreadLocalRandom.current().nextDouble() < newMsgProbability) sendNewMessage();
        }
    }

    private void tryReceive() {
        ofNullable(ingress.tryReceive()).ifPresent(this::processMsg);
    }

    private void processMsg(Message msg) {
        if (msg.getDst() == i) {
            msg.setReceivedAt(LocalDateTime.now());
            logger.registerReceived(msg);
        } else {
            sendToNext(msg);
        }
    }

    private void sendNewMessage() {
        int to;
        do {
            to = ThreadLocalRandom.current().nextInt(N);
        }
        while (to == i);
        var msg = new Message(valueOf(ThreadLocalRandom.current().nextLong()), i, to, now());
        sendToNext(msg);
        logger.registerSent(msg);
    }

    private void sendToNext(Message msg) {
        egress.send(msg);
    }
}
