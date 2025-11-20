package service.reservation;

import model.customer.Customer;
import model.reservation.Reservation;
import model.room.IRoom;
import model.room.enums.RoomType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author joseneto
 *
 */
public class ReservationService {

    private static final ReservationService SINGLETON = new ReservationService();
    private static final int RECOMMENDED_ROOMS_DEFAULT_PLUS_DAYS = 7;

    private final Map<String, IRoom> rooms = new HashMap<>();
    private final Map<String, Collection<Reservation>> reservations = new HashMap<>();

    private ReservationService() {}

    public static ReservationService getSingleton() {
        return SINGLETON;
    }

    public void addRoom(final IRoom room) {
        rooms.put(room.getRoomNumber(), room);
    }

    public IRoom getARoom(final String roomNumber) {
        return rooms.get(roomNumber);
    }

    public Collection<IRoom> getAllRooms() {
        return rooms.values();
    }

    public Reservation reserveARoom(final Customer customer, final IRoom room,
            final Date checkInDate, final Date checkOutDate) {

        if (customer == null) {
            throw new NullPointerException("Customer cannot be null");
        }
        if (room == null) {
            throw new NullPointerException("room cannot be null");
        }
        if (checkInDate == null || checkOutDate == null) {
            throw new NullPointerException("dates cannot be null");
        }

        // Check if this room is already reserved in the same date range
        for (Reservation existing : getAllReservations()) {
            if (existing.getRoom().getRoomNumber().equals(room.getRoomNumber())
                    && reservationOverlaps(existing, checkInDate, checkOutDate)) {
                // prevent double booking by throwing an exception
                throw new IllegalStateException("Room is already booked for the selected period");
            }
        }

        // Create a new reservation object after passing all validations
        final Reservation reservation = new Reservation(customer, room, checkInDate, checkOutDate);

        Collection<Reservation> customerReservations = getCustomersReservation(customer);

        if (customerReservations == null) {
            customerReservations = new LinkedList<>();
        }

        customerReservations.add(reservation);
        reservations.put(customer.getEmail(), customerReservations);

        return reservation;
    }

    public Collection<IRoom> findRooms(final Date checkInDate, final Date checkOutDate) {
        return findAvailableRooms(checkInDate, checkOutDate);
    }

    public Collection<IRoom> findAlternativeRooms(final Date checkInDate, final Date checkOutDate) {
        return findAvailableRooms(addDefaultPlusDays(checkInDate), addDefaultPlusDays(checkOutDate));
    }

    private Collection<IRoom> findAvailableRooms(final Date checkInDate, final Date checkOutDate) {
        final Collection<Reservation> allReservations = getAllReservations();
        final Collection<IRoom> notAvailableRooms = new LinkedList<>();

        for (Reservation reservation : allReservations) {
            if (reservationOverlaps(reservation, checkInDate, checkOutDate)) {
                notAvailableRooms.add(reservation.getRoom());
            }
        }

        return rooms.values().stream().filter(room -> notAvailableRooms.stream()
                .noneMatch(notAvailableRoom -> notAvailableRoom.equals(room)))
                .collect(Collectors.toList());
    }

    public Date addDefaultPlusDays(final Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, RECOMMENDED_ROOMS_DEFAULT_PLUS_DAYS);

