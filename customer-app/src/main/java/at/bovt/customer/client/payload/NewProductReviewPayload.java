package at.bovt.customer.client.payload;

public record NewProductReviewPayload(Integer productId, Integer rating, String review) {
}
