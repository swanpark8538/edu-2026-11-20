package com.example.board.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "BOARD")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    @Id
    private String boardId;
    private String title;
    private String content;
    private String writer;
}
