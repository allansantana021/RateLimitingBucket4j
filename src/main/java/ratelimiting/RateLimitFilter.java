package ratelimiting;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter implements Filter {

    // 5 requisições por minuto
    private final Bucket bucket = Bucket.builder()
            .addLimit(
                    Bandwidth.classic(
                            5,                             // quantidade permitida
                            Refill.intervally(5, Duration.ofMinutes(1)) // renova a cada minuto
                    )
            )
            .build();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (bucket.tryConsume(1)) {
            // permite continuar
            chain.doFilter(request, response);
        } else {
            // bloqueia
            HttpServletResponse http = (HttpServletResponse) response;
            http.setStatus(429); // Too Many Requests
            http.getWriter().write("Limite de requisições excedido. Tente novamente em instantes.");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}
