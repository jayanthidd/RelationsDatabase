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
    private Student prepareStudent(String firstName, String lastName, Gender gender, String birthdayString){
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setGender(gender);
        if(birthdayString!=null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            student.setBirthday(LocalDate.parse(birthdayString, formatter));
        }
        return student;
    }
    private void create(Student student){
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
    public void tearDown(){
        if(manager.isOpen())// if we do not check and we try to close an already closed manager or factoy, it will throw an exception
            manager.close();
        if(factory.isOpen())
            factory.close();
    }

    @Test
    public void persistAnimalAndOwnerStoresRelationInDatabase(){
        //given
        Animal bunny = new Animal();
        bunny.setName("Hansi");

        Student owner = prepareStudent("firstname", "lastname", Gender.FEMALE, null);
        bunny.setOwner(owner);

        //when
        manager.getTransaction().begin();
        manager.persist(bunny);
        manager.persist(owner);
        manager.getTransaction().commit();

        manager.clear();

        //then
        Animal bunnyFromDb = manager.find(Animal.class,bunny.getName());
        assertThat(bunnyFromDb.getOwner(),is(owner));
    }
}
