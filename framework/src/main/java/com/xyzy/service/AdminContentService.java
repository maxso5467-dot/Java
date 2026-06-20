package com.xyzy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyzy.domain.ResponseResult;
import com.xyzy.domain.dto.ArticleWriteRequest;
import com.xyzy.domain.dto.CategoryWriteRequest;
import com.xyzy.domain.dto.LinkWriteRequest;
import com.xyzy.domain.dto.TagWriteRequest;
import com.xyzy.domain.entity.Article;
import com.xyzy.domain.entity.ArticleTag;
import com.xyzy.domain.entity.Category;
import com.xyzy.domain.entity.Link;
import com.xyzy.domain.entity.Tag;
import com.xyzy.domain.vo.PageVo;
import com.xyzy.enums.AppHttpCodeEnum;
import com.xyzy.exception.SystemException;
import com.xyzy.mapper.ArticleMapper;
import com.xyzy.mapper.ArticleTagMapper;
import com.xyzy.mapper.CategoryMapper;
import com.xyzy.mapper.LinkMapper;
import com.xyzy.mapper.TagMapper;
import com.xyzy.utils.RedisCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service
public class AdminContentService {
    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final LinkMapper linkMapper;
    private final ArticleTagMapper articleTagMapper;
    private final RedisCache redisCache;

    public AdminContentService(ArticleMapper articleMapper, CategoryMapper categoryMapper, TagMapper tagMapper,
                               LinkMapper linkMapper, ArticleTagMapper articleTagMapper, RedisCache redisCache) {
        this.articleMapper = articleMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.linkMapper = linkMapper;
        this.articleTagMapper = articleTagMapper;
        this.redisCache = redisCache;
    }

    public ResponseResult<PageVo> articlePage(int pageNum, int pageSize, String title, String status) {
        LambdaQueryWrapper<Article> query = new LambdaQueryWrapper<Article>()
                .like(StringUtils.hasText(title), Article::getTitle, title)
                .eq(StringUtils.hasText(status), Article::getStatus, status)
                .orderByDesc(Article::getCreateTime);
        Page<Article> page = articleMapper.selectPage(new Page<>(pageNum, pageSize), query);
        return ResponseResult.okResult(new PageVo(page.getRecords(), page.getTotal()));
    }

    public ResponseResult<Article> articleDetail(Long id) {
        return ResponseResult.okResult(require(articleMapper.selectById(id)));
    }

    @Transactional
    public ResponseResult<Long> createArticle(ArticleWriteRequest request) {
        requireCategory(request.categoryId());
        Article article = toArticle(request);
        article.setViewCount(0L);
        articleMapper.insert(article);
        replaceArticleTags(article.getId(), request.tagIds());
        return ResponseResult.okResult(article.getId());
    }

    @Transactional
    public ResponseResult<Void> updateArticle(Long id, ArticleWriteRequest request) {
        require(articleMapper.selectById(id));
        requireCategory(request.categoryId());
        Article article = toArticle(request).setId(id);
        articleMapper.updateById(article);
        replaceArticleTags(id, request.tagIds());
        return ResponseResult.okResult();
    }

    @Transactional
    public ResponseResult<Void> deleteArticle(Long id) {
        require(articleMapper.selectById(id));
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        articleMapper.deleteById(id);
        return ResponseResult.okResult();
    }

    public ResponseResult<Void> changeArticleStatus(Long id, String status) {
        require(articleMapper.selectById(id));
        articleMapper.updateById(new Article().setId(id).setStatus(status));
        return ResponseResult.okResult();
    }

