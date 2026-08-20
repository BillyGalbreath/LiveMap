package net.pl3x.livemap.configuration;

import java.nio.file.Path;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.util.FileUtil;

/**
 * LiveMap's language config.
 */
public class Lang extends AbstractConfig {
    @Key("internal-webserver-started")
    public static String HTTPD_STARTED = "Internal webserver started on <bind> port(s) <port>";
    @Key("internal-webserver-stopped")
    public static String HTTPD_STOPPED = "Internal webserver stopped";
    @Key("internal-webserver-error")
    public static String HTTPD_ERROR = "An error occurred starting the internal webserver";
    @Key("internal-webserver-disabled")
    public static String HTTPD_DISABLED = "Internal webserver is disabled";

    private static final Lang LANG = new Lang();

    private Lang() {
        Path dir = LiveMap.api().getDataPath().resolve("lang");

        // extract locale dir from jar
        FileUtil.extractDir("/lang/", dir, false);

        super(dir.resolve(Config.LANGUAGE_FILE));
    }

    /**
     * Reloads configuration from YAML file.
     */
    public static void reload() {
        LANG.reload0();
    }
}
