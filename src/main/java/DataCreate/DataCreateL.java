package DataCreate;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;

public class DataCreateL {

    private static final String[] ADJECTIVES = {
        "Hidden", "Lost", "Secret", "Mystic", "Silent", "Forgotten", "Ancient", "Infinite"
    };

    private static final String[] NOUNS = {
        "Kingdom", "World", "Journey", "Path", "Legends", "Echoes", "Dreams", "Destiny"
    };

    private static final String[] AUTHORS_FIRST = {
        "Meera", "Arjun", "Leena", "Rohan", "Priya", "Vikram", "Anika", "Karan"
    };

    private static final String[] AUTHORS_LAST = {
        "Nair", "Kapoor", "Thomas", "Das", "Menon", "Sharma", "Iyer", "Singh"
    };

    private static final String[] GENRES = {
        "Fantasy", "Science Fiction", "Mystery", "Romance", "Thriller", "Historical"
    };
    private static final Random RANDOM = new Random();

    

  

    // --------- BOOK GENERATORS ---------
    public static String generateTitle() {
        String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        return adjective + " of " + noun;
    }

    public static String generateAuthor() {
        String first = AUTHORS_FIRST[RANDOM.nextInt(AUTHORS_FIRST.length)];
        String last = AUTHORS_LAST[RANDOM.nextInt(AUTHORS_LAST.length)];
        return first + " " + last;
    }

    public static String generateGenre() {
        return GENRES[RANDOM.nextInt(GENRES.length)];
    }

    public static boolean isAvailable() {
        return RANDOM.nextBoolean();
    }

    public static int generatePublishedYear() {
        return 1900 + RANDOM.nextInt(126);
    }

    public static String generateBookJson() {
        return "{\n" +
                "  \"title\": \"" + generateTitle() + "\",\n" +
                "  \"author\": \"" + generateAuthor() + "\",\n" +
                "  \"genre\": \"" + generateGenre() + "\",\n" +
                "  \"available\": " + isAvailable() + ",\n" +
                "  \"publishedYear\": " + generatePublishedYear() + "\n" +
                "}";
    }

    // --------- LIBRARY GENERATORS ---------
    private static final String[] SYLLABLES = {
            "Green", "River", "Hill", "Sun", "Wood", "Stone", "Bright", "Lake",
            "Maple", "Oak", "Pine", "Grand", "Silver", "Golden", "Harbor", "Spring"
        };

        private static final String[] SUFFIXES = {
            " Library", " Center", " Archive", " Hall"
        };

        private static final String[] LOCATION_SUFFIXES = {
            "town", "ville", "City", "borough", "land", "field", "Bay"
        };

        public static String generateWord() {
            return SYLLABLES[RANDOM.nextInt(SYLLABLES.length)];
        }

        public static String generateLibraryName() {
            return generateWord() + " " + generateWord() + SUFFIXES[RANDOM.nextInt(SUFFIXES.length)];
        }

        public static String generateLocation() {
            return generateWord() + LOCATION_SUFFIXES[RANDOM.nextInt(LOCATION_SUFFIXES.length)];
        }

        public static String generateCreatedDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            return sdf.format(new Date());
        }

        public static String generateLibraryJson() {
            return "{\n" +
                    "  \"libraryName\": \"" + generateLibraryName() + "\",\n" +
                    "  \"createdDate\": \"" + generateCreatedDate() + "\",\n" +
                    "  \"location\": \"" + generateLocation() + "\"\n" +
                    "}";
        }


    public static void main(String[] args) {
        // Generate one Book JSON
        System.out.println("Book JSON:");
        System.out.println(generateBookJson());

        // Generate one Library JSON
        System.out.println("\nLibrary JSON:");
        System.out.println(generateLibraryJson());
    }
}
