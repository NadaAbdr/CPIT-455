import api.HotelResource;
import model.reservation.Reservation;
import model.room.IRoom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Scanner;

/**
 * @author joseneto
 *
 */
public class MainMenu {
    
    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    static final HotelResource hotelResource = HotelResource.getSingleton();

    public static void mainMenu() {
        String line = "";
        Scanner scanner = new Scanner(System.in);

        printMainMenu();

        try {
            do {
                line = scanner.nextLine();

                if (line.length() == 1) {
                    switch (line.charAt(0)) {
                        case '1':
                            findAndReserveRoom(scanner);
                            break;
                        case '2':
                            seeMyReservation(scanner);
                            break;
                        case '3':
                            createAccount(scanner);
                            break;
                        case '4':
                            AdminMenu.adminMenu();
                            break;
                        case '5':
                            System.out.println("Exit");
                            break;
                        default:
                            System.out.println("Unknown action\n");
                            break;
                    }
                } else {
                    System.out.println("Error: Invalid action\n");
                }
            } while (line.charAt(0) != '5' || line.length() != 1);
        } catch (StringIndexOutOfBoundsException ex) {
            System.out.println("Empty input received. Exiting program...");
        }
    }

    static void findAndReserveRoom(final Scanner scanner) {
        // The method works like this
        // 1- it takes the check-in date
        // 2- it takes the check-out date
        // 3- it checks if there is an available room or not
        /*
            =====================REFACTOR NOTES:=======================
            OLD: not recursive;
            NEW: while loop to be recursive; 
            WHY: The recursion complicates testing,
            in the old the recursive method came in from the getInputDate 
            method which caused the scanner to be consumed incorrectly
            */
            Date checkIn = null;
            while (checkIn == null) {
                System.out.println("Enter Check-In Date mm/dd/yyyy example 02/01/2020");
                checkIn = getInputDate(scanner);
            }

            Date checkOut = null;
            while (checkOut == null) {
                System.out.println("Enter Check-Out Date mm/dd/yyyy example 02/21/2020");
                checkOut = getInputDate(scanner);
            }

        if (checkIn != null && checkOut != null) {
            Collection<IRoom> availableRooms = hotelResource.findARoom(checkIn, checkOut);

            if (availableRooms.isEmpty()) {
                Collection<IRoom> alternativeRooms = hotelResource.findAlternativeRooms(checkIn, checkOut);

                if (alternativeRooms.isEmpty()) {
                    System.out.println("No rooms found.");
                } else {
                    final Date alternativeCheckIn = hotelResource.addDefaultPlusDays(checkIn);
                    final Date alternativeCheckOut = hotelResource.addDefaultPlusDays(checkOut);
                    System.out.println("We've only found rooms on alternative dates:" +
                            "\nCheck-In Date:" + alternativeCheckIn +
                            "\nCheck-Out Date:" + alternativeCheckOut);

                    printRooms(alternativeRooms);
                    reserveRoom(scanner, alternativeCheckIn, alternativeCheckOut, alternativeRooms);
                }
            } else {
                printRooms(availableRooms);
                reserveRoom(scanner, checkIn, checkOut, availableRooms);
            }
        }
    }

