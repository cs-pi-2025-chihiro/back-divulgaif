
package br.com.divulgaifback.common.configs;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class HibernateCacheConfig {

    @Bean
    public Statistics hibernateStatistics(EntityManagerFactory entityManagerFactory) {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        return statistics;
    }

    @Bean
    public HealthIndicator cacheHealthIndicator(Statistics statistics) {
        return () -> {
            long hitCount = statistics.getSecondLevelCacheHitCount();
            long missCount = statistics.getSecondLevelCacheMissCount();
            long putCount = statistics.getSecondLevelCachePutCount();

            double hitRatio = hitCount + missCount > 0
                ? (double) hitCount / (hitCount + missCount) * 100
                : 0;

            return Health.up()
                    .withDetail("cache.hits", hitCount)
                    .withDetail("cache.misses", missCount)
                    .withDetail("cache.puts", putCount)
                    .withDetail("cache.hitRatio", String.format("%.2f%%", hitRatio))
                    .withDetail("query.cache.hits", statistics.getQueryCacheHitCount())
                    .withDetail("query.cache.misses", statistics.getQueryCacheMissCount())
                    .withDetail("query.cache.puts", statistics.getQueryCachePutCount())
                    .build();
        };
    }
}

