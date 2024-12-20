package com.sirius.game.object;

import com.sirius.game.cache.AutoBean;
import com.sirius.game.cache.AutoDB;
import com.sirius.game.cache.CacheService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class RoleObject extends WorldObject implements IPulseObject {
    @Autowired
    private ConfigurableListableBeanFactory configurableListableBeanFactory;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    private ChannelHandlerContext channelHandlerContext;

    private SceneObject sceneObject;

    private final Map<Class<?>, Object> serviceMap = new HashMap<>();

    private final Map<Class<?>, Object> dbPool = new HashMap<>();

    private final Map<Class<?>, List<?>> dbListPool = new HashMap<>();

    @Override
    public void pulse() {

    }

    public void dispatchMsg(int msgId, byte[] data) {
        for (Method method : cacheService.getDispatchMethods(msgId)) {
            sceneObject.offerConsumer((sceneService) -> {
                try {
                    method.invoke(getService(method.getDeclaringClass()), this, data);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                         InstantiationException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void replyMsg(String msg) {
        channelHandlerContext.writeAndFlush(msg);
    }

//    public void publishLocal(ApplicationEvent event) {
//        for (Method method : cacheService.getEventMethods(event.getClass())) {
//            try {
//                method.invoke(getService(method.getDeclaringClass()), event);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public Object getService(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!serviceMap.containsKey(clazz)) {
            Object base = cacheService.getConstructor(clazz).newInstance();
            configurableListableBeanFactory.autowireBean(base);
            serviceMap.put(clazz, configurableListableBeanFactory.initializeBean(base, clazz.getName()));
            autowireBean(base);
        }
        return serviceMap.get(clazz);
    }

    public void autowireBean(Object object) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (object == null) {
            return;
        }
        for (Field field : object.getClass().getDeclaredFields()) {
            Class<?> fieldClass = field.getType();
            field.setAccessible(true);
            if (fieldClass.getName().contains("CglibAopProxy")) {
                Object warpper = field.get(object);
                autowireBean(warpper);
            } else if (field.getAnnotation(AutoBean.class) != null) {
                if (!serviceMap.containsKey(fieldClass)) {
                    Object base = cacheService.getConstructor(fieldClass).newInstance();
                    configurableListableBeanFactory.autowireBean(base);
                    serviceMap.put(fieldClass, configurableListableBeanFactory.initializeBean(base, fieldClass.getName()));
                    autowireBean(base);
                }
                field.set(object, serviceMap.get(fieldClass));
            } else if (field.getAnnotation(AutoDB.class) != null) {
                autowireDB(object, field);
            }
        }
    }

    public void autowireDB(Object object, Field field) throws IllegalAccessException {
        Class<?> entityClass = cacheService.getEntityClass(field);
        if (field.getType() == List.class) {
            if (dbListPool.containsKey(entityClass)) {
                field.set(object, dbListPool.get(entityClass));
            } else {
                List<?> dataList = r2dbcEntityTemplate.select(entityClass).matching(Query.query(Criteria.where("id").is(id))).all().collectList().block();
                dbListPool.put(entityClass, dataList);
                field.set(object, dataList);
            }
        } else {
            if (dbPool.containsKey(entityClass)) {
                field.set(object, dbPool.get(entityClass));
            } else {
                Object data = r2dbcEntityTemplate.select(entityClass).matching(Query.query(Criteria.where("id").is(id))).first().block();
                if (data != null) {
                    dbPool.put(entityClass, data);
                }
                field.set(object, data);
            }
        }
    }
}
