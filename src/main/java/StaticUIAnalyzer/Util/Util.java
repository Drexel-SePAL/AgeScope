package StaticUIAnalyzer.Util;

import soot.Value;

public class Util {
    public static boolean sootValueCompare(Value sootVal, String val) {
        return sootVal.toString().equals(val);
    }
}
