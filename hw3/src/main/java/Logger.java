import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

import static java.lang.String.*;
import static java.time.Duration.between;
import static java.util.List.of;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class Logger {
    private final int n;
    private final double secondsToRun;
    private long meessagesSent = 0;
    private long messagesReceived = 0;
    private long totalDelay = 0;

    private long ticks = 0;
    private long totalSentCounter = 0;
    private long totalReceivedCounter = 0;

    public void registerSent(Message msg) {
        //System.out.printf("%d sending message %s to %d\n", msg.getSrc(), msg.getPayload(), msg.getDst());
        ++ticks;
        totalSentCounter += ++meessagesSent;
    }

    public void registerReceived(Message msg) {
        //System.out.printf("%d got message %s from %d\n", msg.getDst(), msg.getPayload(), msg.getSrc());
        ++ticks;
        totalReceivedCounter += ++messagesReceived;
        totalDelay += between(msg.getSentAt(), msg.getReceivedAt()).toNanos();
    }

    public static Logger joint(List<Logger> threadLoggers) {
        return new Logger(
                threadLoggers.get(0).n,
                threadLoggers.get(0).secondsToRun,
                threadLoggers.stream().mapToLong(Logger::getMeessagesSent).sum(),
                threadLoggers.stream().mapToLong(Logger::getMeessagesSent).sum(),
                threadLoggers.stream().mapToLong(Logger::getTotalDelay).sum(),
                (long) threadLoggers.stream().mapToLong(Logger::getTicks).sum(),
                threadLoggers.stream().mapToLong(Logger::getTotalSentCounter).sum(),
                threadLoggers.stream().mapToLong(Logger::getTotalReceivedCounter).sum()
        );
    }

    public Logs detach() {
        return new Logs(
                n,
                secondsToRun,
                meessagesSent,
                messagesReceived,
                meessagesSent - messagesReceived,
                (meessagesSent - messagesReceived) * 1.0 / meessagesSent,
                totalDelay * 1.0 / messagesReceived,
                (totalSentCounter - totalReceivedCounter) * 1.0 / ticks
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
