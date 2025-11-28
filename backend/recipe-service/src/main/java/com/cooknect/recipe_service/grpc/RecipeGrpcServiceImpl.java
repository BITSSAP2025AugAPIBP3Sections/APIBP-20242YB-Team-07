package com.cooknect.recipe_service.grpc;

import com.cooknect.recipe_service.model.Recipe;
import com.cooknect.recipe_service.repository.RecipeRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import com.recipe.RecipeServiceGrpc;
import com.recipe.GetRecipeByIdRequest;
import com.recipe.RecipeResponse;
import com.recipe.Ingredient;
import com.recipe.Comment;

import java.util.Optional;

@GrpcService
public class RecipeGrpcServiceImpl extends RecipeServiceGrpc.RecipeServiceImplBase {

    @Autowired
    private RecipeRepository recipeRepository;

    @Override
    public void getRecipeById(GetRecipeByIdRequest request, StreamObserver<RecipeResponse> responseObserver) {
        Optional<Recipe> recipeOpt = recipeRepository.findById(request.getRecipeId());
        RecipeResponse.Builder responseBuilder = RecipeResponse.newBuilder();

        if (recipeOpt.isPresent()) {
            Recipe recipe = recipeOpt.get();
            responseBuilder
                .setId(recipe.getId())
                .setTitle(recipe.getTitle())
                .setDescription(recipe.getDescription())
//                .setUsername(recipe.getUsername())
                .setLikes(recipe.getLikes())
                .setCuisine(String.valueOf(recipe.getCuisine()))
                .setLanguage(recipe.getLanguage())
                .setUserId(recipe.getUserId() != null ? recipe.getUserId() : 0L) // Set userId
                .setIsTribute(recipe.isTribute());
                
            // Set tribute fields if it's a tribute
            if (recipe.isTribute()) {
                if (recipe.getAuthorName() != null) {
                    responseBuilder.setAuthorName(recipe.getAuthorName());
                }
                if (recipe.getTributeDescription() != null) {
                    responseBuilder.setTributeDescription(recipe.getTributeDescription());
                }
                if (recipe.getTributeImageUrl() != null) {
                    responseBuilder.setPhoto(recipe.getTributeImageUrl());
                }
            }

            // Add ingredients
            if (recipe.getIngredients() != null) {
                recipe.getIngredients().forEach(ing -> {
                    Ingredient grpcIngredient = Ingredient.newBuilder()
                        .setName(ing.getName())
                        .setQuantity(ing.getQuantity())
                        .build();
                    responseBuilder.addIngredients(grpcIngredient);
                });
            }

            // Add comments
            if (recipe.getComments() != null) {
                recipe.getComments().forEach(com -> {
                    Comment grpcComment = Comment.newBuilder()
                        .setText(com.getText())
                        .setAuthor(com.getAuthor())
                        .build();
                    responseBuilder.addComments(grpcComment);
                });
            }
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
