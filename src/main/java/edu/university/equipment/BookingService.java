package edu.university.equipment;
import java.util.*; import org.springframework.stereotype.Service;
@Service
public class BookingService {
 private final Map<String,Equipment> equipment=new LinkedHashMap<>(); private final Map<String,Booking> bookings=new LinkedHashMap<>();
 public BookingService(){equipment.put("LAPTOP01",new Equipment("LAPTOP01","Laptop","COMPUTING",10));equipment.put("PROJECTOR01",new Equipment("PROJECTOR01","Projector","PRESENTATION",4));equipment.put("TABLET01",new Equipment("TABLET01","Tablet","COMPUTING",8));}
 public synchronized List<Equipment> listEquipment(){return new ArrayList<>(equipment.values());}
 public synchronized Equipment getEquipment(String id){Equipment e=equipment.get(id);if(e==null)throw new NoSuchElementException("Equipment not found: "+id);return e;}
 public synchronized Booking create(String borrower,String equipmentId,int quantity){
  if(borrower==null||borrower.isBlank())throw new IllegalArgumentException("borrowerId must not be blank");
  if(quantity<=0)throw new IllegalArgumentException("quantity must be positive");
  Equipment e=getEquipment(equipmentId);
  if(quantity > e.getAvailableQuantity()) throw new IllegalStateException("Requested quantity exceeds available inventory");
  e.setAvailableQuantity(e.getAvailableQuantity()-quantity);
  Booking b=new Booking(UUID.randomUUID().toString(),borrower,equipmentId,quantity);bookings.put(b.getId(),b);return b;
 }
 public synchronized Booking getBooking(String id){Booking b=bookings.get(id);if(b==null)throw new NoSuchElementException("Booking not found: "+id);return b;}
 public synchronized Booking cancel(String id){Booking b=getBooking(id);if("CANCELLED".equals(b.getStatus()))throw new IllegalStateException("Booking already cancelled");Equipment e=getEquipment(b.getEquipmentId());e.setAvailableQuantity(e.getAvailableQuantity()+b.getQuantity());b.setStatus("CANCELLED");return b;}
}