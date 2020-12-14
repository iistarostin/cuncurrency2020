import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.Math.pow;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class App {
    static final int secondsToRun = 10;

    public static void main(String[] args) throws FileNotFoundException {
        File benchmark = new File("benchmark_4.csv");
        try (PrintWriter pw = new PrintWriter(benchmark)) {
            pw.print(
                    benchmark()
                            .peek(log -> System.out.println(log.prettyPrint()))
                            .map(Logger.Logs::toCsvRow)
                            .collect(joining("\n")));
        }
    }

    private static Stream<Logger.Logs> benchmark() {
        return range(2, 20).boxed().flatMap(n ->
                range(-4, -1)
                        .mapToObj(p -> pow(10.0, p))
                        .map(p -> {
                            try {
                                return test(n, secondsToRun, p).detach();
                            } catch (InterruptedException ignored) {
                                return null;
                            }
                        }))
                .filter(Objects::nonNull);
    }

    private static Logger test(final int N, final int secondsToRun, final double newMsgProbability) throws InterruptedException {
        final Network network = new Network(N);
        var nodes = range(0, N)
                .mapToObj(i -> new Node(
                        i,
                        N,
                        now().plusSeconds(secondsToRun),
                        newMsgProbability,
                        network.from((N + i - 1) % N).to(i),
                        network.from(i).to((i + 1) % N),
                        new Logger(N, secondsToRun)
                )).collect(toList());
        var threads = nodes.stream()
                .map(node -> new Thread(node::run))
                .peek(Thread::start)
                .collect(toList());
        for (Thread thread : threads) {
            thread.join();
        }
        return Logger.joint(nodes.stream().map(Node::getLogger).collect(toList()));
    }
}
