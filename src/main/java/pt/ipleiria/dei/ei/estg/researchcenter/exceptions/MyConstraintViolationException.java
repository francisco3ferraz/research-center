package pt.ipleiria.dei.ei.estg.researchcenter.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

public class MyConstraintViolationException extends Exception {
    public MyConstraintViolationException(ConstraintViolationException e) {
        super(getConstraintViolationMessages(e));
    }

    public MyConstraintViolationException(Throwable t) {
        super(t == null ? "Constraint violation" : t.getMessage(), t);
    }

    private static String getConstraintViolationMessages(ConstraintViolationException e) {
        return e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
    }
}