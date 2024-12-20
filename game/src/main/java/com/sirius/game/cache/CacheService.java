package com.sirius.game.cache;

import com.sirius.game.boot.MsgId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CacheService {
    @Autowired
    private List<Class<?>> classList;

    @Cacheable(cacheNames = "constructor")
    public Constructor<?> getConstructor(Class<?> constructorClass) throws NoSuchMethodException {
        return constructorClass.getConstructor();
    }

    @Cacheable(cacheNames = "dispatch")
    public List<Method> getDispatchMethods(int id) {
        List<Method> methodList = new ArrayList<>();
        for (Class<?> clazz : classList) {
            for (Method method : clazz.getDeclaredMethods()) {
                method.setAccessible(true);
                MsgId msgId = method.getAnnotation(MsgId.class);
                if (msgId != null) {
                    for (int i : msgId.id()) {
                        if (id == i) {
                            methodList.add(method);
                        }
                    }
                }
            }
        }
        return methodList;
    }

    @Cacheable(cacheNames = "event")
    public List<Method> getEventMethods(Class<?> eventClass) {
        List<Method> methodList = new ArrayList<>();
        for (Class<?> clazz : classList) {
            for (Method method : clazz.getDeclaredMethods()) {
                method.setAccessible(true);
                EventListener listener = method.getAnnotation(EventListener.class);
                if (listener != null) {
                    for (Class<?> aClass : listener.value()) {
                        if (aClass == eventClass) {
                            methodList.add(method);
                        }
                    }
                }
            }
        }
        return methodList;
    }

    @Cacheable(cacheNames = "entity")
    public Class<?> getEntityClass(Field field) {
        if (field.getType() == List.class) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type[] types = parameterizedType.getActualTypeArguments();
            return (Class<?>) types[0];
        } else {
            return field.getType();
        }
    }
}
