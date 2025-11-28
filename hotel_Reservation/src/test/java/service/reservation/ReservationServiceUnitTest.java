/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.reservation;

import model.customer.Customer;
import model.reservation.Reservation;
import model.room.IRoom;
import model.room.enums.RoomType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author alhas
 */

// UNIT TEST for ReservationService using Mockito

public class ReservationServiceUnitTest {

    private ReservationService service;

    // Shared mocks
    private Customer mockCustomer1;
    private Customer mockCustomer2;
    private IRoom mockRoom1;
    private IRoom mockRoom2;

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

        // Create mocks
        mockCustomer1 = mock(Customer.class);
        mockCustomer2 = mock(Customer.class);
        mockRoom1 = mock(IRoom.class);
        mockRoom2 = mock(IRoom.class);

        // Define basic mock behavior
        when(mockCustomer1.getEmail()).thenReturn("user1@test.com");
        when(mockCustomer2.getEmail()).thenReturn("user2@test.com");

        when(mockRoom1.getRoomNumber()).thenReturn("101");
        when(mockRoom2.getRoomNumber()).thenReturn("202");

        when(mockRoom1.getRoomType()).thenReturn(RoomType.SINGLE);
        when(mockRoom2.getRoomType()).thenReturn(RoomType.DOUBLE);
    }

    @After
    public void tearDown() {
    }

    // -------- Helper methods --------

    private Date createDate(int year, int month, int day) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    
    // Function 1 : reserveARoom

    // Case 1: First reservation for a new customer (no previous reservations)
    @Test
    public void testReserveARoom_FirstReservationForCustomer_WithMocks() {
        service.addRoom(mockRoom1);
        Date checkIn = createDate(2025, java.util.Calendar.JANUARY, 1);
        Date checkOut = createDate(2025, java.util.Calendar.JANUARY, 3);

        Reservation reservation = service.reserveARoom(mockCustomer1, mockRoom1, checkIn, checkOut);

        assertNotNull("reserveARoom must return a Reservation object", reservation);
        // Verify that the reserved room is the same room passed into the method
        assertEquals(mockRoom1, reservation.getRoom());

        Collection<Reservation> customerReservations = service.getCustomersReservation(mockCustomer1);
        // Verify that the customer's reservation list was created
        assertNotNull("Customer reservations must not be null after first booking", customerReservations);
        assertEquals("Customer should have exactly 1 reservation", 1, customerReservations.size());
        // Verify that the returned reservation is stored in the customer's reservations
        assertTrue("Returned reservation must be stored in customer's reservations", customerReservations.contains(reservation));

        // Verify that the service actually called email/roomNumber on mocks
        verify(mockCustomer1, atLeastOnce()).getEmail();
        verify(mockRoom1, atLeastOnce()).getRoomNumber();
    }
    
    // Case 2: Additional reservation for the same customer Customer already has previous reservations
    @Test
    public void testReserveARoom_AdditionalReservationForSameCustomer_WithMocks() {
        service.addRoom(mockRoom1);
        service.addRoom(mockRoom2);

        Date d1 = createDate(2025, java.util.Calendar.FEBRUARY, 1);
        Date d2 = createDate(2025, java.util.Calendar.FEBRUARY, 3);
        Date d3 = createDate(2025, java.util.Calendar.FEBRUARY, 10);
        Date d4 = createDate(2025, java.util.Calendar.FEBRUARY, 12);

        Reservation r1 = service.reserveARoom(mockCustomer1, mockRoom1, d1, d2);
        Reservation r2 = service.reserveARoom(mockCustomer1, mockRoom2, d3, d4);

        Collection<Reservation> customerReservations = service.getCustomersReservation(mockCustomer1);
        assertEquals("Customer should have 2 reservations", 2, customerReservations.size());
        assertTrue("First reservation must be stored", customerReservations.contains(r1));
        assertTrue("Second reservation must be stored", customerReservations.contains(r2));
    }

    /*
      Case 3: customer = null
      The current implementation throws a NullPointerException
    */
    @Test(expected = NullPointerException.class)
    public void testReserveARoom_NullCustomerThrowsException_WithMocks() {
        // null customer
        service.reserveARoom(null, mockRoom1,
                createDate(2025, java.util.Calendar.MARCH, 1),
                createDate(2025, java.util.Calendar.MARCH, 3));
    }

    /*
      Case 4: room = null
      Depending on the Reservation constructor, this results in a NullPointerException
    */
    @Test(expected = NullPointerException.class)
    public void testReserveARoom_NullRoomThrowsException_WithMocks() {
        // null room
        service.reserveARoom(mockCustomer1, null,
                createDate(2025, java.util.Calendar.APRIL, 1),
                createDate(2025, java.util.Calendar.APRIL, 3));
    }

    /*
      Case 5: checkInDate is null
      The method should throw a NullPointerException according to implementation
    */
    @Test(expected = NullPointerException.class)
    public void testReserveARoom_NullCheckInDate_ThrowsException_WithMocks() {
        service.addRoom(mockRoom1);
        // null checkInDate
        service.reserveARoom(mockCustomer1, mockRoom1,
                null,
                createDate(2025, java.util.Calendar.MAY, 5));
    }

    /*
      Case 6: checkOutDate is null
      The method should throw a NullPointerException according to implementation
    */
    @Test(expected = NullPointerException.class)
    public void testReserveARoom_NullCheckOutDate_ThrowsException_WithMocks() {
        service.addRoom(mockRoom1);
        // null checkOutDate
        service.reserveARoom(mockCustomer1, mockRoom1,
                createDate(2025, java.util.Calendar.MAY, 5),
                null);
    }

    /*
      Case 7: Two different customers booking the same room for the same date range
    */
    @Test(expected = IllegalStateException.class)
    public void testReserveARoom_SameRoomDifferentCustomerSameDates_WithMocks() {
        service.addRoom(mockRoom1);
        Date checkIn = createDate(2026, java.util.Calendar.JUNE, 1);
        Date checkOut = createDate(2026, java.util.Calendar.JUNE, 5);

        // First reservation should succeed
        Reservation r1 = service.reserveARoom(mockCustomer1, mockRoom1, checkIn, checkOut);
        assertNotNull("First reservation must be created", r1);

        // Second reservation for the same room & same dates should fail
        service.reserveARoom(mockCustomer2, mockRoom1, checkIn, checkOut);
    }

    
    // Function 2 : cancelReservation

    /*
      Case 1: Successfully cancel a single existing reservation
      Ensures the method returns true and the customer's reservation list becomes empty
    */
    @Test
    public void testCancelReservation_SingleExistingReservation_WithMocks() {
        service.addRoom(mockRoom1);
        Date checkIn = createDate(2026, java.util.Calendar.JANUARY, 1);
        Date checkOut = createDate(2026, java.util.Calendar.JANUARY, 5);

        service.reserveARoom(mockCustomer1, mockRoom1, checkIn, checkOut);
        
        // Pre-condition: customer has exactly 1 reservation
        assertEquals("Pre-condition: customer must have 1 reservation", 1, service.getCustomersReservation(mockCustomer1).size());

        // Perform cancellation
        boolean result = service.cancelReservation(mockCustomer1, "101", checkIn);

        assertTrue("Cancellation must return true upon success", result);
        
        // Post-condition: reservation list should be empty
        Collection<Reservation> remaining = service.getCustomersReservation(mockCustomer1);
        assertEquals("Customer must have 0 reservations after cancellation", 0, remaining.size());
    }

    /*
      Case 2: Cancel one reservation from a list of multiple reservations
      Only the matching reservation should be removed; the other must remain
    */
    @Test
    public void testCancelReservation_OneOfMultipleReservations_WithMocks() {
        service.addRoom(mockRoom1);
        service.addRoom(mockRoom2);

        Date checkInToCancel = createDate(2026, java.util.Calendar.FEBRUARY, 1);
        Date checkOutToCancel = createDate(2026, java.util.Calendar.FEBRUARY, 5);

        Date checkInToKeep = createDate(2026, java.util.Calendar.FEBRUARY, 10);
        Date checkOutToKeep = createDate(2026, java.util.Calendar.FEBRUARY, 15);

        service.reserveARoom(mockCustomer1, mockRoom1, checkInToCancel, checkOutToCancel);
        Reservation reservationToKeep =
                service.reserveARoom(mockCustomer1, mockRoom2, checkInToKeep, checkOutToKeep);

        // Pre-condition: 2 reservations
        assertEquals(2, service.getCustomersReservation(mockCustomer1).size());

        // Cancel the first reservation
        boolean result = service.cancelReservation(mockCustomer1, "101", checkInToCancel);

        assertTrue("Cancellation must succeed", result);
        
        // Post-condition: only one reservation remains
        Collection<Reservation> remaining = service.getCustomersReservation(mockCustomer1);
        assertEquals("Customer must have 1 remaining reservation", 1, remaining.size());
        assertTrue("The remaining reservation must be the one we want to keep", remaining.contains(reservationToKeep));
    }

    // Case 3: Cancellation fails when the reservation does not exist, even if the customer has other reservations
    @Test
    public void testCancelReservation_ReservationNotFound_WithMocks() {
        service.addRoom(mockRoom1);
        Date checkIn = createDate(2026, java.util.Calendar.MARCH, 1);
        Date checkOut = createDate(2026, java.util.Calendar.MARCH, 5);

        // Create a valid reservation
        service.reserveARoom(mockCustomer1, mockRoom1, checkIn, checkOut);
        assertEquals(1, service.getCustomersReservation(mockCustomer1).size());

        // Try to cancel with wrong room number
        boolean result = service.cancelReservation(mockCustomer1, "999", checkIn);

        assertFalse("Cancellation must fail when roomNumber or date does not match", result);
        
        // Reservation list should remain unchanged
        assertEquals("Reservation count must remain 1", 1, service.getCustomersReservation(mockCustomer1).size());
    }

    /*
      Case 4: Room number matches but check-in date is different
      This covers the branch where first part of && is true and second part is false
     */
    @Test
    public void testCancelReservation_RoomMatchesButCheckInDateDifferent_WithMocks() {
        service.addRoom(mockRoom1);

        Date correctCheckIn = createDate(2026, java.util.Calendar.AUGUST, 1);
        Date correctCheckOut = createDate(2026, java.util.Calendar.AUGUST, 5);

        // Create one reservation
        service.reserveARoom(mockCustomer1, mockRoom1, correctCheckIn, correctCheckOut);
        assertEquals(1, service.getCustomersReservation(mockCustomer1).size());

        // Try to cancel with same room number but different check-in date
        Date wrongCheckIn = createDate(2026, java.util.Calendar.AUGUST, 2);

        boolean result = service.cancelReservation(mockCustomer1, "101", wrongCheckIn);
        assertFalse("Cancellation must fail when room matches but check-in date is different", result);
    }

    /*
      Case 5: Customer has no reservations
      The method should return false because there is nothing to cancel
    */
    @Test
    public void testCancelReservation_CustomerHasNoReservations_WithMocks() {
        Date checkIn = createDate(2026, java.util.Calendar.APRIL, 1);

        boolean result = service.cancelReservation(mockCustomer1, "101", checkIn);
        assertFalse("Cancellation must return false if customer reservation list is empty", result);
    }
    
    /*
      Case 6: customer = null
      According to the service implementation, the method should return false
    */
    @Test
    public void testCancelReservation_NullCustomerInput_WithMocks() {
        Date checkIn = createDate(2026, java.util.Calendar.MAY, 1);
        // null customer
        boolean result = service.cancelReservation(null, "101", checkIn);
        assertFalse("Cancellation must return false if customer is null", result);
    }

    /*
      Case 7: roomNumber = null
      The service checks for null and should return false
    */
    @Test
    public void testCancelReservation_NullRoomNumberInput_WithMocks() {
        Date checkIn = createDate(2026, java.util.Calendar.JUNE, 1);
        // null roomNumber
        boolean result = service.cancelReservation(mockCustomer1, null, checkIn);
        assertFalse("Cancellation must return false if roomNumber is null", result);
    }
    
    /*
      Case 8: Input checkInDate = null
      The service checks for null and should return false
    */
    @Test
    public void testCancelReservation_NullCheckInDateInput_WithMocks() {
        // null checkInDate
        boolean result = service.cancelReservation(mockCustomer1, "101", null);
        assertFalse("Cancellation must return false if checkInDate is null", result);
    }
    
    
    // Function 3 : findMostPopularRoom
    
    // Case 1: room "202" has more bookings than room "101" (3 vs 1), so we expect the method to return "202"
    @Test
    public void testFindMostPopularRoom_MostBookedRoom_WithMocks() {
        service.addRoom(mockRoom1);
        service.addRoom(mockRoom2);

        Date a1 = createDate(2026, java.util.Calendar.JANUARY, 1);
        Date a2 = createDate(2026, java.util.Calendar.JANUARY, 5);
        Date a3 = createDate(2026, java.util.Calendar.JANUARY, 6);
        Date a4 = createDate(2026, java.util.Calendar.JANUARY, 10);

        Date b1 = createDate(2026, java.util.Calendar.FEBRUARY, 1);
        Date b2 = createDate(2026, java.util.Calendar.FEBRUARY, 5);

        // Room 202 (mockRoom2) is booked 3 times
        service.reserveARoom(mockCustomer1, mockRoom2, a1, a2);
        service.reserveARoom(mockCustomer1, mockRoom2, a3, a4);
        service.reserveARoom(mockCustomer2, mockRoom2, b1, b2);

        // Room 101 (mockRoom1) booked once
        service.reserveARoom(mockCustomer1, mockRoom1,
                createDate(2026, java.util.Calendar.MARCH, 1),
                createDate(2026, java.util.Calendar.MARCH, 5));

        String result = service.findMostPopularRoom();

        assertNotNull("Result should not be null", result);
        assertEquals("202", result);
    }

    // Case 2: No reservations exist in the system, The method is expected to return null
    @Test
    public void testFindMostPopularRoom_NoReservations_WithMocks() {
        String result = service.findMostPopularRoom();
        assertNull("Result must be null when there are no reservations", result);
    }

    // Case 3: Both rooms have the same number of bookings, so the result can be either one
    @Test
    public void testFindMostPopularRoom_EqualPopularity_WithMocks() {
        service.addRoom(mockRoom1);
        service.addRoom(mockRoom2);

        Date d1 = createDate(2026, java.util.Calendar.JANUARY, 1);
        Date d2 = createDate(2026, java.util.Calendar.JANUARY, 5);
        Date d3 = createDate(2026, java.util.Calendar.JANUARY, 6);
        Date d4 = createDate(2026, java.util.Calendar.JANUARY, 10);

        // Room 101 booked twice
        service.reserveARoom(mockCustomer1, mockRoom1, d1, d2);
        service.reserveARoom(mockCustomer1, mockRoom1, d3, d4);

        // Room 202 booked twice
        service.reserveARoom(mockCustomer2, mockRoom2, d1, d2);
        service.reserveARoom(mockCustomer2, mockRoom2, d3, d4);

        String result = service.findMostPopularRoom();

        assertNotNull("Result should not be null", result);
        // It should be either 101 or 202 depending on internal ordering
        assertTrue("Result must be one of the equally popular rooms (101 or 202)",
                   result.equals("101") || result.equals("202"));
    }

    // Case 4: Only one reservation exists in the system, The method should return the room number associated with that reservation
    @Test
    public void testFindMostPopularRoom_SingleReservation_WithMocks() {
        service.addRoom(mockRoom1);

        service.reserveARoom(mockCustomer1, mockRoom1,
                createDate(2026, java.util.Calendar.MARCH, 1),
                createDate(2026, java.util.Calendar.MARCH, 5));

        String result = service.findMostPopularRoom();

        assertNotNull("Result should not be null", result);
        assertEquals("The most popular room should be 101", "101", result);
    }
}
