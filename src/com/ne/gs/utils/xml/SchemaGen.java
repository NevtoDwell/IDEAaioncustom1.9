/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

import com.ne.gs.dataholders.StaticData;

public final class SchemaGen {

    public static void main(String[] args) throws Exception {
        final File baseDir = new File("./data/static_data");

        class MySchemaOutputResolver extends SchemaOutputResolver {

            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                return new StreamResult(new File(baseDir, "static_data1.xsd"));
            }
        }
        JAXBContext context = JAXBContext.newInstance(StaticData.class);
        context.generateSchema(new MySchemaOutputResolver());
    }
}
