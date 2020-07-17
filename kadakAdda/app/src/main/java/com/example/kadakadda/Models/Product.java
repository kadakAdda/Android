package com.example.kadakadda.Models;

public class Product {
    public String id, description, imgPath;
    public Boolean availability;
    public Long price;
    public String title, type;

    public Product(String id, Boolean availability, String description, String imgPath, Long price, String title, String type) {
        this.id = id;
        this.availability = availability;
        this.description = description;
        this.imgPath = imgPath;
        this.price = price;
        this.title = title;
        this.type = type;
    }
}
