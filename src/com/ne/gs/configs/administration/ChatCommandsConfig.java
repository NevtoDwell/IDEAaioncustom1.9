/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.administration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.ne.commons.scripting.scriptmanager.ScriptManager;
import com.ne.commons.utils.xml.XmlUtil;
import com.ne.gs.GameServer;
import com.ne.gs.utils.chathandlers.*;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * @author hex1r0
 *         // TODO implement script loader
 */
public final class ChatCommandsConfig {

    private static final Logger _log = LoggerFactory.getLogger(ChatCommandsConfig.class);

    public static void load(final ChatProcessor cp) {
        try {
            GameServer.addStartupHook(new GameServer.StartupHook() {
                @Override
                public void onStartup() {
                    reload(cp);
                }
            });
        } catch (Exception e) {
            _log.warn("Error while loading chat commands config", e);
        }
    }

    public static void reload(ChatProcessor cp) {
        Context context = new Context();

        loadXml(context,cp);
        _log.info("########################CHAT#######################");
        loadScripts(context);
    }

    private static void loadXml(Context context,ChatProcessor cp) {
        try {
            cp.getInstance().FlushPrivelegies();
            Document doc = XmlUtil.loadXmlSAX("./config/administration/chat_commands.xml");
            for (Node root : XmlUtil.nodesByName(doc, "chat_commands")) {
                for (Node registry : XmlUtil.nodesByName(root, "registry")) {
                    for (Node admin : XmlUtil.nodesByName(registry, "admin")) {
                        parseAliases(context.ra, admin);
                    }

                  
                }
            }
            NodeList nodelist = doc.getElementsByTagName("permission");
            for(int i = 0; i < nodelist.getLength(); i++){
                Node node =  nodelist.item(i);
                NamedNodeMap attrib = node.getAttributes();
                Node command = attrib.getNamedItem("command");
                Node accLvL = attrib.getNamedItem("accesslevel");
                
                cp.getInstance().addPermission(command.getNodeValue(), 
                        Integer.parseInt(accLvL.getNodeValue()));
                
            }
        } catch (Exception e) {
            _log.warn("Error while loading chat commands config", e);
        }
    }

    private static ScriptManager sma;
  
    private static void loadScripts(Context context) {
        
        if (sma != null) {
            sma.shutdown();
        }

     

        sma = new ScriptManager();
    
        context.ra.setSm(sma);
    

        context.ra.load();
     

        ChatCommandHandler.clearHandlers();
        _log.info(context.ha.toString());
    
        
        ChatCommandHandler.addHandler(context.ha);
     
    }

    private static void parseRules(Node n) {
        
    }

    private static void parseAliases(ChatCommandRegistry commands, Node node) {
        ChatCommandAliasRegistry aliases = new ChatCommandAliasRegistryImpl();
        for (Node clazz : XmlUtil.nodesByName(node, "class")) {
            for (Node alias : XmlUtil.nodesByName(clazz, "alias")) {
                aliases.addAlias(XmlUtil.getAttribute(clazz, "name"), XmlUtil.getAttribute(alias, "name"));
            }
        }

        commands.setAliasRegistry(aliases);
    }

    


    

    

    private static class Context {

        final ChatCommandRegistry ra = new AdminCommandRegistry();
       // final ChatCommandRegistry ru = new UserCommandRegistry();
       // final ChatCommandRegistry rw = new WeddingCommandRegistry();

    

        final ChatCommandHandler ha = new AdminCommandHandler(ra);
       // final ChatCommandHandler hu = new UserCommandHandler(ru);
       // final ChatCommandHandler hw = new WeddingCommandHandler(rw);
    }
}
