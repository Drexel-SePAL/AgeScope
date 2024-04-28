package StaticUIAnalyzer.Decoder;

import StaticUIAnalyzer.Base.XMLDecoderBase;

import java.util.Map;
import java.util.regex.Pattern;

public class LayoutXMLDecoder extends XMLDecoderBase {
    public final StringsXMLDecoder stringsXMLDecoder;

    public LayoutXMLDecoder(String xmlFile, StringsXMLDecoder stringsXMLDecoder) {
        super(xmlFile);
        this.stringsXMLDecoder = stringsXMLDecoder;
    }

    public Map.Entry<String, String> ageCheckLayout() {
        var pattern = Pattern.compile("adult(s)?( only)?|(over )?\\b(?:1[4-9]|2[0-9])\\b?|under( )?age|age of \\b(?:1[4-9]|2[0-9])\\b|af_num_adults|未成年|\\b(?:1[4-9]|2[0-9])\\b岁|年龄|real identity|ID No|实名");
        var match = foundPatternFromTags(pattern);
        if (!match.isEmpty()) {
            return Map.entry(this.xmlFile.getPath(), match);
        }

        return null;
    }

    public Map.Entry<String, String> idCheckLayout() {
        var pattern = Pattern.compile("身\\s*份\\s*证\\s*|姓\\s*名");
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
                var matcher = pattern.matcher(val);
                if (!val.isEmpty() && matcher.find()) {
                    return matcher.group();
                }
            }
        }

        return "";
    }
}
