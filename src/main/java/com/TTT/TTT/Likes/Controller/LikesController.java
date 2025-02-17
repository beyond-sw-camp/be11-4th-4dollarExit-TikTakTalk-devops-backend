package com.TTT.TTT.Likes.Controller;

import com.TTT.TTT.Common.dtos.CommonDto;
import com.TTT.TTT.Likes.Service.LikesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("ttt/Likes")
public class LikesController {

    private final LikesService likesService;

    public LikesController(LikesService likesService) {
        this.likesService = likesService;
    }

//  1.좋아요 누르기(한번 누르면 좋아요, 다시 누르면 좋아요 해제)
    @PostMapping("/add/{id}")
    public ResponseEntity<?> addLike(@PathVariable Long id){
        Map<String,Object>likeStatus = likesService.toggleLike(id);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"toggle succeed",likeStatus),HttpStatus.OK);

    }
}
