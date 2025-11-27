package ratelimiting.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest; // Importação necessária
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap; // Importação para Mapa Concorrente

@Component
public class RateLimitFilter implements Filter {


    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();


    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
                5,
                Refill.intervally(5, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }


    private Bucket getBucket(String key) {

        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {


        String clientIp = request.getRemoteAddr();


        Bucket ipBucket = getBucket(clientIp);


        if (ipBucket.tryConsume(1)) {

            chain.doFilter(request, response);
        } else {

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