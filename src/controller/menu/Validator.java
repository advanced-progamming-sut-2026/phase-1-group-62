package controller.menu;

public class Validator {
    public enum ValidationResult {
        VALID,
        INVALID_FORMAT,
        INVALID_LENGTH,
        EMPTY_OR_NULL,
        //password
        WEAK_PASSWORD_NO_UPPER, WEAK_PASSWORD_NO_LOWER,
        WEAK_PASSWORD_NO_DIGIT, WEAK_PASSWORD_NO_SPECIAL,
        PASSWORD_MISMATCH,
        //email
        INVALID_EMAIL_FORMAT,
        //gender
        INVALID_GENDER
    }


    private boolean isEmptyOrNull(String str) {
        return str == null || str.trim().isEmpty();
    }

    public ValidationResult validateUsername(String username) {
        if (isEmptyOrNull(username)) return ValidationResult.EMPTY_OR_NULL;

        if (!username.matches("^[a-zA-Z0-9-]*$")) {
            return ValidationResult.INVALID_FORMAT;
        }
        if (username.length() < 3 || username.length() > 15) {
            return ValidationResult.INVALID_LENGTH;
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validateGender(String gender) {
        if (isEmptyOrNull(gender)) return ValidationResult.EMPTY_OR_NULL;


        if (!gender.equalsIgnoreCase("male") && !gender.equalsIgnoreCase("female")) {
            return ValidationResult.INVALID_GENDER;
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validateEmail(String email) {
        if (isEmptyOrNull(email)) return ValidationResult.EMPTY_OR_NULL;

        String emailRegex = "^[a-zA-Z0-9]([a-zA-Z0-9._-]*[a-zA-Z0-9])?@[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\\.[a-zA-Z]{2,}$";

        if (!email.matches(emailRegex) || email.contains("..")) {
            return ValidationResult.INVALID_FORMAT;
        }

        return ValidationResult.VALID;
    }

    public ValidationResult validatePassword(String password, String confirmPassword) {
        if (isEmptyOrNull(password) || isEmptyOrNull(confirmPassword)) return ValidationResult.EMPTY_OR_NULL;

        if (!password.equals(confirmPassword)) return ValidationResult.PASSWORD_MISMATCH;
        if (password.length() < 8) return ValidationResult.INVALID_LENGTH;
        if (!password.matches(".*[A-Z].*")) return ValidationResult.WEAK_PASSWORD_NO_UPPER;
        if (!password.matches(".*[a-z].*")) return ValidationResult.WEAK_PASSWORD_NO_LOWER;
        if (!password.matches(".*[0-9].*")) return ValidationResult.WEAK_PASSWORD_NO_DIGIT;
        if (!password.matches(".*[!#$%^&*()=+{}\\[\\]|/:;'\",<>?].*")) return ValidationResult.WEAK_PASSWORD_NO_SPECIAL;

        return ValidationResult.VALID;
    }

    public ValidationResult validateNickname(String nickname) {
        if (isEmptyOrNull(nickname)) return ValidationResult.EMPTY_OR_NULL;

        if (nickname.length() < 3 || nickname.length() > 30) {
            return ValidationResult.INVALID_LENGTH;
        }
        return ValidationResult.VALID;
    }
}