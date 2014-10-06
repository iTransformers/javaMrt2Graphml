package org.javamrt.dumper;

import org.javamrt.dumper.structures.ASContainer;
import org.javamrt.dumper.structures.ASInfo;
import org.javamrt.dumper.structures.PrefixInfo;
import org.javamrt.mrt.*;
import org.javamrt.utils.Debug;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Route2GraphmlDumper {
    private final static String IPV4_KEY = "IPV4";
    private final static String IPV6_KEY = "IPV6";


    public static void main(String args[]) throws IOException {
        Map<String, String> params = CmdLineParser.parseCmdLine(args);
        PrintWriter writer;
        ZipOutputStream zos = null;
        if (params.containsKey("-o")) {
            writer = new PrintWriter(params.get("-o"));
        } else if (params.containsKey("-z")) {
            zos = new ZipOutputStream(new FileOutputStream(params.get("-z")));
            writer = new PrintWriter(zos);
        } else {
            writer = new PrintWriter(System.out);
        }
        String file = null;
        OutputStream logOutputStream;
        if (params.containsKey("-f2")) {
            String file2 = params.get("-f2");
            logOutputStream = new FileOutputStream(file2);
        } else {
            logOutputStream = new NullOutputStream();
        }
        if (params.containsKey("-f")) {
            file = params.get("-f");
        } else {
            usage();
            System.exit(1);
        }
        ASContainer ases = new ASContainer();
        System.out.println("Start reading MRT file");
        File tmpEdgeFile = File.createTempFile(file+"_",".txt");
        System.out.println("Creating edge tmp file: "+tmpEdgeFile.getAbsolutePath());
        Writer edgeWriter = new PrintWriter(tmpEdgeFile);
        dumpToXmlString(new String[]{file}, new PrintWriter(logOutputStream), edgeWriter, ases);
        System.out.println();
        System.err.flush();
        edgeWriter.close();

        System.out.println("Start dumping to Graphml file, AS count: "+ases.getAsInfoMap().size());
        if (zos != null) {
            zos.putNextEntry(new ZipEntry(params.get("-z").replace(".zip", "")));
        }
        try {
            dumpGraphml(ases, writer, tmpEdgeFile);
        } finally {
            tmpEdgeFile.delete();
            if (zos != null) {
                zos.closeEntry();
                zos.close();
            } else {
                writer.close();
            }
        }
        System.out.println("Graphml file created");

    }
    public static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException { }
    }


    public static void dumpToXmlString(String[] files, Writer logWriter, Writer edgeWriter, ASContainer ases) throws IOException {
        MRTRecord record;
        BGPFileReader in;
        AS traverses = null;
        AS originator = null;
        InetAddress peer = null;
        Prefix prefix = null;
        Checker checker = null;
        boolean oldall = true;
        Set<String> edgesIds = new TreeSet<String>();
        logWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ");
        logWriter.append("<root>\n");
        for (String file1 : files) {
            try {
                in = new BGPFileReader(file1);
                int lastSize = 0;
                while (!in.eof()) {
                    try {
                        if ((record = in.readNext()) == null){
                            checker = new Checker(prefix,peer,originator,traverses,record);
                            break;
                        }
                        if (record instanceof Open
                                || record instanceof KeepAlive
                                || record instanceof Notification){
                            checker = new Checker(prefix,peer,originator,traverses,record);
                            continue;
                        }
                        if (record instanceof StateChange) {
                            if (oldall && checker.checkPeer()) {
                                logWriter.append(toXmlString(record, ases, edgeWriter, edgesIds));
                                continue;
                            }
                        }
                        if ((record instanceof TableDump)
                                || (record instanceof Bgp4Update)) {
                            checker = new Checker(prefix,peer,originator,traverses,record);
                            int size = ases.getAsInfoMap().size();
                            if ((size % 100) == 0 && size != lastSize) {
                                System.out.print("\rProcessed: " + size);
                                lastSize = size;
                            }
                            try {
                                if (!checker.checkPrefix())
                                    continue;
                                if (!checker.checkPeer())
                                    continue;
                                if (!checker.checkASPath())
                                    continue;
                            } catch (Exception e) {
                                e.printStackTrace(System.err);
                                System.err.printf("record = %s\n",record);
                            }
                            logWriter.append(toXmlString(record, ases, edgeWriter, edgesIds));
                        }
                    } catch (RFC4893Exception rfce) {
                        boolean printRFC4893violations = false;
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
                in.close();
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + file1);
            } catch (Exception ex) {
                System.err.println("Exception caught when reading " + file1);
                ex.printStackTrace(System.err);
            }
        }
        logWriter.append("\n</root>");
    }

    private static String toXmlString(MRTRecord record, ASContainer ases, Writer edgeWriter, Set<String> edgesIds){
        if (record instanceof TableDump) {
            return tableDumpToXmlString((TableDump) record, ases, edgeWriter, edgesIds);
        } else if (record instanceof Bgp4Update) {
            return bgp4UpdateToXmlString((Bgp4Update) record, ases);
        } else if (record instanceof StateChange){
            return stateChangeToXmlString((StateChange) record, ases);
        }
        return null;
    }

    private static String stateChangeToXmlString(StateChange stateChange, ASContainer ases) {
        return null;
    }

    private static String bgp4UpdateToXmlString(Bgp4Update bgp4Update, ASContainer ases) {
        String peerString = ipAddressString(bgp4Update.getPeer());

        String result = "<"+"BGP4MP"+ " time=\"" + bgp4Update.getTime() + "\""
                + // this comes from MRTRecord
                "updateType=\""+"?" + "\"" +"peerString=\"" + peerString + "\""
                + "peerAS=\""+bgp4Update.getPeerAS() + "\"" + "prefix=\"" + bgp4Update.getPrefix().toString()+"\""+"/>";

        if (bgp4Update.getAttributes() != null)
            result += '|' + bgp4Update.getAttributes().toString();

        return result;
    }

    private static String tableDumpToXmlString (TableDump tableDump, ASContainer ases, Writer edgeWriter, Set<String> edgesIds)
    {
        StringBuffer dumpString = new StringBuffer();
        String cleanPeer = // this.Peer.getHostAddress ().replaceFirst("(:0){2,7}", ":").replaceFirst("^0::", "::");
                ipAddressString(tableDump.getPeer());
        dumpString =
                new StringBuffer("\n<"+tableDump.getType()+"  origTime=\""+tableDump.getOrigTime()+"\"");
        dumpString.append (" updateType=\"B\" cleanPeer=\"" + cleanPeer  +"\" ");
        if (tableDump.getPeerAS() != null) {
            String as = tableDump.getPeerAS().toString();
            dumpString.append (" AS=\""+ as +"\"");
        }
        dumpString.append (" prefix=\"" + tableDump.getPrefix().toString()+"\"");
        dumpString.append (">\n");

        Attributes tableDumpAttributes = tableDump.getAttributes();

        try {
            String str = attributesToXmlString(tableDumpAttributes, ases, tableDump.getPrefix(), edgeWriter, edgesIds);
            dumpString.append (str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dumpString.append ("</"+tableDump.getType()+">");
        return dumpString.toString();
    }

    private static String ipAddressString(InetAddress ia)
    {
        return ia.getHostAddress().
                // replaceFirst("^[^/]*/", "").
                        replaceFirst(":0(:0)+","::").
                replaceFirst("^0:","").
                replaceFirst(":::", "::");
    }

    private static String attributesToXmlString(Attributes attributes, ASContainer ases, Prefix prefix, Writer edgeWriter, Set<String> edgesIds) throws Exception {
        StringBuilder toStr = new StringBuilder();
        toStr.append("\t<attributes>\n");
        for (int i = MRTConstants.ATTRIBUTE_AS_PATH; i < MRTConstants.ATTRIBUTE_TOTAL; i++) {
//            System.out.println(attributes.elementAt(i));
            if (attributes.getAttribute(i)!=null){
                String type = "";
                if (i== MRTConstants.ATTRIBUTE_ORIGIN){
                    type="Origin";
                    toStr.append("\t\t<attribute").append(" type="+"\""+type+"\""+">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                } else {
                    if (i == MRTConstants.ATTRIBUTE_AS_PATH) {
                        type = "ASPath";
                        String[] ASes = attributes.getAttribute(i).toString().split(" ");
                        dumpAsPath(ASes, edgeWriter, "", edgesIds);
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">");
                        LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
                        for (String as : ASes) {
                            Integer f = map.get(as);

                            if (f == null)
                                map.put(as, 1);
                            else
                                map.put(as, ++f);
                        }
                        Iterator<Map.Entry<String, Integer>> entries = map.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry<String, Integer> thisEntry = (Map.Entry) entries.next();
                            String key = thisEntry.getKey();
                            Integer value = thisEntry.getValue();
                            if (entries.hasNext()) {
                                toStr.append("\n\t\t\t<AS count=\"" + value + "\">" + key + "</AS>");
                            } else {
                                toStr.append("\n\t\t\t<AS count=\"" + value + "\" " + "last=\"true\">" + key + "</AS>");
                            }
                        }
                        for (String as : ASes) {
                            if (!ases.getAsInfoMap().containsKey(as)) {
                                ASInfo asinfo = new ASInfo(as);
                                ases.getAsInfoMap().put(as, asinfo);
                            }
                        }
                        String asKey = ASes[ASes.length - 1];
                        ASInfo asInfo = ases.getAsInfoMap().get(asKey);
                        if (!asInfo.getPrefixInfo().contains(new PrefixInfo(prefix.toString()))) {
                            asInfo.getPrefixInfo().add(new PrefixInfo(prefix.toString()));
                        }

//                    for (Map.Entry<String, Integer> entry : map.entrySet()){
//                        if (entry.){
//                            toStr.append("\n\t\t\t<AS count=\""+entry.getValue()+"\">"+entry.getKey()+"</AS>");
//                        }else{
//                            toStr.append("\n\t\t\t<AS count=\""+entry.getValue() +"\"" +"last=\"true\">"+entry.getKey()+"</AS>");
//                        }
//                    }
                        toStr.append("\n\t\t</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_NEXT_HOP) {
                        type = "nextHop";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");
                    } else if (i == MRTConstants.ATTRIBUTE_LOCAL_PREF) {
                        type = "localPref";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_MULTI_EXIT) {
                        type = "MED";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_COMMUNITY) {
                        type = "Community";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_ATOMIC_AGGREGATE) {
                        type = "ATOMIC_AGGREGATE";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_AGGREGATOR) {
                        type = "AGGREGATOR";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_ORIGINATOR_ID) {
                        type = "ORIGINATOR_ID";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_CLUSTER_LIST) {
                        type = "CLUSTER_LIST";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_DPA) {
                        type = "DPA";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_ADVERTISER) {
                        type = "ADVERTISER";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_CLUSTER_ID) {
                        type = "CLUSTER_ID";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_MP_REACH) {
                        type = "MP_REACH";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_MP_UNREACH) {
                        type = "MP_UNREACH";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_EXT_COMMUNITIES) {
                        type = "EXT_COMMUNITIES";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_AS4_PATH) {
                        type = "AS4_PATH";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_AS4_AGGREGATOR) {
                        type = "AS4_AGGREGATOR";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_ASPATHLIMIT) {
                        type = "ASPATHLIMIT";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    } else if (i == MRTConstants.ATTRIBUTE_TOTAL) {
                        type = "TOTAL";
                        toStr.append("\t\t<attribute").append(" type=" + "\"" + type + "\"" + ">").append(attributes.getAttribute(i).toString()).append("</attribute>\n");

                    }
                }
            }
//            if (attributes.elementAt(i) != null && !"".equals(attributes.elementAt(i)))   {
//
//            } else {
//				if (i == MRTConstants.ATTRIBUTE_LOCAL_PREF || i == MRTConstants.ATTRIBUTE_MULTI_EXIT){
//                    toStr.append("\t\t<attribute").append(" type="+"\""+type+"\""+">").append(attributes.elementAt(i).toString()).append("</attribute>\n");
//
//                    //toStr.append("\t\t<attribute>").append("0").append("</attribute>\n");
//                } else if (i == MRTConstants.ATTRIBUTE_ATOMIC_AGGREGATE) {
//                    toStr.append("\t\t<attribute>").append("NAG").append("</attribute>\n");
//                }
//			}
        }
        toStr.append("\t</attributes>\n");
        return toStr.toString();
    }

    public static void dumpGraphml(ASContainer ases, Writer writer, File edgeTmpFile) throws IOException {
        writer.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"   \n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  \n" +
                "         xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \n" +
                "           http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\"  \n" +
                "         xmlns:y=\"http://www.yworks.com/xml/graphml\"> \n ");
        writer.write("\t<graph id=\"BGPInternetMap\" edgedefault=\"directed\">\n");
        writer.write("\t<key id=\"ASName\" for=\"node\" type=\"string\"/>\n");
        writer.write("\t<key id=\"IPv4PrefixCount\" for=\"node\" type=\"int\"/>\n");
        writer.write("\t<key id=\"IPv6PrefixCount\" for=\"node\" type=\"int\"/>\n");
        writer.write("\t<key id=\"IPv4Flag\" for=\"node\" type=\"string\"/>\n");
        writer.write("\t<key id=\"IPv6Flag\" for=\"node\" type=\"string\"/>\n");
        writer.write("\t<key id=\"IPv4AddressSpace\" for=\"node\" type=\"int\"/>\n");
        writer.write("\t<key id=\"IPv6AddressSpace\" for=\"node\" type=\"int\"/>\n");
        writer.write("\t<key id=\"Country\" for=\"node\" type=\"string\"/>\n");
        writer.write("\t<key id=\"Description\" for=\"node\" type=\"string\"/>\n");

        dumpNodes(ases, writer,"\t\t\t");
        dumpEdges(edgeTmpFile, writer,"\t\t\t");
        writer.write("\t</graph>  \n");
        writer.write("</graphml>");
    }

    public static void dumpNodes(ASContainer ases, Writer writer, String tabs) throws IOException {
        TreeMap<String, AsNameLoader.ASName> asNames = AsNameLoader.retrieveAsNames();
        for (ASInfo asInfo : ases.getAsInfoMap().values()) {
            writer.write(tabs+"<node id=\""+asInfo.getId()+"\">\n");
            List<PrefixInfo> prefixInfo = asInfo.getPrefixInfo();
            Map<String, Integer> ipvXcounter = countIpVXPrefixes(prefixInfo);
            Map<String, Long> ipvXAddressSpace = countIpVXAdressSpace(prefixInfo);
            writer.write(tabs+"\t<data key=\"IPv4AddressSpace\">"+ ipvXAddressSpace.get(IPV4_KEY)+"</data>\n");
            writer.write(tabs+"\t<data key=\"IPv6AddressSpace\">"+ ipvXAddressSpace.get(IPV6_KEY)+"</data>\n");
            writer.write(tabs+"\t<data key=\"countOriginatedPrefixes\">"+ prefixInfo.size()+"</data>\n");
            writer.write(tabs+"\t<data key=\"IPv4Flag\">"+ ("" + (ipvXcounter.get(IPV4_KEY) > 0)).toUpperCase()+"</data>\n");
            writer.write(tabs+"\t<data key=\"IPv6Flag\">"+ ("" + (ipvXcounter.get(IPV6_KEY) > 0)).toUpperCase()+"</data>\n");
            AsNameLoader.ASName asName = asNames.get(asInfo.getId());
            if (asName != null) {
                writer.write(tabs + "\t<data key=\"ASName\">" + asName.getName() + "</data>\n");
                writer.write(tabs + "\t<data key=\"Description\">" + asName.getDescription() + "</data>\n");
                writer.write(tabs + "\t<data key=\"Country\">" + asName.getCountry() + "</data>\n");

            }
            writer.write(tabs+"</node>\n");
        }
    }

    private static Map<String, Long> countIpVXAdressSpace(List<PrefixInfo> prefixInfoList) {
        HashMap<String, Long> result = new HashMap<String, Long>();
        long ipv4AddressSpace = 0;
        long ipv6AddressSpace = 0;
        for (PrefixInfo prefixInfo : prefixInfoList) {
            String prefix = prefixInfo.getPrefix();
            int slashIndex = prefix.indexOf("/");
            String maskStr = prefix.substring(slashIndex+1);
            int mask = Integer.parseInt(maskStr);
            if (prefix.contains(".")){ // IP_V4
                ipv4AddressSpace += powOfInt(2,(32-mask));
            } else { // IP_V6
                ipv6AddressSpace += powOfInt(2,(64-mask));
            }
        }
        result.put(IPV4_KEY, ipv4AddressSpace);
        result.put(IPV6_KEY, ipv6AddressSpace);
        return result;
    }

    private static long powOfInt(int M, int N){
        long powerOfInt = 1;
        for (int i=0;i<N;i++) {
            powerOfInt = M * powerOfInt;
        }
        return powerOfInt;
    }

    public static void dumpEdges(File edgeTmpFile, Writer writer, String tabs) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(edgeTmpFile));
        PrintWriter pw = new PrintWriter(writer);
        String line;
        while ((line = br.readLine()) != null) {
            pw.print(tabs);
            pw.println(line);
        }
        pw.flush();
    }

    private static void dumpAsPath(String[] path, Writer writer, String tabs, Set<String> edgesIds) throws IOException {
        int repeatCounter = 0;
        String lastNode = null;
        for (String node : path) {
            if (lastNode != null && lastNode.equals(node)) {
                repeatCounter++;
                continue;
            } else if (lastNode != null){
                String id;
                if (lastNode.compareTo(node) < 0) {
                    id = lastNode + "_" + node;
                } else {
                    id =  node + "_" + lastNode;
                }
                if (!edgesIds.contains(id)) {
                    edgesIds.add(id);
                    String edgeAttributes = "id=\"" + id + "\" source=\"" + lastNode + "\"" + " target=\"" + node + "\"";
                    if (repeatCounter > 0) {
                        writer.write(tabs + "<edge " + edgeAttributes + "\">\n");
                        writer.write(tabs + "\t<data key=\"weight\">" + repeatCounter + "</data>\n");
                        writer.write(tabs + "</edge>\n");
                    } else {
                        writer.write(tabs + "<edge " + edgeAttributes + "/>\n");
                    }
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


	private static void usage() {
		PrintStream ps = System.err;

		ps.println("route_btoa <options> f1 ...");
		ps.println("  -h        print this help message");
		ps.println("  -m        legacy compatibility wth MRT: include all records");
		ps.println("  -4        print IPv4 prefixes only");
		ps.println("  -6        print IPv6 prefixes only");
		ps.println("  -p peer   print updates from a specific peer only");
		ps.println("  -P prefix print updates for a specific prefix only");
		if (Debug.compileDebug)
			ps.println("  -D        enable debugging");
		ps.println("  -o as     print updates generated by one AS only");
		ps.println("  -t as     print updates where AS is in ASPATH");
        ps.println(" -f fName   dump to a file with name FileName");
        ps.println(" -x         dump in a XML file");
        ps.println("         -4 and -6 together are not allowed");
		ps.println(" f1 ... are filenames or URL's");
        ps.println(" Use URL's according to the server's policies");
		ps.println(" Only prints records in machine readable format\n");

	}

}
