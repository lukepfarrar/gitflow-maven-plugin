package uk.co.theboo.maven.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import org.apache.commons.io.IOUtils;
import org.apache.maven.archetype.common.MavenJDOMWriter;
import org.apache.maven.archetype.common.util.Format;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class PomUtils {

    public static Model readPom(final File pomFile) throws FileNotFoundException, IOException {
        FileReader fileReader = null;

        try {
            fileReader = new FileReader(pomFile);
            MavenXpp3Reader m = new MavenXpp3Reader();
            Model model;
            try {
                model = m.read(fileReader);
            } catch (XmlPullParserException ex) {
                throw new IOException("POM " + pomFile.getAbsolutePath() + " could not be parsed.", ex);
            }
//            model.setPomFile(pomFile);
            return model;
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }
    }

    public static void writePom(Model model, File pom) throws IOException {
        Writer writer = null;
        try {
            final SAXBuilder builder = new SAXBuilder();
            builder.setIgnoringBoundaryWhitespace(false);
            builder.setIgnoringElementContentWhitespace(false);

            final Document doc = builder.build(pom);

            String encoding = model.getModelEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
            }

            final Format format = Format.getRawFormat().setEncoding(encoding).setTextMode(Format.TextMode.PRESERVE);
            format.setLineSeparator(IOUtils.LINE_SEPARATOR);
            writer = WriterFactory.newWriter(pom, encoding);

            new MavenJDOMWriter().write(model, doc, writer, format);
        } catch (JDOMException ex) {
            throw new IOException("Error parsing " + pom.getName(), ex);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
}
