package com.nick.mantis_jira_bridge.webhook;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Buffers the request body so it can be read multiple times (by the filter and by Jackson). */
class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    byte[] getCachedBody() {
        return cachedBody;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(new ByteArrayInputStream(cachedBody));
    }

    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final InputStream source;

        CachedBodyServletInputStream(InputStream source) {
            this.source = source;
        }

        @Override public boolean isFinished() { try { return source.available() == 0; } catch (IOException e) { return true; } }
        @Override public boolean isReady() { return true; }
        @Override public void setReadListener(ReadListener listener) {}
        @Override public int read() throws IOException { return source.read(); }
        @Override public int read(byte[] b, int off, int len) throws IOException { return source.read(b, off, len); }
    }
}
