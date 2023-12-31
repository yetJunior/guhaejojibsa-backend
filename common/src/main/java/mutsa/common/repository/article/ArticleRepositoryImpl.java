/**
 * @project backend
 * @author ARA
 * @since 2023-08-17 PM 4:33
 */

package mutsa.common.repository.article;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import mutsa.common.customRepository.Querydsl4RepositorySupport;
import mutsa.common.domain.filter.article.ArticleFilter;
import mutsa.common.domain.models.article.Article;
import mutsa.common.domain.models.article.QArticle;
import mutsa.common.domain.models.user.QUser;
import mutsa.common.dto.order.OrderResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import static mutsa.common.domain.models.article.QArticle.article;

@Repository
@Transactional
public class ArticleRepositoryImpl extends Querydsl4RepositorySupport implements ArticleRepositoryCustom {
    public ArticleRepositoryImpl() {
        super(Article.class);
    }

    public Page<Article> getPage(ArticleFilter articleFilter, Pageable pageable) {
        JPAQuery<Article> query = getQuery();

        doFilter(query, articleFilter);

        return getResultPage(query, pageable);
    }

    private JPAQuery<Article> getQuery() {
        return selectFrom(article);
    }

    private void doFilter(JPAQuery<Article> query, ArticleFilter articleFilter) {

        //  게시글 현재 상태에 따른 필터 처리 (숨김, 공개)
        switch (articleFilter.getArticleStatus()) {
            case LIVE, EXPIRED -> query.where(article.articleStatus.eq(articleFilter.getArticleStatus()));
        }

        //  게시글 현재 상태에 따른 필터 처리 (삭제[soft], 존재)
        switch (articleFilter.getStatus()) {
            case ACTIVE, DELETED -> query.where(article.status.eq(articleFilter.getStatus()));
        }

        if (StringUtils.hasText(articleFilter.getTitle())) {
            query.where(article.title.contains(articleFilter.getTitle()));
        }

        if (StringUtils.hasText(articleFilter.getDescription())) {
            query.where(article.description.contains(articleFilter.getDescription()));
        }

        if (StringUtils.hasText(articleFilter.getUsername())) {
            query.where(article.user.username.eq(articleFilter.getUsername()));
        }

        query.leftJoin(article.user).fetchJoin();
    }

    private Page<Article> getResultPage(JPAQuery<Article> query, Pageable pageable) {
        QueryResults<Article> queryResults = this.getQuerydsl().applyPagination(pageable, query).fetchResults();
        return new PageImpl<Article>(queryResults.getResults(), pageable, queryResults.getTotal());
    }
}
