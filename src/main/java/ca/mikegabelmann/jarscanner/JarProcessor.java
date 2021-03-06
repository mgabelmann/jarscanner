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

    private static final String REGEX1 = "Implementation-Version: ";
    private static final String REGEX2 = "Version: ";
    private static final String REGEX3 = "Bundle-Version: ";
    private static final String REGEX4 = "<version>";
    private static final String REGEX5 = "\\s";
    private static final String REGEX6 = "<[/]?version>";

    private static final String BLANK = "";

    private final Path file;

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
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("{}={}, version={}", ft, zf.getName(), version);
                }

            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("{}={}, version not found", ft, zf.getName());
                }
            }

        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    /**
     * Get version.
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Process MANIFEST.MF file.
     * @param zf zip file
     * @param ze zip entry
     * @return version or null
     * @throws IOException error
     */
    private String processManifest(final ZipFile zf, final ZipEntry ze) throws IOException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("processing  manifest {}", zf.getName());
        }

        //try with resources which will be closed automatically
        try (BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)))) {
            for (String line; (line = br.readLine()) != null;) {
                //FIXME: there should be a better way to do this with REGEX

                if (line.startsWith(REGEX1)) {
                    //some spring libs use this
                    return line.replace(REGEX1, BLANK);

                } else if (line.startsWith(REGEX2)) {
                    return line.replace(REGEX2, BLANK);    //NOT found in maven repo so far

                } else if (line.startsWith(REGEX3)) {
                    return line.replace(REGEX3, BLANK);
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

        //FIXME: it would be better to read the xml file and then process it as some maven projects have parents with a version set

        //try with resources which will be closed automatically
        try (BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)))) {
            for (String line; (line = br.readLine()) != null;) {
                if (line.contains(REGEX4)) {
                    //strip whitespace, <version>, </version> tags from line
                    return line.replaceAll(REGEX5, BLANK).replaceAll(REGEX6, BLANK);
                }
            }
        }

        return null;
    }

}
