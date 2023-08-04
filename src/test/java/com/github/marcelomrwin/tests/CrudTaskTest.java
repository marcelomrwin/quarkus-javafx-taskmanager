package com.github.marcelomrwin.tests;

import java.time.LocalDateTime;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;

import com.github.marcelomrwin.model.Task;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class CrudTaskTest {

    @Test
    public void testCreateCrud() {

        Task task = Task.builder().name("Primeira tarefa")
                .deadLine(LocalDateTime.now().plusDays(2))
                .description("Esta task foi criada para mostrar para Sara como funciona um builder")
                .build();

        QuarkusTransaction.requiringNew().run(task::persistAndFlush);

        task = Task.findById(task.getId());

        assertNotNull(task.getId());
        assertTrue(task.getId() > 0);
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
    }

    @Test
    @Transactional
    public void testCreateWithError() {
        Task task = Task.builder().name("Primeira tarefa").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        assertThrows(ConstraintViolationException.class, () -> {
            task.persistAndFlush();
        });
    }

    @Test
    public void testReadCrud() {

        Task task = Task.findById(51);
        assertNotNull(task);
        assertEquals("Esta task foi criada para mostrar para Sara como funciona um builder", task.getDescription());

        task = Task.findByName("Primeira tarefa");
        assertNotNull(task);

    }

    @Test
    @Transactional
    public void testUpdateCrud() {
        Task task = Task.findById(51);
        assertNotNull(task);
        assertEquals("Esta task foi criada para mostrar para Sara como funciona um builder", task.getDescription());
        Task.update("description = 'Descrição alterada para Sara ficar feliz' where id = ?1", 51);
        Task.flush();
        task = Task.findById(51);
        assertNotNull(task);
        assertEquals("Descrição alterada para Sara ficar feliz", task.getDescription());
    }

}