    static Date getInputDate(final Scanner scanner) {
        try {
            return new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(scanner.nextLine());
        } catch (ParseException ex) {
            System.out.println("Error: Invalid date.");
            /*
            =====================REFACTOR NOTES:=======================
            OLD: findAndReserveRoom();
            NEW: return null; 
            WHY: The findAndReserveRoom is a recursion creates a new Scanner,
            which consumes the same System.in input thats why its much better to move the recursion to findAndReserveRoom
            */
            return null; 
        }
    }
    // refactored since this method cant handel other inputs other than y/n and added loop too
    static void reserveRoom(final Scanner scanner, final Date checkInDate,
    final Date checkOutDate, final Collection<IRoom> rooms) {
    while (true) {
        System.out.println("Would you like to book? (y/n)");
        final String bookRoom = scanner.nextLine().trim().toLowerCase();

        if ("y".equals(bookRoom)) {
            System.out.println("Do you have an account with us? (y/n)");
            final String haveAccount = scanner.nextLine().trim().toLowerCase();

            if ("y".equals(haveAccount)) {
                System.out.println("Enter Email format: name@domain.com");
                final String customerEmail = scanner.nextLine();

                if (hotelResource.getCustomer(customerEmail) == null) {
                    System.out.println("Customer not found. You may need to create a new account.");
                    break;
                }
                else{
                System.out.println("What room number would you like to reserve?");
                final String roomNumber = scanner.nextLine();

                if (rooms.stream().anyMatch(room -> room.getRoomNumber().equals(roomNumber))) {
                        final IRoom room = hotelResource.getRoom(roomNumber);

                        final Reservation reservation = hotelResource
                                .bookARoom(customerEmail, room, checkInDate, checkOutDate);
                        System.out.println("Reservation created successfully!");
                        System.out.println(reservation);
                        break;
                } else {
                    System.out.println("Error: room number not available. Start reservation again.");
                    break;
                }
                }

            } else if ("n".equals(haveAccount)) {
                System.out.println("Please, create an account.");
                break;
            } else {
                System.out.println("Invalid input. Expected 'y' or 'n'.");
                continue;
            }
        } else if ("n".equals(bookRoom)) {
            System.out.println("Booking cancelled.");
            break;
        } else {
            System.out.println("Invalid input. Please enter 'y' or 'n'.");
        }
    }

    printMainMenu();
}
    static void printRooms(final Collection<IRoom> rooms) {
        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
        } else {
            rooms.forEach(System.out::println);
        }
    }

    
    // Displays the current customer's reservations and optionally allows cancellation
    static void seeMyReservation(Scanner scanner) {

        System.out.println("Enter your Email format: name@domain.com");
        final String customerEmail = scanner.nextLine();
        String choice;

        Collection<Reservation> reservations = hotelResource.getCustomersReservations(customerEmail);

        if (reservations == null || reservations.isEmpty()) {
            System.out.println("No reservations found.");
        } else {
            // Print all existing reservations for this customer
            printReservations(reservations);
            
            // test failed since the method cant handel other inputs other than y 
            while (true) {
            System.out.println("Would you like to cancel a reservation? (y/n)");
            choice = scanner.nextLine();

            if("y".equals(choice)) {
                cancelReservation(scanner, customerEmail);
            } else if ("n".equals(choice)) {
                System.out.println("No cancellation performed.");
                break;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
            }
        }
        printMainMenu();
    }

    static void cancelReservation(Scanner scanner, String customerEmail) {
        System.out.println("Enter the ROOM NUMBER of the reservation you want to cancel:");
        final String roomNumber = scanner.nextLine();

        System.out.println("Enter the CHECK-IN DATE (mm/dd/yyyy) of the reservation to cancel:");
        Date checkInDate = getInputDate(scanner);

        if (checkInDate != null) {
            boolean success = hotelResource.cancelReservation(customerEmail, roomNumber, checkInDate);

            if (success) {
                System.out.println("Reservation for room " + roomNumber + " cancelled successfully.");
            } else {
                System.out.println("Error: Reservation not found or unable to cancel. Check room number and date.");
            }
        } else {
            System.out.println("Invalid date format. Returning to main menu.");
        }
        printMainMenu();
    }

    static void printReservations(final Collection<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            System.out.println("No reservations found.");
        } else {
            reservations.forEach(reservation -> System.out.println("\n" + reservation));
        }
    }
        /*
            =====================REFACTOR NOTES:=======================
            OLD: createAccount();
            NEW: createAccount(Scanner scanner)
            WHY: No new Scanner per recursion, since each time the when the method is called a new Scanner is created
            */
        static void createAccount(Scanner scanner) {
        System.out.println("Enter Email format: name@domain.com");
        String email = scanner.nextLine();

        System.out.println("First Name:");
        String firstName = scanner.nextLine();

        System.out.println("Last Name:");
        String lastName = scanner.nextLine();

        try {
            hotelResource.createACustomer(email, firstName, lastName);
            System.out.println("Account created successfully!");
            printMainMenu();
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }


    public static void printMainMenu()
    {
        System.out.print("\nWelcome to the Hotel Reservation Application\n" +
                "--------------------------------------------\n" +
                "1. Find and reserve a room\n" +
                "2. See my reservations\n" +
                "3. Create an Account\n" +
                "4. Admin\n" +
                "5. Exit\n" +
                "--------------------------------------------\n" +
                "Please select a number for the menu option:\n");
    }
}