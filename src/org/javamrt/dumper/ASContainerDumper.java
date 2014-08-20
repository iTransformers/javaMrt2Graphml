package org.javamrt.dumper;

import org.javamrt.dumper.structures.ASContainer;
import org.javamrt.dumper.structures.ASInfo;
import org.javamrt.dumper.structures.ASPathInfo;
import org.javamrt.dumper.structures.PrefixInfo;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vasko on 8/20/14.
 */
public class ASContainerDumper {
    private final static String IPV4_KEY = "IPV4";
    private final static String IPV6_KEY = "IPV6";

    public static void dump(ASContainer ases, Writer writer) throws IOException {
        writer.write("<nodes>\n");
        dumpNodes(ases, writer);
        writer.write("</nodes>\n");
        writer.write("<edges>\n");
        dumpEdges(ases, writer);
        writer.write("</edges>\n");
    }

    public static void dumpNodes(ASContainer ases, Writer writer) throws IOException {
        for (ASInfo asInfo : ases.getAsInfoMap().values()) {
            writer.write("\t<node id=\""+asInfo.getName()+"\">\n");
            List<PrefixInfo> prefixInfo = asInfo.getPrefixInfo();
            Map<String, Integer> ipvXcounter = countIpVXPrefixes(prefixInfo);
            writer.write("\t\t<data key=\"countOriginatedPrefixes\">"+ prefixInfo.size()+"</data>\n");
            writer.write("\t\t<data key=\"IPv4Flag\">"+ ("" + (ipvXcounter.get(IPV4_KEY) > 0)).toUpperCase()+"</data>\n");
            writer.write("\t\t<data key=\"IPv6Flag\">"+ ("" + (ipvXcounter.get(IPV6_KEY) > 0)).toUpperCase()+"</data>\n");
            writer.write("\t</node>\n");
        }
    }

    public static void dumpEdges(ASContainer ases, Writer writer) throws IOException {
        List<ASPathInfo> asPathInfoList = ases.getAsPathInfoList();
        for (ASPathInfo asPathInfo : asPathInfoList) {
            dumpAsPath(asPathInfo, writer);
        }

    }

    private static void dumpAsPath(ASPathInfo asPathInfo, Writer writer) throws IOException {
        List<String> path = asPathInfo.getPath();
        int repeatCounter = 0;
        String lastNode = null;
        for (String node : path) {
            if (lastNode != null && lastNode.equals(node)) {
                repeatCounter++;
                continue;
            } else if (lastNode != null){
                String edgeAttributes= "id=\"" + lastNode + "_" + node + "\" from=\"" + lastNode + "\"" + " to=\"" + node+"\"";
                if (repeatCounter > 0) {
                    writer.write("\t<edge "+ edgeAttributes + "\">\n");
                    writer.write("\t\t<data key=\"weight\">"+repeatCounter+"</data>\n");
                    writer.write("\t</edge>\n");
                } else {
                    writer.write("\t<edge "+edgeAttributes+"/>\n");
                }
                repeatCounter = 0;
            }
            lastNode = node;
        }
    }

    private static Map<String, Integer> countIpVXPrefixes(List<PrefixInfo> prefixInfoList){
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        int ipV4Counter = 0;
        int ipV6Counter = 0;
        for (PrefixInfo prefixInfo : prefixInfoList) {
            if (prefixInfo.getPrefix().contains(".")){
                ipV4Counter++;
            } else {
                ipV6Counter++;
            }
        }
        result.put(IPV4_KEY, ipV4Counter);
        result.put(IPV6_KEY, ipV6Counter);
        return result;
    }

}
