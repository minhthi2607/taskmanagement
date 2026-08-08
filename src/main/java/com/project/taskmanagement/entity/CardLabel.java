package com.project.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "card_labels",
    uniqueConstraints = @UniqueConstraint(columnNames = {"card_id", "label_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", insertable = false, updatable = false)
    private Card card;

    @Column(name = "label_id", nullable = false)
    private Long labelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_id", insertable = false, updatable = false)
    private Label label;
}
