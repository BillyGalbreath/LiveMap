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

    @Key("error-invalid-world")
    public static String ERROR_INVALID_WORLD = "Invalid world name or id";
    @Key("error-missing-world")
    public static String ERROR_MISSING_WORLD = "You must enter a world name or id!";

    @Key("command-fullrender-started")
    public static String FULLRENDER_STARTED = "Starting fullrender on <grey><world>";
    @Key("command-fullrender-finished")
    public static String FULLRENDER_FINISHED = "Finished fullrender on <grey><world></grey> in <yellow><elapsed></yellow> at <dark_aqua><cps>cps</dark_aqua> (<grey><chunks></grey> total chunks scanned)";
    @Key("command-fullrender-errored")
    public static String FULLRENDER_ERRORED = "<red>Fullrender errored on <grey><world>\n<red>Error: <error>";

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
