package StaticUIAnalyzer.Decoder;

import CommonUtils.CommonString;
import StaticUIAnalyzer.Base.XMLDecoderBase;

import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

public class LayoutXMLDecoder extends XMLDecoderBase {
    public final StringsXMLDecoder stringsXMLDecoder;

    public LayoutXMLDecoder(String xmlFile, StringsXMLDecoder stringsXMLDecoder) {
        super(xmlFile);
        this.stringsXMLDecoder = stringsXMLDecoder;
    }

    public Map.Entry<String, String> ageCheckLayout() {
        var match = foundPatternFromTags(CommonString.pattern);
        if (!match.isEmpty()) {
            return Map.entry(this.xmlFile.getPath(), match);
        }

        return null;
    }

    public Map.Entry<String, String> idCheckLayout() {
        var pattern = Pattern.compile("身\\s*份\\s*证\\s*(号)?\\s*(码)?\\s*|姓\\s*名");
        var numOfEditText = 0;
        for (var tag : this.tags) {
            if (tag.tagName.equals("EditText")) {
                numOfEditText += 1;
            }
        }

        var match = foundPatternFromTags(pattern);
        if (!match.isEmpty() && numOfEditText >= 2) {
            return Map.entry(this.xmlFile.getPath(), match);
        }

        return null;
    }

    private String foundPatternFromTags(Pattern pattern) {
        for (var e : this.tags) {
            var val = e.attributes.getOrDefault("android:text", "");
            if (val.isEmpty()) {
                val = e.attributes.getOrDefault("android:contentDescription", "");
            }

            if (!val.isEmpty()) {
                val = stringsXMLDecoder.values.getOrDefault(val, "");
                if (val.isEmpty()) {
                    continue;
                }

                var matcher = pattern.matcher(val);
                var resList = new HashSet<String>();
                while (matcher.find()) {
                    resList.add(matcher.group());
                }

                return resList.isEmpty() ? "" : String.join(", ", resList);
            }
        }

        return "";
    }
}
