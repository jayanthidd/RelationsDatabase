package at.campus02.dbp2.jpa;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OneToManySpec {
    private EntityManagerFactory factory;
    private EntityManager manager;

    // <editor-fold description ="Help Methods">
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
    public void persistSpeciesWithCascadeAlsoPersistsAnimals() {
        //given
        Animal bunny = new Animal();
        bunny.setName("Hansi");
        Animal cat = new Animal();
        cat.setName("Rudolf");
        Species mammals = new Species();
        mammals.setName("Mammals");

        // manage the references in db
        bunny.setSpecies(mammals);
        cat.setSpecies(mammals);

        // for cascading, we should add the animals as well as the species
        mammals.getAnimals().add(bunny);
        mammals.getAnimals().add(cat);

        //when
        manager.getTransaction().begin();
        manager.persist(mammals);
        manager.getTransaction().commit();
        manager.clear();

        //then
        Species mammalsFromDb = manager.find(Species.class, mammals.getId());
        //as a precaution
        manager.refresh(mammalsFromDb);

        assertThat(mammalsFromDb.getAnimals().size(), is(2));
        assertThat(bunny.getSpecies().getId(), is(mammalsFromDb.getId()));
        assertThat(cat.getSpecies().getId(), is(mammalsFromDb.getId()));
    }

    @Test
    public void updateExample() {
        //given
        Animal clownfish = new Animal();
        clownfish.setName("Nemo");
        Animal squirrel = new Animal();
        squirrel.setName("Squirrel");

        Species fish = new Species();
        fish.setName("Fish");

        //manage references
        clownfish.setSpecies(fish);
        // we are deliberately making this mistake here to update later
        squirrel.setSpecies(fish);
        // so that cascading works and we are saving down the species, we need to add these animals to the Species as follows
        fish.getAnimals().add(clownfish);
        fish.getAnimals().add(squirrel);

        //Save :  We have cascade on the species so we do not need to save the animals separately
        manager.getTransaction().begin();
        manager.persist(fish);
        manager.getTransaction().commit();
        manager.clear();

        // we are going to correct the error
        manager.getTransaction().begin();
        // we have to separately update this in the Animal database as well as the Species Databse
        // we could change only in the Animal Database and refresh.  Not the other way around
        fish.getAnimals().remove(squirrel);
         // merge brings the object back to the manager.  Before that, it was a detatched object.  Now it can be committed

        manager.remove(manager.merge(squirrel));
        manager.getTransaction().commit();
    }
}
