package StaticUIAnalyzer.Base;

import StaticUIAnalyzer.Model.XMLTag;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XMLDecoderBase {
    public final List<XMLTag> tags;
    public File xmlFile;

    public XMLDecoderBase(String xmlFile) {
        this.xmlFile = new File(xmlFile);
        this.tags = new ArrayList<>();
    }

    public void parse() throws ParserConfigurationException, IOException, SAXException {
        var dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        var dBuilder = dbFactory.newDocumentBuilder();
        var doc = dBuilder.parse(this.xmlFile);
        doc.getDocumentElement().normalize();

        flatten(doc.getDocumentElement(), "");
    }

    public void flatten(Node node, String path) {
        var tag = new XMLTag(node.getNodeName());
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        var element = (Element) node;
        var currentPath = path.isEmpty() ? element.getTagName() : path + "." + element.getTagName();

        if (element.getChildNodes().getLength() == 1 && element.getFirstChild().getNodeType() == Node.TEXT_NODE) {
            tag.text = element.getTextContent();
        }

        var attrMap = element.getAttributes();
        for (int i = 0; i < attrMap.getLength(); i++) {
            var attr = attrMap.item(i);
            tag.attributes.put(attr.getNodeName(), attr.getNodeValue());
        }

        var children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            flatten(children.item(i), currentPath);
        }

        this.tags.add(tag);
    }
}
