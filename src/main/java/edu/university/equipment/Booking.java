package edu.university.equipment;
public class Booking {
 private final String id, borrowerId, equipmentId; private final int quantity; private String status="CONFIRMED";
 public Booking(String id,String borrowerId,String equipmentId,int quantity){this.id=id;this.borrowerId=borrowerId;this.equipmentId=equipmentId;this.quantity=quantity;}
 public String getId(){return id;} public String getBorrowerId(){return borrowerId;} public String getEquipmentId(){return equipmentId;}
 public int getQuantity(){return quantity;} public String getStatus(){return status;} public void setStatus(String s){status=s;}
}