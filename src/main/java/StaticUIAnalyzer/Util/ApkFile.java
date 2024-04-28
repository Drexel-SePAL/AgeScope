package StaticUIAnalyzer.Util;

import StaticUIAnalyzer.Decoder.LayoutXMLDecoder;
import StaticUIAnalyzer.Decoder.PublicXMLDecoder;
import StaticUIAnalyzer.Decoder.StringsXMLDecoder;
import brut.androlib.ApkDecoder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ApkFile implements AutoCloseable {
    public final Map<String, Map<String, String>> checkLayouts = new HashMap<>() {{
        put("age", new HashMap<>());
        put("id", new HashMap<>());
    }};
    public final Set<String> layoutIds = new HashSet<>();
    public final String apkPath;
    private final String out;
    public List<String> filenames;
    private StringsXMLDecoder stringsXmlDecoder;

    public ApkFile(String apkPath, String outDir) {
        this.apkPath = apkPath;
        this.out = outDir + FilenameUtils.removeExtension(Paths.get(apkPath).getFileName().toString());
    }

    public void decodeApk() {
        try {
            var decoder = new ApkDecoder(new File(this.apkPath));
            decoder.decode(new File(this.out));
        } catch (Exception ignored) {
        }
    }

    public void prepare() {
        this.prepareListOfLayouts();
        this.prepareStringsXml();
    }

    private void prepareListOfLayouts() {
        var matcher = FileSystems.getDefault().getPathMatcher("glob:**/res/layout*/*.xml");

        try (var directoryListing = Files.walk(Path.of(this.out))) {
            this.filenames = directoryListing.filter(Files::isRegularFile).filter(matcher::matches).map(Path::toString).toList();
        } catch (Exception ignored) {
        }
    }

    private void prepareStringsXml() {
        var path = this.out + "/res/values/strings.xml";
        try {
            var exists = new File(path).exists();
            if (exists) {
                this.stringsXmlDecoder = new StringsXMLDecoder(path);
            }
        } catch (Exception ignored) {
        }
    }

    public boolean examLayouts() {
        var existCheckLayouts = false;

        for (var i : this.filenames) {
            try {
                var decoder = new LayoutXMLDecoder(i, this.stringsXmlDecoder);
                decoder.parse();

                var ageCheckLayoutXml = decoder.ageCheckLayout();
                if (ageCheckLayoutXml != null) {
                    this.checkLayouts.get("age").put(ageCheckLayoutXml.getKey(), ageCheckLayoutXml.getValue());
                    existCheckLayouts = true;
                    this.layoutIds.add(this.getLayoutIdByName(i));
                }

                var idCheckLayoutXml = decoder.idCheckLayout();
                if (idCheckLayoutXml != null) {
                    this.checkLayouts.get("id").put(idCheckLayoutXml.getKey(), idCheckLayoutXml.getValue());
                    existCheckLayouts = true;
                    this.layoutIds.add(this.getLayoutIdByName(i));
                }
            } catch (Exception ignored) {
            }
        }

        return existCheckLayouts;
    }

    public String getLayoutIdByName(String layout) {
        try {
            var publicXmlDecoder = new PublicXMLDecoder(this.out + "/res/values/public.xml");
            var file = FilenameUtils.removeExtension(FilenameUtils.getBaseName(layout));

            return publicXmlDecoder.values.getOrDefault(file, "");
        } catch (Exception ignored) {
        }

        return "";
    }

    @Override
    public void close() {
        try {
            FileUtils.deleteDirectory(new File(this.out));
        } catch (Exception ignore) {
            System.err.println("[ApkFile]: unable to remove " + this.out);
        }
    }
}
