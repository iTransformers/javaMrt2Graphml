package org.javamrt.dumper;

import java.util.HashMap;
import java.util.Map;

public class CmdLineParser {
    public static Map<String, String> parseCmdLine(String[] args){
        Map<String, String> params = new HashMap<String, String>();
        int i = 0;
        while (i < args.length){
            String key = args[i++];
            if (i == args.length) {
                return params;
            }
            String value = args[i++];
            params.put(key, value);
        }
        return params;
    }
}