package com.lewis.liveclient.Annotation;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.lang.reflect.Field;

/**
 * Created by Lewis on 2017/11/8.
 *
 */

public class AndroidAnnotation {
    public static <V extends View, A extends AppCompatActivity> void init(A obj)
            throws ClassNotFoundException, IllegalAccessException {
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Bind.class)) {
                Bind bind = field.getAnnotation(Bind.class);

//                View view = ((AppCompatActivity)obj).findViewById(bind.value());
                V view = obj.findViewById(bind.value());

                field.setAccessible(true);
                field.set(obj, view);
            }
        }
    }
}
