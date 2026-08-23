package com.project.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "labels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", insertable = false, updatable = false)
    private Board board;

    private String name;

    private String color;

    @Builder.Default
    @OneToMany(mappedBy = "label", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardLabel> cardLabels = new ArrayList<>();

    /**
     * Tự động tính màu chữ (trắng/đen) đảm bảo độ tương phản WCAG AA
     */
    public String getTextColor() {
        if (color == null || color.isBlank()) {
            return "#212529";
        }
        String hex = color.trim().replace("#", "");
        if (hex.length() == 3) {
            hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
        }
        if (hex.length() != 6) {
            return "#ffffff";
        }
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            double luminance = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0;
            return luminance > 0.5 ? "#212529" : "#ffffff";
        } catch (Exception e) {
            return "#ffffff";
        }
    }
}
