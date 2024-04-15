package StaticUIAnalyzer.Util;

import soot.Value;

public class Utils {
    public static boolean sootValueCompare(Value sootVal, String val) {
        return sootVal.toString().equals(val);
    }
}
