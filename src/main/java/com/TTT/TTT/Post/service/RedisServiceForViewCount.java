package com.TTT.TTT.Post.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class RedisServiceForViewCount {
    //"post:viewCount"를 중복으로 사용하므로, 상수로 선언하여 편하게 사용(나중에 키값이 바뀌더라도 한번에 수정하기 용이해서 상수선언하였음)
    private static final String VIEW_COUNT_PREFIX = "post-viewCount-"; // 레디스에 저장 될 조회수 키
    private static final String VIEW_LOCK_PREFIX = "post-viewLock-"; //

    @Qualifier("viewCount")
    private final RedisTemplate<String,String> redisTemplate;

    public RedisServiceForViewCount(@Qualifier("viewCount") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    //레디스에서 조회수 증가시키는 함수
    public void increaseViewCount(Long postId,String userId){
        String key = VIEW_COUNT_PREFIX + postId;
        String lockKey = VIEW_LOCK_PREFIX + postId +"-" + userId;

        if(Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))){
            return;
        } //유저아이디와 아이디가 본 게시물id를 조합한 키값이 이미 있다면 그 유저는 그 게시물을 1분이내에 봐서 조회수를 증가시켰다는 의미로 아래 로직을 타지않고 리턴시킴
         //그런데 redisTemplate.hasKey(lockKey).equals(true)를 하지 않는 이유는 앞에서 null값이면 에러가 터지니까  반대로 적용하였음

        redisTemplate.opsForValue().increment(key); //레디스에 해당 해당 게시물id키값에 대해 조회수를 1증가시킴
        redisTemplate.opsForValue().set(lockKey,"1", Duration.ofMinutes(1)); // 유저아이디와 아이디가 본 게시물id를 조합한 키값에 대한 밸류를 1로 설정하고 1분뒤 삭제
    }
    //레디스에서 조회수 가지고 오는 함수
    public int getViewCount(Long postId){
        String key = VIEW_COUNT_PREFIX + postId;
        String countStr = redisTemplate.opsForValue().get(key); //레디스에 해당 키가 없으면 countStr = null이 됨
        return countStr == null ? 0 : Integer.parseInt(countStr);
    }

    //레디스에 저장되어 있는 데이터 rdb반영하기 위한 작업
    public Map<Long,Integer> getAllViewCountForRdb(){
        Set<String>keys = redisTemplate.keys(VIEW_COUNT_PREFIX+"*"); //.keys는 특정패턴을 가진 모든 키 목록을 가지고 오는 것.우리는 post-viewCount-로 시작되는 모든 키를 가지고 온다.
        Map<Long,Integer> viewCounts = new HashMap<>();

        if(keys !=null){
            for(String key :keys){
                Long postId = Long.parseLong(key.replace(VIEW_COUNT_PREFIX,""));//키값에서 앞에 우리가 붙인 접두사 post-viewCount-를 없애면 id값만 남는다.
                Integer count = Integer.parseInt(redisTemplate.opsForValue().get(key));
                viewCounts.put(postId, count == null? 0 : count);
            }
        }
        return viewCounts;
    }

}
