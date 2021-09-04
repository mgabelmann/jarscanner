package ca.mikegabelmann.jarscanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author mgabelmann
 */
public class JarProcessor implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(JarProcessor.class);
    private static final String FILENAME_MANIFEST = "MANIFEST.MF";
    private static final String FILENAME_POM = "pom.xml";

    private Path file;
    private String version = null;


    /**
     * Constructor.
     * @param file file to process
     */
    public JarProcessor(final Path file) {
        this.file = file;
    }

    @Override
    public void run() {
        try {
            ZipFile zf = new ZipFile(file.toFile());
            Enumeration<? extends ZipEntry> e = zf.entries();

            boolean processed = false;
            FileType ft = null;

            while (e.hasMoreElements() && !processed) {
                ZipEntry ze = e.nextElement();

                if (ze.getName().endsWith(FILENAME_MANIFEST)) {
                    ft = FileType.MANIFEST;
                    this.version = this.processManifest(zf, ze);

                } else if (ze.getName().endsWith(FILENAME_POM)) {
                    ft = FileType.POM;
                    this.version = this.processPom(zf, ze);
                }

                if (this.version != null) {
                    processed = true;
                }
            }

            zf.close();

            if (version != null) {
                LOGGER.info("{}={}, version={}", ft, zf.getName(), version);

            } else {
                LOGGER.warn("{}={}, version not found", ft, zf.getName());
            }

        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    /**
     * Process MANIFEST.MF file.
     * @param zf zip file
     * @param ze zip entry
     * @return version or null
     * @throws IOException error
     */
    private String processManifest(final ZipFile zf, final ZipEntry ze) throws IOException {
        LOGGER.trace("processing  manifest {}", zf.getName());

        //try with resources which will be closed automatically
        try (BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)))) {
            for (String line; (line = br.readLine()) != null;) {
                //FIXME: there should be a better way to do this with REGEX

                /*if (line.startsWith("Specification-Version: ")) {
                    return line.replace("Specification-Version: ", "");

                } else */

                if (line.startsWith("Implementation-Version: ")) {
                    //some spring libs use this
                    return line.replace("Implementation-Version: ", "");

                } else if (line.startsWith("Verison: ")) {
                    return line.replace("Version: ", "");

                } else if (line.startsWith("Bundle-Version: ")) {
                    return line.replace("Bundle-Version: ", "");
                }
            }
        }

        return null;
    }

    /**
     * Process pom.xml file.
     * @param zf zip file
     * @param ze zip entry
     * @return version or null
     * @throws IOException error
     */
    private String processPom(final ZipFile zf, final ZipEntry ze) throws IOException {
        LOGGER.trace("processing  pom {}", zf.getName());

        //try with resources which will be closed automatically
        try (BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)))) {
            for (String line; (line = br.readLine()) != null;) {
                if (line.contains("<version>")) {
                    return line.replaceAll("\\s", "").replaceAll("<[/]?version>", "");
                }
            }
        }

        return null;
    }

}
