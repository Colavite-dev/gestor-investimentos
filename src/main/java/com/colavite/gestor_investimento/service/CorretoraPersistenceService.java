package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.exception.CnpjDuplicadoException;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Locale;

@Service
public class CorretoraPersistenceService {

    private static final String DUPLICATE_KEY_SQL_STATE = "23505";
    private static final String CNPJ_UNIQUE_CONSTRAINT = "UK_CORRETORAS_USUARIO_CNPJ";

    private final CorretoraRepository repository;

    public CorretoraPersistenceService(CorretoraRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Corretora persistir(Corretora corretora) {
        try {
            return repository.saveAndFlush(corretora);
        } catch (DataIntegrityViolationException exception) {
            if (isCnpjUniqueViolation(exception)) {
                throw new CnpjDuplicadoException(corretora.getCnpj());
            }
            throw exception;
        }
    }

    private boolean isCnpjUniqueViolation(DataIntegrityViolationException exception) {
        ConstraintViolationException constraintViolation = findCause(exception, ConstraintViolationException.class);
        if (constraintViolation != null && constraintViolation.getConstraintName() != null) {
            return isCnpjConstraint(constraintViolation.getConstraintName())
                    && hasSqlState(exception, DUPLICATE_KEY_SQL_STATE);
        }
        return false;
    }

    private boolean isCnpjConstraint(String constraintName) {
        String normalized = constraintName.trim().toUpperCase(Locale.ROOT);
        return normalized.contains(CNPJ_UNIQUE_CONSTRAINT);
    }

    private boolean hasSqlState(Throwable exception, String sqlState) {
        return findCause(exception, SQLException.class) != null
                && sqlState.equals(findCause(exception, SQLException.class).getSQLState());
    }

    private <T extends Throwable> T findCause(Throwable exception, Class<T> type) {
        Throwable current = exception;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }
}
