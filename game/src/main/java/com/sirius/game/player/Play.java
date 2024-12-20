package com.sirius.game.player;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("play")
public class Play {
    @Id
    private int id;
    private String name;
}
