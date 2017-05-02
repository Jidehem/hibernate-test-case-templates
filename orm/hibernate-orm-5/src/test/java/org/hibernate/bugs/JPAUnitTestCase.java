package org.hibernate.bugs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.test.Contact;
import org.hibernate.test.ContactTypeEnum;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class JPAUnitTestCase {

    private EntityManagerFactory entityManagerFactory;

    private CriteriaBuilder cb;

    private EntityManager em;

    private EntityTransaction transaction;

    @Before
    public void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("templatePU");
        final Map<String, String> properties = new HashMap<>();
        properties.put(AvailableSettings.SHOW_SQL, Boolean.TRUE.toString());
        properties.put(AvailableSettings.FORMAT_SQL, Boolean.TRUE.toString());
        em = entityManagerFactory.createEntityManager(properties);
        cb = em.getCriteriaBuilder();
        transaction = em.getTransaction();
    }

    @After
    public void destroy() {
        if (transaction.isActive()) {
            transaction.commit();
        }
        transaction = null;
        cb = null;
        em.close();
        em = null;
        entityManagerFactory.close();
        entityManagerFactory = null;
    }

    private Contact buildContact(final ContactTypeEnum type, final String nom, final String prenom,
            final String nomComplementaire) {
        final Contact c = new Contact();
        c.setNom(nom);
        c.setType(type);
        c.setNomComplementaire(nomComplementaire);
        c.setPrenom(prenom);
        return c;
    }

    private void doInTransaction(final Runnable runnable) {
        transaction.begin();
        runnable.run();
        transaction.commit();
    }

    // Entities are auto-discovered, so just add them anywhere on class-path
    // Add your tests, using standard JUnit.
    @Test
    public void hhh11715TestSelectCaseInOrderBy() {
        final CriteriaQuery<String> query = cb.createQuery(String.class);
        final Root<Contact> contactRoot = query.from(Contact.class);
        query.select(contactRoot.get("nom"))
                .orderBy(cb.asc(cb.<String> selectCase()
                        .when(cb.equal(contactRoot.get("type"), ContactTypeEnum.INDIVIDU), contactRoot.get("prenom"))
                        .otherwise(contactRoot.get("nomComplementaire"))));

        testQueryOrderBy(query);
    }

    @Test
    public void hhh11715TestSelectCaseInSelect() {
        final CriteriaQuery<String> query = cb.createQuery(String.class);
        final Root<Contact> contactRoot = query.from(Contact.class);
        query.select(cb.<String> selectCase()
                .when(cb.equal(contactRoot.get("type"), ContactTypeEnum.INDIVIDU), contactRoot.get("prenom"))
                .otherwise(contactRoot.get("nomComplementaire")));

        testQuery(query);
    }

    @Test
    public void hhh11715TestSimpleCaseInOrderBy() {
        final CriteriaQuery<String> query = cb.createQuery(String.class);
        final Root<Contact> contactRoot = query.from(Contact.class);
        query.select(contactRoot.get("nom"))
                .orderBy(cb.asc(cb.<ContactTypeEnum, String> selectCase(contactRoot.get("type"))
                        .when(ContactTypeEnum.INDIVIDU, contactRoot.get("prenom"))
                        .otherwise(contactRoot.get("nomComplementaire"))));

        testQueryOrderBy(query);
    }

    @Test
    public void hhh11715TestSimpleCaseInSelect() {
        final CriteriaQuery<String> query = cb.createQuery(String.class);
        final Root<Contact> contactRoot = query.from(Contact.class);
        query.select(cb.<ContactTypeEnum, String> selectCase(contactRoot.get("type"))
                .when(ContactTypeEnum.INDIVIDU, contactRoot.get("prenom"))
                .otherwise(contactRoot.get("nomComplementaire")));

        testQuery(query);
    }

    private void testQuery(final CriteriaQuery<String> query) {
        doInTransaction(() -> {
            final List<String> resultEmpty = em.createQuery(query).getResultList();
            Assert.assertTrue(resultEmpty.isEmpty());
        });

        doInTransaction(() -> {
            final Contact c1 = buildContact(ContactTypeEnum.INDIVIDU, "i1nom", "i1prenom", "i1nomCompl");
            em.persist(c1);
            final Contact c2 = buildContact(ContactTypeEnum.ETAB, "i2nom", "i2prenom", "i2nomCompl");
            em.persist(c2);
        });

        doInTransaction(() -> {
            final List<String> result2 = em.createQuery(query).getResultList();
            Assert.assertEquals(2, result2.size());
            Assert.assertTrue(result2.contains("i1prenom"));
            Assert.assertTrue(result2.contains("i2nomCompl"));
        });
    }

    private void testQueryOrderBy(final CriteriaQuery<String> query) {
        doInTransaction(() -> {
            final List<String> resultEmpty = em.createQuery(query).getResultList();
            Assert.assertTrue(resultEmpty.isEmpty());
        });

        doInTransaction(() -> {
            final Contact c1 = buildContact(ContactTypeEnum.INDIVIDU, "i1nom", "i1prenom", "i1nomCompl");
            em.persist(c1);
            final Contact c2 = buildContact(ContactTypeEnum.ETAB, "i2nom", "i2prenom", "i2nomCompl");
            em.persist(c2);
        });
        doInTransaction(() -> {
            final List<String> result = em.createQuery(query).getResultList();
            Assert.assertEquals(2, result.size());
            Assert.assertEquals("i1nom", result.get(0));
            Assert.assertEquals("i2nom", result.get(1));
        });

        doInTransaction(() -> {
            final Contact c3 = buildContact(ContactTypeEnum.ETAB, "i3nom", "i3prenom", "aaa");
            em.persist(c3);
        });

        doInTransaction(() -> {
            final List<String> result = em.createQuery(query).getResultList();
            Assert.assertEquals(3, result.size());
            Assert.assertEquals("i3nom", result.get(0));
            Assert.assertEquals("i1nom", result.get(1));
            Assert.assertEquals("i2nom", result.get(2));
        });
    }

}
