package StaticUIAnalyzer.Model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResultReport {
    public String packageSha256;
    public boolean activityResult;
    public boolean dialogResult;
    public Map<String, Boolean> activity = new HashMap<>();
    public Map<String, Boolean> dialog = new HashMap<>();
    public Set<String> layoutIds = new HashSet<>();
    public Map<String, Map<String, String>> layouts = new HashMap<>();
    public double timeInSecond;
}
