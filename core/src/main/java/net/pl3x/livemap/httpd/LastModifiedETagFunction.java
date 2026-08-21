package net.pl3x.livemap.httpd;

import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.util.ETag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.pl3x.livemap.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class LastModifiedETagFunction implements PathResourceManager.ETagFunction {
    LastModifiedETagFunction() {
    }

    @Override
    @Nullable
    public ETag generate(@NotNull Path path) {
        try {
            return new ETag(false, Long.toString(Files.getLastModifiedTime(path).toMillis()));
        } catch (IOException e) {
            Logger.error("ETag function error:", e);
            return null;
        }
    }
}
