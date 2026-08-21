package net.pl3x.livemap.httpd;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.resource.ResourceManager;
import java.io.IOException;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.configuration.Lang;

/**
 * The internal undertow web server.
 */
public class HttpdServer {
    private Undertow server;

    /**
     * Constructs a new instance of HttpsServer.
     */
    public HttpdServer() {
        // Explicit constructor to satisfy Javadoc and linter tools
    }

    /**
     * Start the web server.
     */
    public void start() {
        if (this.server != null) {
            stop();
        }

        if (!Config.HTTPD_ENABLED) {
            Logger.info(Lang.HTTPD_DISABLED);
            return;
        }

        try (ResourceManager resourceManager = new FriendlyUrlPathResourceManager()) {
            this.server = Undertow.builder()
                .addHttpListener(Config.HTTPD_PORT, Config.HTTPD_BIND)
                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .setHandler(new TilesPathHandler(resourceManager))
                .build();

            this.server.start();

            Logger.info(Lang.HTTPD_STARTED
                .replace("<bind>", Config.HTTPD_BIND)
                .replace("<port>", Integer.toString(Config.HTTPD_PORT))
            );
        } catch (IOException e) {
            this.server = null;
            Logger.error(Lang.HTTPD_ERROR, e);
        }
    }

    /**
     * Stop the web server.
     */
    public void stop() {
        if (this.server == null) {
            return;
        }

        this.server.stop();
        this.server = null;

        Logger.info(Lang.HTTPD_STOPPED);
    }
}
