package edu.university.equipment;
import static org.junit.jupiter.api.Assertions.*; import java.util.NoSuchElementException; import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test;
class BookingServiceTest {
 private BookingService service; @BeforeEach void setup(){service=new BookingService();}
 @Test void successfulBookingReducesInventory(){service.create("STU1001","LAPTOP01",2);assertEquals(8,service.getEquipment("LAPTOP01").getAvailableQuantity());}
 @Test void zeroQuantityRejected(){assertThrows(IllegalArgumentException.class,()->service.create("STU1001","LAPTOP01",0));assertEquals(10,service.getEquipment("LAPTOP01").getAvailableQuantity());}
 @Test void blankBorrowerRejected(){assertThrows(IllegalArgumentException.class,()->service.create(" ","LAPTOP01",1));}
 @Test void unknownEquipmentRejected(){assertThrows(NoSuchElementException.class,()->service.create("STU1001","UNKNOWN",1));}
 @Test void ordinaryOverbookingRejected(){assertThrows(IllegalStateException.class,()->service.create("STU1001","PROJECTOR01",6));assertEquals(4,service.getEquipment("PROJECTOR01").getAvailableQuantity());}
 @Test void cancellationRestoresInventoryOnce(){Booking b=service.create("STU1001","TABLET01",3);service.cancel(b.getId());assertEquals(8,service.getEquipment("TABLET01").getAvailableQuantity());assertThrows(IllegalStateException.class,()->service.cancel(b.getId()));}
 @Test
 void quantityExactlyEqualToAvailableInventoryAccepted() {
     Booking booking = service.create("STU1001", "PROJECTOR01", 4);
 
     assertNotNull(booking);
     assertEquals(0,
             service.getEquipment("PROJECTOR01").getAvailableQuantity());
 }

 @Test
 void quantityOneGreaterThanAvailableInventoryRejected() {
     assertThrows(IllegalStateException.class,
             () -> service.create("STU1001", "PROJECTOR01", 5));
 
     assertEquals(4,
             service.getEquipment("PROJECTOR01").getAvailableQuantity());
 }
 
 @Test
 void negativeQuantityRejected() {
     assertThrows(IllegalArgumentException.class,
             () -> service.create("STU1001", "LAPTOP01", -1));
 
     assertEquals(10,
             service.getEquipment("LAPTOP01").getAvailableQuantity());
 }
 @Test
 void validBorrowerAccepted() {
     Booking booking = service.create("STU1001", "LAPTOP01", 1);
 
     assertNotNull(booking);
     assertEquals(9,
             service.getEquipment("LAPTOP01").getAvailableQuantity());
 }
 
 @Test
 void emptyBorrowerRejectedWithoutChangingInventory() {
     assertThrows(IllegalArgumentException.class,
             () -> service.create("", "LAPTOP01", 1));
 
     assertEquals(10,
             service.getEquipment("LAPTOP01").getAvailableQuantity());
 }
 
 @Test
 void whitespaceOnlyBorrowerRejectedWithoutChangingInventory() {
     assertThrows(IllegalArgumentException.class,
             () -> service.create("   ", "LAPTOP01", 1));
 
     assertEquals(10,
             service.getEquipment("LAPTOP01").getAvailableQuantity());
 }
 
 @Test
 void nullBorrowerRejectedWithoutChangingInventory() {
     assertThrows(IllegalArgumentException.class,
             () -> service.create(null, "LAPTOP01", 1));
 
     assertEquals(10,
             service.getEquipment("LAPTOP01").getAvailableQuantity());
 } 
}