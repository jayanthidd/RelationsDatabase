package at.campus02.dbp2.jpa;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OneToOneSpec {

    private EntityManagerFactory factory;
    private EntityManager manager;

    //<editor-fold description ="Help Methods">
    private Student prepareStudent(String firstName, String lastName, Gender gender, String birthdayString) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setGender(gender);
        if (birthdayString != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            student.setBirthday(LocalDate.parse(birthdayString, formatter));
        }
        return student;
    }

    private void create(Student student) {
        manager.getTransaction().begin();
        manager.persist(student);
        manager.getTransaction().commit();
    }

    //</editor-fold>
    @Before
    public void setup() {
        factory = Persistence.createEntityManagerFactory("nameOfJpaPersistenceUnit");
        manager = factory.createEntityManager();
    }

    @After
    public void tearDown() {
        if (manager.isOpen())// if we do not check and we try to close an already closed manager or factoy, it will throw an exception
            manager.close();
        if (factory.isOpen())
            factory.close();
    }

    @Test
    public void persistAnimalAndOwnerStoresRelationInDatabase() {
        //given
        Animal bunny = new Animal();
        bunny.setName("Hansi");

        Student owner = prepareStudent("firstname", "lastname", Gender.FEMALE, null);
        bunny.setOwner(owner);

        owner.setPet(bunny);

        //when (also functions without cascade.  Everything is independently persisted)
        manager.getTransaction().begin();
        manager.persist(bunny);
        manager.persist(owner);
        manager.getTransaction().commit();

        manager.clear();

        //then
        Animal bunnyFromDb = manager.find(Animal.class, bunny.getName());
        assertThat(bunnyFromDb.getOwner(), is(owner));

        Student ownerFromDb = manager.find(Student.class, owner.getId());
        assertThat(ownerFromDb.getPet(), is(notNullValue()));
    }

    @Test
    public void persistStudentwithCascadePersistsRelationInDatabase() {
        //given
        Animal bunny = new Animal();
        bunny.setName("Hansi");

        Student owner = prepareStudent("firstname", "lastname", Gender.FEMALE, null);
        bunny.setOwner(owner);
        // if it is not refreshed or the cache not emptied, the references should be saved.  I translated this sentence and it does not make sense to me :-(
        owner.setPet(bunny);
        // for cascading to work, both above values need to be set

        //when : we dont have to separately save down the pet because cascade will do the job
        manager.getTransaction().begin();
        manager.persist(owner);
        manager.getTransaction().commit();

        manager.clear();

        //then
        Animal bunnyFromDb = manager.find(Animal.class, bunny.getName());
        assertThat(bunnyFromDb.getOwner(), is(owner));

        Student ownerFromDb = manager.find(Student.class, owner.getId());
        assertThat(ownerFromDb.getPet(), is(notNullValue()));
        assertThat(ownerFromDb.getPet().getName(), is(bunny.getName()));
    }

    @Test
    public void refreshClosesReferencesNotHandledInMemory() {
        //given
        Animal bunny = new Animal();
        bunny.setName("Hansi");

        Student owner = prepareStudent("firstname", "lastname", Gender.FEMALE, null);
        bunny.setOwner(owner);
        // we are not going to set the pet for the owner but we are going to refresh

        //when (also functions without cascade)
        manager.getTransaction().begin();
        manager.persist(owner);
        // we have not set the pet for the owner so we will persist the bunny separately
        manager.persist(bunny);
        manager.getTransaction().commit();

        //deletes all Level 1 Cache(Persistence Unit), detatches all managed entities
        //this object is possibly not cached in lower levels
        manager.clear();

        // to clear all levels of cache
        // manager.getEntityManagerFactory().getCache().evictAll();

        //then
        Animal bunnyFromDb = manager.find(Animal.class, bunny.getName());
        assertThat(bunnyFromDb.getOwner(), is(owner));

        Student ownerFromDb = manager.find(Student.class, owner.getId());

        // without refresh(or a completely empty cache), the relationship will not be completed
        assertThat(ownerFromDb.getPet(), is(nullValue()));

        // when : with refresh the relationship is complete.  We need to refresh the object(s) that have been queried from the database

        manager.refresh(ownerFromDb);

        assertThat(ownerFromDb.getPet(), is(notNullValue()));
        assertThat(ownerFromDb.getPet().getName(), is(bunny.getName()));
    }
}
