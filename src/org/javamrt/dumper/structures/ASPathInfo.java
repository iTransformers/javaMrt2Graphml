package org.javamrt.dumper.structures;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vasko on 8/20/14.
 */
public class ASPathInfo {
    private List<ASInfo> path = new ArrayList<ASInfo>();

    public ASPathInfo() {
    }

    public List<ASInfo> getPath() {
        return path;
    }

    public void setPath(List<ASInfo> path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "ASPathInfo{" +
                "path=" + path +
                '}';
    }
}
