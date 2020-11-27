import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.*;
import static java.time.Duration.between;
import static java.util.List.of;

@RequiredArgsConstructor
public class Logger {
    private final int n;
    private final double secondsToRun;
    private final AtomicLong totalMessagesSent = new AtomicLong(0);
    private final AtomicLong totalMessagesReceived = new AtomicLong(0);
    private final AtomicLong totalDelay = new AtomicLong(0);

    private final AtomicLong ticks = new AtomicLong(0);
    private final AtomicLong totalSentTimestamp = new AtomicLong(0);
    private final AtomicLong totalReceivedTimestamp = new AtomicLong(0);

    public void registerSent(Message msg) {
        //System.out.printf("%d sending message %s to %d\n", msg.getSrc(), msg.getPayload(), msg.getDst());
        ticks.getAndIncrement();
        totalSentTimestamp.getAndAdd(totalMessagesSent.incrementAndGet());
    }

    public void registerReceived(Message msg) {
        //System.out.printf("%d got message %s from %d\n", msg.getDst(), msg.getPayload(), msg.getSrc());
        ticks.getAndIncrement();
        totalReceivedTimestamp.getAndAdd(totalMessagesReceived.incrementAndGet());
        totalDelay.getAndAdd(between(msg.getSentAt(), msg.getReceivedAt()).toNanos());
    }

    public Logs detach() {
        return new Logs(
                n,
                secondsToRun,
                totalMessagesSent.get(),
                totalMessagesReceived.get(),
                totalMessagesSent.get() - totalMessagesReceived.get(),
                (totalMessagesSent.get() - totalMessagesReceived.get()) * 1.0 / totalMessagesSent.get(),
                totalDelay.get() * 1.0 / totalMessagesReceived.get(),
                (totalSentTimestamp.get() - totalReceivedTimestamp.get()) * 1.0 / ticks.get()
        );
    }

    @Value
    public class Logs {
        int n;
        double secondsToRun;
        long sent;
        long received;
        long lost;
        double lostPercent;
        double avgDelay;
        double avgInTransit;

        public String prettyPrint() {
            return format(
                    "N: \t\t\t\t%d\n" +
                    "Sent:\t\t\t%d\n" +
                            "Received:\t\t%d\n" +
                            "Lost:\t\t\t%d (%f %%)\n" +
                            "Avg. delay:\t\t%f microseconds\n" +
                            "Avg in transit:\t%f\n",
                    getN(),
                    getSent(),
                    getReceived(),
                    getLost(),
                    getLostPercent(),
                    getAvgDelay(),
                    getAvgInTransit()
            );
        }

        public String toCsvRow() {
            return join(",", of(
                    valueOf(getN()),
                    valueOf(getAvgInTransit()),
                    valueOf(getReceived() * 1.0 / secondsToRun),
                    valueOf(getAvgDelay())));
        }
    }
}
