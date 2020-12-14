import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.valueOf;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class Node {
    private final int i;
    private final int N;
    private final LocalDateTime timeToStop;
    private final double newMsgProbability;

    private final Network.RequestTemplate ingress;
    private final Network.RequestTemplate egress;
    @Getter
    private final Logger logger;

    public void run() {
        while (now().isBefore(timeToStop)) {
            var nProcessed = tryReceive();
            //in order to preserve the expected total number of messages sent compared to previous models,
            //after processing a node gets a chance to send new msg for each processed msg
            while (nProcessed-- >= 0) {
                if (ThreadLocalRandom.current().nextDouble() < newMsgProbability) sendNewMessage();
            }
        }
    }

    private int tryReceive() {
        int nProcessed = 0;
        Message next;
        while (nonNull(next = ingress.tryReceive())) {
            nProcessed++;
            processMsg(next);
        }
        return nProcessed;
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
