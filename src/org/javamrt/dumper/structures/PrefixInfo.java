package org.javamrt.dumper.structures;

/**
 * Created by vasko on 8/20/14.
 */
public class PrefixInfo {

    private String prefix;

    public PrefixInfo(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return "PrefixInfo{" +
                "prefix='" + prefix + '\'' +
                '}';
    }
}
