/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.reservation;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import model.customer.Customer;
import model.reservation.Reservation;
import model.room.IRoom;
import model.room.Room;
import model.room.enums.RoomType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author alhas
 */
public class ReservationServiceTest {
    
    private ReservationService service;
    
    public ReservationServiceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws Exception {
        // Use the Singleton instance of ReservationService
        service = ReservationService.getSingleton();

        // Clear the internal maps (rooms and reservations) before each test
        java.lang.reflect.Field roomsField = ReservationService.class.getDeclaredField("rooms");
        roomsField.setAccessible(true);
        ((java.util.Map<?, ?>) roomsField.get(service)).clear();

        java.lang.reflect.Field reservationsField = ReservationService.class.getDeclaredField("reservations");
        reservationsField.setAccessible(true);
        ((java.util.Map<?, ?>) reservationsField.get(service)).clear();
    }
    
    @After
    public void tearDown() {
    }

    // ====== Helper methods ======

    private Date createDate(int year, int month, int day) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Customer createCustomer(String email) {
        return new Customer("Samia", "Test", email);
    }

    private Room createRoom(String number, double price, RoomType type) {
        return new Room(number, price, type);
    }
    
    // Function 1 : reserveARoom

    /**
      Case 1: First reservation for a new customer (no previous reservations)
      Verifies that the reservation is created, stored, and associated correctly with the customer
    **/
    @Test
    public void testReserveARoom_FirstReservationForCustomer() {
        Customer customer = createCustomer("user1@example.com");
        IRoom room = createRoom("101", 150.0, RoomType.SINGLE);
        service.addRoom(room);

        Date checkIn = createDate(2025, java.util.Calendar.JANUARY, 1);
        Date checkOut = createDate(2025, java.util.Calendar.JANUARY, 3);

        Reservation reservation = service.reserveARoom(customer, room, checkIn, checkOut);

        assertNotNull("reserveARoom must return a Reservation object", reservation);
        // Verify that the reserved room is the same room passed into the method
        assertEquals(room, reservation.getRoom());

        Collection<Reservation> customerReservations =
                service.getCustomersReservation(customer);
        // Verify that the customer's reservation list was created
        assertNotNull("Customer reservations must not be null after first booking", customerReservations);
        assertEquals("Customer should have exactly 1 reservation", 1, customerReservations.size());
        // Verify that the returned reservation is stored in the customer's reservations
        assertTrue("Returned reservation must be stored in customer's reservations", customerReservations.contains(reservation));
    }

    /**
      Case 2: Additional reservation for the same customer (customer already has previous reservations)
      Ensures multiple reservations are stored and both are present in the list
    **/
    @Test
    public void testReserveARoom_AdditionalReservationForSameCustomer() {
        Customer customer = createCustomer("user2@example.com");
        IRoom room1 = createRoom("201", 200.0, RoomType.SINGLE);
        IRoom room2 = createRoom("202", 220.0, RoomType.DOUBLE);
        service.addRoom(room1);
        service.addRoom(room2);

        Date d1 = createDate(2025, java.util.Calendar.FEBRUARY, 1);
        Date d2 = createDate(2025, java.util.Calendar.FEBRUARY, 3);
        Date d3 = createDate(2025, java.util.Calendar.FEBRUARY, 10);
        Date d4 = createDate(2025, java.util.Calendar.FEBRUARY, 12);

        Reservation r1 = service.reserveARoom(customer, room1, d1, d2);
        Reservation r2 = service.reserveARoom(customer, room2, d3, d4);

        Collection<Reservation> customerReservations = service.getCustomersReservation(customer);

        assertEquals("Customer should have 2 reservations", 2, customerReservations.size());
        assertTrue("First reservation must be stored", customerReservations.contains(r1));
        assertTrue("Second reservation must be stored", customerReservations.contains(r2));
    }

    /**
      Case 3: customer = null
      The current implementation throws a NullPointerException when accessing customer.getEmail()
    **/
    @Test(expected = NullPointerException.class)
    public void testReserveARoom_NullCustomerThrowsException() {
        // null customer
        service.reserveARoom(null, createRoom("301", 180.0, RoomType.SINGLE),
                createDate(2025, java.util.Calendar.MARCH, 1),
                createDate(2025, java.util.Calendar.MARCH, 3));
    }

    /**
      Case 4: room = null
      Depending on the Reservation constructor, this results in a NullPointerException.
    **/
    @Test(expected = NullPointerException.class)
    public void testReserveARoom_NullRoomThrowsException() {
        Customer customer = createCustomer("nullroom@test.com");
        // null room
        service.reserveARoom(customer, null, 
                            createDate(2025, java.util.Calendar.APRIL, 1), 
                            createDate(2025, java.util.Calendar.APRIL, 3));
    }

