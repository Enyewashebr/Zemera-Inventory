package com.zemera.inventory.model;

 public class Branch {
    private Integer id;
    private String name;
    private String phone;
    private String createdAt;

    public Branch() {
    }

    public Branch(Integer id, String name, String phone, String createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    // getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getCreatedAt() { return createdAt; }
    // setters
       // ✅ setters
    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
