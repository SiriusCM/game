package com.sirius.game.scene;

import com.sirius.game.object.SceneObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SceneService {
    private Map<Long, SceneObject> sceneObjectMap = new HashMap<>();

    public void createSceneObject() {
        SceneObject sceneObject = new SceneObject();
        sceneObject.setId(System.currentTimeMillis());
        Thread.startVirtualThread(sceneObject);
        sceneObjectMap.put(sceneObject.getId(), sceneObject);
    }
}
