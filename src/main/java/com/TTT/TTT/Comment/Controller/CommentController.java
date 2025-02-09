package com.TTT.TTT.Comment.Controller;

import com.TTT.TTT.Comment.Dtos.CommentCreateDto;
import com.TTT.TTT.Comment.Dtos.CommentUpdateDto;
import com.TTT.TTT.Comment.Service.CommentService;
import com.TTT.TTT.Common.dtos.CommonDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("ttt/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    //   1.댓글, 대댓글달기
    @PostMapping("create")
    public ResponseEntity<?> save(@RequestBody @Valid CommentCreateDto commentCreateDto) {
        commentService.save(commentCreateDto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "comment is created successfully", "sucess"), HttpStatus.CREATED);
    }

    //  2.댓글 수정
    @PatchMapping("update/{id}")
    public ResponseEntity<?> update(@RequestBody @Valid CommentUpdateDto commentUpdateDto, @PathVariable Long id) {
        commentService.update(commentUpdateDto, id);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "comment update is done successfully", "sucess"), HttpStatus.OK);
    }

    //  3.댓글 삭제
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        commentService.delete(id);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "comment is deleted successfully", "sucess"), HttpStatus.OK);

    }
}
