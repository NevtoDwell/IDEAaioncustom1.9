/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.utils;

/**
 *
 * @author userd
 */
public class ColorUtil {

    public static String convertFromUTF8(String s) {
        String out;
        try {
            byte[] bytes = s.getBytes();
            for (int i = 0; i < bytes.length - 1; i++) {
                if (bytes[i] == -48 && bytes[i + 1] == 63) {
                    bytes[i] = (byte) 208;
                    bytes[i + 1] = (byte) 152;
                }
            }
            out = new String(bytes, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }

    public static String convertFromCP866(String s) {
        String out;
        try {
            out = new String(s.getBytes("Cp866"), "Cp1251");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }

    public static String convertTextToColor(String message, String RGB) {
        StringBuilder sb = new StringBuilder();
        int length = message.length();
        for (int i = 0; i < length; i += 5) {
            int n = ((i + 5) <= length) ? i + 5 : length;
            sb.append("[color:").append(message.substring(i, n)).append(";").append(RGB).append("]");

        }
        sb.append(" ");
        return sb.toString();
    }

    public static String convertText(String word, String RGB) {
        StringBuilder sb = new StringBuilder();
        int length = word.length();
        for (int i = 0; i < length; i += 5) {
            int n = ((i + 5) <= length) ? i + 5 : length;
            sb.append("[color:").append(word.substring(i, n)).append(";").append(RGB).append("]");
        }
        return sb.toString();
    }

    public static String convertHexCodeToRGB(String hex) {
        int i = Integer.decode(hex);
        int r = (i >> 16) & 0xFF, g = (i >> 8) & 0xFF, b = i & 0xFF;
        String RGB = r + " " + g + " " + b;
        return RGB;
    }

    public static int convertHexTo16(String hex) {
        int rgb = Integer.parseInt(hex, 16);
        int bgra = 0xFF | ((rgb & 0xFF) << 24) | ((rgb & 0xFF00) << 8) | ((rgb & 0xFF0000) >>> 8);
        return bgra;
    }

    public static String colorName(String color) {
        if (color.equalsIgnoreCase("turquoise") || color.equalsIgnoreCase("бирюзовый")) {
            color = "198d94";
        } // 169200001, 169201001
        else if (color.equalsIgnoreCase("blue") || color.equalsIgnoreCase("синий")) {
            color = "1f87f5";
        } // 169200002, 169201002
        else if (color.equalsIgnoreCase("brown") || color.equalsIgnoreCase("коричневый")) {
            color = "66250e";
        } // 169200003, 169201003
        else if (color.equalsIgnoreCase("purple") || color.equalsIgnoreCase("фиолетовый")) {
            color = "c38df5";
        } // 169200004, 169201004
        else if (color.equalsIgnoreCase("true red") || color.equalsIgnoreCase("red") || color.equalsIgnoreCase("красный")) {
            color = "c22626";
        } // 169200005, 169201005, 169220001, 169230001, 169231001
        else if (color.equalsIgnoreCase("true white") || color.equalsIgnoreCase("white") || color.equalsIgnoreCase("белый")) {
            color = "ffffff";
        } // 169200006, 169201006, 169220002, 169231002
        else if (color.equalsIgnoreCase("black") || color.equalsIgnoreCase("true black") || color.equalsIgnoreCase("черный")) {
            color = "000000";
        } // 169200007, 169201007, 169230008, 169231008
        else if (color.equalsIgnoreCase("hot orange") || color.equalsIgnoreCase("оранжевый")) {
            color = "e36b00";
        } // 169201008, 169220004, 169230009, 169231009
        else if (color.equalsIgnoreCase("rich purple") || color.equalsIgnoreCase("светло-фиолетовый")) {
            color = "440b9a";
        } // 169201009, 169220005, 169230007, 169231003
        else if (color.equalsIgnoreCase("hot pink") || color.equalsIgnoreCase("ярко-розовый")) {
            color = "d60b7e";
        } // 169201010, 169220006, 169230010, 169231010
        else if (color.equalsIgnoreCase("mustard") || color.equalsIgnoreCase("горчичный")) {
            color = "fcd251";
        } // 169201011, 169220007, 169230004, 169231004
        else if (color.equalsIgnoreCase("green tea") || color.equalsIgnoreCase("зеленый чай")) {
            color = "61bb4f";
        } // 169201012, 169220008, 169230003, 169231005
        else if (color.equalsIgnoreCase("olive green") || color.equalsIgnoreCase("оливково-зеленый")) {
            color = "5f730e";
        } // 169201013, 169220009, 169230005, 169231006
        else if (color.equalsIgnoreCase("deep blue") || color.equalsIgnoreCase("густо-синий")) {
            color = "14398b";
        } // 169201014, 169220010, 169230006, 169231007
        else if (color.equalsIgnoreCase("romantic purple") || color.equalsIgnoreCase("романтический фиолетовый")) {
            color = "80185d";
        } // 169230011
        else if (color.equalsIgnoreCase("wiki") || color.equalsIgnoreCase("ярко-зеленый")) {
            color = "85e831";
        } // 169240001
        else if (color.equalsIgnoreCase("omblic") || color.equalsIgnoreCase("ярко-красный")) {
            color = "ff5151";
        } // 169240002
        else if (color.equalsIgnoreCase("meon") || color.equalsIgnoreCase("лиловый")) {
            color = "afaf26";
        } // 169240003
        else if (color.equalsIgnoreCase("ormea") || color.equalsIgnoreCase("ярко-оранжевый")) {
            color = "ffaa11";
        } // 169240004
        else if (color.equalsIgnoreCase("tange") || color.equalsIgnoreCase("светло-фиолетовый")) {
            color = "bd5fff";
        } // 169240005
        else if (color.equalsIgnoreCase("ervio") || color.equalsIgnoreCase("голубой")) {
            color = "3bb7fe";
        } // 169240006
        else if (color.equalsIgnoreCase("lunime") || color.equalsIgnoreCase("шта")) {
            color = "c7af27";
        } // 169240007
        else if (color.equalsIgnoreCase("vinna") || color.equalsIgnoreCase("темно-синий")) {
            color = "052775";
        } // 169240008
        else if (color.equalsIgnoreCase("kirka") || color.equalsIgnoreCase("розовый")) {
            color = "ca84ff";
        } // 169240009
        else if (color.equalsIgnoreCase("brommel") || color.equalsIgnoreCase("шта2")) {
            color = "c7af27";
        } // 169240010
        else if (color.equalsIgnoreCase("pressa") || color.equalsIgnoreCase("темно-оранжевый")) {
            color = "ff9d29";
        } // 169240011
        else if (color.equalsIgnoreCase("merone") || color.equalsIgnoreCase("светло-зеленый")) {
            color = "8df598";
        } // 169240012
        else if (color.equalsIgnoreCase("kukar") || color.equalsIgnoreCase("желтый")) {
            color = "ffff96";
        } // 169240013
        else if (color.equalsIgnoreCase("leopis") || color.equalsIgnoreCase("светло-голубой")) {
            color = "31dfff";
        } // 169240014
        return color;
    }
}
