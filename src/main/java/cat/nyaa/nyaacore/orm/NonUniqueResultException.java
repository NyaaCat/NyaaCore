package cat.nyaa.nyaacore.orm;

public class NonUniqueResultException extends Exception {
    public NonUniqueResultException(String s) {
        super(s);
    }
}
