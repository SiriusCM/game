package com.sirius.game.object;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;


@Component
@Scope("prototype")
public class SceneObject extends WorldObject implements Runnable {

    private Queue<Consumer<SceneObject>> consumerQueue = new LinkedBlockingQueue<>();

    private Map<Long, WorldObject> worldObjectMap = new HashMap<>();

    public void offerConsumer(Consumer<SceneObject> consumer) {
        consumerQueue.offer(consumer);
    }

    public void enterScene(WorldObject worldObject) {
        worldObjectMap.put(worldObject.getId(), worldObject);
    }

    public void leaveScene(WorldObject worldObject) {
        worldObjectMap.remove(worldObject.getId());
    }

    @Override
    public void pulse() {
        while (!consumerQueue.isEmpty()) {
            Consumer<SceneObject> consumer = consumerQueue.poll();
            consumer.accept(this);
        }
        worldObjectMap.values().forEach(WorldObject::pulse);
    }

    @Override
    public void run() {
        while (!worldObjectMap.isEmpty()) {
            try {
                pulse();
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
