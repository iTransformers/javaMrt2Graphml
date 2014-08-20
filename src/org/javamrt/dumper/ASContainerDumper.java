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
        writer.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"   \n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  \n" +
                "         xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \n" +
                "           http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\"  \n" +
                "         xmlns:y=\"http://www.yworks.com/xml/graphml\"> \n ");
        writer.write("\t<graph id=\"G\" edgedefault=\"directed\">\n");
        writer.write("\t<key id=\"countOriginatedPrefixes\" for=\"node\" type=\"string\"/>\n");
        writer.write("\t<key id=\"IPv4Flag\" for=\"node\" type=\"string\"/>\n");
        writer.write("\t<key id=\"IPv6Flag\" for=\"node\" type=\"string\"/>\n");
        writer.write("\t<key id=\"weight\" for=\"edge\" type=\"int\"/>\n");
        writer.write("\t\t<nodes>\n");
        dumpNodes(ases, writer,"\t\t\t");
        writer.write("\t\t</nodes>\n");
        writer.write("\t\t<edges>\n");
        dumpEdges(ases, writer,"\t\t\t");
        writer.write("\t\t</edges>\n");
        writer.write("\t</graph>  \n");
        writer.write("</graphml>");
    }

    public static void dumpNodes(ASContainer ases, Writer writer, String tabs) throws IOException {
        for (ASInfo asInfo : ases.getAsInfoMap().values()) {
            writer.write(tabs+"<node id=\""+asInfo.getName()+"\">\n");
            List<PrefixInfo> prefixInfo = asInfo.getPrefixInfo();
            Map<String, Integer> ipvXcounter = countIpVXPrefixes(prefixInfo);
            writer.write(tabs+"\t<data key=\"countOriginatedPrefixes\">"+ prefixInfo.size()+"</data>\n");
            writer.write(tabs+"\t<data key=\"IPv4Flag\">"+ ("" + (ipvXcounter.get(IPV4_KEY) > 0)).toUpperCase()+"</data>\n");
            writer.write(tabs+"\t<data key=\"IPv6Flag\">"+ ("" + (ipvXcounter.get(IPV6_KEY) > 0)).toUpperCase()+"</data>\n");
            writer.write(tabs+"</node>\n");
        }
    }

    public static void dumpEdges(ASContainer ases, Writer writer, String tabs) throws IOException {
        List<ASPathInfo> asPathInfoList = ases.getAsPathInfoList();
        for (ASPathInfo asPathInfo : asPathInfoList) {
            dumpAsPath(asPathInfo, writer, tabs );
        }

    }

    private static void dumpAsPath(ASPathInfo asPathInfo, Writer writer, String tabs) throws IOException {
        List<String> path = asPathInfo.getPath();
        int repeatCounter = 0;
        String lastNode = null;
        for (String node : path) {
            if (lastNode != null && lastNode.equals(node)) {
                repeatCounter++;
                continue;
            } else if (lastNode != null){
                String edgeAttributes= "id=\"" + lastNode + "_" + node + "\" source=\"" + lastNode + "\"" + " target=\"" + node+"\"";
                if (repeatCounter > 0) {
                    writer.write(tabs+"<edge "+ edgeAttributes + "\">\n");
                    writer.write(tabs+"\t<data key=\"weight\">"+repeatCounter+"</data>\n");
                    writer.write(tabs+"</edge>\n");
                } else {
                    writer.write(tabs+"<edge "+edgeAttributes+"/>\n");
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
