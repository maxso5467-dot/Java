package com.xyzy.job;

import com.xyzy.constants.SystemConstants;
import com.xyzy.domain.entity.Article;
import com.xyzy.service.ArticleService;
import com.xyzy.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UpdateViewCountJob {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ArticleService articleService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void updateViewCount() {
        Map<String, Long> viewCountMap = redisCache.getCacheMap(SystemConstants.REDIS_ARTICLE_VIEWCOUNT);
        if (viewCountMap == null || viewCountMap.isEmpty()) return;
        viewCountMap.forEach((key, value) -> {
            Article article = new Article();
            article.setId(Long.valueOf(key));
            article.setViewCount(value);
            articleService.updateById(article);
        });
    }
}
