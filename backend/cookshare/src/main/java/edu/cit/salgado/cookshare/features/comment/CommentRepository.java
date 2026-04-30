package edu.cit.salgado.cookshare.features.comment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByRecipeIdOrderByCreatedAtDesc(String recipeId);

    long countByUserEmail(String userEmail);

    List<Comment> findByUserEmail(String userEmail);
}