package com.TTT.TTT.Common.service;

import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.Post.repository.PostRepository;
import com.TTT.TTT.Post.service.RedisServiceForViewCount;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class ViewCountBatchService {
    private final RedisServiceForViewCount redisServiceForViewCount;
    private final PostRepository postRepository;

    public ViewCountBatchService(RedisServiceForViewCount redisServiceForViewCount, PostRepository postRepository) {
        this.redisServiceForViewCount = redisServiceForViewCount;
        this.postRepository = postRepository;
    }

    @Scheduled(fixedRate = 600000) //10분마다 실행
    public void syncViewCounts(){
        Map<Long,Integer> viewCounts = redisServiceForViewCount.getAllViewCountForRdb();

//        내 방식 => 더티체킹을 활용하는 것.학원에서 배운 코드로 가능,쉽다.
        //        for(Long key :viewCounts.keySet()){
//            Post post = postRepository.findById(key).orElseThrow(()->new EntityNotFoundException("없는 게시물입니다"));
//            post.updateViewCounts(viewCounts.get(key));
//        }

//        gpt방식 => 대량의 데이터 업데이트에 유리(네이티브 쿼리 사용하여 성능에 주안점을 둔 커뮤니티 사이트에 맞는 방식이라고 함)
        for(Map.Entry<Long, Integer> entry : viewCounts.entrySet()){
            Long postId = entry.getKey();
            Integer views = entry.getValue();
            postRepository.increaseViewCountByValue(postId,views);
//     지금 increseViewCountByValue메서드는 레디스에 있는 조회수로 업데이트 하는 걸로 했는데 원래라면 레디스에 있는 조회수 만큼 증가시키는 걸로 바꾸고
//     여시에 레디스에 있는 조회수값을 삭제하는 코드가 추가되었어야함
//     그런데  ttt에선 게시물의 조회수를 db에서 갖고 오는게 아니라 레디스에서 계속 가지고 오는 걸로 세팅했기 때문에 이렇게 코드를 짬.(실제 커뮤니티에서 조회수는 그냥 db에서 가지고 오는게 나을듯)
        }





    }


}
