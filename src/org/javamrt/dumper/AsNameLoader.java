package org.javamrt.dumper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsNameLoader {
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        TreeMap<String, ASName> asNames = retrieveAsNames();
        long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    public static TreeMap<String, ASName> retrieveAsNames() throws IOException {
        TreeMap<String, ASName> asNames;
        asNames = new TreeMap<String, ASName>();
        URL url = new URL("http://www.potaroo.net/bgp/iana/asn.txt");
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = reader.readLine();
        do {
            String nextLine = reader.readLine();
            if (nextLine == null) break;
            if (!nextLine.matches("^\\d+.*")){
                String nextLine2 = reader.readLine();
                line = line + " - " + nextLine + nextLine2;
                nextLine = reader.readLine();
            }
            ASName asName = parseLine(line);
            if(asName!=null){
                 asNames.put(asName.getId(), asName);
            }
            line = nextLine;
        } while (line != null);
        is.close();
        return asNames;
    }

    static ASName parseLine(String str) {
        String expr = "^(\\d+)\\s*(?:-)?\\s*(.*),\\s*(.*)$";
        String expr1 = "^(\\d+)\\s*(\\w+)?\\s*(?:-)?\\s*(.*),([a-zA-Z]+)";
        Pattern patter = Pattern.compile(expr);
        Matcher matcher = patter.matcher(str);
        if (matcher.matches()) {

            if (matcher.groupCount() == 4) {
                String id = matcher.group(1);
                String name = matcher.group(2);
                if (name == null) {
                    name = "AS" + id;
                }
                String org = matcher.group(3);
                String country = matcher.group(4);
                return new ASName(id, name, org, country);

            }else {
                String id = matcher.group(1);
                String name = matcher.group(2);
                String org = null;
                if (name == null) {
                    name = "AS" + id;
                }   else {
                    String [] names = name.split(" - ");
                    if (names[0]!=null){
                        name = names[0];

                    if (names.length == 2)
                        org = names[1];


                    }



                }

                String country = matcher.group(3);

                return new ASName(id,name ,org, country);


            }
        }
        else {
                System.out.println("Can not parse line: "+str);
            return null;

        }
    }
    public static class ASName {
        String id;
        String name;
        String description;
        String country;

        public ASName(String id, String name, String description, String country) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.country = country;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}
