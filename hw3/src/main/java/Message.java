import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
public class Message {
    private final String payload;
    private final int src;
    private final int dst;
    private final LocalDateTime sentAt;
    private LocalDateTime receivedAt;
}
