package api;

import model.customer.Customer;
import model.reservation.Reservation;
import model.room.IRoom;
import model.room.enums.RoomType;
import service.customer.CustomerService;
import service.reservation.ReservationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Nada Alsulami
 */
@RunWith(MockitoJUnitRunner.class)
public class HotelResourceComponentTest {
    
    @Mock
    private CustomerService mockCustomerService;
    
    @Mock
    private ReservationService mockReservationService;
    
    @Mock
    private Customer mockCustomer;
    
    @Mock
    private IRoom mockRoom;
    
    @Mock
    private Reservation mockReservation;
    
    private HotelResource hotelResource;
    
    // Test data constants
    private static final String TEST_EMAIL = "component.test@example.com";
    private static final String TEST_EMAIL_2 = "component.test2@example.com";
    private static final String TEST_FIRST_NAME = "Ahmed";
    private static final String TEST_LAST_NAME = "Mohammed";
    private static final String TEST_ROOM_NUMBER = "201";
    private static final String TEST_ROOM_NUMBER_2 = "202";
    private static final Double TEST_ROOM_PRICE = 150.0;
    
    @Before
    public void setUp() throws Exception {
        // Get singleton instance
        hotelResource = HotelResource.getSingleton();
        
        // Inject mocks into the singleton
        injectMockService("customerService", mockCustomerService);
        injectMockService("reservationService", mockReservationService);
        
        // Setup default mock behaviors
        setupDefaultMockBehaviors();
    }
    
    /**
     * Helper method to inject mock services using reflection
     */
    private void injectMockService(String fieldName, Object mockService) throws Exception {
        Field field = HotelResource.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(hotelResource, mockService);
    }
    
    /**
     * Setup default behaviors for mocks
     */
    private void setupDefaultMockBehaviors() {
        // Default: customer has valid email
        when(mockCustomer.getEmail()).thenReturn(TEST_EMAIL);
        
        // Default: room has valid room number
        when(mockRoom.getRoomNumber()).thenReturn(TEST_ROOM_NUMBER);
    }
    
    // ==================== COMPONENT TEST: Customer Creation and Retrieval ====================
    
    @Test
    public void testComponent_CreateCustomer_ShouldCallServiceWithCorrectParameters() {
        // When: Create a customer
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        
        // Then: Should call CustomerService.addCustomer with correct parameters
        verify(mockCustomerService, times(1))
            .addCustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        
        // And: Should not call any other methods
        verifyNoMoreInteractions(mockCustomerService);
    }
    
    @Test
    public void testComponent_GetCustomer_WhenExists_ShouldReturnFromService() {
        // Given: Customer exists in service
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        
        // When: Get customer
        Customer result = hotelResource.getCustomer(TEST_EMAIL);
        
        // Then: Should return the customer from service
        assertNotNull("Customer should not be null", result);
        assertSame("Should return mock customer", mockCustomer, result);
        
        // And: Should have called service exactly once
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
    }
    
    @Test
    public void testComponent_GetCustomer_WhenNotExists_ShouldReturnNull() {
        // Given: Customer does not exist
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(null);
        
        // When: Get customer
        Customer result = hotelResource.getCustomer(TEST_EMAIL);
        
        // Then: Should return null
        assertNull("Customer should be null", result);
        
        // And: Should have called service
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
    }
    
    @Test
    public void testComponent_CreateMultipleCustomers_ShouldCallServiceMultipleTimes() {
        // When: Create multiple customers
        hotelResource.createACustomer(TEST_EMAIL, "Ahmed", "Ali");
        hotelResource.createACustomer(TEST_EMAIL_2, "Sara", "Hassan");
        
        // Then: Should call service for each customer
        verify(mockCustomerService, times(1)).addCustomer(TEST_EMAIL, "Ahmed", "Ali");
        verify(mockCustomerService, times(1)).addCustomer(TEST_EMAIL_2, "Sara", "Hassan");
        verify(mockCustomerService, times(2)).addCustomer(anyString(), anyString(), anyString());
    }
    
    // ==================== COMPONENT TEST: Room Operations ====================
    
