package main.java.com.fileshare.wx.util;

import java.util.HashMap;
import java.util.Map;

public class ArgsParser {

    private ArgsParser(){}

    public static Map argsParser (String[] args) {

        Map<String,Object> map = new HashMap<>();

        if (args ==null | args.length==0){
            return map;
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")){
                String temp = arg.substring(2);
                if (temp.contains("=")){
                    String[] part = temp.split("=",2);
                    map.put(part[0],part[1]);
                } else if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    map.put(args[i],args[i+1]);
                    i++;
                }else {map.put(temp,true);}
            }
        }
        map.forEach((key,value) -> System.out.println(key+"="+value));

        return map;
    }
}
