package edu.university.equipment;
import jakarta.validation.constraints.Min; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
public class CreateBookingRequest {
 @NotBlank private String borrowerId; @NotBlank private String equipmentId; @NotNull @Min(1) private Integer quantity;
 public String getBorrowerId(){return borrowerId;} public void setBorrowerId(String v){borrowerId=v;}
 public String getEquipmentId(){return equipmentId;} public void setEquipmentId(String v){equipmentId=v;}
 public Integer getQuantity(){return quantity;} public void setQuantity(Integer v){quantity=v;}
}