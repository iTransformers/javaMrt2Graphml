package org.javamrt.dumper.structures;

import java.util.List;

/**
 * Created by vasko on 8/20/14.
 */
public class ASPathInfo {
    private List<String> path;

    public ASPathInfo(List<String> path) {
        this.path = path;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "ASPathInfo{" +
                "path=" + path +
                '}';
    }
}
