package com.example.finalproject;

public class Bill_model_billpage {
    private String name;
    private String category;
    private double amount;
    private String dueDate;
    private boolean isPaid;

    public Bill_model_billpage(String name, String category, double amount, String dueDate, boolean isPaid) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public boolean isPaid() {
        return isPaid;
    }
}
