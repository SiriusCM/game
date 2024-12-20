package com.sirius.game.player;

import com.sirius.game.boot.MsgId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
@Scope("prototype")
public class PlayService {
    @Autowired
    private PlayRepository playRepository;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;
//    @Autowired
//    private DatabaseClient databaseClient;

    @MsgId(id = 1001)
    @Transactional
    public void play() {
        Play play = playRepository.findById(1).block();
    }

    public void test() {
        Flux<Play> flux = r2dbcEntityTemplate.select(Play.class).all();
        Play play = flux.blockFirst();
    }
}
