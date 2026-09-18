package com.example.backendmid.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.backendmid.entity.Cliente;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;

class OptimisticLockingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void dueTransazioniModificanoStessoCliente_secondaFallisce() {

        Long clienteId;

        EntityManager setupEm = entityManagerFactory.createEntityManager();

        try {
            setupEm.getTransaction().begin();

            Cliente cliente = new Cliente(
                    "Mario",
                    "mario-lock@test.it");

            setupEm.persist(cliente);
            setupEm.getTransaction().commit();

            clienteId = cliente.getId();
        } finally {
            if (setupEm.getTransaction().isActive()) {
                setupEm.getTransaction().rollback();
            }
            setupEm.close();
        }

        EntityManager em1 = entityManagerFactory.createEntityManager();
        EntityManager em2 = entityManagerFactory.createEntityManager();

        try {
            em1.getTransaction().begin();
            em2.getTransaction().begin();

            Cliente clienteTx1 = em1.find(Cliente.class, clienteId);
            Cliente clienteTx2 = em2.find(Cliente.class, clienteId);

            assertThat(clienteTx1.getVersion())
                    .isEqualTo(clienteTx2.getVersion());

            assertThat(clienteTx1)
                    .isNotSameAs(clienteTx2);

            Long versioneIniziale = clienteTx1.getVersion();

            clienteTx1.setNome("Mario TX1");
            em1.getTransaction().commit();

            assertThat(clienteTx1.getVersion())
                    .isEqualTo(versioneIniziale + 1);

            assertThat(clienteTx2.getVersion())
                    .isEqualTo(versioneIniziale);

            clienteTx2.setNome("Mario TX2");

            assertThatThrownBy(() -> em2.getTransaction().commit())
                    .isInstanceOf(RollbackException.class)
                    .hasCauseInstanceOf(OptimisticLockException.class);

            EntityManager verificaEm = entityManagerFactory.createEntityManager();

            try {
                Cliente clienteFinale =
                        verificaEm.find(Cliente.class, clienteId);

                assertThat(clienteFinale.getNome())
                        .isEqualTo("Mario TX1");

                assertThat(clienteFinale.getVersion())
                        .isEqualTo(versioneIniziale + 1);
            } finally {
                verificaEm.close();
            }
        } finally {
            if (em1.getTransaction().isActive()) {
                em1.getTransaction().rollback();
            }

            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }

            em1.close();
            em2.close();
        }
    }
}
