package org.javamrt.dumper.structures;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vasko on 8/20/14.
 */
public class ASInfo {
    private String name;
    private List<PrefixInfo> prefixInfo = new ArrayList<PrefixInfo>();

    public ASInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PrefixInfo> getPrefixInfo() {
        return prefixInfo;
    }

    public void setPrefixInfo(List<PrefixInfo> prefixInfo) {
        this.prefixInfo = prefixInfo;
    }

    @Override
    public String toString() {
        return "ASInfo{" +
                "name='" + name + '\'' +
                ", prefixInfo=" + prefixInfo +
                '}';
    }
}
