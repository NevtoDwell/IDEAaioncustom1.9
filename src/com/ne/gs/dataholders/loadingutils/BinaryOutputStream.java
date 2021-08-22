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
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import com.ne.commons.utils.TypeUtil;

/**
 * Author: MetaWind
 * Skype: O_Wind_O
 */
public class BinaryOutputStream extends ObjectOutputStream {

    private static boolean DEFAULT_BOOLEAN;
    private static byte DEFAULT_BYTE;
    private static short DEFAULT_SHORT;
    private static int DEFAULT_INT;
    private static long DEFAULT_LONG;
    private static float DEFAULT_FLOAT;
    private static double DEFAULT_DOUBLE;

    public BinaryOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    public void serializeObject(Object sObject) throws IOException, IllegalAccessException {
        List<Field> fields = new ArrayList<>();

        for(Field f : sObject.getClass().getFields())
            if(needToSerialize(sObject, f))
                fields.add(f);

        writeInt(fields.size());

        for (Field field : fields)
        {
            Class<?> ft = field.getType();
            writeInt(field.getAnnotation(BinaryField.class).id());

            if (ft.isPrimitive())
                writeObject(field.get(sObject));
            else if(ft.isAnnotationPresent(BinaryContract.class))
                serializeObject(field.get(sObject));
            else if(ft == List.class)
                writeList((List) field.get(sObject));
            else if (ft == Integer.class)
                writeInt((Integer) field.get(sObject));
            else if (ft == String.class)
                writeString((String) field.get(sObject));
            else if(ft == TIntObjectHashMap.class)
                writeHashMap((TIntObjectHashMap)field.get(sObject));
            else
                throw new NotSerializableException(field.getType().getName());
        }
    }

    private void writeList(List value) throws IOException, IllegalAccessException {
        writeInt(value == null ? -1 : value.size());

        for(int i = 0; i < value.size(); i++)
            serializeObject(value.get(i));
    }

    private void writeString(String value) throws IOException {
        byte[] val = value.getBytes();
        writeInt(val.length);
        write(val);
    }

    private void writeHashMap(TIntObjectHashMap value) throws IOException, IllegalAccessException {
        writeInt(value == null ? -1 : value.size());

        value.forEachEntry(new TIntObjectProcedure() {
                @Override
                public boolean execute(int i, Object o) {

                    try {
                        writeInt(i);
                        serializeObject(o);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                return true;
            }
        });
    }


    private boolean needToSerialize(Object sObj, Field currentField) throws IllegalAccessException {
         return currentField.isAnnotationPresent(BinaryField.class) && !currentField.get(sObj).equals(getDefaultValue(currentField.get(sObj).getClass()));
    }

    public static Object getDefaultValue(Class clazz) {
        if (TypeUtil.isBool(clazz)) {
            return DEFAULT_BOOLEAN;
        } else if (TypeUtil.isByte(clazz)) {
            return DEFAULT_BYTE;
        } else if (TypeUtil.isShort(clazz)) {
            return DEFAULT_SHORT;
        } else if (TypeUtil.isInt(clazz)) {
            return DEFAULT_INT;
        } else if (TypeUtil.isLong(clazz)) {
            return DEFAULT_LONG;
        } else if (TypeUtil.isFloat(clazz)) {
            return DEFAULT_FLOAT;
        } else if (TypeUtil.isDouble(clazz)) {
            return DEFAULT_DOUBLE;
        } else {
            return null;
        }
    }
}
