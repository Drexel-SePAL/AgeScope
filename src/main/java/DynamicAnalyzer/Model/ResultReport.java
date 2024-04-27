package DynamicAnalyzer.Model;

import java.util.HashSet;

public class ResultReport {
    public String packageSha256;
    public boolean stringMatched;
    public HashSet<String> matchedLines;
}