    @Test
    public void testComponent_GetRoom_ShouldDelegateToService() {
        // Given: Room exists in service
        when(mockReservationService.getARoom(TEST_ROOM_NUMBER)).thenReturn(mockRoom);
        
        // When: Get room
        IRoom result = hotelResource.getRoom(TEST_ROOM_NUMBER);
        
        // Then: Should return room from service
        assertNotNull("Room should not be null", result);
        assertSame("Should return mock room", mockRoom, result);
        
        // And: Should have called service exactly once
        verify(mockReservationService, times(1)).getARoom(TEST_ROOM_NUMBER);
    }
    
    @Test
    public void testComponent_GetRoom_WhenNotExists_ShouldReturnNull() {
        // Given: Room does not exist
        when(mockReservationService.getARoom("999")).thenReturn(null);
        
        // When: Get non-existent room
        IRoom result = hotelResource.getRoom("999");
        
        // Then: Should return null
        assertNull("Room should be null", result);
        
        // And: Should have called service
        verify(mockReservationService, times(1)).getARoom("999");
    }
    
    // ==================== COMPONENT TEST: Booking Workflow ====================
    
    @Test
    public void testComponent_BookRoom_CompleteWorkflow() {
        // Given: Customer and room exist
        Date checkIn = createDate(2025, Calendar.MARCH, 15);
        Date checkOut = createDate(2025, Calendar.MARCH, 20);
        
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.reserveARoom(mockCustomer, mockRoom, checkIn, checkOut))
            .thenReturn(mockReservation);
        
        // When: Book a room
        Reservation result = hotelResource.bookARoom(TEST_EMAIL, mockRoom, checkIn, checkOut);
        
        // Then: Should return reservation
        assertNotNull("Reservation should not be null", result);
        assertSame("Should return mock reservation", mockReservation, result);
        
        // And: Should call CustomerService to get customer
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
        
