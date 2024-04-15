package StaticUIAnalyzer.Model;

import java.util.*;

public class ResultReport {
    public String packageName;
    public boolean activityResult;
    public boolean dialogResult;
    public List<Map<String, Boolean>> activity = new ArrayList<>();
    public List<Map<String, Boolean>> dialog = new ArrayList<>();
    public Set<String> layoutIds = new HashSet<>();
    public Set<String> layouts = new HashSet<>();
}
