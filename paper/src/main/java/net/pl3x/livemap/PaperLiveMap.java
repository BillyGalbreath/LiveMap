package net.pl3x.livemap;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.nio.file.Path;
import java.util.List;
import net.pl3x.livemap.command.LiveMapCommand;
import net.pl3x.livemap.command.PaperSource;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.configuration.Lang;
import net.pl3x.livemap.httpd.HttpdServer;
import net.pl3x.livemap.util.FileUtil;
import net.pl3x.livemap.world.PaperWorldRegistry;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PaperLiveMap extends JavaPlugin implements LiveMap {
    private Path webDir;
    private Path tilesDir;

    private final PaperWorldRegistry worldRegistry = new PaperWorldRegistry();

    private HttpdServer httpdServer;
    private Metrics metrics;

    public PaperLiveMap() {
        super();
        Provider.api = this;
        Logger.logger = getLogger();
    }

    @Override
    public void onEnable() {
        Config.reload();
        Lang.reload();

        Path dir = Path.of(Config.WEB_DIR);
        this.webDir = dir.isAbsolute() ? dir : getDataPath().resolve(dir);
        this.tilesDir = getWebDir().resolve("tiles");

        FileUtil.extractDir("/web/", getWebDir(), !Config.WEB_DIR_READONLY);

        // block registry

        // biome registry

        // render registry

        // world registry

        // internal webserver
        this.httpdServer = new HttpdServer();
        getHttpdServer().start();

        // register commands
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
            event.registrar().register(
                new LiveMapCommand<>(PaperSource.getConverter()).build(),
                "LiveMap command. '/map help'",
                List.of("map")
            )
        );

        // scheduler

        // bstats metrics
        this.metrics = new Metrics(this, 26542);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);

        if (this.metrics != null) {
            this.metrics.shutdown();
            this.metrics = null;
        }

        if (this.httpdServer != null) {
            getHttpdServer().stop();
            this.httpdServer = null;
        }
    }

    @Override
    @NotNull
    public Path getDataPath() {
        return super.getDataPath();
    }

    @Override
    @NotNull
    public Path getWebDir() {
        return this.webDir;
    }

    @Override
    @NotNull
    public Path getTilesDir() {
        return this.tilesDir;
    }

    @Override
    @NotNull
    public HttpdServer getHttpdServer() {
        return this.httpdServer;
    }

    @Override
    public @NotNull PaperWorldRegistry getWorldRegistry() {
        return this.worldRegistry;
    }
}
