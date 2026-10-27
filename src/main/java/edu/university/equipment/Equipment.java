package edu.university.equipment;
public class Equipment {
 private final String id, name, category; private final int totalQuantity; private int availableQuantity;
 public Equipment(String id, String name, String category, int quantity) { this.id=id; this.name=name; this.category=category; this.totalQuantity=quantity; this.availableQuantity=quantity; }
 public String getId(){return id;} public String getName(){return name;} public String getCategory(){return category;}
 public int getTotalQuantity(){return totalQuantity;} public int getAvailableQuantity(){return availableQuantity;}
 public void setAvailableQuantity(int quantity){availableQuantity=quantity;}
}