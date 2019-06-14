package com.hyzs;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author ：liyong
 * @date ：Created in 2019/5/14 0014 8:49
 */
public interface PostRepository extends ElasticsearchRepository<Post,String> {

}
