package at.campus02.dbp2.jpa;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity // This is the class that will get converted to a table in the back end
@NamedQuery(name = "Student.findAllByGender", query = "select s from Student s WHERE s.gender  = :gender")
// Named queries help organize code
public class Student {
    @Id
    @GeneratedValue
    private Integer id;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private Gender gender;

    @OneToOne (mappedBy = "owner")
    private Animal pet;

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(id, student.id) &&
                Objects.equals(firstName, student.firstName) &&
                Objects.equals(lastName, student.lastName) &&
                Objects.equals(birthday, student.birthday) &&
                gender == student.gender;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, birthday, gender);
    }
}
