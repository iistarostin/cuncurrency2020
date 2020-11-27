import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {
    private final String payload;
    private final int src;
    private final int dst;
    private final LocalDateTime sentAt;
    private LocalDateTime receivedAt;
}
