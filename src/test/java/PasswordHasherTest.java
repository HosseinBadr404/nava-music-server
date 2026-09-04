import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {
    @Test
    void hashesAndVerifiesPasswordsWithUniqueSalts() {
        String first = PasswordHasher.hash("correct-horse-battery-staple");
        String second = PasswordHasher.hash("correct-horse-battery-staple");
        assertNotEquals(first, second);
        assertTrue(PasswordHasher.verify("correct-horse-battery-staple", first));
        assertFalse(PasswordHasher.verify("wrong-password", first));
    }
}