    /**
      Case 5: checkInDate = null
      The service does not validate date parameters, so passing null leads to
      a NullPointerException inside the Reservation object
    **/
    @Test(expected = NullPointerException.class)
    public void testReserveARoom_NullDatesThrowException() {
        Customer customer = createCustomer("nulldate@test.com");
        IRoom room = createRoom("401", 250.0, RoomType.DOUBLE);
        service.addRoom(room);

        // null checkInDate
        service.reserveARoom(customer, room, 
                            null, 
                            createDate(2025, java.util.Calendar.MAY, 5));
    }
    
    /**
      Case 6: Two different customers booking the same room for the same date range
    **/
    @Test(expected = IllegalStateException.class)
    public void testReserveARoom_SameRoomDifferentCustomerSameDates() {
        Customer customerA = createCustomer("customerA@test.com");
        Customer customerB = createCustomer("customerB@test.com");
        IRoom room = createRoom("601", 100.0, RoomType.SINGLE);
        service.addRoom(room);

        Date checkIn = createDate(2026, java.util.Calendar.JUNE, 1);
        Date checkOut = createDate(2026, java.util.Calendar.JUNE, 5);

        // First reservation should succeed
        Reservation rA = service.reserveARoom(customerA, room, checkIn, checkOut);
        assertNotNull("Reservation A must be created.", rA);

        // Second reservation for the same room & same dates should fail
        service.reserveARoom(customerB, room, checkIn, checkOut);
    }
   
    
    // Function 2 : cancelReservation

    /**
      Case 1: Successfully cancel a single existing reservation
      Ensures the method returns true and the customer's reservation list becomes empty
    **/
    @Test
    public void testCancelReservation_SingleExistingReservation() {
        Customer customer = createCustomer("cancel1@test.com");
        IRoom room = createRoom("C1", 100.0, RoomType.SINGLE);
        service.addRoom(room);

        Date checkIn = createDate(2026, java.util.Calendar.JANUARY, 1);
        
        // Create a reservation
        service.reserveARoom(customer, room, checkIn, createDate(2026, java.util.Calendar.JANUARY, 5));

        // Pre-condition: customer has exactly 1 reservation
        assertEquals("Pre-condition: Customer must have 1 reservation", 1, service.getCustomersReservation(customer).size());

        // Perform cancellation
        boolean result = service.cancelReservation(customer, "C1", checkIn);

        assertTrue("Cancellation must return true upon success", result);
        
        // Post-condition: reservation list should be empty
        Collection<Reservation> remainingReservations = service.getCustomersReservation(customer);
        assertEquals("Post-condition: Customer must have 0 reservations", 0, remainingReservations.size());
    }
    
    /**
      Case 2: Cancel one reservation from a list of multiple reservations
      Only the matching reservation should be removed; the other must remain
    **/
    @Test
    public void testCancelReservation_OneOfMultipleReservations() {
        Customer customer = createCustomer("cancel2@test.com");
        IRoom room1 = createRoom("C2A", 100.0, RoomType.SINGLE);
        IRoom room2 = createRoom("C2B", 200.0, RoomType.DOUBLE);
        service.addRoom(room1);
        service.addRoom(room2);

        Date checkInToCancel = createDate(2026, java.util.Calendar.FEBRUARY, 1);
        Date checkInToKeep = createDate(2026, java.util.Calendar.FEBRUARY, 10);

        service.reserveARoom(customer, room1, checkInToCancel, createDate(2026, java.util.Calendar.FEBRUARY, 5));
        Reservation rToKeep = service.reserveARoom(customer, room2, checkInToKeep, createDate(2026, java.util.Calendar.FEBRUARY, 15));

        // Pre-condition: 2 reservations
        assertEquals(2, service.getCustomersReservation(customer).size());

        // Cancel the first reservation
        boolean result = service.cancelReservation(customer, "C2A", checkInToCancel);

        assertTrue("Cancellation must succeed", result);
        
        // Post-condition: only one reservation remains
        Collection<Reservation> remainingReservations = service.getCustomersReservation(customer);
        assertEquals("Customer must have 1 remaining reservation", 1, remainingReservations.size());
        
        assertTrue("The remaining reservation must be intact", remainingReservations.contains(rToKeep));
    }
    
    /**
      Case 3: Cancellation fails when the reservation does not exist, even if the customer has other reservations
    **/
    @Test
    public void testCancelReservation_ReservationNotFound() {
        Customer customer = createCustomer("cancel3@test.com");
        IRoom room = createRoom("C3", 100.0, RoomType.SINGLE);
        service.addRoom(room);

        // Create a valid reservation
        service.reserveARoom(customer, room, createDate(2026, java.util.Calendar.MARCH, 1), createDate(2026, java.util.Calendar.MARCH, 5));
        assertEquals(1, service.getCustomersReservation(customer).size());

        // Try to cancel with wrong room number
        boolean result = service.cancelReservation(customer, "C99", createDate(2026, java.util.Calendar.MARCH, 1));

        assertFalse("Cancellation must fail as the reservation data does not match", result);
        
        // Reservation list should remain unchanged
        assertEquals("Reservation count must remain 1", 1, service.getCustomersReservation(customer).size());
    }
    