    public ResponseResult<PageVo> categoryPage(int pageNum, int pageSize, String name) {
        Page<Category> page = categoryMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Category>().like(StringUtils.hasText(name), Category::getName, name)
                        .orderByAsc(Category::getName));
        return ResponseResult.okResult(new PageVo(page.getRecords(), page.getTotal()));
    }

    public ResponseResult<List<Category>> categoryList() {
        return ResponseResult.okResult(categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getStatus, "0").orderByAsc(Category::getName)));
    }

    public ResponseResult<Long> createCategory(CategoryWriteRequest request) {
        ensureCategoryNameUnique(request.name(), null);
        Category category = new Category().setName(request.name()).setPid(defaultLong(request.pid(), -1L))
                .setDescription(request.description()).setStatus(defaultString(request.status(), "0"));
        categoryMapper.insert(category);
        return ResponseResult.okResult(category.getId());
    }

    public ResponseResult<Void> updateCategory(Long id, CategoryWriteRequest request) {
        require(categoryMapper.selectById(id));
        ensureCategoryNameUnique(request.name(), id);
        categoryMapper.updateById(new Category().setId(id).setName(request.name())
                .setPid(defaultLong(request.pid(), -1L)).setDescription(request.description())
                .setStatus(defaultString(request.status(), "0")));
        return ResponseResult.okResult();
    }

    public ResponseResult<Void> deleteCategory(Long id) {
        require(categoryMapper.selectById(id));
        long uses = articleMapper.selectCount(new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, id));
        if (uses > 0) throw new SystemException(AppHttpCodeEnum.DATA_CONFLICT);
        categoryMapper.deleteById(id);
        return ResponseResult.okResult();
    }

    public ResponseResult<PageVo> tagPage(int pageNum, int pageSize, String name) {
        Page<Tag> page = tagMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Tag>().like(StringUtils.hasText(name), Tag::getName, name)
                        .orderByAsc(Tag::getName));
        return ResponseResult.okResult(new PageVo(page.getRecords(), page.getTotal()));
    }

    public ResponseResult<List<Tag>> tagList() {
        return ResponseResult.okResult(tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getName)));
    }

    public ResponseResult<Long> createTag(TagWriteRequest request) {
        ensureTagNameUnique(request.name(), null);
        Tag tag = new Tag(); tag.setName(request.name()); tag.setRemark(request.remark());
        tagMapper.insert(tag);
        return ResponseResult.okResult(tag.getId());
    }

    public ResponseResult<Void> updateTag(Long id, TagWriteRequest request) {
        require(tagMapper.selectById(id));
        ensureTagNameUnique(request.name(), id);
        Tag tag = new Tag(); tag.setId(id); tag.setName(request.name()); tag.setRemark(request.remark());
        tagMapper.updateById(tag);
        return ResponseResult.okResult();
    }

    @Transactional
    public ResponseResult<Void> deleteTag(Long id) {
        require(tagMapper.selectById(id));
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, id));
        tagMapper.deleteById(id);
        return ResponseResult.okResult();
    }

    public ResponseResult<PageVo> linkPage(int pageNum, int pageSize, String name, String status) {
        Page<Link> page = linkMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<Link>()
                .like(StringUtils.hasText(name), Link::getName, name)
                .eq(StringUtils.hasText(status), Link::getStatus, status)
                .orderByDesc(Link::getCreateTime));
        return ResponseResult.okResult(new PageVo(page.getRecords(), page.getTotal()));
    }

    public ResponseResult<Long> createLink(LinkWriteRequest request) {
        Link link = toLink(request); linkMapper.insert(link); return ResponseResult.okResult(link.getId());
    }

    public ResponseResult<Void> updateLink(Long id, LinkWriteRequest request) {
        require(linkMapper.selectById(id));
        Link link = toLink(request); link.setId(id); linkMapper.updateById(link);
        return ResponseResult.okResult();
    }

    public ResponseResult<Void> deleteLink(Long id) {
        require(linkMapper.selectById(id)); linkMapper.deleteById(id); return ResponseResult.okResult();
    }

    private void replaceArticleTags(Long articleId, List<Long> tagIds) {
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId));
        if (tagIds == null) return;
        for (Long tagId : new LinkedHashSet<>(tagIds)) {
            if (tagId != null) articleTagMapper.insert(new ArticleTag(articleId, tagId));
        }
    }

    private Article toArticle(ArticleWriteRequest r) {
        return new Article().setTitle(r.title()).setContent(r.content()).setSummary(r.summary())
                .setCategoryId(r.categoryId()).setThumbnail(r.thumbnail())
                .setIsTop(defaultString(r.isTop(), "0")).setStatus(defaultString(r.status(), "1"))
                .setIsComment(defaultString(r.isComment(), "1"));
    }

    private Link toLink(LinkWriteRequest r) {
        Link link = new Link(); link.setName(r.name()); link.setLogo(r.logo()); link.setDescription(r.description());
        link.setAddress(r.address()); link.setStatus(defaultString(r.status(), "2")); return link;
    }

    private void ensureCategoryNameUnique(String name, Long excludedId) {
        long count = categoryMapper.selectCount(new LambdaQueryWrapper<Category>().eq(Category::getName, name)
                .ne(excludedId != null, Category::getId, excludedId));
        if (count > 0) throw new SystemException(AppHttpCodeEnum.DATA_CONFLICT);
    }

    private void ensureTagNameUnique(String name, Long excludedId) {
        long count = tagMapper.selectCount(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name)
                .ne(excludedId != null, Tag::getId, excludedId));
        if (count > 0) throw new SystemException(AppHttpCodeEnum.TAG_EXIST);
    }

    private void requireCategory(Long id) { require(categoryMapper.selectById(id)); }
    private <T> T require(T value) {
        if (value == null) throw new SystemException(AppHttpCodeEnum.DATA_NOT_FOUND);
        return value;
    }
    private String defaultString(String value, String fallback) { return StringUtils.hasText(value) ? value : fallback; }
    private Long defaultLong(Long value, Long fallback) { return Objects.requireNonNullElse(value, fallback); }
}
