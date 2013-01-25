package uk.co.theboo.maven.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

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
}
