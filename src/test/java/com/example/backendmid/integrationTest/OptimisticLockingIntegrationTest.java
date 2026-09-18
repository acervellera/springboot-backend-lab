package com.example.backendmid.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.backendmid.entity.Cliente;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;

class OptimisticLockingIntegrationTest
        extends AbstractIntegrationTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void dueTransazioniModificanoStessoCliente_secondaFallisce() {



    setupEm.getTransaction().begin();

    Cliente cliente = new Cliente(
            "Mario",
            "mario-lock@test.it");

    setupEm.persist(cliente);

    setupEm.getTransaction().commit();

    Long clienteId = cliente.getId();

    setupEm.close();

    // ==========================================
    // 2. CREIAMO DUE PERSISTENCE CONTEXT DIVERSI
    // ==========================================

    var em1 = entityManagerFactory.createEntityManager();
    var em2 = entityManagerFactory.createEntityManager();

    // ==========================================
    // 3. APRIAMO DUE TRANSAZIONI
    // ==========================================

    em1.getTransaction().begin();em2.getTransaction().begin();

    // ==========================================
    // 4. ENTRAMBE LE TX LEGGONO LO STESSO CLIENTE
    // ==========================================

    Cliente clienteTx1 = em1.find(Cliente.class, clienteId);

    Cliente clienteTx2 = em2.find(Cliente.class, clienteId);

    // Entrambi hanno letto la stessa versione
    assertThat(clienteTx1.getVersion())
                .isEqualTo(clienteTx2.getVersion());

        // Ma sono due oggetti Java differenti
        assertThat(clienteTx1)
                .isNotSameAs(clienteTx2);

        Long versioneIniziale = clienteTx1.getVersion();

        // ==========================================
        // 5. TX1 MODIFICA IL CLIENTE
        // ==========================================

        clienteTx1.setNome("Mario TX1");

        // Non serve save():
        // clienteTx1 è managed e Hibernate usa dirty checking

        em1.getTransaction().commit();

        // ==========================================
        // 6. LA VERSIONE DEVE ESSERE AUMENTATA
        // ==========================================

        assertThat(clienteTx1.getVersion())
                .isEqualTo(versioneIniziale + 1);

        // ==========================================
        // 7. TX2 POSSIEDE ANCORA LA VERSIONE VECCHIA
        // ==========================================

        assertThat(clienteTx2.getVersion())
                .isEqualTo(versioneIniziale);

        clienteTx2.setNome("Mario TX2");

        // ==========================================
        // 8. TX2 PROVA A FARE COMMIT
        // ==========================================

        assertThatThrownBy(
                () -> em2.getTransaction().commit())
                .isInstanceOf(RollbackException.class)
                .hasCauseInstanceOf(OptimisticLockException.class);

        // ==========================================
        // 9. VERIFICHIAMO COSA È RIMASTO NEL DB
        // ==========================================

        var verificaEm = entityManagerFactory.createEntityManager();

        Cliente clienteFinale = verificaEm.find(Cliente.class, clienteId);

        // Deve essere rimasta la modifica di TX1
        assertThat(clienteFinale.getNome())
                .isEqualTo("Mario TX1");

        // TX2 non deve aver incrementato nuovamente la versione
        assertThat(clienteFinale.getVersion())
                .isEqualTo(versioneIniziale + 1);

        // ==========================================
        // 10. CHIUSURA ENTITY MANAGER
        // ==========================================

        verificaEm.close();
        em1.close();
        em2.close();
    }
}