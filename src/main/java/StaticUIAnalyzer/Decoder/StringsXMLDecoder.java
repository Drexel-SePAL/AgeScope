package StaticUIAnalyzer.Decoder;

import StaticUIAnalyzer.Base.XMLDecoderBase;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StringsXMLDecoder extends XMLDecoderBase {

    public Map<String, String> values;

    public StringsXMLDecoder(String filename) throws ParserConfigurationException, IOException, SAXException {
        super(filename);
        this.values = new HashMap<>();
        this.parse();
        this.prepare();
    }

    private void prepare() {
        for (var tag : this.tags) {
            if (!tag.tagName.equals("string")) {
                continue;
            }

            this.values.put("@string/" + tag.attributes.get("name"), tag.text);
        }
    }
}
