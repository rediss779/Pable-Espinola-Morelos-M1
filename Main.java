import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    static Random ran = new Random();
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        UserDAO userDAO = new UserDAO();
        BookingDAO bookingDAO = new BookingDAO();

        String currentUser = "";
        boolean logged = false;

        while(!logged){
            System.out.println("====Welcome====");
            System.out.print("1. Login\n2. Create Account\nEnter your choice: ");
            int choice=sc.nextInt(); sc.nextLine();

            if(choice==1){
                System.out.print("\nUsername:"); String u = sc.nextLine();
                System.out.print("Password:"); String p = sc.nextLine();

                if(userDAO.login(u,p)){
                    logged=true;
                    currentUser=u;
                    System.out.println("\nLogin Successful!");
                }else System.out.println("\nInvalid login.");
            }

            if(choice==2){
                System.out.print("\nUsername:"); String u=sc.nextLine();
                System.out.print("Password:"); String p=sc.nextLine();
                User user = new User.Builder().setUsername(u).setPassword(p).build();
                userDAO.createUser(user);
            }
        }

        while(true) {
            System.out.println("\nSYSTEM MENU");
            System.out.println("1 Reserve workspace\n2 Reserve meeting room\n3 Walk in booking" +
                    "\n4 Check availability\n5 View bookings\n6 Cancel booking\n7 Exit");
            System.out.print("Enter your choice: ");
            int c = sc.nextInt();
            sc.nextLine();

            if (c == 1 || c == 2) {
                String type = (c == 1) ? "workspace" : "meeting_room";
                System.out.println("==========");

                System.out.print("Date (YYYY-MM-DD): ");
                String date = sc.nextLine();
                if (!Booking.isValidDate(date)) {
                    System.out.println("\nInvalid date.");
                    continue;
                }

                System.out.print("Time In (HH:mm): ");
                String timeIn = sc.nextLine();
                if (!Booking.isValidTime(timeIn)) {
                    System.out.println("\nInvalid time.");
                    continue;
                }

                System.out.print("Time Out (HH:mm): ");
                String timeOut = sc.nextLine();
                if (!Booking.isValidTime(timeOut)) {
                    System.out.println("\nInvalid time.");
                    continue;
                }

                if (LocalTime.parse(timeOut).isBefore(LocalTime.parse(timeIn))) {
                    System.out.println("Time out must be after time in.");
                    continue;
                }
                LocalDate today = LocalDate.now();
                LocalTime now = LocalTime.now();

                if (date.equals(today.toString())) {
                    if (LocalTime.parse(timeIn).isBefore(now)) {
                        System.out.println("\nCannot book past time for today.");
                        continue;
                    }
                }

                int max = (type.equals("workspace")) ? 20 : 10;

                System.out.println("\nAvailable numbers:");
                for (int i = 1; i <= max; i++) {
                    if (bookingDAO.isRoomAvailable(type, date, timeIn, timeOut, i)) {
                        System.out.print("(" + i + "), ");
                    }
                }
                System.out.println();

                int roomNumber;
                while (true) {
                    System.out.print("Enter number (1-" + max + "): ");
                    String input = sc.nextLine();

                    try {
                        roomNumber = Integer.parseInt(input);
                    } catch (Exception e) {
                        System.out.println("\nInvalid input.");
                        continue;
                    }

                    if (roomNumber < 1 || roomNumber > max) {
                        System.out.println("Invalid number. Choose 1-" + max);
                        continue;
                    }

                    if (!bookingDAO.isRoomAvailable(type, date, timeIn, timeOut, roomNumber)) {
                        System.out.println("Not available.");
                        continue;
                    }
                    break;
                }
                Booking booking = new Booking.Builder()
                        .setUsername(currentUser)
                        .setType(type)
                        .setDate(date)
                        .setTimeIn(timeIn)
                        .setTimeOut(timeOut)
                        .setStatus("reserved")
                        .setRoomNumber(roomNumber)
                        .build();

                bookingDAO.reserve(booking);

                System.out.print("Do you want a motivational quote? (yes/no): ");
                String ans = sc.nextLine();
                if (ans.equalsIgnoreCase("yes")) {
                    System.out.println("\n" + getRandomQuote());
                }
            }
            if (c == 3) {
                System.out.println("==========");
                System.out.println("1 Workspace\n2 Meeting Room");
                System.out.print("Enter your choice: ");
                String choiceInput = sc.nextLine();

                int choice;
                try {
                    choice = Integer.parseInt(choiceInput);
                } catch (Exception e) {
                    System.out.println("\nInvalid input.");
                    continue;
                }

                if(choiceInput.equals("1") || choiceInput.equals("2")) {
                    String type = (choice == 1) ? "workspace" : "meeting_room";
                    int max = (type.equalsIgnoreCase("workspace")) ? 20 : 10;
                    String date = LocalDate.now().toString();
                    String timeIn = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

                    System.out.println("Available numbers:");
                    for (int i = 1; i <= max; i++) {
                        if (bookingDAO.isRoomAvailable(type, date, timeIn,
                                LocalTime.parse(timeIn).plusHours(1).toString(), i)) {
                            System.out.print("(" + i + "), ");
                        }
                    }
                    System.out.println();

                    int roomNumber;
                    while (true) {
                        System.out.print("Enter number (1-" + max + "): ");
                        String input = sc.nextLine();

                        try {
                            roomNumber = Integer.parseInt(input);
                        } catch (Exception e) {
                            System.out.println("\nInvalid input.");
                            continue;
                        }

                        if (roomNumber < 1 || roomNumber > max) {
                            System.out.println("Invalid number.");
                            continue;
                        }
                        break;
                    }

                    System.out.print("Time Out (HH:mm): ");
                    String timeOut = sc.nextLine();
                    if (!Booking.isValidTime(timeOut)) {
                        System.out.println("\nInvalid time.");
                        continue;
                    }
                    if (LocalTime.parse(timeOut).isBefore(LocalTime.parse(timeIn))) {
                        System.out.println("\nTime out must be after current time.");
                        continue;
                    }
                    Booking booking = new Booking.Builder()
                            .setUsername(currentUser)
                            .setType(type)
                            .setDate(date)
                            .setTimeIn(timeIn)
                            .setTimeOut(timeOut)
                            .setStatus("reserved")
                            .setRoomNumber(roomNumber)
                            .build();

                    bookingDAO.reserve(booking);

                    System.out.print("Do you want a motivational quote? (yes/no): ");
                    String ans = sc.nextLine();
                    if (ans.equalsIgnoreCase("yes")) {
                        System.out.println("\n" + getRandomQuote());
                    }
                }else{
                    System.out.println("\nInvalid input.");
                    continue;
                }
            }

            if(c==4){
                System.out.println("==========");
                System.out.print("Type (workspace/meeting_room): ");
                String type=sc.nextLine();

                if(type.equals("workspace") || type.equals("meeting_room")) {

                    System.out.print("Date: ");
                    String date = sc.nextLine();

                    if (!Booking.isValidDate(date)) {
                        System.out.println("\nInvalid date.");
                        continue;
                    }

                    System.out.print("Time In: ");
                    String timeIn = sc.next();
                    System.out.print("Time Out: ");
                    String timeOut = sc.next();

                    if (!Booking.isValidTime(timeIn) || !Booking.isValidTime(timeOut)) {
                        System.out.println("\nInvalid time.");
                        continue;
                    }

                    LocalDate today = LocalDate.now();
                    LocalTime now = LocalTime.now();

                    if (LocalDate.parse(date).isBefore(today)) {
                        System.out.println("\nCannot check past date.");
                        continue;
                    }
                    if (date.equals(today.toString())) {
                        if (LocalTime.parse(timeIn).isBefore(now)) {
                            System.out.println("\nCannot check past time.");
                            continue;
                        }
                    }
                    bookingDAO.checkAvailability(type, date, timeIn, timeOut);
                } else {
                    System.out.println("\nInvalid input.");
                    continue;
                }
            }

            if(c==5){ bookingDAO.viewBookings(currentUser); }

            if(c==6){
                System.out.println("==========");
                System.out.print("Type: "); String type=sc.nextLine();
                if(type.equals("meeting_room") || type.equals("workspace")) {
                    System.out.print("Date: ");
                    String date = sc.nextLine();
                    System.out.print("Time In: ");
                    String timeIn = sc.next();
                    System.out.print("Time Out: ");
                    String timeOut = sc.next();
                    System.out.print("Room Number: ");
                    int room = sc.nextInt();
                    sc.nextLine();
                    bookingDAO.cancelBooking(type, date, timeIn, timeOut, currentUser, room);
                }else{
                    System.out.println("\nInvalid input");
                    continue;
                }
            }

            if(c==7) break;
        }

        System.out.println("Thank you!");
    }
    public static String getRandomQuote(){
        String[] quotes = {
                "Believe you can and you're halfway there.",
                "Push yourself, no one else will.",
                "Make each day your masterpiece.",
                "Dream it. Do it.",
                "Trust the timing of your life.",
                "Stay strong and keep going.",
                "Change the world by being yourself."
        };
        return ran.nextInt(quotes.length) >= 0 ? quotes[ran.nextInt(quotes.length)] : quotes[0];
    }
}
