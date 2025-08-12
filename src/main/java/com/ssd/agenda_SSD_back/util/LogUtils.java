package com.ssd.agenda_SSD_back.util;

import com.ssd.agenda_SSD_back.entity.User;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


public class LogUtils {
    public static Map<String, String> getChangeFields(Object oldObject, Object newObject) {
        Map<String, String> changes = new HashMap<>();

        if (oldObject == null || newObject == null || !oldObject.getClass().equals(newObject.getClass())) {
            throw new IllegalArgumentException("Ambos os objetos devem ser do mesmo tipo e não podem ser nulos.");
        }

        Class<?> clazz = oldObject.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // Permite acesso a campos privados

            try {
                Object oldValue = field.get(oldObject);
                Object newValue = field.get(newObject);

                if (oldValue == null && newValue == null) {
                    continue;
                }
                if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
                    String oldValStr;
                    String newValStr;

                    if (oldValue instanceof User oldUser) {
                        oldValStr = oldUser.getName();
                    } else {
                        oldValStr = oldValue == null ? "null" : oldValue.toString();
                    }

                    if (newValue instanceof User newUser) {
                        newValStr = newUser.getName();
                    } else {
                        newValStr = newValue == null ? "null" : newValue.toString();
                    }

                    changes.put(field.getName(), oldValStr + " -> " + newValStr);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Erro ao acessar o campo: " + field.getName(), e);
            }
        }
        return changes;
    }
}
