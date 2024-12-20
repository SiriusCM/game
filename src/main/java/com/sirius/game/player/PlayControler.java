package com.sirius.game.player;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlayControler {
    @Autowired
    private PlayService playService;

    @GetMapping("test")
    public void test() {
        playService.test();
    }
}
