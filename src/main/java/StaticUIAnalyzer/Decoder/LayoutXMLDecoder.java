package StaticUIAnalyzer.Decoder;

import StaticUIAnalyzer.Base.XMLDecoderBase;

import java.util.regex.Pattern;

public class LayoutXMLDecoder extends XMLDecoderBase {
    public final StringsXMLDecoder stringsXMLDecoder;

    public LayoutXMLDecoder(String xmlFile, StringsXMLDecoder stringsXMLDecoder) {
        super(xmlFile);
        this.stringsXMLDecoder = stringsXMLDecoder;
    }

    public String ageCheckLayout() {
        var pattern = Pattern.compile("adult(s)?( only)?|(over )?[1-2]\\d\\+?|under( )?age|age of [1-2]\\d|af_num_adults");

        if (foundPatternFromTags(pattern)) {
            return this.xmlFile.getPath();
        }

        return "";
    }

    public String idCheckLayout() {
        var pattern = Pattern.compile("身\\s*份\\s*证\\s*|姓\\s*名");
        var numOfEditText = 0;
        for (var tag : this.tags) {
            if (tag.tagName.equals("EditText")) {
                numOfEditText += 1;
            }
        }

        if (foundPatternFromTags(pattern) && numOfEditText >= 2) {
            return this.xmlFile.getPath();
        }

        return "";
    }

    private boolean foundPatternFromTags(Pattern pattern) {
        for (var e : this.tags) {
            var val = e.attributes.getOrDefault("android:text", "");
            if (val.isEmpty()) {
                val = e.attributes.getOrDefault("android:contentDescription", "");
            }

            if (!val.isEmpty()) {
                val = stringsXMLDecoder.values.getOrDefault(val, "");
                if (!val.isEmpty() && pattern.matcher(val).find()) {
                    return true;
                }
            }
        }

        return false;
    }
}
