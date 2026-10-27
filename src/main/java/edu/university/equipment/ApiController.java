package edu.university.equipment;
import java.util.*; import jakarta.validation.Valid; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*;
@RestController
public class ApiController {
 private final BookingService service; public ApiController(BookingService s){service=s;}
 @GetMapping("/api/equipment") public List<Equipment> equipment(){return service.listEquipment();}
 @GetMapping("/api/equipment/{id}") public Equipment equipment(@PathVariable String id){return service.getEquipment(id);}
 @PostMapping("/api/bookings") @ResponseStatus(HttpStatus.CREATED) public Booking create(@Valid @RequestBody CreateBookingRequest r){return service.create(r.getBorrowerId(),r.getEquipmentId(),r.getQuantity());}
 @GetMapping("/api/bookings/{id}") public Booking booking(@PathVariable String id){return service.getBooking(id);}
 @PostMapping("/api/bookings/{id}/cancel") public Booking cancel(@PathVariable String id){return service.cancel(id);}
 @ExceptionHandler(NoSuchElementException.class) @ResponseStatus(HttpStatus.NOT_FOUND) public Map<String,String> notFound(Exception e){return Map.of("error","NOT_FOUND","message",e.getMessage());}
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST) public Map<String,String> badRequest(Exception e){return Map.of("error","BAD_REQUEST","message",e.getMessage());}
 @ExceptionHandler(IllegalStateException.class) @ResponseStatus(HttpStatus.CONFLICT) public Map<String,String> conflict(Exception e){return Map.of("error","CONFLICT","message",e.getMessage());}
}