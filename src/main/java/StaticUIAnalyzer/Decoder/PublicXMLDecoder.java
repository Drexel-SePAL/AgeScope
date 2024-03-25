package StaticUIAnalyzer.Decoder;

import StaticUIAnalyzer.Base.XMLDecoderBase;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PublicXMLDecoder extends XMLDecoderBase {
    public Map<String, String> values;

    public PublicXMLDecoder(String xmlFile) throws ParserConfigurationException, IOException, SAXException {
        super(xmlFile);
        this.values = new HashMap<>();
        this.parse();
        this.prepare();
    }

    private void prepare() {
        for (var tag : this.tags) {
            if (!tag.tagName.equals("public") && !tag.attributes.getOrDefault("type", "").equals("layout")) {
                continue;
            }

            this.values.put(tag.attributes.get("name"), tag.attributes.get("id"));
        }
    }
}
