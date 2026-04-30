package edu.cit.salgado.cookshare.features.comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentRepository commentRepository;

    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // Get all comments for a recipe
    @GetMapping
    public List<Comment> getComments(@RequestParam String recipeId) {
        return commentRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId);
    }

    // Post a comment
    @PostMapping
    public ResponseEntity<Comment> postComment(@RequestBody Map<String, String> body) {
        Comment comment = new Comment();
        comment.setUserEmail(body.get("email"));
        comment.setAuthorName(body.get("authorName"));
        comment.setRecipeId(body.get("recipeId"));
        comment.setText(body.get("text"));
        comment.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.ok(commentRepository.save(comment));
    }

    // Delete a comment
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id) {
        commentRepository.deleteById(id);
        return ResponseEntity.ok("Comment deleted");
    }
}