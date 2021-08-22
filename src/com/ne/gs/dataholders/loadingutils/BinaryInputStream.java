/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders.loadingutils;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Author: MetaWind
 * Skype: O_Wind_O
 */
public class BinaryInputStream extends ObjectInputStream{

    private static Object METHOD_PARAMS[] = {};
    private static Class CLASS_PARAMS[] = {};

    public BinaryInputStream(InputStream in) throws IOException {
        super(in);
    }

    public <T> T deserealizeObject(Class<T> klass) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        T tObject = klass.newInstance();

        List<Field> fields = new ArrayList<>();

        for(Field f : klass.getFields())
            if(f.isAnnotationPresent(BinaryField.class))
                fields.add(f);

        int elementsCounter = readInt();

        for(int i = 0; i < elementsCounter; i++)
        {
            int fieldId = readInt();

            for (Field field : fields)
            {
                if (field.getAnnotation(BinaryField.class).id() == fieldId) {
                    Class<?> ft = field.getType();

                    if(ft.isPrimitive())
                        field.set(tObject, readObject());
                    else if(ft.isAnnotationPresent(BinaryContract.class))
                        field.set(tObject, deserealizeObject(field.getType()));
                    else if(ft == List.class)
                        field.set(tObject, readList(field));
                    else if (ft == Integer.class)
                        field.set(tObject, readInt());
                    else if (ft == String.class)
                        field.set(tObject, readString());
                    else if(ft == TIntObjectHashMap.class)
                        field.set(tObject, readHashMap(field));
                    else
                        throw new NotSerializableException(field.getType().getName());

                    break;
                }
            }
        }

        String pshm = klass.getAnnotation(BinaryContract.class).useMethodAfterDeserialize();

        if(pshm.length() > 0)
            klass.getMethod(pshm, CLASS_PARAMS).invoke(tObject, METHOD_PARAMS);

        return tObject;
    }

    private List readList(Field field) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        int lLen = readInt();

        if(lLen < 0)
            return null;

        Class genClass = (Class)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];

        if(!genClass.isAnnotationPresent(BinaryContract.class))
            throw new NotSerializableException(genClass.getName());

        List lst = new ArrayList();

        for (int it = 0; it < lLen; it++)
            lst.add(it, deserealizeObject(genClass));

        return lst;
    }

    private TIntObjectHashMap readHashMap(Field field) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        int hmLen = readInt();

        if(hmLen < 0)
            return null;

        Class genClass = (Class)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];

        if(!genClass.isAnnotationPresent(BinaryContract.class))
            throw new NotSerializableException(genClass.getName());

        TIntObjectHashMap hm = new TIntObjectHashMap();

        for (int it = 0; it < hmLen; it++)
            hm.put(readInt(), deserealizeObject(genClass));

        return hm;
    }

    private String readString() throws IOException {
        byte[] buf = new byte[readInt()];
        read(buf);

        return new String(buf);
    }
}
