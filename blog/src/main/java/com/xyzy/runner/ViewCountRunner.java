package com.xyzy.runner;

import com.xyzy.constants.SystemConstants;
import com.xyzy.domain.entity.Article;
import com.xyzy.mapper.ArticleMapper;
import com.xyzy.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ViewCountRunner implements CommandLineRunner {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void run(String... args) {
        List<Article> articles = articleMapper.selectList(null);
        Map<String, Long> viewCountMap = articles.stream()
                .collect(Collectors.toMap(article -> article.getId().toString(), Article::getViewCount));
        redisCache.setCacheMap(SystemConstants.REDIS_ARTICLE_VIEWCOUNT, viewCountMap);
    }
}
