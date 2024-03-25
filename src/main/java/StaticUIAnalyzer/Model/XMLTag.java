package StaticUIAnalyzer.Model;

import java.util.HashMap;
import java.util.Map;

public class XMLTag {
    public String tagName;
    public Map<String, String> attributes;
    public String text;

    public XMLTag(String tagName) {
        this.tagName = tagName;
        this.attributes = new HashMap<>();
        this.text = "";
    }
}
