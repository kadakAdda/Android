package com.example.kadakadda.Models;

public class CartItem {
    public String deliveryDate;
    public Long quantity;
    public double totalCost;
    public Product product;

    public CartItem(Product product, Long quantity) {
        this.product = product;
        this.quantity = quantity;
        this.deliveryDate = "Delivery by 31 April";
        this.update();
    }

    public void update(){
        this.totalCost = this.quantity * this.product.price;
    }

}
