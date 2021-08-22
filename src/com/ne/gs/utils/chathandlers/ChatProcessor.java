/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.utils.chathandlers;
import java.util.HashMap;
import java.util.Map;
import com.ne.gs.configs.administration.ChatCommandsConfig;
import com.ne.gs.model.gameobjects.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author SoftIce
 */
public class ChatProcessor {
    
    final Map<String,Integer>permissions = new HashMap<String, Integer>();
    private static final Logger log = LoggerFactory.getLogger(ChatCommandsConfig.class);
    
    public static ChatProcessor getInstance() {
        
        return SingletonHolder.instance;
    }
    
    public void Load(){
        ChatProcessor cp = SingletonHolder.instance;
        ChatCommandsConfig.reload(cp);
    }
    
    public void removepermission(String command){
            permissions.remove(command);
    }
    
    public Integer addPermission(String command, int Level){
        return permissions.put(command, Level);
    }
    
    private static final class SingletonHolder {
        protected static final ChatProcessor instance = new ChatProcessor();
    }
    
    public boolean process(Player pl , String command){
           switch(ParsePrefix(command)){
               case -1:
               return false;
               case 2:
               int accessLevel = pl.getAccessLevel();
               
               log.info(pl.getName() + " try admin command: " +command);
               
               if(accessLevel<1)
                   return false;
               
               String admincommmand = command.substring(2);
               if(command.contains(" ")){
               int spaceIndex = command.indexOf(" ");
                    admincommmand = command.substring(2, spaceIndex);
               }
               int commandlevel = 5;
               
               if(permissions.containsKey(admincommmand))
               commandlevel = permissions.get(admincommmand);
               
               if(commandlevel>accessLevel)
                return false;
               
               ChatCommandHandler.perform(pl, command);
               return true;
               case 1:
                   String usercommand = "";
                   String preusercommand = command.substring(1);
                   
                   if (command.contains(" ")){
                        int spaceIndexUser = command.indexOf(" ");
                         usercommand = command.substring(1, spaceIndexUser);
                   }
                   else
                         usercommand = preusercommand;
               int commandleveluser = -1;
               if(permissions.containsKey(usercommand))
               commandleveluser = permissions.get(usercommand);
               else
                   return false;
               if(commandleveluser != 0)
                   return false;
               ChatCommandHandler.perform(pl, "//" + preusercommand);
               return true;
            
               default:
                   break;        
           }
           return false;
    }
    
    public void FlushPrivelegies(){
        if(!permissions.isEmpty())
            permissions.clear();
    }
    
    public int ParsePrefix(String message){
        if (message.startsWith("//"))
            return 2;
        if (message.startsWith("."))
            return 1;
        return -1;
    }
   
}
