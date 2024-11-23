package hhplus.ecommerce.server.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_outboxes")
@Entity
public class OrderOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topicName;
    private String transactionKey;
    private String originalMessage;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(31)")
    private OrderOutboxStatus status;
    @Column(columnDefinition = "TEXT")
    private String reason;

    private LocalDateTime createdDateTime;
    private LocalDateTime publishedDateTime;

    private int retryCount;

    @Builder
    protected OrderOutbox(String topicName, String transactionKey, String originalMessage) {
        this.topicName = topicName;
        this.transactionKey = transactionKey;
        this.originalMessage = originalMessage;
        this.createdDateTime = LocalDateTime.now();
        this.status = OrderOutboxStatus.CREATED;
        this.retryCount = 0;
    }

    public void logPublished() {
        this.status = OrderOutboxStatus.PUBLISHED;
        this.publishedDateTime = LocalDateTime.now();
    }

    public void logFailed(String reason) {
        this.retryCount++;
        this.reason = reason;
    }
}