        return calendar.getTime();
    }

    private boolean reservationOverlaps(final Reservation reservation, final Date checkInDate,
                                        final Date checkOutDate){
        return checkInDate.before(reservation.getCheckOutDate())
                && checkOutDate.after(reservation.getCheckInDate());
    }

    public Collection<Reservation> getCustomersReservation(final Customer customer) {
        return reservations.get(customer.getEmail());
    }

    public void printAllReservation() {
        final Collection<Reservation> reservations = getAllReservations();

        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
        } else {
            for (Reservation reservation : reservations) {
                System.out.println(reservation + "\n");
            }
        }
    }

    private Collection<Reservation> getAllReservations() {
        final Collection<Reservation> allReservations = new LinkedList<>();

        for(Collection<Reservation> reservations : reservations.values()) {
            allReservations.addAll(reservations);
        }

        return allReservations;
    }

    // ================== الميثودات الجديدة ==================

    /**
     * إلغاء حجز معين لعميل
     * Cancels a specific booking for a customer
     * 
     * @param customer العميل - The customer
     * @param roomNumber رقم الغرفة - Room number
     * @param checkInDate تاريخ تسجيل الدخول - Check-in date
     * @return true إذا تم الإلغاء بنجاح، false إذا لم يتم العثور على الحجز
     */
    public boolean cancelReservation(final Customer customer, final String roomNumber, final Date checkInDate) {
        if (customer == null || roomNumber == null || checkInDate == null) {
            return false;
        }

        Collection<Reservation> customerReservations = getCustomersReservation(customer);

        if (customerReservations == null || customerReservations.isEmpty()) {
            return false;
        }

        // البحث عن الحجز المطلوب إلغاؤه
        Reservation reservationToRemove = null;
        for (Reservation reservation : customerReservations) {
            if (reservation.getRoom().getRoomNumber().equals(roomNumber)
                    && reservation.getCheckInDate().equals(checkInDate)) {
                reservationToRemove = reservation;
                break;
            }
        }

        // إزالة الحجز إذا تم العثور عليه
        if (reservationToRemove != null) {
            customerReservations.remove(reservationToRemove);
            return true;
        }

        return false;
    }

    /**
     * إيجاد الغرفة الأكثر حجزاً
     * Finds which room number has been booked the most times
     * 
     * @return رقم الغرفة الأكثر شعبية أو null إذا لم يكن هناك حجوزات
     */
    public String findMostPopularRoom() {
        final Collection<Reservation> allReservations = getAllReservations();

        if (allReservations.isEmpty()) {
            return null;
        }

        Map<String, Integer> roomCounts = new HashMap<>();

        // عد الحجوزات لكل غرفة
        for (Reservation reservation : allReservations) {
            String roomNumber = reservation.getRoom().getRoomNumber();
            roomCounts.put(roomNumber, roomCounts.getOrDefault(roomNumber, 0) + 1);
        }

        // إيجاد الغرفة الأكثر حجزاً
        String mostPopularRoom = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : roomCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostPopularRoom = entry.getKey();
            }
        }

        return mostPopularRoom;
    }

    /**
     * البحث عن غرف متاحة من نوع معين
     * Gets available rooms by specific room type
     * 
     * @param checkInDate تاريخ تسجيل الدخول - Check-in date
     * @param checkOutDate تاريخ المغادرة - Check-out date
     * @param roomType نوع الغرفة - Room type (SINGLE or DOUBLE)
     * @return قائمة بالغرف المتاحة من النوع المحدد
     */
    public Collection<IRoom> getAvailableRoomsByType(final Date checkInDate, final Date checkOutDate, 
                                                     final RoomType roomType) {
        if (checkInDate == null || checkOutDate == null || roomType == null) {
            return new LinkedList<>();
        }

        // الحصول على كل الغرف المتاحة
        Collection<IRoom> availableRooms = findAvailableRooms(checkInDate, checkOutDate);

        // تصفية حسب النوع
        return availableRooms.stream()
                .filter(room -> room.getRoomType().equals(roomType))
                .collect(Collectors.toList());
    }

    /**
     * الحصول على تاريخ حجوزات العميل مرتبة من الأحدث للأقدم
     * Gets customer reservation history sorted from newest to oldest
     * 
     * @param customer العميل - The customer
     * @return قائمة مرتبة بحجوزات العميل
     */
    public List<Reservation> getCustomerReservationHistory(final Customer customer) {
        if (customer == null) {
            return new LinkedList<>();
        }

        Collection<Reservation> customerReservations = getCustomersReservation(customer);

        if (customerReservations == null || customerReservations.isEmpty()) {
            return new LinkedList<>();
        }

        // تحويل Collection إلى List للترتيب
        List<Reservation> sortedReservations = new LinkedList<>(customerReservations);

        // ترتيب من الأحدث للأقدم (حسب تاريخ تسجيل الدخول)
        sortedReservations.sort((r1, r2) -> r2.getCheckInDate().compareTo(r1.getCheckInDate()));

        return sortedReservations;
    }
}