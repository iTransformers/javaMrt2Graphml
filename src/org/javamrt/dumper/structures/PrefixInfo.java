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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrefixInfo that = (PrefixInfo) o;

        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return prefix != null ? prefix.hashCode() : 0;
    }

    @Override
    public String toString() {
        return prefix;
    }
}
