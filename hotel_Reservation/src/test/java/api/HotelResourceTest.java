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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HotelResourceTest {
    
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

    @Captor
    private ArgumentCaptor<String> emailCaptor;

    @Captor
    private ArgumentCaptor<Date> dateCaptor;

    private HotelResource hotelResource;

    // Test data constants
    private static final String TEST_EMAIL = "unit.test@example.com";
    private static final String TEST_EMAIL_2 = "unit.test2@example.com";
    private static final String TEST_FIRST_NAME = "Nada";
    private static final String TEST_LAST_NAME = "Alsulami";
    private static final String TEST_ROOM_NUMBER = "101";
    private static final String TEST_ROOM_NUMBER_2 = "102";
    private static final Double TEST_ROOM_PRICE = 150.0;

    @Before
    public void setUp() throws Exception {
        // Get singleton instance
        hotelResource = HotelResource.getSingleton();

        // Inject mocks into the singleton using reflection
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
        when(mockCustomer.getEmail()).thenReturn(TEST_EMAIL);
        when(mockRoom.getRoomNumber()).thenReturn(TEST_ROOM_NUMBER);
        when(mockRoom.getRoomPrice()).thenReturn(TEST_ROOM_PRICE);
        when(mockRoom.getRoomType()).thenReturn(RoomType.SINGLE);
    }

    // ==================== TEST: getSingleton() ====================

    @Test
    public void testGetSingleton_ShouldReturnSameInstance() {
        // When: Call getSingleton twice
        HotelResource instance1 = HotelResource.getSingleton();
        HotelResource instance2 = HotelResource.getSingleton();

        // Then: Should return same instance
        assertNotNull("Singleton should not be null", instance1);
        assertSame("Should return same singleton instance", instance1, instance2);
    }

    @Test
    public void testGetSingleton_ShouldNeverReturnNull() {
        // When: Get singleton
        HotelResource instance = HotelResource.getSingleton();

        // Then: Should never be null
        assertNotNull("Singleton should never be null", instance);
    }

    @Test
    public void testGetSingleton_MultipleCallsShouldReturnIdenticalInstance() {
        // When: Call getSingleton multiple times
        HotelResource instance1 = HotelResource.getSingleton();
        HotelResource instance2 = HotelResource.getSingleton();
        HotelResource instance3 = HotelResource.getSingleton();

        // Then: All should be identical
        assertSame("All instances should be identical", instance1, instance2);
        assertSame("All instances should be identical", instance2, instance3);
    }

    // ==================== TEST: getCustomer() ====================

    @Test
    public void testGetCustomer_WhenExists_ShouldReturnCustomer() {
        // Given: Customer exists in service
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);

        // When: Get customer
        Customer result = hotelResource.getCustomer(TEST_EMAIL);

        // Then: Should return the customer
        assertNotNull("Customer should not be null", result);
        assertSame("Should return mock customer", mockCustomer, result);
    }

    @Test
    public void testGetCustomer_WhenExists_ShouldCallServiceOnce() {
        // Given: Customer exists
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);

        // When: Get customer
        hotelResource.getCustomer(TEST_EMAIL);

        // Then: Should call service exactly once
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
    }

    @Test
    public void testGetCustomer_WhenNotExists_ShouldReturnNull() {
        // Given: Customer does not exist
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(null);

        // When: Get customer
        Customer result = hotelResource.getCustomer(TEST_EMAIL);

        // Then: Should return null
        assertNull("Customer should be null", result);
    }

    @Test
    public void testGetCustomer_ShouldPassCorrectEmailToService() {
        // Given: Setup
        when(mockCustomerService.getCustomer(anyString())).thenReturn(mockCustomer);

        // When: Get customer
        hotelResource.getCustomer(TEST_EMAIL);

        // Then: Should pass exact email to service
        verify(mockCustomerService).getCustomer(eq(TEST_EMAIL));
    }

    @Test
    public void testGetCustomer_WithDifferentEmails_ShouldCallServiceWithCorrectEmail() {
        // Given: Different customers
        Customer mockCustomer2 = mock(Customer.class);
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockCustomerService.getCustomer(TEST_EMAIL_2)).thenReturn(mockCustomer2);

        // When: Get both customers
        Customer customer1 = hotelResource.getCustomer(TEST_EMAIL);
        Customer customer2 = hotelResource.getCustomer(TEST_EMAIL_2);

        // Then: Should return correct customers
        assertSame("First customer should match", mockCustomer, customer1);
        assertSame("Second customer should match", mockCustomer2, customer2);

        // And: Should call service for each email
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL_2);
    }

    // ==================== TEST: createACustomer() ====================

    @Test
    public void testCreateACustomer_ShouldCallServiceWithCorrectParameters() {
        // When: Create a customer
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // Then: Should call service with exact parameters
        verify(mockCustomerService, times(1))
            .addCustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
    }

    @Test
    public void testCreateACustomer_ShouldCallServiceOnce() {
        // When: Create a customer
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // Then: Should call service exactly once
        verify(mockCustomerService, times(1)).addCustomer(anyString(), anyString(), anyString());
    }

    @Test
    public void testCreateACustomer_ShouldNotCallOtherMethods() {
        // When: Create a customer
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // Then: Should only call addCustomer, not getCustomer
        verify(mockCustomerService, times(1)).addCustomer(anyString(), anyString(), anyString());
        verify(mockCustomerService, never()).getCustomer(anyString());
    }

    @Test
    public void testCreateACustomer_WithDifferentData_ShouldPassCorrectValues() {
        // Given: Different customer data
        String email = "different@test.com";
        String firstName = "Ahmed";
        String lastName = "Mohammed";

        // When: Create customer
        hotelResource.createACustomer(email, firstName, lastName);

        // Then: Should pass exact values
        verify(mockCustomerService).addCustomer(eq(email), eq(firstName), eq(lastName));
    }

    @Test
    public void testCreateACustomer_MultipleCustomers_ShouldCallServiceForEach() {
        // When: Create multiple customers
        hotelResource.createACustomer(TEST_EMAIL, "Ahmed", "Ali");
        hotelResource.createACustomer(TEST_EMAIL_2, "Sara", "Hassan");

        // Then: Should call service twice
        verify(mockCustomerService, times(2)).addCustomer(anyString(), anyString(), anyString());
        verify(mockCustomerService).addCustomer(TEST_EMAIL, "Ahmed", "Ali");
        verify(mockCustomerService).addCustomer(TEST_EMAIL_2, "Sara", "Hassan");
    }

    // ==================== TEST: getRoom() ====================

    @Test
    public void testGetRoom_WhenExists_ShouldReturnRoom() {
        // Given: Room exists in service
        when(mockReservationService.getARoom(TEST_ROOM_NUMBER)).thenReturn(mockRoom);

        // When: Get room
        IRoom result = hotelResource.getRoom(TEST_ROOM_NUMBER);

        // Then: Should return room
        assertNotNull("Room should not be null", result);
        assertSame("Should return mock room", mockRoom, result);
    }

    @Test
    public void testGetRoom_WhenExists_ShouldCallServiceOnce() {
        // Given: Room exists
        when(mockReservationService.getARoom(TEST_ROOM_NUMBER)).thenReturn(mockRoom);

        // When: Get room
        hotelResource.getRoom(TEST_ROOM_NUMBER);

        // Then: Should call service exactly once
        verify(mockReservationService, times(1)).getARoom(TEST_ROOM_NUMBER);
    }

    @Test
    public void testGetRoom_WhenNotExists_ShouldReturnNull() {
        // Given: Room does not exist
        when(mockReservationService.getARoom("999")).thenReturn(null);

        // When: Get room
        IRoom result = hotelResource.getRoom("999");

        // Then: Should return null
        assertNull("Room should be null", result);
    }

    @Test
    public void testGetRoom_ShouldPassCorrectRoomNumberToService() {
        // Given: Setup
        when(mockReservationService.getARoom(anyString())).thenReturn(mockRoom);

        // When: Get room
        hotelResource.getRoom(TEST_ROOM_NUMBER);

        // Then: Should pass exact room number
        verify(mockReservationService).getARoom(eq(TEST_ROOM_NUMBER));
    }

    @Test
    public void testGetRoom_WithDifferentRoomNumbers_ShouldCallServiceCorrectly() {
        // Given: Different rooms
        IRoom mockRoom2 = mock(IRoom.class);
        when(mockReservationService.getARoom(TEST_ROOM_NUMBER)).thenReturn(mockRoom);
        when(mockReservationService.getARoom(TEST_ROOM_NUMBER_2)).thenReturn(mockRoom2);

        // When: Get both rooms
        IRoom room1 = hotelResource.getRoom(TEST_ROOM_NUMBER);
        IRoom room2 = hotelResource.getRoom(TEST_ROOM_NUMBER_2);

        // Then: Should return correct rooms
        assertSame("First room should match", mockRoom, room1);
        assertSame("Second room should match", mockRoom2, room2);
    }

    // ==================== TEST: bookARoom() ====================

    @Test
    public void testBookARoom_ShouldReturnReservation() {
        // Given: Customer exists and booking succeeds
        Date checkIn = createDate(2025, Calendar.MARCH, 1);
        Date checkOut = createDate(2025, Calendar.MARCH, 5);

        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.reserveARoom(mockCustomer, mockRoom, checkIn, checkOut))
            .thenReturn(mockReservation);

        // When: Book room
        Reservation result = hotelResource.bookARoom(TEST_EMAIL, mockRoom, checkIn, checkOut);

        // Then: Should return reservation
        assertNotNull("Reservation should not be null", result);
        assertSame("Should return mock reservation", mockReservation, result);
    }

    @Test
    public void testBookARoom_ShouldCallCustomerServiceToGetCustomer() {
        // Given: Setup
        Date checkIn = createDate(2025, Calendar.APRIL, 1);
        Date checkOut = createDate(2025, Calendar.APRIL, 5);

        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.reserveARoom(any(), any(), any(), any()))
            .thenReturn(mockReservation);

        // When: Book room
        hotelResource.bookARoom(TEST_EMAIL, mockRoom, checkIn, checkOut);

        // Then: Should call customer service
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
    }

    @Test
    public void testBookARoom_ShouldCallReservationServiceWithCorrectParameters() {
        // Given: Setup
        Date checkIn = createDate(2025, Calendar.MAY, 1);
        Date checkOut = createDate(2025, Calendar.MAY, 5);

        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.reserveARoom(mockCustomer, mockRoom, checkIn, checkOut))
            .thenReturn(mockReservation);

        // When: Book room
        hotelResource.bookARoom(TEST_EMAIL, mockRoom, checkIn, checkOut);

        // Then: Should call reservation service with exact parameters
        verify(mockReservationService).reserveARoom(
            eq(mockCustomer),
            eq(mockRoom),
            eq(checkIn),
            eq(checkOut)
        );
    }

    @Test
    public void testBookARoom_MultipleTimes_ShouldCallServicesEachTime() {
        // Given: Setup
        Date checkIn1 = createDate(2025, Calendar.JUNE, 1);
        Date checkOut1 = createDate(2025, Calendar.JUNE, 5);
        Date checkIn2 = createDate(2025, Calendar.JULY, 1);
        Date checkOut2 = createDate(2025, Calendar.JULY, 5);

        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.reserveARoom(any(), any(), any(), any()))
            .thenReturn(mockReservation);

        // When: Book multiple times
        hotelResource.bookARoom(TEST_EMAIL, mockRoom, checkIn1, checkOut1);
        hotelResource.bookARoom(TEST_EMAIL, mockRoom, checkIn2, checkOut2);

        // Then: Should call services twice each
        verify(mockCustomerService, times(2)).getCustomer(TEST_EMAIL);
        verify(mockReservationService, times(2)).reserveARoom(any(), any(), any(), any());
    }

    // ==================== TEST: getCustomersReservations() ====================

    @Test
    public void testGetCustomersReservations_WhenCustomerExists_ShouldReturnReservations() {
        // Given: Customer exists with reservations
        Collection<Reservation> reservations = new ArrayList<>();
        reservations.add(mockReservation);

        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.getCustomersReservation(mockCustomer)).thenReturn(reservations);

        // When: Get reservations
        Collection<Reservation> result = hotelResource.getCustomersReservations(TEST_EMAIL);

        // Then: Should return reservations
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
        assertTrue("Should contain mock reservation", result.contains(mockReservation));
    }

    @Test
    public void testGetCustomersReservations_WhenCustomerNotExists_ShouldReturnEmptyCollection() {
        // Given: Customer does not exist
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(null);

        // When: Get reservations
        Collection<Reservation> result = hotelResource.getCustomersReservations(TEST_EMAIL);

        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testGetCustomersReservations_WhenCustomerNotExists_ShouldNotCallReservationService() {
        // Given: Customer does not exist
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(null);

        // When: Get reservations
        hotelResource.getCustomersReservations(TEST_EMAIL);

        // Then: Should NOT call reservation service
        verify(mockReservationService, never()).getCustomersReservation(any());
    }

    @Test
    public void testGetCustomersReservations_WhenCustomerExistsWithNoReservations_ShouldReturnEmpty() {
        // Given: Customer exists but no reservations
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.getCustomersReservation(mockCustomer))
            .thenReturn(Collections.emptyList());

        // When: Get reservations
        Collection<Reservation> result = hotelResource.getCustomersReservations(TEST_EMAIL);

        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testGetCustomersReservations_WithMultipleReservations_ShouldReturnAll() {
        // Given: Customer has multiple reservations
        Reservation reservation1 = mock(Reservation.class);
        Reservation reservation2 = mock(Reservation.class);
        Reservation reservation3 = mock(Reservation.class);

        Collection<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation1);
        reservations.add(reservation2);
        reservations.add(reservation3);

        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.getCustomersReservation(mockCustomer)).thenReturn(reservations);

        // When: Get reservations
        Collection<Reservation> result = hotelResource.getCustomersReservations(TEST_EMAIL);

        // Then: Should return all reservations
        assertEquals("Should have 3 reservations", 3, result.size());
    }

    // ==================== TEST: findARoom() ====================

    @Test
    public void testFindARoom_ShouldReturnAvailableRooms() {
        // Given: Rooms are available
        Date checkIn = createDate(2025, Calendar.AUGUST, 1);
        Date checkOut = createDate(2025, Calendar.AUGUST, 5);

        Collection<IRoom> availableRooms = new ArrayList<>();
        availableRooms.add(mockRoom);

        when(mockReservationService.findRooms(checkIn, checkOut)).thenReturn(availableRooms);

        // When: Find rooms
        Collection<IRoom> result = hotelResource.findARoom(checkIn, checkOut);

        // Then: Should return available rooms
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 room", 1, result.size());
        assertTrue("Should contain mock room", result.contains(mockRoom));
    }

    @Test
    public void testFindARoom_ShouldCallServiceWithCorrectDates() {
        // Given: Setup
        Date checkIn = createDate(2025, Calendar.SEPTEMBER, 1);
        Date checkOut = createDate(2025, Calendar.SEPTEMBER, 5);

        when(mockReservationService.findRooms(any(), any())).thenReturn(Collections.emptyList());

        // When: Find rooms
        hotelResource.findARoom(checkIn, checkOut);

        // Then: Should call service with exact dates
        verify(mockReservationService).findRooms(eq(checkIn), eq(checkOut));
    }

    @Test
    public void testFindARoom_WhenNoRoomsAvailable_ShouldReturnEmptyCollection() {
        // Given: No rooms available
        Date checkIn = createDate(2025, Calendar.OCTOBER, 1);
        Date checkOut = createDate(2025, Calendar.OCTOBER, 5);

        when(mockReservationService.findRooms(checkIn, checkOut)).thenReturn(Collections.emptyList());

        // When: Find rooms
        Collection<IRoom> result = hotelResource.findARoom(checkIn, checkOut);

        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testFindARoom_ShouldCallServiceOnce() {
        // Given: Setup
        Date checkIn = createDate(2025, Calendar.NOVEMBER, 1);
        Date checkOut = createDate(2025, Calendar.NOVEMBER, 5);

        when(mockReservationService.findRooms(any(), any())).thenReturn(Collections.emptyList());

        // When: Find rooms
        hotelResource.findARoom(checkIn, checkOut);

        // Then: Should call service exactly once
        verify(mockReservationService, times(1)).findRooms(any(), any());
    }

    @Test
    public void testFindARoom_MultipleSearches_ShouldCallServiceEachTime() {
        // Given: Setup
        Date checkIn1 = createDate(2025, Calendar.DECEMBER, 1);
        Date checkOut1 = createDate(2025, Calendar.DECEMBER, 5);
        Date checkIn2 = createDate(2026, Calendar.JANUARY, 1);
        Date checkOut2 = createDate(2026, Calendar.JANUARY, 5);

        when(mockReservationService.findRooms(any(), any())).thenReturn(Collections.emptyList());

        // When: Search multiple times
        hotelResource.findARoom(checkIn1, checkOut1);
        hotelResource.findARoom(checkIn2, checkOut2);

        // Then: Should call service twice
        verify(mockReservationService, times(2)).findRooms(any(), any());
    }

    // ==================== TEST: findAlternativeRooms() ====================

    @Test
    public void testFindAlternativeRooms_ShouldReturnRooms() {
        // Given: Alternative rooms available
        Date checkIn = createDate(2025, Calendar.FEBRUARY, 1);
        Date checkOut = createDate(2025, Calendar.FEBRUARY, 5);

        Collection<IRoom> alternativeRooms = new ArrayList<>();
        alternativeRooms.add(mockRoom);

        when(mockReservationService.findAlternativeRooms(checkIn, checkOut))
            .thenReturn(alternativeRooms);

        // When: Find alternative rooms
        Collection<IRoom> result = hotelResource.findAlternativeRooms(checkIn, checkOut);

        // Then: Should return rooms
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 room", 1, result.size());
    }

    @Test
    public void testFindAlternativeRooms_ShouldCallServiceWithCorrectDates() {
        // Given: Setup
        Date checkIn = createDate(2025, Calendar.MARCH, 10);
        Date checkOut = createDate(2025, Calendar.MARCH, 15);

        when(mockReservationService.findAlternativeRooms(any(), any()))
            .thenReturn(Collections.emptyList());

        // When: Find alternative rooms
        hotelResource.findAlternativeRooms(checkIn, checkOut);

        // Then: Should call service with exact dates
        verify(mockReservationService).findAlternativeRooms(eq(checkIn), eq(checkOut));
    }

    @Test
    public void testFindAlternativeRooms_WhenNoneAvailable_ShouldReturnEmptyCollection() {
        // Given: No alternative rooms
        Date checkIn = createDate(2025, Calendar.APRIL, 10);
        Date checkOut = createDate(2025, Calendar.APRIL, 15);

        when(mockReservationService.findAlternativeRooms(checkIn, checkOut))
            .thenReturn(Collections.emptyList());

        // When: Find alternative rooms
        Collection<IRoom> result = hotelResource.findAlternativeRooms(checkIn, checkOut);

        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testFindAlternativeRooms_ShouldCallServiceOnce() {
        // Given: Setup
        Date checkIn = createDate(2025, Calendar.MAY, 10);
        Date checkOut = createDate(2025, Calendar.MAY, 15);

        when(mockReservationService.findAlternativeRooms(any(), any()))
            .thenReturn(Collections.emptyList());

        // When: Find alternative rooms
        hotelResource.findAlternativeRooms(checkIn, checkOut);

        // Then: Should call service exactly once
        verify(mockReservationService, times(1)).findAlternativeRooms(any(), any());
    }

    // ==================== TEST: addDefaultPlusDays() ====================

    @Test
    public void testAddDefaultPlusDays_ShouldReturnModifiedDate() {
        // Given: Original date
        Date originalDate = createDate(2025, Calendar.JUNE, 1);
        Date expectedDate = createDate(2025, Calendar.JUNE, 8);

        when(mockReservationService.addDefaultPlusDays(originalDate)).thenReturn(expectedDate);

        // When: Add default plus days
        Date result = hotelResource.addDefaultPlusDays(originalDate);

        // Then: Should return modified date
        assertNotNull("Result should not be null", result);
        assertSame("Should return expected date", expectedDate, result);
    }

    @Test
    public void testAddDefaultPlusDays_ShouldCallServiceWithCorrectDate() {
        // Given: Setup
        Date originalDate = createDate(2025, Calendar.JULY, 15);
        Date resultDate = createDate(2025, Calendar.JULY, 22);

        when(mockReservationService.addDefaultPlusDays(originalDate)).thenReturn(resultDate);

        // When: Add days
        hotelResource.addDefaultPlusDays(originalDate);

        // Then: Should pass exact date to service
        verify(mockReservationService).addDefaultPlusDays(eq(originalDate));
    }

    @Test
    public void testAddDefaultPlusDays_ShouldCallServiceOnce() {
        // Given: Setup
        Date originalDate = createDate(2025, Calendar.AUGUST, 1);

        when(mockReservationService.addDefaultPlusDays(any())).thenReturn(originalDate);

        // When: Add days
        hotelResource.addDefaultPlusDays(originalDate);

        // Then: Should call service exactly once
        verify(mockReservationService, times(1)).addDefaultPlusDays(any());
    }

    @Test
    public void testAddDefaultPlusDays_MultipleCalls_ShouldCallServiceEachTime() {
        // Given: Setup
        Date date1 = createDate(2025, Calendar.SEPTEMBER, 1);
        Date date2 = createDate(2025, Calendar.OCTOBER, 1);

        when(mockReservationService.addDefaultPlusDays(any())).thenReturn(date1);

        // When: Call multiple times
        hotelResource.addDefaultPlusDays(date1);
        hotelResource.addDefaultPlusDays(date2);

        // Then: Should call service twice
        verify(mockReservationService, times(2)).addDefaultPlusDays(any());
    }

    // ==================== TEST: cancelReservation() ====================

    @Test
    public void testCancelReservation_WhenCustomerNotExists_ShouldReturnFalse() {
        // Given: Customer does not exist
        Date checkIn = createDate(2025, Calendar.NOVEMBER, 1);

        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(null);

        // When: Cancel reservation
        boolean result = hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);

        // Then: Should return false
        assertFalse("Should return false for non-existent customer", result);
    }

    @Test
    public void testCancelReservation_WhenCustomerNotExists_ShouldNotCallReservationService() {
        // Given: Customer does not exist
        Date checkIn = createDate(2025, Calendar.DECEMBER, 1);

        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(null);

        // When: Cancel reservation
        hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);

        // Then: Should NOT call reservation service
        verify(mockReservationService, never()).cancelReservation(any(), anyString(), any());
    }

    @Test
    public void testCancelReservation_WhenCustomerExists_ShouldCallReservationService() {
        // Given: Customer exists
        Date checkIn = createDate(2026, Calendar.JANUARY, 1);

        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.cancelReservation(mockCustomer, TEST_ROOM_NUMBER, checkIn))
            .thenReturn(true);

        // When: Cancel reservation
        hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);

        // Then: Should call reservation service
        verify(mockReservationService, times(1))
            .cancelReservation(mockCustomer, TEST_ROOM_NUMBER, checkIn);
    }

    @Test
    public void testCancelReservation_WhenSuccessful_ShouldReturnTrue() {
        // Given: Customer exists and cancellation succeeds
        Date checkIn = createDate(2026, Calendar.FEBRUARY, 1);

        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.cancelReservation(mockCustomer, TEST_ROOM_NUMBER, checkIn))
            .thenReturn(true);

        // When: Cancel reservation
        boolean result = hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);

        // Then: Should return true
        assertTrue("Should return true when cancellation succeeds", result);
    }

    @Test
    public void testCancelReservation_WhenReservationNotFound_ShouldReturnFalse() {
        // Given: Customer exists but reservation not found
        Date checkIn = createDate(2026, Calendar.MARCH, 1);

        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockReservationService.cancelReservation(mockCustomer, TEST_ROOM_NUMBER, checkIn))
            .thenReturn(false);

        // When: Cancel reservation
        boolean result = hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);

        // Then: Should return false
        assertFalse("Should return false when reservation not found", result);
    }

    @Test
    public void testCancelReservation_ShouldPassCorrectParametersToService() {
        // Given: Setup
        Date specificCheckIn = createDate(2026, Calendar.APRIL, 15);
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
