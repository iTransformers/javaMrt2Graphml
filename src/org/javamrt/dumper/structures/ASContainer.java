package org.javamrt.dumper.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vasko on 8/20/14.
 */
public class ASContainer {
    private Map<String, ASInfo> asInfoMap = new HashMap<String, ASInfo>();

    private List<ASPathInfo> asPathInfoList = new ArrayList<ASPathInfo>();

    public Map<String, ASInfo> getAsInfoMap() {
        return asInfoMap;
    }

    public void setAsInfoMap(Map<String, ASInfo> asInfoMap) {
        this.asInfoMap = asInfoMap;
    }

    public List<ASPathInfo> getAsPathInfoList() {
        return asPathInfoList;
    }

    public void setAsPathInfoList(List<ASPathInfo> asPathInfoList) {
        this.asPathInfoList = asPathInfoList;
    }

    @Override
    public String toString() {
        return "ASContainer{" +
                "asInfoMap=" + asInfoMap +
                ", asPathInfoMap=" + asPathInfoList +
                '}';
    }
}
