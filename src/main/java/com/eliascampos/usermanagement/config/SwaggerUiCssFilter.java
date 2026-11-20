package com.eliascampos.usermanagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Injeta a folha de estilo propria no HTML do Swagger UI. O buffer evita o conflito
 * entre getOutputStream() e getWriter() que ocorre com o ContentCachingResponseWrapper.
 */
@Component
public class SwaggerUiCssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        boolean swaggerIndex = uri.endsWith("/swagger-ui.html") || uri.endsWith("/swagger-ui/index.html");
        if (!swaggerIndex) {
            filterChain.doFilter(request, response);
            return;
        }

        BufferingResponseWrapper wrapper = new BufferingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);

        byte[] body = wrapper.getContent();
        String text = new String(body, StandardCharsets.UTF_8);

        if (text.contains("</head>") && !text.contains("swagger-custom.css")) {
            text = text.replace("</head>",
                    "<link rel=\"stylesheet\" type=\"text/css\" href=\"/swagger-custom.css\"></head>");
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            response.setContentType("text/html;charset=UTF-8");
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } else {
            String contentType = wrapper.getContentType();
            if (contentType != null) {
                response.setContentType(contentType);
            }
            response.getOutputStream().write(body);
            response.getOutputStream().flush();
        }
    }

    private static final class BufferingResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private ServletOutputStream outputStream;
        private PrintWriter writer;

        BufferingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (outputStream == null) {
                outputStream = new ServletOutputStream() {
                    @Override
                    public void write(int b) {
                        buffer.write(b);
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setWriteListener(jakarta.servlet.WriteListener listener) {
                        // resposta bloqueante: nao ha listener assincrono
                    }
                };
            }
            return outputStream;
        }

        @Override
        public PrintWriter getWriter() {
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(buffer, getCharacterEncoding() == null
                        ? StandardCharsets.UTF_8
                        : java.nio.charset.Charset.forName(getCharacterEncoding())));
            }
            return writer;
        }

        @Override
        public void flushBuffer() {
            // buffer em memoria: nada a descarregar
        }

        byte[] getContent() {
            if (writer != null) {
                writer.flush();
            }
            return buffer.toByteArray();
        }
    }
}
