package com.sirius.game.player;

import com.sirius.game.boot.MsgId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class PlayService {
    @Autowired
    private PlayRepository playRepository;


    @MsgId(id = 1001)
    @Transactional
    public void play() {
        Play play = playRepository.findById(1).block();
    }

    public void test() {
        Play play = playRepository.findById(1).block();
    }
}