        // And: Should call ReservationService to create reservation
        verify(mockReservationService, times(1))
            .reserveARoom(mockCustomer, mockRoom, checkIn, checkOut);
    }
    
    @Test
    public void testComponent_BookRoom_VerifyCorrectParametersPassed() {
        // Given: Setup
        Date checkIn = createDate(2025, Calendar.APRIL, 1);
        Date checkOut = createDate(2025, Calendar.APRIL, 5);
        
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.reserveARoom(mockCustomer, mockRoom, checkIn, checkOut))
            .thenReturn(mockReservation);
        
        // When: Book room
        hotelResource.bookARoom(TEST_EMAIL, mockRoom, checkIn, checkOut);
        
        // Then: Verify exact parameters were passed
        verify(mockReservationService).reserveARoom(
            eq(mockCustomer),
            eq(mockRoom),
            eq(checkIn),
            eq(checkOut)
        );
    }
    
    @Test
    public void testComponent_BookMultipleRooms_ShouldCallServiceMultipleTimes() {
        // Given: Customer exists
        Date checkIn1 = createDate(2025, Calendar.MAY, 1);
        Date checkOut1 = createDate(2025, Calendar.MAY, 5);
        Date checkIn2 = createDate(2025, Calendar.JUNE, 1);
        Date checkOut2 = createDate(2025, Calendar.JUNE, 5);
        
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.reserveARoom(any(), any(), any(), any()))
            .thenReturn(mockReservation);
        
        // When: Book multiple rooms
        hotelResource.bookARoom(TEST_EMAIL, mockRoom, checkIn1, checkOut1);
        hotelResource.bookARoom(TEST_EMAIL, mockRoom, checkIn2, checkOut2);
        
        // Then: Should call service twice
        verify(mockReservationService, times(2))
            .reserveARoom(any(Customer.class), any(IRoom.class), any(Date.class), any(Date.class));
    }
    
    // ==================== COMPONENT TEST: Get Customer Reservations ====================
    
    @Test
    public void testComponent_GetCustomerReservations_WithReservations() {
        // Given: Customer has reservations
        Collection<Reservation> reservations = new ArrayList<>();
        reservations.add(mockReservation);
        
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.getCustomersReservation(mockCustomer)).thenReturn(reservations);
        
        // When: Get customer reservations
        Collection<Reservation> result = hotelResource.getCustomersReservations(TEST_EMAIL);
        
        // Then: Should return reservations
        assertNotNull("Reservations should not be null", result);
        assertEquals("Should have 1 reservation", 1, result.size());
        assertTrue("Should contain mock reservation", result.contains(mockReservation));
        
        // And: Should call services
        verify(mockCustomerService, times(2)).getCustomer(TEST_EMAIL); // Called twice in method
        verify(mockReservationService, times(1)).getCustomersReservation(mockCustomer);
    }
    
    @Test
    public void testComponent_GetCustomerReservations_WhenCustomerNotExists() {
        // Given: Customer does not exist
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(null);
        
        // When: Get reservations
        Collection<Reservation> result = hotelResource.getCustomersReservations(TEST_EMAIL);
        
        // Then: Should return empty list (based on HotelResource logic)
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
        
        // And: Should call customer service but NOT reservation service
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
        verify(mockReservationService, never()).getCustomersReservation(any());
    }
    
    @Test
    public void testComponent_GetCustomerReservations_WhenNoReservations() {
        // Given: Customer exists but has no reservations
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.getCustomersReservation(mockCustomer))
            .thenReturn(Collections.emptyList());
        
        // When: Get reservations
        Collection<Reservation> result = hotelResource.getCustomersReservations(TEST_EMAIL);
        
        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
        
        // And: Should call both services
        verify(mockCustomerService, times(2)).getCustomer(TEST_EMAIL);
        verify(mockReservationService, times(1)).getCustomersReservation(mockCustomer);
    }
    
    // ==================== COMPONENT TEST: Find Rooms ====================
    
    @Test
    public void testComponent_FindRooms_ShouldReturnAvailableRooms() {
        // Given: Available rooms exist
        Date checkIn = createDate(2025, Calendar.JULY, 1);
        Date checkOut = createDate(2025, Calendar.JULY, 5);
        
        Collection<IRoom> availableRooms = new ArrayList<>();
        availableRooms.add(mockRoom);
        
        when(mockReservationService.findRooms(checkIn, checkOut)).thenReturn(availableRooms);
        
        // When: Find rooms
        Collection<IRoom> result = hotelResource.findARoom(checkIn, checkOut);
        
        // Then: Should return available rooms
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 room", 1, result.size());
        assertTrue("Should contain mock room", result.contains(mockRoom));
        
        // And: Should call service with exact dates
        verify(mockReservationService, times(1)).findRooms(checkIn, checkOut);
    }
    
    @Test
    public void testComponent_FindRooms_WhenNoRoomsAvailable() {
        // Given: No rooms available
        Date checkIn = createDate(2025, Calendar.AUGUST, 1);
        Date checkOut = createDate(2025, Calendar.AUGUST, 5);
        
        when(mockReservationService.findRooms(checkIn, checkOut))
            .thenReturn(Collections.emptyList());
        
        // When: Find rooms
        Collection<IRoom> result = hotelResource.findARoom(checkIn, checkOut);
        
        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
        
        // And: Should have called service
        verify(mockReservationService, times(1)).findRooms(checkIn, checkOut);
    }
    
    @Test
    public void testComponent_FindRooms_MultipleSearches() {
        // Given: Multiple date ranges
        Date checkIn1 = createDate(2025, Calendar.SEPTEMBER, 1);
        Date checkOut1 = createDate(2025, Calendar.SEPTEMBER, 5);
        Date checkIn2 = createDate(2025, Calendar.OCTOBER, 1);
        Date checkOut2 = createDate(2025, Calendar.OCTOBER, 5);
        
        Collection<IRoom> rooms = new ArrayList<>();
        rooms.add(mockRoom);
        
        when(mockReservationService.findRooms(any(), any())).thenReturn(rooms);
        
        // When: Search multiple times
        hotelResource.findARoom(checkIn1, checkOut1);
        hotelResource.findARoom(checkIn2, checkOut2);
        
        // Then: Should call service twice
        verify(mockReservationService, times(2)).findRooms(any(Date.class), any(Date.class));
    }
    
    // ==================== COMPONENT TEST: Find Alternative Rooms ====================
    
    @Test
    public void testComponent_FindAlternativeRooms_ShouldDelegateToService() {
        // Given: Alternative rooms available
        Date checkIn = createDate(2025, Calendar.NOVEMBER, 1);
        Date checkOut = createDate(2025, Calendar.NOVEMBER, 5);
        
        Collection<IRoom> alternativeRooms = new ArrayList<>();
        alternativeRooms.add(mockRoom);
        
        when(mockReservationService.findAlternativeRooms(checkIn, checkOut))
            .thenReturn(alternativeRooms);
        
        // When: Find alternative rooms
        Collection<IRoom> result = hotelResource.findAlternativeRooms(checkIn, checkOut);
        
        // Then: Should return alternative rooms
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 room", 1, result.size());
        
        // And: Should call service
        verify(mockReservationService, times(1)).findAlternativeRooms(checkIn, checkOut);
    }
    
    @Test
    public void testComponent_FindAlternativeRooms_WhenNoneAvailable() {
        // Given: No alternative rooms
        Date checkIn = createDate(2025, Calendar.DECEMBER, 1);
        Date checkOut = createDate(2025, Calendar.DECEMBER, 5);
        
        when(mockReservationService.findAlternativeRooms(checkIn, checkOut))
            .thenReturn(Collections.emptyList());
        
        // When: Find alternative rooms
        Collection<IRoom> result = hotelResource.findAlternativeRooms(checkIn, checkOut);
        
        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
        
        // And: Should call service
        verify(mockReservationService, times(1)).findAlternativeRooms(checkIn, checkOut);
    }
    
    // ==================== COMPONENT TEST: Date Operations ====================
    
    @Test
    public void testComponent_AddDefaultPlusDays_ShouldDelegateToService() {
        // Given: Original date
        Date originalDate = createDate(2025, Calendar.JANUARY, 1);
        Date expectedDate = createDate(2025, Calendar.JANUARY, 8);
        
        when(mockReservationService.addDefaultPlusDays(originalDate)).thenReturn(expectedDate);
        
        // When: Add default plus days
        Date result = hotelResource.addDefaultPlusDays(originalDate);
        
        // Then: Should return modified date
        assertNotNull("Result should not be null", result);
        assertSame("Should return expected date", expectedDate, result);
        
        // And: Should call service
        verify(mockReservationService, times(1)).addDefaultPlusDays(originalDate);
    }
    
    @Test
    public void testComponent_AddDefaultPlusDays_VerifyCorrectDatePassed() {
        // Given: Specific date
        Date specificDate = createDate(2025, Calendar.FEBRUARY, 15);
        Date resultDate = createDate(2025, Calendar.FEBRUARY, 22);
        
        when(mockReservationService.addDefaultPlusDays(specificDate)).thenReturn(resultDate);
        
        // When: Add days
        hotelResource.addDefaultPlusDays(specificDate);
        
        // Then: Should pass exact date to service
        verify(mockReservationService).addDefaultPlusDays(eq(specificDate));
    }
    
    // ==================== COMPONENT TEST: Cancel Reservation ====================
    
    @Test
    public void testComponent_CancelReservation_WhenCustomerNotExists() {
        // Given: Customer does not exist
        Date checkIn = createDate(2025, Calendar.MARCH, 1);
        
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(null);
        
        // When: Try to cancel
        boolean result = hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);
        
        // Then: Should return false
        assertFalse("Should return false for non-existent customer", result);
        
        // And: Should call customer service but NOT reservation service
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
        verify(mockReservationService, never()).cancelReservation(any(), anyString(), any());
    }
    
    @Test
    public void testComponent_CancelReservation_WhenCustomerExists() {
        // Given: Customer exists
        Date checkIn = createDate(2025, Calendar.APRIL, 1);
        
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.cancelReservation(mockCustomer, TEST_ROOM_NUMBER, checkIn))
            .thenReturn(true);
        
        // When: Cancel reservation
        boolean result = hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);
        
        // Then: Should return true
        assertTrue("Should return true when cancellation succeeds", result);
        
        // And: Should call both services
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
        verify(mockReservationService, times(1))
            .cancelReservation(mockCustomer, TEST_ROOM_NUMBER, checkIn);
    }
    
    @Test
    public void testComponent_CancelReservation_WhenReservationNotFound() {
        // Given: Customer exists but reservation doesn't
        Date checkIn = createDate(2025, Calendar.MAY, 1);
        
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.cancelReservation(mockCustomer, TEST_ROOM_NUMBER, checkIn))
            .thenReturn(false);
        
        // When: Try to cancel
        boolean result = hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);
        
        // Then: Should return false
        assertFalse("Should return false when reservation not found", result);
        
        // And: Should have called both services
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
        verify(mockReservationService, times(1))
            .cancelReservation(mockCustomer, TEST_ROOM_NUMBER, checkIn);
    }
    
    @Test
    public void testComponent_CancelReservation_VerifyCorrectParametersPassed() {
        // Given: Setup
        Date specificCheckIn = createDate(2025, Calendar.JUNE, 15);
        String specificRoomNumber = "305";
        
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.cancelReservation(mockCustomer, specificRoomNumber, specificCheckIn))
            .thenReturn(true);
        
        // When: Cancel with specific parameters
        hotelResource.cancelReservation(TEST_EMAIL, specificRoomNumber, specificCheckIn);
        
        // Then: Verify exact parameters were passed
        verify(mockReservationService).cancelReservation(
            eq(mockCustomer),
            eq(specificRoomNumber),
            eq(specificCheckIn)
        );
    }
    
    // ==================== COMPONENT TEST: Integration Scenarios ====================
    
    @Test
    public void testComponent_CompleteBookingCancellationFlow() {
        // Scenario: Book a room and then cancel it
        
        Date checkIn = createDate(2025, Calendar.JULY, 10);
        Date checkOut = createDate(2025, Calendar.JULY, 15);
        
        // Step 1: Get customer
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        
        // Step 2: Book room
        when(mockReservationService.reserveARoom(mockCustomer, mockRoom, checkIn, checkOut))
            .thenReturn(mockReservation);
        
        Reservation reservation = hotelResource.bookARoom(TEST_EMAIL, mockRoom, checkIn, checkOut);
        assertNotNull("Reservation should be created", reservation);
        
        // Step 3: Cancel reservation
        when(mockReservationService.cancelReservation(mockCustomer, TEST_ROOM_NUMBER, checkIn))
            .thenReturn(true);
        
        boolean cancelled = hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);
        assertTrue("Cancellation should succeed", cancelled);
        
        // Verify interactions
        verify(mockCustomerService, times(2)).getCustomer(TEST_EMAIL); // Once for book, once for cancel
        verify(mockReservationService, times(1)).reserveARoom(any(), any(), any(), any());
        verify(mockReservationService, times(1)).cancelReservation(any(), anyString(), any());
    }
    
    @Test
    public void testComponent_SearchAndBookFlow() {
        // Scenario: Search for rooms, then book one
        
        Date checkIn = createDate(2025, Calendar.AUGUST, 5);
        Date checkOut = createDate(2025, Calendar.AUGUST, 10);
        
        // Step 1: Search for available rooms
        Collection<IRoom> availableRooms = new ArrayList<>();
        availableRooms.add(mockRoom);
        
        when(mockReservationService.findRooms(checkIn, checkOut)).thenReturn(availableRooms);
        
        Collection<IRoom> foundRooms = hotelResource.findARoom(checkIn, checkOut);
        assertFalse("Should find available rooms", foundRooms.isEmpty());
        
        // Step 2: Book the room
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.reserveARoom(mockCustomer, mockRoom, checkIn, checkOut))
            .thenReturn(mockReservation);
        
        Reservation reservation = hotelResource.bookARoom(TEST_EMAIL, mockRoom, checkIn, checkOut);
        assertNotNull("Reservation should be created", reservation);
        
        // Verify both operations
        verify(mockReservationService, times(1)).findRooms(checkIn, checkOut);
        verify(mockReservationService, times(1)).reserveARoom(any(), any(), any(), any());
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Helper method to create dates consistently
     */
    private Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}