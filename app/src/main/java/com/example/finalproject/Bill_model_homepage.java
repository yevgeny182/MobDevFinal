package com.example.finalproject;

public class Bill_model_homepage {
    private String amount;
    private String category;
    private String dueDate;

    public Bill_model_homepage(String amount, String category, String dueDate) {
        this.amount = amount;
        this.category = category;
        this.dueDate = dueDate;
    }

    public String getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDueDate() {
        return dueDate;
    }
}