    /**
      Case 4: Customer has no reservations
      The method should return false because there is nothing to cancel
    **/
    @Test
    public void testCancelReservation_CustomerHasNoReservations() {
        Customer customer = createCustomer("cancel4@test.com");     
        
        boolean result = service.cancelReservation(customer, "C4", createDate(2026, java.util.Calendar.APRIL, 1));
        assertFalse("Cancellation must return false if customer reservation list is empty/null", result);
    }
    
    /**
      Case 5: customer = null
      According to the service implementation, the method should return false
    **/
    @Test
    public void testCancelReservation_NullCustomerInput() {
        // null customer
        boolean result = service.cancelReservation(null, "C5", createDate(2026, java.util.Calendar.MAY, 1));
        assertFalse("Cancellation must return false if customer is null (as per code logic)", result);
    }
    
    /**
      Case 6: roomNumber = null
      The service checks for null and should return false
    **/
    @Test
    public void testCancelReservation_NullRoomNumberInput() {
        Customer customer = createCustomer("cancel6@test.com");
        // null roomNumber
        boolean result = service.cancelReservation(customer, null, createDate(2026, java.util.Calendar.JUNE, 1));
        assertFalse("Cancellation must return false if roomNumber is null (as per code logic)", result);
    }
    
    /**
      Case 7: Input checkInDate = null
      The service checks for null and should return false
    **/
    @Test
    public void testCancelReservation_NullCheckInDateInput() {
        Customer customer = createCustomer("cancel7@test.com");
        // null checkInDate
        boolean result = service.cancelReservation(customer, "C7", null);
        assertFalse("Cancellation must return false if checkInDate is null (as per code logic)", result);
    }
    
    
    // Function 3 : findMostPopularRoom

    /**
      Case 1: Clear winner scenario
      Room "201" is booked 3 times, while room "100" is booked once
      The method should return "201"
    **/
    @Test
    public void testFindPopularRoom_ClearWinner() {
        Customer c1 = createCustomer("pop1@test.com");
        Customer c2 = createCustomer("pop2@test.com");
        
        IRoom roomA = createRoom("100", 100.0, RoomType.SINGLE);
        IRoom roomB = createRoom("201", 200.0, RoomType.DOUBLE);
        service.addRoom(roomA);
        service.addRoom(roomB);

        // Book Room B (201) three times
        service.reserveARoom(c1, roomB, createDate(2026, 1, 1), createDate(2026, 1, 5));
        service.reserveARoom(c1, roomB, createDate(2026, 1, 6), createDate(2026, 1, 10));
        service.reserveARoom(c2, roomB, createDate(2026, 2, 1), createDate(2026, 2, 5));

        // Book Room A (100) once
        service.reserveARoom(c1, roomA, createDate(2026, 3, 1), createDate(2026, 3, 5));

        String result = service.findMostPopularRoom();

        assertNotNull("Result should not be null", result);
        assertEquals("The most popular room should be 201 (Room B)", "201", result);
    }
    
    /**
      Case 2: No reservations exist in the system
      The method is expected to return null
    **/
    @Test
    public void testFindPopularRoom_NoReservations() {
        String result = service.findMostPopularRoom();
        assertNull("Result must be null when there are no reservations", result);
    }
    
    /**
      Case 3: Tie in popularity between two rooms. 
      Both rooms have the same number of bookings, so the result can be either one
    **/
    @Test
    public void testFindPopularRoom_EqualPopularity() {
        Customer customer = createCustomer("pop3@test.com");
        IRoom roomX = createRoom("X10", 100.0, RoomType.SINGLE);
        IRoom roomY = createRoom("Y20", 200.0, RoomType.DOUBLE);
        service.addRoom(roomX);
        service.addRoom(roomY);

        // Book Room X twice
        service.reserveARoom(customer, roomX, createDate(2026, 1, 1), createDate(2026, 1, 5));
        service.reserveARoom(customer, roomX, createDate(2026, 1, 6), createDate(2026, 1, 10));

        // Book Room Y twice
        service.reserveARoom(customer, roomY, createDate(2026, 2, 1), createDate(2026, 2, 5));
        service.reserveARoom(customer, roomY, createDate(2026, 2, 6), createDate(2026, 2, 10));

        String result = service.findMostPopularRoom();

        assertNotNull("Result should not be null", result);
        // It should be either X10 or Y20 depending on internal ordering
        assertTrue("Result must be one of the equally popular rooms (X10 or Y20)", 
                   result.equals("X10") || result.equals("Y20"));
    }
    
    /**
      Case 4: Only one reservation exists in the system
      The method should return the room number associated with that reservation
    **/
    @Test
    public void testFindPopularRoom_SingleReservation() {
        Customer customer = createCustomer("pop4@test.com");
        IRoom roomZ = createRoom("Z30", 300.0, RoomType.DOUBLE);
        service.addRoom(roomZ);

        service.reserveARoom(customer, roomZ, createDate(2026, 3, 1), createDate(2026, 3, 5));

        String result = service.findMostPopularRoom();

        assertNotNull("Result should not be null", result);
        assertEquals("The most popular room should be the only room booked (Z30)", "Z30", result);
    }
}