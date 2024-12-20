package com.sirius.game.player;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PlayRepository extends ReactiveCrudRepository<Play, Integer> {
}
