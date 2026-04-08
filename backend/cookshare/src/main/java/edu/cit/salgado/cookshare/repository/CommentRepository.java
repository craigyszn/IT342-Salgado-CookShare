package edu.cit.salgado.cookshare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.salgado.cookshare.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByRecipeIdOrderByCreatedAtDesc(String recipeId);

    long countByUserEmail(String userEmail);

    List<Comment> findByUserEmail(String userEmail);
}