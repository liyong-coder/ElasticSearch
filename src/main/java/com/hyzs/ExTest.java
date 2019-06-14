package com.hyzs;

import com.hyzs.gz.common.core.util.CommonUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;

import java.util.ArrayList;
import java.util.Map;


/**
 * @author ：liyong
 * @date ：Created in 2019/5/14 0014 8:52
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ExTest {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private PostRepository postRepository;

    @Test
    public void singleTitle() {
        String key = "浣溪沙";
       HighlightBuilder.Field[] high = {
               new HighlightBuilder.Field("content").requireFieldMatch(false),
               new HighlightBuilder.Field("title").requireFieldMatch(false)
       };


        NativeSearchQuery nativeSearchQuery=new NativeSearchQueryBuilder()
                .withHighlightFields(high)
                .withPageable(PageRequest.of(0,5))
                .withQuery(QueryBuilders.multiMatchQuery("浣溪","content","title"))
                .build();
        AggregatedPage<Post> page = elasticsearchTemplate.queryForPage(nativeSearchQuery, Post.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                List<Post> list = new ArrayList<Post>();
                SearchHits hits = response.getHits();
                for (SearchHit searchHit : hits) {
                    if (hits.getHits().length <= 0) {
                        return null;
                    }
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    String fileId=  (String) sourceAsMap.get("fileId");
                    String title= (String) sourceAsMap.get("title");
                    String content= (String) sourceAsMap.get("content");
                    Post post = new Post();
                    HighlightField fieldtitle =searchHit.getHighlightFields().get("title");
                    HighlightField fieldcontent =searchHit.getHighlightFields().get("content");
                    if(fieldtitle==null){
                        post.setTitle(title);
                    }else{
                        post.setTitle(fieldtitle.fragments()[0].toString());
                    }
                    if(fieldcontent==null){
                        post.setContent(content);
                    }else{
                        post.setContent(fieldcontent.fragments()[0].toString());
                    }

                    post.setId(fileId);
                    list.add(post);
                }
                if (list.size() > 0) {
                    return new AggregatedPageImpl<T>((List<T>) list,pageable,response.getHits().getTotalHits());
                }
                return null;
            }
        });

        System.out.println(CommonUtils.object2Json(page));

    }




}
