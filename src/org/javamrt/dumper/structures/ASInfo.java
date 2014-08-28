package org.javamrt.dumper.structures;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vasko on 8/20/14.
 */
public class ASInfo {
    private String id;
    private List<PrefixInfo> prefixInfo = new ArrayList<PrefixInfo>();

    public ASInfo(String name) {
        this.id = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PrefixInfo> getPrefixInfo() {
        return prefixInfo;
    }

    public void setPrefixInfo(List<PrefixInfo> prefixInfo) {
        this.prefixInfo = prefixInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASInfo asInfo = (ASInfo) o;

        if (id != null ? !id.equals(asInfo.id) : asInfo.id != null) return false;
        if (prefixInfo != null ? !prefixInfo.equals(asInfo.prefixInfo) : asInfo.prefixInfo != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (prefixInfo != null ? prefixInfo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ASInfo{" +
                "id='" + id + '\'' +
                ", prefixInfo=" + prefixInfo +
                '}';
    }
}
