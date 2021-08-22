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
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Author: MetaWind
 * Skype: O_Wind_O
 * TODO: rework binary stream to ignore sensetive of classmembers counter change, if that need
 */
public class BinarySerializer {

    public static void serialize(Object sObject, OutputStream stream) throws IOException, IllegalAccessException {
        new BinaryOutputStream(stream).serializeObject(sObject);
    }

    public static <T> T deserialize(Class<T> tClass, InputStream stream) throws IllegalAccessException, InstantiationException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        return new BinaryInputStream(stream).deserealizeObject(tClass);
    }


}
