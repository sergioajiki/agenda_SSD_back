package com.ssd.agenda_SSD_back.util;

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
                if (oldValue == null || newValue == null || !oldValue.equals(newObject)) {
                    // Adiciona a mudança no mapa no formato: "campo: valor_antigo -> valor_novo"
                    changes.put(field.getName(), (oldValue == null ? "null" : oldValue.toString()) + " -> " +
                            (newValue == null ? "null" : newObject.toString()));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Erro ao acessar o campo: " + field.getName(), e);
            }
        }
        return changes;
    }
}
